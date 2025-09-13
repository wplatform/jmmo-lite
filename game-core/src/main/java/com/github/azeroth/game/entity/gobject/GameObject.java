package com.github.azeroth.game.entity.gobject;


import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.utils.IntArray;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.defines.GOState;
import com.github.azeroth.defines.GameObjectDynamicLowFlag;
import com.github.azeroth.defines.GameObjectType;
import com.github.azeroth.defines.LootMode;
import com.github.azeroth.game.ai.AISelector;
import com.github.azeroth.game.ai.GameObjectAI;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.domain.gobject.*;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.spawn.RespawnInfo;

import com.github.azeroth.game.entity.object.*;
import com.github.azeroth.game.domain.object.enums.CellMoveState;
import com.github.azeroth.game.entity.object.update.UpdateFieldFlag;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.loot.LootManager;
import com.github.azeroth.game.loot.LootStorage;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.model.GameObjectModel;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.SpellInfo;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


@Getter
@Setter
public class GameObject extends WorldObject implements GirdObject {

    private final ArrayList<ObjectGuid> uniqueUsers = new ArrayList<>();
    private final HashMap<Integer, ObjectGuid> chairListSlots = new HashMap<Integer, ObjectGuid>();
    private final ArrayList<ObjectGuid> skillupList = new ArrayList<>();
    protected GameObjectValue goValueProtected = new GameObjectValue(); // TODO: replace with m_goTypeImpl
    protected GameObjectTemplate goInfo;
    protected GameObjectTemplateAddon goTemplateAddonProtected;
    private GameObjectTypeBase goTypeImpl;
    private GameObjectData goData;
    private long spawnId;
    private int spellId;
    private long respawnTime; // (secs) time of next respawn (or despawn if GO have owner()),
    private int respawnDelayTime; // (secs) if 0 then current GO state no dependent from timer
    private int despawnDelay;
    private Duration despawnRespawnTime; // override respawn time after delayed despawn
    private ObjectGuid lootStateUnitGuid = ObjectGuid.EMPTY; // GUID of the unit passed with setLootState(LootState, Unit*)    private LootState lootState = getLootState().values()[0];
    private boolean spawnedByDefault;
    private long restockTime;
    private long cooldownTime; // used as internal reaction delay time store (not state change reaction).
    private Player ritualOwner; // used for GAMEOBJECT_TYPE_SUMMONING_RITUAL where GO is not summoned (no owner)
    // For traps this: spell casting cooldown, for doors/buttons: reset time.
    private int usetimes;
    private ArrayList<ObjectGuid> tapList = new ArrayList<>();
    private LootMode lootMode = LootMode.DEFAULT; // bitmask, default LOOT_MODE_DEFAULT, determines what loot will be lootable
    private long packedRotation;
    private QuaternionData localRotation;
    private GameObjectAI ai;
    private boolean respawnCompatibilityMode;
    private short animKitId;
    private int worldEffectId;
    private Integer gossipMenuId = null;
    private HashMap<ObjectGuid, PerPlayerState> perPlayerState;
    private GOState prevGoState = GOState.values()[0]; // What state to set whenever resetting
    private HashMap<ObjectGuid, Loot> personalLoot = new HashMap<>();
    private ObjectGuid linkedTrap = ObjectGuid.EMPTY;
    private Cell currentCell;
    private CellMoveState moveState;
    private Position newPosition = new Position();
    private GameObjectData gameObjectFieldData;
    private Position stationaryPosition;
    private Loot loot;
    private GameObjectModel model;
    private Float pathProgressForClient;


    public GameObject() {
        super(false);
        setObjectTypeMask(TypeMask.forValue(getObjectTypeMask().getValue() | TypeMask.gameObject.getValue()));
        setObjectTypeId(TypeId.gameObject);

        updateFlag.stationary = true;
        updateFlag.rotation = true;

        respawnDelayTime = 300;
        despawnDelay = 0;
        lootState = LootState.NotReady;
        spawnedByDefault = true;

        resetLootMode(); // restore default loot mode
        setStationaryPosition(new Position());

        setGameObjectFieldData(new GameObjectFieldData());
    }

    public static GameObject createGameObject(int entry, Map map, Position pos, Quaternion rotation, int animProgress, GOState goState) {
        return createGameObject(entry, map, pos, rotation, animProgress, goState, 0);
    }

    public static GameObject createGameObject(int entry, Map map, Position pos, Quaternion rotation, int animProgress, GOState goState, int artKit) {
        var goInfo = global.getObjectMgr().getGameObjectTemplate(entry);

        if (goInfo == null) {
            return null;
        }

        GameObject go = new GameObject();

        if (!go.create(entry, map, pos, rotation, animProgress, goState, artKit, false, 0)) {
            return null;
        }

        return go;
    }

    public static GameObject createGameObjectFromDb(long spawnId, Map map) {
        return createGameObjectFromDb(spawnId, map, true);
    }

    public static GameObject createGameObjectFromDb(long spawnId, Map map, boolean addToMap) {
        GameObject go = new GameObject();

        if (!go.loadFromDB(spawnId, map, addToMap)) {
            return null;
        }

        return go;
    }

    public static boolean deleteFromDB(long spawnId) {
        var data = global.getObjectMgr().getGameObjectData(spawnId);

        if (data == null) {
            return false;
        }

        SQLTransaction trans = new SQLTransaction();

        global.getMapMgr().DoForAllMapsWithMapId(data.getMapId(), map ->
        {
            // despawn all active objects, and remove their respawns
            ArrayList<GameObject> toUnload = new ArrayList<>();

            for (var creature : map.GameObjectBySpawnIdStore.get(spawnId)) {
                toUnload.add(creature);
            }

            for (var obj : toUnload) {
                map.addObjectToRemoveList(obj);
            }

            map.removeRespawnTime(SpawnObjectType.gameObject, spawnId, trans);
        });

        // delete data from memory
        global.getObjectMgr().deleteGameObjectData(spawnId);

        trans = new SQLTransaction();

        // ... and the database
        var stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_GAMEOBJECT);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_EVENT_GAMEOBJECT);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.GOToGO.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.GOToCreature.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN_MASTER);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.GOToGO.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN_MASTER);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.CreatureToGO.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_GAMEOBJECT_ADDON);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        DB.World.CommitTransaction(trans);

        return true;
    }

    public final GameObjectData getGameObjectFieldData() {
        return gameObjectFieldData;
    }

    public final void setGameObjectFieldData(GameObjectData value) {
        gameObjectFieldData = value;
    }

    public final Position getStationaryPosition() {
        return stationaryPosition;
    }

    public final void setStationaryPosition(Position value) {
        stationaryPosition = value;
    }

    public final Loot getLoot() {
        return loot;
    }

    public final void setLoot(Loot value) {
        loot = value;
    }

    public final GameObjectModel getModel() {
        return model;
    }

    public final void setModel(GameObjectModel value) {
        model = value;
    }

    @Override
    public short getAIAnimKitId() {
        return animKitId;
    }

    @Override
    public ObjectGuid getOwnerGUID() {
        return getGameObjectFieldData().createdBy;
    }

    public final void setOwnerGUID(ObjectGuid owner) {
        // Owner already found and different than expected owner - remove object from old owner
        if (!owner.isEmpty() && !getOwnerGUID().isEmpty() && ObjectGuid.opNotEquals(getOwnerGUID(), owner)) {
            Log.outWarn(LogFilter.spells, "Owner already found and different than expected owner - remove object from old owner");
        } else {
            spawnedByDefault = false; // all object with owner is despawned after delay
            setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().createdBy), owner);
        }
    }

    @Override
    public int getFaction() {
        return getGameObjectFieldData().factionTemplate;
    }

    @Override
    public void setFaction(int value) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().factionTemplate), value);
    }

    public Transport toTransport() {
        if (goInfo.type == GameObjectType.MAP_OBJ_TRANSPORT)
            return (Transport) this;
        else
            return null;
    }


    @Override
    public float getStationaryX() {
        return getStationaryPosition().getX();
    }

    @Override
    public float getStationaryY() {
        return getStationaryPosition().getY();
    }

    @Override
    public float getStationaryZ() {
        return getStationaryPosition().getZ();
    }

    @Override
    public float getStationaryO() {
        return getStationaryPosition().getO();
    }

    public final String getAiName() {
        var got = global.getObjectMgr().getGameObjectTemplate(getEntry());

        if (got != null) {
            return got.AIName;
        }

        return "";
    }

    public final GameObjectOverride getGameObjectOverride() {
        if (spawnId != 0) {
            var goOverride = global.getObjectMgr().getGameObjectOverride(spawnId);

            if (goOverride != null) {
                return goOverride;
            }
        }

        return goTemplateAddonProtected;
    }

    public final boolean isTransport() {
        // If something is marked as a transport, don't transmit an out of range packet for it.
        var gInfo = getTemplate();

        if (gInfo == null) {
            return false;
        }

        return gInfo.type == GameObjectType.transport || gInfo.type == GameObjectType.MapObjTransport;
    }

    // is Dynamic transport = non-stop Transport
    public final boolean isDynTransport() {
        // If something is marked as a transport, don't transmit an out of range packet for it.
        var gInfo = getTemplate();

        if (gInfo == null) {
            return false;
        }

        return gInfo.type == GameObjectType.MapObjTransport || gInfo.type == GameObjectType.transport;
    }

    public final boolean isDestructibleBuilding() {
        var gInfo = getTemplate();

        if (gInfo == null) {
            return false;
        }

        return gInfo.type == GameObjectType.destructibleBuilding;
    }

    public final Transport getAsTransport() {
        return getTemplate().type == GameObjectType.MapObjTransport ? (this instanceof Transport ? (transport) this : null) : null;
    }

    public final int getScriptId() {
        var gameObjectData = getGameObjectData();

        if (gameObjectData != null) {
            var scriptId = gameObjectData.scriptId;

            if (scriptId != 0) {
                return scriptId;
            }
        }

        return getTemplate().scriptId;
    }

    public final boolean isFullyLooted() {
        if (getLoot() != null && !getLoot().isLooted()) {
            return false;
        }


        for (var(_, loot) : personalLoot) {
            if (!loot.isLooted()) {
                return false;
            }
        }

        return true;
    }

    public final GameObject getLinkedTrap() {
        return ObjectAccessor.getGameObject(this, linkedTrap);
    }

    public final void setLinkedTrap(GameObject value) {
        linkedTrap = value.getGUID();
    }

    public final int getWorldEffectID() {
        return worldEffectId;
    }

    public final void setWorldEffectID(int value) {
        worldEffectId = value;
    }

    public final int getGossipMenuId() {
        if (gossipMenuId != null) {
            return gossipMenuId.intValue();
        }

        return getTemplate().getGossipMenuId();
    }

    public final void setGossipMenuId(int value) {
        gossipMenuId = value;
    }

    public final GameObjectTemplate getTemplate() {
        return goInfo;
    }

    public final GameObjectTemplateAddon getTemplateAddon() {
        return goTemplateAddonProtected;
    }

    public final GameObjectData getGameObjectData() {
        return goData;
    }

    public final GameObjectValue getGoValue() {
        return goValueProtected;
    }

    public final long getSpawnId() {
        return spawnId;
    }

    public final long getPackedLocalRotation() {
        return packedRotation;
    }

    public final int getSpellId() {
        return spellId;
    }

    public final void setSpellId(int value) {
        spawnedByDefault = false; // all summoned object is despawned after delay
        spellId = value;
    }

    public final long getRespawnTime() {
        return respawnTime;
    }

    public final void setRespawnTime(int respawn) {
        respawnTime = respawn > 0 ? gameTime.GetGameTime() + respawn : 0;
        respawnDelayTime = (int) (respawn > 0 ? respawn : 0);

        if (respawn != 0 && !spawnedByDefault) {
            updateObjectVisibility(true);
        }
    }

    public final long getRespawnTimeEx() {
        var now = gameTime.GetGameTime();

        if (respawnTime > now) {
            return respawnTime;
        } else {
            return now;
        }
    }

    public final boolean isSpawned() {
        return respawnDelayTime == 0 || (respawnTime > 0 && !spawnedByDefault) || (respawnTime == 0 && spawnedByDefault);
    }

    public final boolean isSpawnedByDefault() {
        return spawnedByDefault;
    }

    public final void setSpawnedByDefault(boolean b) {
        spawnedByDefault = b;
    }

    public final int getRespawnDelay() {
        return respawnDelayTime;
    }

    public final GameObjectType getGoType() {
        return GameObjectType.forValue((byte) getGameObjectFieldData().typeID);
    }

    public final void setGoType(GameObjectType value) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().typeID), (byte) value.getValue());
    }

    public final GOState getGoState() {
        return GOState.forValue((byte) getGameObjectFieldData().state);
    }

    public final void setGoState(GOState state) {
        var oldState = getGoState();
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().state), (byte) state.getValue());

        if (getAI() != null) {
            getAI().onStateChanged(state);
        }

        if (goTypeImpl != null) {
            goTypeImpl.onStateChanged(oldState, state);
        }

        if (getModel() != null && !isTransport()) {
            if (!isInWorld()) {
                return;
            }

            // startOpen determines whether we are going to add or remove the LoS on activation
            var collision = false;

            if (state == GOState.Ready) {
                collision = !collision;
            }

            enableCollision(collision);
        }
    }

    public final LootState getLootState() {
        return lootState;
    }

    public final void setLootState(LootState state) {
        setLootState(state, null);
    }

    public final LootMode getLootMode() {
        return lootMode;
    }

    private void setLootMode(LootMode value) {
        lootMode = value;
    }

    public final int getUseCount() {
        return usetimes;
    }

    public final GameObjectAI getAI() {
        return ai;
    }

    public final int getDisplayId() {
        return getGameObjectFieldData().displayID;
    }

    public final void setDisplayId(int value) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().displayID), value);
        updateModel();
    }

    public final Position getStationaryPosition1() {
        return getStationaryPosition();
    }

    // There's many places not ready for dynamic spawns. This allows them to live on for now.
    public final boolean getRespawnCompatibilityMode() {
        return respawnCompatibilityMode;
    }

    private void setRespawnCompatibilityMode(boolean value) {
        respawnCompatibilityMode = value;
    }

    public final int getGoArtKit() {
        return getGameObjectFieldData().artKit;
    }

    public final void setGoArtKit(int value) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().artKit), value);
        var data = global.getObjectMgr().getGameObjectData(spawnId);

        if (data != null) {
            data.artKit = value;
        }
    }

    private byte getGoAnimProgress() {
        return getGameObjectFieldData().percentHealth;
    }

    public final void setGoAnimProgress(int animprogress) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().percentHealth), (byte) animprogress);
    }

    private ArrayList<ObjectGuid> getTapList() {
        return tapList;
    }

    private void setTapList(ArrayList<ObjectGuid> value) {
        tapList = value;
    }

    private boolean getHasLootRecipient() {
        return !tapList.isEmpty();
    }

    private GameObjectDestructibleState getDestructibleState() {
        if ((getGameObjectFieldData().flags & (int) GameObjectFlags.destroyed.getValue()) != 0) {
            return GameObjectDestructibleState.destroyed;
        }

        if ((getGameObjectFieldData().flags & (int) GameObjectFlags.Damaged.getValue()) != 0) {
            return GameObjectDestructibleState.Damaged;
        }

        return GameObjectDestructibleState.Intact;
    }

    public final void setDestructibleState(GameObjectDestructibleState state) {
        setDestructibleState(state, null, false);
    }

    @Override
    public void close() throws IOException {
        ai = null;
        setModel(null);

        super.close();
    }

    public final boolean AIM_Initialize() {
        ai = AISelector.selectGameObjectAI(this);

        if (ai == null) {
            return false;
        }

        ai.initializeAI();

        return true;
    }

    @Override
    public void cleanupsBeforeDelete(boolean finalCleanup) {
        super.cleanupsBeforeDelete(finalCleanup);

        removeFromOwner();
    }

    @Override
    public void addToWorld() {
        //- Register the gameobject for guid lookup
        if (!isInWorld()) {
            if (getZoneScript() != null) {
                getZoneScript().onGameObjectCreate(this);
            }


            getMap().getObjectsStore().TryAdd(getGUID(), this);

            if (spawnId != 0) {
                getMap().getGameObjectBySpawnIdStore().add(spawnId, this);
            }

            // The state can be changed after gameObject.Create but before gameObject.AddToWorld
            var toggledState = getGoType() == GameObjectType.Chest ? getLootState() == LootState.Ready : (getGoState() == GOState.Ready || isTransport());

            if (getModel() != null) {
                var trans = getAsTransport();

                if (trans) {
                    trans.setDelayedAddModelToMap();
                } else {
                    getMap().insertGameObjectModel(getModel());
                }
            }

            enableCollision(toggledState);
            super.addToWorld();
        }
    }

    @Override
    public void removeFromWorld() {
        //- Remove the gameobject from the accessor
        if (isInWorld()) {
            try {
                if (getZoneScript() != null) {
                    getZoneScript().onGameObjectRemove(this);
                }

                removeFromOwner();

                if (getModel() != null) {
                    if (getMap().containsGameObjectModel(getModel())) {
                        getMap().removeGameObjectModel(getModel());
                    }
                }

                // If linked trap exists, despawn it
                var linkedTrap = getLinkedTrap();

                if (linkedTrap != null) {
                    linkedTrap.despawnOrUnsummon();
                }

                super.removeFromWorld();

                if (spawnId != 0) {
                    getMap().getGameObjectBySpawnIdStore().remove(spawnId, this);
                }

                tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

                getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
                _ = tempOut__.outArgValue;
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    @Override
    public void update(int diff) {
        super.update(diff);

        if (getAI() != null) {
            getAI().updateAI(diff);
        } else if (!AIM_Initialize()) {
            Log.outError(LogFilter.Server, "Could not initialize GameObjectAI");
        }

        if (despawnDelay != 0) {
            if (despawnDelay > diff) {
                _despawnDelay -= diff;
            } else {
                despawnDelay = 0;
                despawnOrUnsummon(duration.ofSeconds(0), despawnRespawnTime);
            }
        }

        if (goTypeImpl != null) {
            goTypeImpl.update(diff);
        }

        if (perPlayerState != null) {

            for (var(guid, playerState) : perPlayerState.ToList()) {
                if (playerState.validUntil > gameTime.GetSystemTime()) {
                    continue;
                }

                var seer = global.getObjAccessor().getPlayer(this, guid);
                var needsStateUpdate = playerState.state != getGoState();
                var despawned = playerState.despawned;

                perPlayerState.remove(guid);

                if (seer) {
                    if (despawned) {
                        seer.updateVisibilityOf(this);
                    } else if (needsStateUpdate) {
                        ObjectFieldData objMask = new objectFieldData();
                        GameObjectFieldData goMask = new gameObjectFieldData();
                        goMask.markChanged(getGameObjectFieldData().state);

                        UpdateData udata = new UpdateData(getLocation().getMapId());
                        buildValuesUpdateForPlayerWithMask(udata, objMask.getUpdateMask(), goMask.getUpdateMask(), seer);
                        UpdateObject packet;
                        tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
                        udata.buildPacket(tempOut_packet);
                        packet = tempOut_packet.outArgValue;
                        seer.sendPacket(packet);
                    }
                }
            }
        }

        switch (lootState) {
            case NotReady: {
                switch (getGoType()) {
                    case Trap: {
                        // Arming Time for GAMEOBJECT_TYPE_TRAP (6)
                        var goInfo = getTemplate();

                        // Bombs
                        var owner = getOwnerUnit();

                        if (goInfo.trap.charges == 2) {
                            cooldownTime = gameTime.GetGameTimeMS() + 10 * time.InMilliseconds; // Hardcoded tooltip value
                        } else if (owner) {
                            if (owner.isInCombat()) {
                                cooldownTime = gameTime.GetGameTimeMS() + goInfo.trap.startDelay * time.InMilliseconds;
                            }
                        }

                        lootState = LootState.Ready;

                        break;
                    }
                    case FishingNode: {
                        // fishing code (bobber ready)
                        if (gameTime.GetGameTime() > _respawnTime - 5) {
                            // splash bobber (bobber ready now)
                            var caster = getOwnerUnit();

                            if (caster != null && caster.isTypeId(TypeId.PLAYER)) {
                                sendCustomAnim(0);
                            }

                            lootState = LootState.Ready; // can be successfully open with some chance
                        }

                        return;
                    }
                    case Chest:
                        if (restockTime > gameTime.GetGameTime()) {
                            return;
                        }

                        // If there is no restock timer, or if the restock timer passed, the chest becomes ready to loot
                        restockTime = 0;
                        lootState = LootState.Ready;
                        clearLoot();
                        updateDynamicFlagsForNearbyPlayers();

                        break;
                    default:
                        lootState = LootState.Ready; // for other GOis same switched without delay to GO_READY

                        break;
                }
            }

            case Ready: {
                if (respawnCompatibilityMode) {
                    if (respawnTime > 0) // timer on
                    {
                        var now = gameTime.GetGameTime();

                        if (respawnTime <= now) // timer expired
                        {
                            var dbtableHighGuid = ObjectGuid.create(HighGuid.GameObject, getLocation().getMapId(), getEntry(), spawnId);
                            var linkedRespawntime = getMap().getLinkedRespawnTime(dbtableHighGuid);

                            if (linkedRespawntime != 0) // Can't respawn, the master is dead
                            {
                                var targetGuid = global.getObjectMgr().getLinkedRespawnGuid(dbtableHighGuid);

                                if (Objects.equals(targetGuid, dbtableHighGuid)) // if linking self, never respawn (check delayed to next day)
                                {
                                    setRespawnTime(time.Week);
                                } else {
                                    respawnTime = (now > linkedRespawntime ? now : linkedRespawntime) + RandomUtil.IRand(5, time.Minute); // else copy time from master and add a little
                                }

                                saveRespawnTime();

                                return;
                            }

                            respawnTime = 0;
                            skillupList.clear();
                            usetimes = 0;

                            switch (getGoType()) {
                                case FISHING_NODE: //  can't fish now
                                {
                                    var caster = getOwnerUnit();

                                    if (caster != null && caster.isTypeId(TypeId.PLAYER)) {
                                        caster.toPlayer().removeGameObject(this, false);
                                        caster.toPlayer().sendPacket(new FishEscaped());
                                    }

                                    // can be delete
                                    lootState = LootState.JustDeactivated;

                                    return;
                                }
                                case Door:
                                case Button:
                                    //we need to open doors if they are closed (add there another condition if this code breaks some usage, but it need to be here for Battlegrounds)
                                    if (getGoState() != GOState.Ready) {
                                        resetDoorOrButton();
                                    }

                                    break;
                                case FishingHole:
                                    // Initialize a new max fish count on respawn
                                    goValueProtected.fishingHole.maxOpens = RandomUtil.URand(getTemplate().fishingHole.minRestock, getTemplate().fishingHole.maxRestock);

                                    break;
                                default:
                                    break;
                            }

                            if (!spawnedByDefault) // despawn timer
                            {
                                // can be despawned or destroyed
                                setLootState(LootState.JustDeactivated);

                                return;
                            }

                            // Call AI Reset (required for example in SmartAI to clear one time events)
                            if (getAI() != null) {
                                getAI().reset();
                            }

                            // respawn timer
                            var poolid = getGameObjectData() != null ? getGameObjectData().poolId : 0;

                            if (poolid != 0) {
                                global.getPoolMgr().<GameObject>UpdatePool(getMap().getPoolData(), poolid, getSpawnId());
                            } else {
                                getMap().addToMap(this);
                            }
                        }
                    }
                }

                // Set respawn timer
                if (!respawnCompatibilityMode && respawnTime > 0) {
                    saveRespawnTime();
                }

                if (isSpawned()) {
                    var goInfo = getTemplate();
                    int maxCharges;

                    if (goInfo.type == GameObjectType.trap) {
                        if (gameTime.GetGameTimeMS() < cooldownTime) {
                            break;
                        }

                        // Type 2 (bomb) does not need to be triggered by a unit and despawns after casting its spell.
                        if (goInfo.trap.charges == 2) {
                            setLootState(LootState.Activated);

                            break;
                        }

                        // Type 0 despawns after being triggered, type 1 does not.
                        // @todo This is activation radius. Casting radius must be selected from spell
                        float radius;

                        if (goInfo.trap.radius == 0f) {
                            // Battlegroundgameobjects have data2 == 0 && data5 == 3
                            if (goInfo.trap.cooldown != 3) {
                                break;
                            }

                            radius = 3.0f;
                        } else {
                            radius = goInfo.trap.radius / 2.0f;
                        }

                        Unit target;

                        // @todo this hack with search required until GO casting not implemented
                        if (getOwnerUnit() != null || goInfo.trap.checkallunits != 0) {
                            // Hunter trap: Search units which are unfriendly to the trap's owner
                            var checker = new NearestAttackableNoTotemUnitInObjectRangeCheck(this, radius);
                            var searcher = new UnitLastSearcher(this, checker, gridType.All);
                            Cell.visitGrid(this, searcher, radius);
                            target = searcher.getTarget();
                        } else {
                            // Environmental trap: Any player
                            var check = new AnyPlayerInObjectRangeCheck(this, radius);
                            var searcher = new PlayerSearcher(this, check, gridType.World);
                            Cell.visitGrid(this, searcher, radius);
                            target = searcher.getTarget();
                        }

                        if (target) {
                            setLootState(LootState.Activated, target);
                        }
                    } else if (goInfo.type == GameObjectType.capturePoint) {
                        var hordeCapturing = goValueProtected.capturePoint.state == BattlegroundCapturePointState.ContestedHorde;
                        var allianceCapturing = goValueProtected.capturePoint.state == BattlegroundCapturePointState.ContestedAlliance;

                        if (hordeCapturing || allianceCapturing) {
                            if (goValueProtected.capturePoint.assaultTimer <= diff) {
                                goValueProtected.capturePoint.state = hordeCapturing ? BattlegroundCapturePointState.HordeCaptured : BattlegroundCapturePointState.AllianceCaptured;

                                if (hordeCapturing) {
                                    goValueProtected.capturePoint.state = BattlegroundCapturePointState.HordeCaptured;
                                    var map = getMap().toBattlegroundMap();

                                    if (map != null) {
                                        var bg = map.getBG();

                                        if (bg != null) {
                                            if (goInfo.capturePoint.captureEventHorde != 0) {
                                                GameEvents.trigger(goInfo.capturePoint.captureEventHorde, this, this);
                                            }

                                            bg.sendBroadcastText(getTemplate().capturePoint.captureBroadcastHorde, ChatMsg.BgSystemHorde);
                                        }
                                    }
                                } else {
                                    goValueProtected.capturePoint.state = BattlegroundCapturePointState.AllianceCaptured;
                                    var map = getMap().toBattlegroundMap();

                                    if (map != null) {
                                        var bg = map.getBG();

                                        if (bg != null) {
                                            if (goInfo.capturePoint.captureEventAlliance != 0) {
                                                GameEvents.trigger(goInfo.capturePoint.captureEventAlliance, this, this);
                                            }

                                            bg.sendBroadcastText(getTemplate().capturePoint.captureBroadcastAlliance, ChatMsg.BgSystemAlliance);
                                        }
                                    }
                                }

                                goValueProtected.capturePoint.lastTeamCapture = hordeCapturing ? TeamId.HORDE : TeamId.ALLIANCE;
                                updateCapturePoint();
                            } else {
                                goValueProtected.capturePoint.AssaultTimer -= diff;
                            }
                        }
                    } else if ((maxCharges = goInfo.getCharges()) != 0) {
                        if (usetimes >= maxCharges) {
                            usetimes = 0;
                            setLootState(LootState.JustDeactivated); // can be despawned or destroyed
                        }
                    }
                }

                break;
            }
            case Activated: {
                switch (getGoType()) {
                    case Door:
                    case Button:
                        if (cooldownTime != 0 && gameTime.GetGameTimeMS() >= cooldownTime) {
                            resetDoorOrButton();
                        }

                        break;
                    case Goober:
                        if (gameTime.GetGameTimeMS() >= cooldownTime) {
                            removeFlag(GameObjectFlags.InUse);

                            setLootState(LootState.JustDeactivated);
                            cooldownTime = 0;
                        }

                        break;
                    case Chest:
                        if (getLoot() != null) {
                            getLoot().update();
                        }


                        for (var(_, loot) : personalLoot) {
                            loot.update();
                        }

                        // Non-consumable chest was partially looted and restock time passed, restock all loot now
                        if (getTemplate().chest.consumable == 0 && getTemplate().chest.chestRestockTime != 0 && gameTime.GetGameTime() >= restockTime) {
                            restockTime = 0;
                            lootState = LootState.Ready;
                            clearLoot();
                            updateDynamicFlagsForNearbyPlayers();
                        }

                        break;
                    case Trap: {
                        var goInfo = getTemplate();
                        var target = global.getObjAccessor().GetUnit(this, lootStateUnitGuid);

                        if (goInfo.trap.charges == 2 && goInfo.trap.spell != 0) {
                            //todo NULL target won't work for target type 1
                            castSpell(goInfo.trap.spell);
                            setLootState(LootState.JustDeactivated);
                        } else if (target) {
                            // Some traps do not have a spell but should be triggered
                            CastSpellExtraArgs args = new CastSpellExtraArgs();
                            args.setOriginalCaster(getOwnerGUID());

                            if (goInfo.trap.spell != 0) {
                                castSpell(target, goInfo.trap.spell, args);
                            }

                            // Template value or 4 seconds
                            cooldownTime = (gameTime.GetGameTimeMS() + (goInfo.trap.cooldown != 0 ? goInfo.trap.cooldown : 4)) * time.InMilliseconds;

                            if (goInfo.trap.charges == 1) {
                                setLootState(LootState.JustDeactivated);
                            } else if (goInfo.trap.charges == 0) {
                                setLootState(LootState.Ready);
                            }

                            // Battleground gameobjects have data2 == 0 && data5 == 3
                            if (goInfo.trap.radius == 0 && goInfo.trap.cooldown == 3) {
                                var player = target.toPlayer();

                                if (player) {
                                    var bg = player.getBattleground();

                                    if (bg) {
                                        bg.handleTriggerBuff(getGUID());
                                    }
                                }
                            }
                        }

                        break;
                    }
                    default:
                        break;
                }

                break;
            }
            case JustDeactivated: {
                // If nearby linked trap exists, despawn it
                var linkedTrap = getLinkedTrap();

                if (linkedTrap) {
                    linkedTrap.despawnOrUnsummon();
                }

                //if Gameobject should cast spell, then this, but some GOs (type = 10) should be destroyed
                if (getGoType() == GameObjectType.goober) {
                    var spellId = getTemplate().goober.spell;

                    if (spellId != 0) {
                        for (var id : uniqueUsers) {
                            // m_unique_users can contain only player GUIDs
                            var owner = global.getObjAccessor().getPlayer(this, id);

                            if (owner != null) {
                                owner.castSpell(owner, spellId, false);
                            }
                        }

                        uniqueUsers.clear();
                        usetimes = 0;
                    }

                    // Only goobers with a lock id or a reset time may reset their go state
                    if (getTemplate().getLockId() != 0 || getTemplate().getAutoCloseTime() != 0) {
                        setGoState(GOState.Ready);
                    }

                    //any return here in case Battleground traps
                    var goOverride = getGameObjectOverride();

                    if (goOverride != null && goOverride.flags.hasFlag(GameObjectFlags.NoDespawn)) {
                        return;
                    }
                }

                clearLoot();

                // Do not delete chests or goobers that are not consumed on loot, while still allowing them to despawn when they expire if summoned
                var isSummonedAndExpired = (getOwnerUnit() != null || getSpellId() != 0) && respawnTime == 0;

                if ((getGoType() == GameObjectType.chest || getGoType() == GameObjectType.goober) && !getTemplate().isDespawnAtAction() && !isSummonedAndExpired) {
                    if (getGoType() == GameObjectType.chest && getTemplate().chest.chestRestockTime > 0) {
                        // Start restock timer when the chest is fully looted
                        restockTime = gameTime.GetGameTime() + getTemplate().chest.chestRestockTime;
                        setLootState(LootState.NotReady);
                        updateDynamicFlagsForNearbyPlayers();
                    } else {
                        setLootState(LootState.Ready);
                    }

                    updateObjectVisibility();

                    return;
                } else if (!getOwnerGUID().isEmpty() || getSpellId() != 0) {
                    setRespawnTime(0);
                    delete();

                    return;
                }

                setLootState(LootState.NotReady);

                //burning flags in some Battlegrounds, if you find better condition, just add it
                if (getTemplate().isDespawnAtAction() || getGoAnimProgress() > 0) {
                    sendGameObjectDespawn();
                    //reset flags
                    var goOverride = getGameObjectOverride();

                    if (goOverride != null) {
                        replaceAllFlags(goOverride.flags);
                    }
                }

                if (respawnDelayTime == 0) {
                    return;
                }

                if (!spawnedByDefault) {
                    respawnTime = 0;

                    if (spawnId != 0) {
                        updateObjectVisibilityOnDestroy();
                    } else {
                        delete();
                    }

                    return;
                }

                var respawnDelay = respawnDelayTime;
                var scalingMode = WorldConfig.getUIntValue(WorldCfg.RespawnDynamicMode);

                if (scalingMode != 0) {
                    tangible.RefObject<Integer> tempRef_respawnDelay = new tangible.RefObject<Integer>(respawnDelay);
                    getMap().applyDynamicModeRespawnScaling(this, spawnId, tempRef_respawnDelay, scalingMode);
                    respawnDelay = tempRef_respawnDelay.refArgValue;
                }

                respawnTime = gameTime.GetGameTime() + respawnDelay;

                // if option not set then object will be saved at grid unload
                // Otherwise just save respawn time to map object memory
                saveRespawnTime();

                if (respawnCompatibilityMode) {
                    updateObjectVisibilityOnDestroy();
                } else {
                    addObjectToRemoveList();
                }

                break;
            }
        }
    }

    public final void refresh() {
        // not refresh despawned not casted GO (despawned casted GO destroyed in all cases anyway)
        if (respawnTime > 0 && spawnedByDefault) {
            return;
        }

        if (isSpawned()) {
            getMap().addToMap(this);
        }
    }

    public final void addUniqueUse(Player player) {
        addUse();
        uniqueUsers.add(player.getGUID());
    }

    public final void despawnOrUnsummon(Duration delay) {
        despawnOrUnsummon(delay, null);
    }

    public final void despawnOrUnsummon() {
        despawnOrUnsummon(null, null);
    }

    public final void despawnOrUnsummon(Duration delay, Duration forceRespawnTime) {
        if (delay > duration.Zero) {
            if (despawnDelay == 0 || despawnDelay > delay.TotalMilliseconds) {
                despawnDelay = (int) delay.TotalMilliseconds;
                despawnRespawnTime = forceRespawnTime;
            }
        } else {
            if (goData != null) {
                var respawnDelay = (int) ((forceRespawnTime > duration.Zero) ? forceRespawnTime.TotalSeconds : respawnDelayTime);
                saveRespawnTime(respawnDelay);
            }

            delete();
        }
    }

    public final void delete() {
        setLootState(LootState.NotReady);
        removeFromOwner();

        if (goInfo.type == GameObjectType.capturePoint) {
            sendMessageToSet(new CapturePointRemoved(getGUID()), true);
        }

        sendGameObjectDespawn();

        if (goInfo.type != GameObjectType.transport) {
            setGoState(GOState.Ready);
        }

        var goOverride = getGameObjectOverride();

        if (goOverride != null) {
            replaceAllFlags(goOverride.flags);
        }

        var poolid = getGameObjectData() != null ? getGameObjectData().poolId : 0;

        if (respawnCompatibilityMode && poolid != 0) {
            global.getPoolMgr().<GameObject>UpdatePool(getMap().getPoolData(), poolid, getSpawnId());
        } else {
            addObjectToRemoveList();
        }
    }

    public final void sendGameObjectDespawn() {
        GameObjectDespawn packet = new GameObjectDespawn();
        packet.objectGUID = getGUID();
        sendMessageToSet(packet, true);
    }

    public final Loot getFishLoot(Player lootOwner) {
        int defaultzone = 1;

        Loot fishLoot = new loot(getMap(), getGUID(), LootType.FISHING, null);

        var areaId = getAreaId();
        AreaTableRecord areaEntry;

        while ((areaEntry = CliDB.AreaTableStorage.get(areaId)) != null) {
            fishLoot.fillLoot(areaId, LootStorage.FISHING, lootOwner, true, true);

            if (!fishLoot.isLooted()) {
                break;
            }

            areaId = areaEntry.ParentAreaID;
        }

        if (fishLoot.isLooted()) {
            fishLoot.fillLoot(defaultzone, LootStorage.FISHING, lootOwner, true, true);
        }

        return fishLoot;
    }

    public final Loot getFishLootJunk(Player lootOwner) {
        int defaultzone = 1;

        Loot fishLoot = new loot(getMap(), getGUID(), LootType.FishingJunk, null);

        var areaId = getAreaId();
        AreaTableRecord areaEntry;

        while ((areaEntry = CliDB.AreaTableStorage.get(areaId)) != null) {
            fishLoot.fillLoot(areaId, LootStorage.FISHING, lootOwner, true, true, LootMode.JunkFish);

            if (!fishLoot.isLooted()) {
                break;
            }

            areaId = areaEntry.ParentAreaID;
        }

        if (fishLoot.isLooted()) {
            fishLoot.fillLoot(defaultzone, LootStorage.FISHING, lootOwner, true, true, LootMode.JunkFish);
        }

        return fishLoot;
    }

    public final void saveToDB() {
        // this should only be used when the gameobject has already been loaded
        // preferably after adding to map, because mapid may not be valid otherwise
        var data = global.getObjectMgr().getGameObjectData(spawnId);

        if (data == null) {
            Log.outError(LogFilter.Maps, "GameObject.SaveToDB failed, cannot get gameobject data!");

            return;
        }

        var mapId = getLocation().getMapId();
        var transport = getTransport();

        if (transport != null) {
            if (transport.getMapIdForSpawning() >= 0) {
                mapId = (int) transport.getMapIdForSpawning();
            }
        }

        saveToDB(mapId, data.spawnDifficulties);
    }

    public final void saveToDB(int mapid, ArrayList<Difficulty> spawnDifficulties) {
        var goI = getTemplate();

        if (goI == null) {
            return;
        }

        if (spawnId == 0) {
            spawnId = global.getObjectMgr().generateGameObjectSpawnId();
        }

        // update in loaded data (changing data only in this place)
        var data = global.getObjectMgr().newOrExistGameObjectData(spawnId);

        if (data.getSpawnId() == 0) {
            data.setSpawnId(spawnId);
        }

        data.id = getEntry();
        data.setMapId(getLocation().getMapId());
        data.spawnPoint.relocate(getLocation());
        data.rotation = localRotation;
        data.spawntimesecs = (int) (_spawnedByDefault ? _respawnDelayTime : -_respawnDelayTime);
        data.animprogress = getGoAnimProgress();
        data.goState = getGoState();
        data.spawnDifficulties = spawnDifficulties;
        data.artKit = (byte) getGoArtKit();

        if (data.getSpawnGroupData() == null) {
            data.setSpawnGroupData(global.getObjectMgr().getDefaultSpawnGroup());
        }

        data.phaseId = getDBPhase() > 0 ? (int) getDBPhase() : data.phaseId;
        data.phaseGroup = getDBPhase() < 0 ? (int) -getDBPhase() : data.phaseGroup;

        // Update in DB
        byte index = 0;
        var stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_GAMEOBJECT);
        stmt.AddValue(0, spawnId);
        DB.World.execute(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.INS_GAMEOBJECT);
        stmt.AddValue(index++, spawnId);
        stmt.AddValue(index++, getEntry());
        stmt.AddValue(index++, mapid);
        stmt.AddValue(index++, data.spawnDifficulties.isEmpty() ? "" : tangible.StringHelper.join(",", data.spawnDifficulties));
        stmt.AddValue(index++, data.phaseId);
        stmt.AddValue(index++, data.phaseGroup);
        stmt.AddValue(index++, getLocation().getX());
        stmt.AddValue(index++, getLocation().getY());
        stmt.AddValue(index++, getLocation().getZ());
        stmt.AddValue(index++, getLocation().getO());
        stmt.AddValue(index++, localRotation.X);
        stmt.AddValue(index++, localRotation.Y);
        stmt.AddValue(index++, localRotation.Z);
        stmt.AddValue(index++, localRotation.W);
        stmt.AddValue(index++, respawnDelayTime);
        stmt.AddValue(index++, getGoAnimProgress());
        stmt.AddValue(index++, (byte) getGoState().getValue());
        DB.World.execute(stmt);
    }

    @Override
    public boolean loadFromDB(long spawnId, Map map, boolean addToMap) {
        return loadFromDB(spawnId, map, addToMap, true);
    }

    @Override
    public boolean loadFromDB(long spawnId, Map map, boolean addToMap, boolean unused) {
        var data = global.getObjectMgr().getGameObjectData(spawnId);


        if (data == null) {
            Log.outError(LogFilter.Maps, "Gameobject (SpawnId: {0}) not found in table `gameobject`, can't load. ", spawnId);

            return false;
        }

        var entry = data.id;

        var animprogress = data.animprogress;
        var go_state = data.goState;
        var artKit = data.artKit;

        spawnId = spawnId;
        respawnCompatibilityMode = ((data.getSpawnGroupData().getFlags().getValue() & SpawnGroupFlags.CompatibilityMode.getValue()) != 0);

        if (!create(entry, map, data.spawnPoint, data.rotation, animprogress, go_state, artKit, !respawnCompatibilityMode, spawnId)) {
            return false;
        }

        PhasingHandler.initDbPhaseShift(getPhaseShift(), data.phaseUseFlags, data.phaseId, data.phaseGroup);
        PhasingHandler.initDbVisibleMapId(getPhaseShift(), data.terrainSwapMap);

        if (data.spawntimesecs >= 0) {
            spawnedByDefault = true;

            if (!getTemplate().getDespawnPossibility() && !getTemplate().isDespawnAtAction()) {
                setFlag(GameObjectFlags.NoDespawn);
                respawnDelayTime = 0;
                respawnTime = 0;
            } else {
                respawnDelayTime = (int) data.spawntimesecs;
                respawnTime = getMap().getGORespawnTime(spawnId);

                // ready to respawn
                if (respawnTime != 0 && respawnTime <= gameTime.GetGameTime()) {
                    respawnTime = 0;
                    getMap().removeRespawnTime(SpawnObjectType.gameObject, spawnId);
                }
            }
        } else {
            if (!respawnCompatibilityMode) {
                Log.outWarn(LogFilter.Sql, String.format("GameObject %1$s (SpawnID %2$s) is not spawned by default, but tries to use a non-hack spawn system. This will not work. Defaulting to compatibility mode.", entry, spawnId));
                respawnCompatibilityMode = true;
            }

            spawnedByDefault = false;
            respawnDelayTime = (int) -data.spawntimesecs;
            respawnTime = 0;
        }

        goData = data;

        if (addToMap && !getMap().addToMap(this)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean hasQuest(int questId) {
        return global.getObjectMgr().getGOQuestRelations(getEntry()).hasQuest(questId);
    }

    @Override
    public boolean hasInvolvedQuest(int questId) {
        return global.getObjectMgr().getGOQuestInvolvedRelations(getEntry()).hasQuest(questId);
    }

    public final void saveRespawnTime() {
        saveRespawnTime(0);
    }

    public final void saveRespawnTime(int forceDelay) {
        if (goData != null && (forceDelay != 0 || respawnTime > gameTime.GetGameTime()) && spawnedByDefault) {
            if (respawnCompatibilityMode) {
                RespawnInfo ri = new RespawnInfo();
                ri.setObjectType(SpawnObjectType.gameObject);
                ri.setSpawnId(spawnId);
                ri.setRespawnTime(respawnTime);
                getMap().saveRespawnInfoDB(ri);

                return;
            }

            var thisRespawnTime = forceDelay != 0 ? gameTime.GetGameTime() + forceDelay : respawnTime;
            getMap().saveRespawnTime(SpawnObjectType.gameObject, spawnId, getEntry(), thisRespawnTime, MapDefine.computeGridCoord(getLocation().getX(), getLocation().getY()).getId());
        }
    }

    @Override
    public boolean isNeverVisibleFor(WorldObject seer) {
        if (super.isNeverVisibleFor(seer)) {
            return true;
        }

        if (getTemplate().getServerOnly() != 0) {
            return true;
        }

        if (getDisplayId() == 0) {
            return true;
        }

        return false;
    }

    @Override
    public boolean isAlwaysVisibleFor(WorldObject seer) {
        if (super.isAlwaysVisibleFor(seer)) {
            return true;
        }

        if (isTransport() || isDestructibleBuilding()) {
            return true;
        }

        if (seer == null) {
            return false;
        }

        // Always seen by owner and friendly units
        var guid = getOwnerGUID();

        if (!guid.isEmpty()) {
            if (Objects.equals(seer.getGUID(), guid)) {
                return true;
            }

            var owner = getOwnerUnit();

            if (owner != null && seer.isType(TypeMask.unit) && owner.isFriendlyTo(seer.toUnit())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isInvisibleDueToDespawn(WorldObject seer) {
        if (super.isInvisibleDueToDespawn(seer)) {
            return true;
        }

        // Despawned
        if (!isSpawned()) {
            return true;
        }

        if (perPlayerState != null) {
            var state = perPlayerState.get(seer.getGUID());

            if (state != null && state.despawned) {
                return true;
            }
        }

        return false;
    }

    public final void respawn() {
        if (spawnedByDefault && respawnTime > 0) {
            respawnTime = gameTime.GetGameTime();
            getMap().respawn(SpawnObjectType.gameObject, spawnId);
        }
    }

    public final boolean activateToQuest(Player target) {
        if (target.hasQuestForGO((int) getEntry())) {
            return true;
        }

        if (!global.getObjectMgr().isGameObjectForQuests(getEntry())) {
            return false;
        }

        switch (getGoType()) {
            case QuestGiver:
                var questStatus = target.getQuestDialogStatus(this);

                if (questStatus != QuestGiverStatus.NONE && questStatus != QuestGiverStatus.Future) {
                    return true;
                }

                break;
            case Chest: {
                // Chests become inactive while not ready to be looted
                if (getLootState() == LootState.NotReady) {
                    return false;
                }

                // scan GO chest with loot including quest items
                if (target.getQuestStatus(getTemplate().chest.questID) == QuestStatus.INCOMPLETE || LootStorage.GAMEOBJECT.haveQuestLootForPlayer(getTemplate().chest.chestLoot, target) || LootStorage.GAMEOBJECT.haveQuestLootForPlayer(getTemplate().chest.chestPersonalLoot, target) || LootStorage.GAMEOBJECT.haveQuestLootForPlayer(getTemplate().chest.chestPushLoot, target)) {
                    var bg = target.getBattleground();

                    if (bg) {
                        return bg.canActivateGO((int) getEntry(), (int) bg.getPlayerTeam(target.getGUID()).getValue());
                    }

                    return true;
                }

                break;
            }
            case Generic: {
                if (target.getQuestStatus(getTemplate().generic.questID) == QuestStatus.INCOMPLETE) {
                    return true;
                }

                break;
            }
            case Goober: {
                if (target.getQuestStatus(getTemplate().goober.questID) == QuestStatus.INCOMPLETE) {
                    return true;
                }

                break;
            }
            default:
                break;
        }

        return false;
    }

    public final void triggeringLinkedGameObject(int trapEntry, Unit target) {
        var trapInfo = global.getObjectMgr().getGameObjectTemplate(trapEntry);

        if (trapInfo == null || trapInfo.type != GameObjectType.trap) {
            return;
        }

        var trapSpell = global.getSpellMgr().getSpellInfo(trapInfo.trap.spell, getMap().getDifficultyID());

        if (trapSpell == null) // checked at load already
        {
            return;
        }

        var trapGO = getLinkedTrap();

        if (trapGO) {
            trapGO.castSpell(target, trapSpell.getId());
        }
    }

    public final void resetDoorOrButton() {
        if (lootState == LootState.Ready || lootState == LootState.JustDeactivated) {
            return;
        }

        removeFlag(GameObjectFlags.InUse);
        setGoState(prevGoState);

        setLootState(LootState.JustDeactivated);
        cooldownTime = 0;
    }

    public final void useDoorOrButton(int time_to_restore, boolean alternative) {
        useDoorOrButton(time_to_restore, alternative, null);
    }

    public final void useDoorOrButton(int time_to_restore) {
        useDoorOrButton(time_to_restore, false, null);
    }

    public final void useDoorOrButton() {
        useDoorOrButton(0, false, null);
    }

    public final void useDoorOrButton(int time_to_restore, boolean alternative, Unit user) {
        if (lootState != LootState.Ready) {
            return;
        }

        if (time_to_restore == 0) {
            time_to_restore = getTemplate().getAutoCloseTime();
        }

        switchDoorOrButton(true, alternative);
        setLootState(LootState.Activated, user);

        cooldownTime = time_to_restore != 0 ? gameTime.GetGameTimeMS() + time_to_restore : 0;
    }

    public final void activateObject(GameObjectActions action, int param, WorldObject spellCaster, int spellId) {
        activateObject(action, param, spellCaster, spellId, -1);
    }

    public final void activateObject(GameObjectActions action, int param, WorldObject spellCaster) {
        activateObject(action, param, spellCaster, 0, -1);
    }

    public final void activateObject(GameObjectActions action, int param) {
        activateObject(action, param, null, 0, -1);
    }

    public final void activateObject(GameObjectActions action, int param, WorldObject spellCaster, int spellId, int effectIndex) {
        var unitCaster = spellCaster ? spellCaster.toUnit() : null;

        switch (action) {
            case None:
                Log.outFatal(LogFilter.spells, String.format("Spell %1$s has action type NONE in effect %2$s", spellId, effectIndex));

                break;
            case AnimateCustom0:
            case AnimateCustom1:
            case AnimateCustom2:
            case AnimateCustom3:
                sendCustomAnim((int) (action - GameObjectActions.AnimateCustom0));

                break;
            case Disturb: // What's the difference with Open?
                if (unitCaster) {
                    use(unitCaster);
                }

                break;
            case Unlock:
                removeFlag(GameObjectFlags.locked);

                break;
            case Lock:
                setFlag(GameObjectFlags.locked);

                break;
            case Open:
                if (unitCaster) {
                    use(unitCaster);
                }

                break;
            case OpenAndUnlock:
                if (unitCaster) {
                    useDoorOrButton(0, false, unitCaster);
                }

                removeFlag(GameObjectFlags.locked);

                break;
            case Close:
                resetDoorOrButton();

                break;
            case ToggleOpen:
                // No use cases, implementation unknown
                break;
            case Destroy:
                if (unitCaster) {
                    useDoorOrButton(0, true, unitCaster);
                }

                break;
            case Rebuild:
                resetDoorOrButton();

                break;
            case Creation:
                // No use cases, implementation unknown
                break;
            case Despawn:
                despawnOrUnsummon();

                break;
            case MakeInert:
                setFlag(GameObjectFlags.NotSelectable);

                break;
            case MakeActive:
                removeFlag(GameObjectFlags.NotSelectable);

                break;
            case CloseAndLock:
                resetDoorOrButton();
                setFlag(GameObjectFlags.locked);

                break;
            case UseArtKit0:
            case UseArtKit1:
            case UseArtKit2:
            case UseArtKit3:
            case UseArtKit4: {
                var templateAddon = getTemplateAddon();

                var artKitIndex = action != GameObjectActions.UseArtKit4 ? (int) (action - GameObjectActions.UseArtKit0) : 4;

                int artKitValue = 0;

                if (templateAddon != null) {
                    artKitValue = templateAddon.ArtKits[artKitIndex];
                }

                if (artKitValue == 0) {
                    Logs.SQL.error(String.format("GameObject %1$s hit by spell %2$s needs `artkit%3$s` in `gameobject_template_addon`", getEntry(), spellId, artKitIndex));
                } else {
                    setGoArtKit(artKitValue);
                }

                break;
            }
            case GoTo1stFloor:
            case GoTo2ndFloor:
            case GoTo3rdFloor:
            case GoTo4thFloor:
            case GoTo5thFloor:
            case GoTo6thFloor:
            case GoTo7thFloor:
            case GoTo8thFloor:
            case GoTo9thFloor:
            case GoTo10thFloor:
                if (getGoType() == GameObjectType.transport) {
                    setGoState(GOState.forValue(action));
                } else {
                    Log.outError(LogFilter.spells, String.format("Spell %1$s targeted non-transport gameobject for transport only action \"Go to Floor\" %2$s in effect %3$s", spellId, action, effectIndex));
                }

                break;
            case PlayAnimKit:
                setAnimKitId((short) param, false);

                break;
            case OpenAndPlayAnimKit:
                if (unitCaster) {
                    useDoorOrButton(0, false, unitCaster);
                }

                setAnimKitId((short) param, false);

                break;
            case CloseAndPlayAnimKit:
                resetDoorOrButton();
                setAnimKitId((short) param, false);

                break;
            case PlayOneShotAnimKit:
                setAnimKitId((short) param, true);

                break;
            case StopAnimKit:
                setAnimKitId((short) 0, false);

                break;
            case OpenAndStopAnimKit:
                if (unitCaster) {
                    useDoorOrButton(0, false, unitCaster);
                }

                setAnimKitId((short) 0, false);

                break;
            case CloseAndStopAnimKit:
                resetDoorOrButton();
                setAnimKitId((short) 0, false);

                break;
            case PlaySpellVisual:
                setSpellVisualId((int) param, spellCaster.getGUID());

                break;
            case StopSpellVisual:
                setSpellVisualId(0);

                break;
            default:
                Log.outError(LogFilter.spells, String.format("Spell %1$s has unhandled action %2$s in effect %3$s", spellId, action, effectIndex));

                break;
        }
    }

    public final void setGoArtKit(int artkit, GameObject go, int lowguid) {
        GameObjectData data = null;

        if (go != null) {
            go.setGoArtKit(artkit);
            data = go.getGameObjectData();
        } else if (lowguid != 0) {
            data = global.getObjectMgr().getGameObjectData(lowguid);
        }

        if (data != null) {
            data.artKit = artkit;
        }
    }

    public final void use(Unit user) {
        // by default spell caster is user
        var spellCaster = user;
        int spellId = 0;
        var triggered = false;

        var playerUser = user.toPlayer();

        if (playerUser != null) {
            if (goInfo.getNoDamageImmune() != 0 && playerUser.hasUnitFlag(UnitFlag.Immune)) {
                return;
            }

            if (!goInfo.isUsableMounted()) {
                playerUser.removeAurasByType(AuraType.Mounted);
            }

            playerUser.getPlayerTalkClass().clearMenus();

            if (getAI().onGossipHello(playerUser)) {
                return;
            }
        }

        // If cooldown data present in template
        var cooldown = getTemplate().getCooldown();

        if (cooldown != 0) {
            if (cooldownTime > gameTime.GetGameTime()) {
                return;
            }

            cooldownTime = gameTime.GetGameTimeMS() + cooldown * time.InMilliseconds;
        }

        switch (getGoType()) {
            case Door: //0
            case Button: //1
                //doors/buttons never really despawn, only reset to default state/flags
                useDoorOrButton(0, false, user);

                return;
            case QuestGiver: //2
            {
                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                player.prepareGossipMenu(this, getTemplate().questGiver.gossipID, true);
                player.sendPreparedGossip(this);

                return;
            }
            case Chest: //3
            {
                var player = user.toPlayer();

                if (!player) {
                    return;
                }

                var bg = player.getBattleground();

                if (bg != null && !bg.canActivateGO((int) getEntry(), (int) bg.getPlayerTeam(user.getGUID()).getValue())) {
                    return;
                }

                var info = getTemplate();

                if (getLoot() == null && info.getLootId() != 0) {
                    if (info.getLootId() != 0) {
                        var group = player.getGroup();
                        var groupRules = group != null && info.chest.usegrouplootrules != 0;

                        setLoot(new loot(getMap(), getGUID(), LootType.chest, groupRules ? group : null));
                        getLoot().setDungeonEncounterId(info.chest.dungeonEncounter);
                        getLoot().fillLoot(info.getLootId(), LootStorage.GAMEOBJECT, player, !groupRules, false, getLootMode(), getMap().getDifficultyLootItemContext());

                        if (getLootMode().getValue() > 0) {
                            var addon = getTemplateAddon();

                            if (addon != null) {
                                getLoot().generateMoneyLoot(addon.mingold, addon.maxgold);
                            }
                        }
                    }

                    /** @todo possible must be moved to loot release (in different from linked triggering)
                     */
                    if (info.chest.triggeredEvent != 0) {
                        GameEvents.trigger(info.chest.triggeredEvent, player, this);
                    }

                    // triggering linked GO
                    var trapEntry = info.chest.linkedTrap;

                    if (trapEntry != 0) {
                        triggeringLinkedGameObject(trapEntry, player);
                    }
                } else if (!personalLoot.containsKey(player.getGUID())) {
                    if (info.chest.chestPersonalLoot != 0) {
                        var addon = getTemplateAddon();

                        if (info.chest.dungeonEncounter != 0) {
                            ArrayList<Player> tappers = new ArrayList<>();

                            for (var tapperGuid : getTapList()) {
                                var tapper = global.getObjAccessor().getPlayer(this, tapperGuid);

                                if (tapper != null) {
                                    tappers.add(tapper);
                                }
                            }

                            if (tappers.isEmpty()) {
                                tappers.add(player);
                            }

                            personalLoot = LootManager.generateDungeonEncounterPersonalLoot(info.chest.dungeonEncounter, info.chest.chestPersonalLoot, LootStorage.GAMEOBJECT, LootType.chest, this, addon != null ? addon.Mingold : 0, addon != null ? addon.Maxgold : 0, (short) getLootMode().getValue(), getMap().getDifficultyLootItemContext(), tappers);
                        } else {
                            Loot loot = new loot(getMap(), getGUID(), LootType.chest, null);
                            personalLoot.put(player.getGUID(), loot);

                            loot.setDungeonEncounterId(info.chest.dungeonEncounter);
                            loot.fillLoot(info.chest.chestPersonalLoot, LootStorage.GAMEOBJECT, player, true, false, getLootMode(), getMap().getDifficultyLootItemContext());

                            if (getLootMode().getValue() > 0 && addon != null) {
                                loot.generateMoneyLoot(addon.mingold, addon.maxgold);
                            }
                        }
                    }
                }

                if (!uniqueUsers.contains(player.getGUID()) && info.getLootId() == 0) {
                    if (info.chest.chestPushLoot != 0) {
                        Loot pushLoot = new loot(getMap(), getGUID(), LootType.chest, null);
                        pushLoot.fillLoot(info.chest.chestPushLoot, LootStorage.GAMEOBJECT, player, true, false, getLootMode(), getMap().getDifficultyLootItemContext());
                        pushLoot.autoStore(player, ItemConst.NullBag, ItemConst.NullSlot);
                    }

                    if (info.chest.triggeredEvent != 0) {
                        GameEvents.trigger(info.chest.triggeredEvent, player, this);
                    }

                    // triggering linked GO
                    var trapEntry = info.chest.linkedTrap;

                    if (trapEntry != 0) {
                        triggeringLinkedGameObject(trapEntry, player);
                    }

                    addUniqueUse(player);
                }

                if (getLootState() != LootState.Activated) {
                    setLootState(LootState.Activated, player);
                }

                // Send loot
                var playerLoot = getLootForPlayer(player);

                if (playerLoot != null) {
                    player.sendLoot(playerLoot);
                }

                break;
            }
            case Trap: //6
            {
                var goInfo = getTemplate();

                if (goInfo.trap.spell != 0) {
                    castSpell(user, goInfo.trap.spell);
                }

                cooldownTime = gameTime.GetGameTimeMS() + (goInfo.trap.cooldown != 0 ? goInfo.trap.cooldown : 4) * time.InMilliseconds; // template or 4 seconds

                if (goInfo.trap.charges == 1) // Deactivate after trigger
                {
                    setLootState(LootState.JustDeactivated);
                }

                return;
            }
            //Sitting: Wooden bench, chairs enzz
            case Chair: //7
            {
                var info = getTemplate();

                if (chairListSlots.isEmpty()) // this is called once at first chair use to make list of available slots
                {
                    if (info.chair.chairslots > 0) // sometimes chairs in DB have error in fields and we dont know number of slots
                    {
                        for (int i = 0; i < info.chair.chairslots; ++i) {
                            chairListSlots.put(i,
                            default); // Last user of current slot set to 0 (none sit here yet)
                        }
                    } else {
                        chairListSlots.put(0,
                        default); // error in DB, make one default slot
                    }
                }

                // a chair may have n slots. we have to calculate their positions and teleport the player to the nearest one
                var lowestDist = SharedConst.DefaultVisibilityDistance;

                int nearest_slot = 0;
                var x_lowest = getLocation().getX();
                var y_lowest = getLocation().getY();

                // the object orientation + 1/2 pi
                // every slot will be on that straight line
                var orthogonalOrientation = getLocation().getO() + MathUtil.PI * 0.5f;
                // find nearest slot
                var found_free_slot = false;


                for (var(slot, sittingUnit) : chairListSlots.ToList()) {
                    // the distance between this slot and the center of the go - imagine a 1D space
                    var relativeDistance = (info.size * slot) - (info.size * (info.chair.chairslots - 1) / 2.0f);

                    var x_i = (float) (getLocation().getX() + relativeDistance * Math.cos(orthogonalOrientation));
                    var y_i = (float) (getLocation().getY() + relativeDistance * Math.sin(orthogonalOrientation));

                    if (!sittingUnit.IsEmpty) {
                        var chairUser = global.getObjAccessor().GetUnit(this, sittingUnit);

                        if (chairUser != null) {
                            if (chairUser.isSitState() && chairUser.getStandState() != UnitStandStateType.Sit && chairUser.getLocation().GetExactDist2d(x_i, y_i) < 0.1f) {
                                continue; // This seat is already occupied by ChairUser. NOTE: Not sure if the ChairUser.getStandState() != UNIT_STAND_STATE_SIT check is required.
                            }

                            chairListSlots.get(slot).clear(); // This seat is unoccupied.
                        } else {
                            chairListSlots.get(slot).clear(); // The seat may of had an occupant, but they're offline.
                        }
                    }

                    found_free_slot = true;

                    // calculate the distance between the player and this slot
                    var thisDistance = user.getDistance2d(x_i, y_i);

                    if (thisDistance <= lowestDist) {
                        nearest_slot = slot;
                        lowestDist = thisDistance;
                        x_lowest = x_i;
                        y_lowest = y_i;
                    }
                }

                if (found_free_slot) {
                    var guid = chairListSlots.get(nearest_slot);

                    if (guid.IsEmpty) {
                        chairListSlots.put(nearest_slot, user.getGUID()); //this slot in now used by player
                        user.nearTeleportTo(x_lowest, y_lowest, getLocation().getZ(), getLocation().getO());
                        user.setStandState(UnitStandStateType.SitLowChair + (byte) info.chair.chairheight);

                        if (info.chair.triggeredEvent != 0) {
                            GameEvents.trigger(info.chair.triggeredEvent, user, this);
                        }

                        return;
                    }
                }

                return;
            }
            case SpellFocus: //8
            {
                // triggering linked GO
                var trapEntry = getTemplate().spellFocus.linkedTrap;

                if (trapEntry != 0) {
                    triggeringLinkedGameObject(trapEntry, user);
                }

                break;
            }
            //big gun, its a spell/aura
            case Goober: //10
            {
                var info = getTemplate();
                var player = user.toPlayer();

                if (player != null) {
                    if (info.goober.pageID != 0) // show page...
                    {
                        PageTextPkt data = new PageTextPkt();
                        data.gameObjectGUID = getGUID();
                        player.sendPacket(data);
                    } else if (info.goober.gossipID != 0) {
                        player.prepareGossipMenu(this, info.goober.gossipID);
                        player.sendPreparedGossip(this);
                    }

                    if (info.goober.eventID != 0) {
                        Log.outDebug(LogFilter.Scripts, "Goober ScriptStart id {0} for GO entry {1} (GUID {2}).", info.goober.eventID, getEntry(), getSpawnId());
                        GameEvents.trigger(info.goober.eventID, player, this);
                    }

                    // possible quest objective for active quests
                    if (info.goober.questID != 0 && global.getObjectMgr().getQuestTemplate(info.goober.questID) != null) {
                        //Quest require to be active for GO using
                        if (player.getQuestStatus(info.goober.questID) != QuestStatus.INCOMPLETE) {
                            break;
                        }
                    }

                    var group = player.getGroup();

                    if (group) {
                        for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                            var member = refe.getSource();

                            if (member) {
                                if (member.isAtGroupRewardDistance(this)) {
                                    member.killCreditGO(info.entry, getGUID());
                                }
                            }
                        }
                    } else {
                        player.killCreditGO(info.entry, getGUID());
                    }
                }

                var trapEntry = info.goober.linkedTrap;

                if (trapEntry != 0) {
                    triggeringLinkedGameObject(trapEntry, user);
                }

                if (info.goober.allowMultiInteract != 0 && player != null) {
                    if (info.isDespawnAtAction()) {
                        despawnForPlayer(player, duration.FromSeconds(respawnDelayTime));
                    } else {
                        setGoStateFor(GOState.active, player);
                    }
                } else {
                    setFlag(GameObjectFlags.InUse);
                    setLootState(LootState.Activated, user);

                    // this appear to be ok, however others exist in addition to this that should have custom (ex: 190510, 188692, 187389)
                    if (info.goober.customAnim != 0) {
                        sendCustomAnim(getGoAnimProgress());
                    } else {
                        setGoState(GOState.active);
                    }

                    cooldownTime = gameTime.GetGameTimeMS() + info.getAutoCloseTime();
                }

                // cast this spell later if provided
                spellId = info.goober.spell;

                if (info.goober.playerCast == 0) {
                    spellCaster = null;
                }

                break;
            }
            case Camera: //13
            {
                var info = getTemplate();

                if (info == null) {
                    return;
                }

                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                if (info.camera.camera != 0) {
                    player.sendCinematicStart(info.camera.camera);
                }

                if (info.camera.eventID != 0) {
                    GameEvents.trigger(info.camera.eventID, player, this);
                }

                return;
            }
            //fishing bobber
            case FishingNode: //17
            {
                var player = user.toPlayer();

                if (player == null) {
                    return;
                }

                if (ObjectGuid.opNotEquals(player.getGUID(), getOwnerGUID())) {
                    return;
                }

                switch (getLootState()) {
                    case Ready: // ready for loot
                    {
                        setLootState(LootState.Activated, player);

                        setGoState(GOState.active);
                        replaceAllFlags(GameObjectFlags.InMultiUse);

                        sendUpdateToPlayer(player);

                        int zone;
                        tangible.OutObject<Integer> tempOut_zone = new tangible.OutObject<Integer>();
                        int subzone;
                        tangible.OutObject<Integer> tempOut_subzone = new tangible.OutObject<Integer>();
                        getZoneAndAreaId(tempOut_zone, tempOut_subzone);
                        subzone = tempOut_subzone.outArgValue;
                        zone = tempOut_zone.outArgValue;

                        var zone_skill = global.getObjectMgr().getFishingBaseSkillLevel(subzone);

                        if (zone_skill == 0) {
                            zone_skill = global.getObjectMgr().getFishingBaseSkillLevel(zone);
                        }

                        //provide error, no fishable zone or area should be 0
                        if (zone_skill == 0) {
                            Logs.SQL.error("Fishable areaId {0} are not properly defined in `skill_fishing_base_level`.", subzone);
                        }

                        int skill = player.getSkillValue(SkillType.ClassicFishing);

                        int chance;

                        if (skill < zone_skill) {
                            chance = (int) (Math.pow((double) skill / zone_skill, 2) * 100);

                            if (chance < 1) {
                                chance = 1;
                            }
                        } else {
                            chance = 100;
                        }

                        var roll = RandomUtil.IRand(1, 100);

                        Log.outDebug(LogFilter.Server, "Fishing check (skill: {0} zone min skill: {1} chance {2} roll: {3}", skill, zone_skill, chance, roll);

                        player.updateFishingSkill();

                        // @todo find reasonable value for fishing hole search
                        var fishingPool = lookupFishingHoleAround(20.0f + SharedConst.contactDistance);

                        // If fishing skill is high enough, or if fishing on a pool, send correct loot.
                        // Fishing pools have no skill requirement as of patch 3.3.0 (undocumented change).
                        if (chance >= roll || fishingPool) {
                            // @todo I do not understand this hack. Need some explanation.
                            // prevent removing GO at spell cancel
                            removeFromOwner();
                            setOwnerGUID(player.getGUID());

                            if (fishingPool) {
                                fishingPool.use(player);
                                setLootState(LootState.JustDeactivated);
                            } else {
                                setLoot(getFishLoot(player));
                                player.sendLoot(getLoot());
                            }
                        } else // If fishing skill is too low, send junk loot.
                        {
                            setLoot(getFishLootJunk(player));
                            player.sendLoot(getLoot());
                        }

                        break;
                    }
                    case JustDeactivated: // nothing to do, will be deleted at next update
                        break;
                    default: {
                        setLootState(LootState.JustDeactivated);
                        player.sendPacket(new FishNotHooked());

                        break;
                    }
                }

                player.finishSpell(CurrentSpellTypes.Channeled);

                return;
            }

            case Ritual: //18
            {
                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                var owner = getOwnerUnit();

                var info = getTemplate();

                // ritual owner is set for GO's without owner (not summoned)
                if (ritualOwner == null && owner == null) {
                    ritualOwner = player;
                }

                if (owner != null) {
                    if (!owner.isTypeId(TypeId.PLAYER)) {
                        return;
                    }

                    // accept only use by player from same group as owner, excluding owner itself (unique use already added in spell effect)
                    if (player == owner.toPlayer() || (info.ritual.castersGrouped != 0 && !player.isInSameRaidWith(owner.toPlayer()))) {
                        return;
                    }

                    // expect owner to already be channeling, so if not...
                    if (owner.getCurrentSpell(CurrentSpellTypes.Channeled) == null) {
                        return;
                    }

                    // in case summoning ritual caster is GO creator
                    spellCaster = owner;
                } else {
                    if (player != ritualOwner && (info.ritual.castersGrouped != 0 && !player.isInSameRaidWith(ritualOwner))) {
                        return;
                    }

                    spellCaster = player;
                }

                addUniqueUse(player);

                if (info.ritual.animSpell != 0) {
                    player.castSpell(player, info.ritual.animSpell, true);

                    // for this case, summoningRitual.spellId is always triggered
                    triggered = true;
                }

                // full amount unique participants including original summoner
                if (getUniqueUseCount() == info.ritual.casters) {
                    if (ritualOwner != null) {
                        spellCaster = ritualOwner;
                    }

                    spellId = info.ritual.spell;

                    if (spellId == 62330) // GO store nonexistent spell, replace by expected
                    {
                        // spell have reagent and mana cost but it not expected use its
                        // it triggered spell in fact casted at currently channeled GO
                        spellId = 61993;
                        triggered = true;
                    }

                    // Cast casterTargetSpell at a random GO user
                    // on the current DB there is only one gameobject that uses this (Ritual of Doom)
                    // and its required target number is 1 (outter for loop will run once)
                    if (info.ritual.casterTargetSpell != 0 && info.ritual.casterTargetSpell != 1) // No idea why this field is a bool in some cases
                    {
                        for (int i = 0; i < info.ritual.casterTargetSpellTargets; i++) {
                            // m_unique_users can contain only player GUIDs
                            var target = global.getObjAccessor().getPlayer(this, uniqueUsers.SelectRandom());

                            if (target != null) {
                                spellCaster.castSpell(target, info.ritual.casterTargetSpell, true);
                            }
                        }
                    }

                    // finish owners spell
                    if (owner != null) {
                        owner.finishSpell(CurrentSpellTypes.Channeled);
                    }

                    // can be deleted now, if
                    if (info.ritual.ritualPersistent == 0) {
                        setLootState(LootState.JustDeactivated);
                    } else {
                        // reset ritual for this GO
                        ritualOwner = null;
                        uniqueUsers.clear();
                        usetimes = 0;
                    }
                } else {
                    return;
                }

                // go to end function to spell casting
                break;
            }
            case SpellCaster: //22
            {
                var info = getTemplate();

                if (info == null) {
                    return;
                }

                if (info.spellCaster.partyOnly != 0) {
                    var caster = getOwnerUnit();

                    if (caster == null || !caster.isTypeId(TypeId.PLAYER)) {
                        return;
                    }

                    if (!user.isTypeId(TypeId.PLAYER) || !user.toPlayer().isInSameRaidWith(caster.toPlayer())) {
                        return;
                    }
                }

                user.removeAurasByType(AuraType.Mounted);
                spellId = info.spellCaster.spell;

                addUse();

                break;
            }
            case MeetingStone: //23
            {
                var info = getTemplate();

                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                var targetPlayer = global.getObjAccessor().findPlayer(player.getTarget());

                // accept only use by player from same raid as caster, except caster itself
                if (targetPlayer == null || targetPlayer == player || !targetPlayer.isInSameRaidWith(player)) {
                    return;
                }

                //required lvl checks!
                var userLevels = global.getDB2Mgr().GetContentTuningData(info.requiredLevel, player.getPlayerData().ctrOptions.getValue().contentTuningConditionMask);

                if (userLevels != null) {
                    if (player.getLevel() < userLevels.getValue().maxLevel) {
                        return;
                    }
                }

                var targetLevels = global.getDB2Mgr().GetContentTuningData(info.requiredLevel, targetPlayer.getPlayerData().ctrOptions.getValue().contentTuningConditionMask);

                if (targetLevels != null) {
                    if (targetPlayer.getLevel() < targetLevels.getValue().maxLevel) {
                        return;
                    }
                }

                if (info.entry == 194097) {
                    spellId = 61994; // Ritual of Summoning
                } else {
                    spellId = 23598; // 59782;                            // Summoning Stone Effect
                }

                break;
            }

            case FlagStand: // 24
            {
                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                if (player.canUseBattlegroundObject(this)) {
                    // in Battlegroundcheck
                    var bg = player.getBattleground();

                    if (!bg) {
                        return;
                    }

                    if (player.getVehicle1() != null) {
                        return;
                    }

                    player.removeAurasByType(AuraType.ModStealth);
                    player.removeAurasByType(AuraType.ModInvisibility);
                    // BG flag click
                    // AB:
                    // 15001
                    // 15002
                    // 15003
                    // 15004
                    // 15005
                    bg.eventPlayerClickedOnFlag(player, this);

                    return; //we don;t need to delete flag ... it is despawned!
                }

                break;
            }

            case FishingHole: // 25
            {
                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                var loot = new loot(getMap(), getGUID(), LootType.Fishinghole, null);
                loot.fillLoot(getTemplate().getLootId(), LootStorage.GAMEOBJECT, player, true);
                personalLoot.put(player.getGUID(), loot);

                player.sendLoot(loot);
                player.updateCriteria(CriteriaType.CatchFishInFishingHole, getTemplate().entry);

                return;
            }

            case FlagDrop: // 26
            {
                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                if (player.canUseBattlegroundObject(this)) {
                    // in Battlegroundcheck
                    var bg = player.getBattleground();

                    if (!bg) {
                        return;
                    }

                    if (player.getVehicle1() != null) {
                        return;
                    }

                    player.removeAurasByType(AuraType.ModStealth);
                    player.removeAurasByType(AuraType.ModInvisibility);
                    // BG flag dropped
                    // WS:
                    // 179785 - Silverwing Flag
                    // 179786 - Warsong Flag
                    // EotS:
                    // 184142 - Netherstorm Flag
                    var info = getTemplate();

                    if (info != null) {
                        switch (info.entry) {
                            case 179785: // Silverwing Flag
                            case 179786: // Warsong Flag
                                if (bg.getTypeID(true) == BattlegroundTypeId.WS) {
                                    bg.eventPlayerClickedOnFlag(player, this);
                                }

                                break;
                            case 184142: // Netherstorm Flag
                                if (bg.getTypeID(true) == BattlegroundTypeId.EY) {
                                    bg.eventPlayerClickedOnFlag(player, this);
                                }

                                break;
                        }

                        if (info.flagDrop.eventID != 0) {
                            GameEvents.trigger(info.flagDrop.eventID, player, this);
                        }
                    }

                    //this cause to call return, all flags must be deleted here!!
                    spellId = 0;
                    delete();
                }

                break;
            }
            case BarberChair: //32
            {
                var info = getTemplate();

                if (info == null) {
                    return;
                }

                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();

                player.sendPacket(new EnableBarberShop());

                // fallback, will always work
                player.teleportTo(getLocation().getMapId(), getLocation().getX(), getLocation().getY(), getLocation().getZ(), getLocation().getO(), (TeleportToOptions.NotLeaveTransport.getValue() | TeleportToOptions.NotLeaveCombat.getValue().getValue() | TeleportToOptions.NotUnSummonPet.getValue().getValue()));
                player.setStandState((UnitStandStateType.SitLowChair + (byte) info.barberChair.chairheight), info.barberChair.sitAnimKit);

                return;
            }
            case NewFlag: {
                var info = getTemplate();

                if (info == null) {
                    return;
                }

                if (!user.isPlayer()) {
                    return;
                }

                spellId = info.newFlag.pickupSpell;

                break;
            }
            case ItemForge: {
                var info = getTemplate();

                if (info == null) {
                    return;
                }

                if (!user.isTypeId(TypeId.PLAYER)) {
                    return;
                }

                var player = user.toPlayer();
                var playerCondition = CliDB.PlayerConditionStorage.get(info.itemForge.conditionID1);

                if (playerCondition != null) {
                    if (!ConditionManager.isPlayerMeetingCondition(player, playerCondition)) {
                        return;
                    }
                }

                switch (info.itemForge.forgeType) {
                    case 0: // Artifact Forge
                    case 1: // Relic Forge
                    {
                        var artifactAura = player.getAura(PlayerConst.ArtifactsAllWeaponsGeneralWeaponEquippedPassive);
                        var item = artifactAura != null ? player.getItemByGuid(artifactAura.castItemGuid) : null;

                        if (!item) {
                            player.sendPacket(new DisplayGameError(GameError.MustEquipArtifact));

                            return;
                        }

                        OpenArtifactForge openArtifactForge = new OpenArtifactForge();
                        openArtifactForge.artifactGUID = item.getGUID();
                        openArtifactForge.forgeGUID = getGUID();
                        player.sendPacket(openArtifactForge);

                        break;
                    }
                    case 2: // Heart Forge
                    {
                        var item = player.getItemByEntry(PlayerConst.ItemIdHeartOfAzeroth, ItemSearchLocation.Everywhere);

                        if (!item) {
                            return;
                        }

                        GameObjectInteraction openHeartForge = new GameObjectInteraction();
                        openHeartForge.objectGUID = getGUID();
                        openHeartForge.interactionType = PlayerInteractionType.AzeriteForge;
                        player.sendPacket(openHeartForge);

                        break;
                    }
                    default:
                        break;
                }

                break;
            }
            case UILink: {
                var player = user.toPlayer();

                if (!player) {
                    return;
                }

                GameObjectInteraction gameObjectUILink = new GameObjectInteraction();
                gameObjectUILink.objectGUID = getGUID();

                switch (getTemplate().UILink.UILinkType) {
                    case 0:
                        gameObjectUILink.interactionType = PlayerInteractionType.AdventureJournal;

                        break;
                    case 1:
                        gameObjectUILink.interactionType = PlayerInteractionType.ObliterumForge;

                        break;
                    case 2:
                        gameObjectUILink.interactionType = PlayerInteractionType.ScrappingMachine;

                        break;
                    case 3:
                        gameObjectUILink.interactionType = PlayerInteractionType.ItemInteraction;

                        break;
                    default:
                        break;
                }

                player.sendPacket(gameObjectUILink);

                return;
            }
            case GatheringNode: //50
            {
                var player = user.toPlayer();

                if (player == null) {
                    return;
                }

                var info = getTemplate();

                if (!personalLoot.containsKey(player.getGUID())) {
                    if (info.gatheringNode.chestLoot != 0) {
                        Loot newLoot = new loot(getMap(), getGUID(), LootType.chest, null);
                        personalLoot.put(player.getGUID(), newLoot);

                        newLoot.fillLoot(info.gatheringNode.chestLoot, LootStorage.GAMEOBJECT, player, true, false, getLootMode(), getMap().getDifficultyLootItemContext());
                    }

                    if (info.gatheringNode.triggeredEvent != 0) {
                        GameEvents.trigger(info.gatheringNode.triggeredEvent, player, this);
                    }

                    // triggering linked GO
                    var trapEntry = info.gatheringNode.linkedTrap;

                    if (trapEntry != 0) {
                        triggeringLinkedGameObject(trapEntry, player);
                    }

                    if (info.gatheringNode.xpDifficulty != 0 && info.gatheringNode.xpDifficulty < 10) {
                        var questXp = CliDB.QuestXPStorage.get(player.getLevel());

                        if (questXp != null) {
                            var xp = quest.roundXPValue(questXp.Difficulty[info.gatheringNode.xpDifficulty]);

                            if (xp != 0) {
                                player.giveXP(xp, null);
                            }
                        }
                    }

                    spellId = info.gatheringNode.spell;
                }

                if (personalLoot.size() >= info.gatheringNode.maxNumberofLoots) {
                    setGoState(GOState.active);
                    setDynamicFlag(GameObjectDynamicLowFlag.NoInterract);
                }

                if (getLootState() != LootState.Activated) {
                    setLootState(LootState.Activated, player);

                    if (info.gatheringNode.objectDespawnDelay != 0) {
                        despawnOrUnsummon(duration.FromSeconds(info.gatheringNode.objectDespawnDelay));
                    }
                }

                // Send loot
                var loot = getLootForPlayer(player);

                if (loot != null) {
                    player.sendLoot(loot);
                }

                break;
            }
            default:
                if (getGoType().getValue() >= GameObjectType.max.getValue()) {
                    Log.outError(LogFilter.Server, "GameObject.use(): unit (type: {0}, guid: {1}, name: {2}) tries to use object (guid: {3}, entry: {4}, name: {5}) of unknown type ({6})", user.getObjectTypeId(), user.getGUID().toString(), user.getName(), getGUID().toString(), getEntry(), getTemplate().name, getGoType());
                }

                break;
        }

        if (spellId == 0) {
            return;
        }

        if (!global.getSpellMgr().hasSpellInfo(spellId, getMap().getDifficultyID())) {
            if (!user.isTypeId(TypeId.PLAYER) || !global.getOutdoorPvPMgr().handleCustomSpell(user.toPlayer(), spellId, this)) {
                Log.outError(LogFilter.Server, "WORLD: unknown spell id {0} at use action for gameobject (Entry: {1} GoType: {2})", spellId, getEntry(), getGoType());
            } else {
                Log.outDebug(LogFilter.Outdoorpvp, "WORLD: {0} non-dbc spell was handled by OutdoorPvP", spellId);
            }

            return;
        }

        var player1 = user.toPlayer();

        if (player1) {
            global.getOutdoorPvPMgr().handleCustomSpell(player1, spellId, this);
        }

        if (spellCaster != null) {
            spellCaster.castSpell(user, spellId, triggered);
        } else {
            castSpell(user, spellId);
        }
    }

    public final void sendCustomAnim(int anim) {
        GameObjectCustomAnim customAnim = new GameObjectCustomAnim();
        customAnim.objectGUID = getGUID();
        customAnim.customAnim = anim;
        sendMessageToSet(customAnim, true);
    }

    public final boolean isInRange(float x, float y, float z, float radius) {
        var info = CliDB.GameObjectDisplayInfoStorage.get(goInfo.displayId);

        if (info == null) {
            return isWithinDist3D(x, y, z, radius);
        }

        var sinA = (float) Math.sin(getLocation().getO());
        var cosA = (float) Math.cos(getLocation().getO());
        var dx = x - getLocation().getX();
        var dy = y - getLocation().getY();
        var dz = z - getLocation().getZ();
        var dist = (float) Math.sqrt(dx * dx + dy * dy);

        //! Check if the distance between the 2 objects is 0, can happen if both objects are on the same position.
        //! The code below this check wont crash if dist is 0 because 0/0 in float operations is valid, and returns infinite
        if (MathUtil.fuzzyEq(dist, 0.0f)) {
            return true;
        }

        var sinB = dx / dist;
        var cosB = dy / dist;
        dx = dist * (cosA * cosB + sinA * sinB);
        dy = dist * (cosA * sinB - sinA * cosB);

        return dx < info.GeoBoxMax.X + radius && dx > info.GeoBoxMin.X - radius && dy < info.GeoBoxMax.Y + radius && dy > info.GeoBoxMin.Y - radius && dz < info.GeoBoxMax.Z + radius && dz > info.GeoBoxMin.Z - radius;
    }

    @Override
    public String getName() {
        return getName(locale.enUS);
    }

    @Override
    public String getName(Locale locale) {
        if (locale != locale.enUS) {
            var cl = global.getObjectMgr().getGameObjectLocale(getEntry());

            if (cl != null) {
                if (cl.name.length > locale.getValue() && !cl.name.charAt(new integer(locale.getValue())).isEmpty()) {
                    return cl.name.charAt(locale.getValue());
                }
            }
        }

        return super.getName(locale);
    }

    public final void updatePackedRotation() {
        final int PACK_YZ = 1 << 20;
        final int PACK_X = PACK_YZ << 1;

        final int PACK_YZ_MASK = (PACK_YZ << 1) - 1;
        final int PACK_X_MASK = (PACK_X << 1) - 1;

        var w_sign = (byte) (localRotation.W >= 0.0f ? 1 : -1);
        long x = (int) (localRotation.X * PACK_X) * w_sign & PACK_X_MASK;
        long y = (int) (localRotation.Y * PACK_YZ) * w_sign & PACK_YZ_MASK;
        long z = (int) (localRotation.Z * PACK_YZ) * w_sign & PACK_YZ_MASK;
        packedRotation = z | (y << 21) | (x << 42);
    }

    public final void setLocalRotation(float qx, float qy, float qz, float qw) {
        var rotation = new Quaternion(qx, qy, qz, qw);
        rotation = Quaternion.Multiply(rotation, 1.0f / (float) Math.sqrt(Quaternion.Dot(rotation, rotation)));

        localRotation.X = rotation.X;
        localRotation.Y = rotation.Y;
        localRotation.Z = rotation.Z;
        localRotation.W = rotation.W;
        updatePackedRotation();
    }

    public final void setParentRotation(Quaternion rotation) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().parentRotation), rotation);
    }

    public final void setLocalRotationAngles(float z_rot, float y_rot, float x_rot) {
        var quat = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(z_rot, y_rot, x_rot));
        setLocalRotation(quat.X, quat.Y, quat.Z, quat.W);
    }

    public final QuaternionData getWorldRotation() {
        var localRotation = getLocalRotation();

        var transport = (Transport) this.getTransport();

        if (transport != null) {
            var worldRotation = transport.getWorldRotation();
            Quaternion worldRotationQuat = new Quaternion(worldRotation.x, worldRotation.y, worldRotation.z, worldRotation.w);
            Quaternion localRotationQuat = new Quaternion(localRotation.x, localRotation.y, localRotation.z, localRotation.w);
            Quaternion mulled = localRotationQuat.mul(worldRotationQuat);
            return new QuaternionData(mulled.x, mulled.y, mulled.z, mulled.w);
        }
        return localRotation;
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nSpawnId: %2$s GoState: %3$s ScriptId: %4$s AIName: %5$s", super.getDebugInfo(), getSpawnId(), getGoState(), getScriptId(), getAiName());
    }

    public final boolean isAtInteractDistance(Player player) {
        return isAtInteractDistance(player, null);
    }

    public final boolean isAtInteractDistance(Player player, SpellInfo spell) {
        if (spell != null || (spell = getSpellForLock(player)) != null) {
            var maxRange = spell.getMaxRange(spell.isPositive());

            if (getGoType() == GameObjectType.spellFocus) {
                return maxRange * maxRange >= getLocation().getExactDistSq(player.getLocation());
            }

            if (CliDB.GameObjectDisplayInfoStorage.containsKey(getTemplate().displayId)) {
                return isAtInteractDistance(player.getLocation(), maxRange);
            }
        }

        return isAtInteractDistance(player.getLocation(), getInteractionDistance());
    }

    public final boolean isWithinDistInMap(Player player) {
        return isInMap(player) && inSamePhase(player) && isAtInteractDistance(player);
    }

    public final SpellInfo getSpellForLock(Player player) {
        if (!player) {
            return null;
        }

        var lockId = getTemplate().getLockId();

        if (lockId == 0) {
            return null;
        }

        var lockEntry = CliDB.LockStorage.get(lockId);

        if (lockEntry == null) {
            return null;
        }

        for (byte i = 0; i < SharedConst.MaxLockCase; ++i) {
            if (lockEntry.LockType[i] == 0) {
                continue;
            }

            if (lockEntry.LockType[i] == (byte) LockKeyType.spell.getValue()) {
                var spell = global.getSpellMgr().getSpellInfo((int) lockEntry.Index[i], getMap().getDifficultyID());

                if (spell != null) {
                    return spell;
                }
            }

            if (lockEntry.LockType[i] != (byte) LockKeyType.skill.getValue()) {
                break;
            }

            for (var playerSpell : player.getSpellMap().entrySet()) {
                var spell = global.getSpellMgr().getSpellInfo(playerSpell.getKey(), getMap().getDifficultyID());

                if (spell != null) {
                    for (var effect : spell.effects) {
                        if (effect.effect == SpellEffectName.OpenLock && effect.miscValue == lockEntry.Index[i]) {
                            if (effect.calcValue(player) >= lockEntry.Skill[i]) {
                                return spell;
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    public final void modifyHealth(double change, WorldObject attackerOrHealer) {
        modifyHealth(change, attackerOrHealer, 0);
    }

    public final void modifyHealth(double change) {
        modifyHealth(change, null, 0);
    }

    public final void modifyHealth(double change, WorldObject attackerOrHealer, int spellId) {
        modifyHealth((int) change, attackerOrHealer, spellId);
    }

    public final void modifyHealth(int change, WorldObject attackerOrHealer) {
        modifyHealth(change, attackerOrHealer, 0);
    }

    public final void modifyHealth(int change) {
        modifyHealth(change, null, 0);
    }

    public final void modifyHealth(int change, WorldObject attackerOrHealer, int spellId) {
        if (goValueProtected.building.maxHealth == 0 || change == 0) {
            return;
        }

        // prevent double destructions of the same object
        if (change < 0 && goValueProtected.building.health == 0) {
            return;
        }

        if (goValueProtected.building.health + change <= 0) {
            goValueProtected.building.health = 0;
        } else if (goValueProtected.building.health + change >= goValueProtected.building.maxHealth) {
            goValueProtected.building.health = goValueProtected.building.maxHealth;
        } else {
            goValueProtected.building.health += (int) change;
        }

        // Set the health bar, value = 255 * healthPct;
        setGoAnimProgress(goValueProtected.building.Health * 255 / goValueProtected.building.maxHealth);

        // dealing damage, send packet
        var player = attackerOrHealer != null ? attackerOrHealer.getCharmerOrOwnerPlayerOrPlayerItself() : null;

        if (player != null) {
            DestructibleBuildingDamage packet = new DestructibleBuildingDamage();
            packet.caster = attackerOrHealer.getGUID(); // todo: this can be a GameObject
            packet.target = getGUID();
            packet.damage = -change;
            packet.owner = player.getGUID();
            packet.spellID = spellId;
            player.sendPacket(packet);
        }

        if (change < 0 && getTemplate().destructibleBuilding.damageEvent != 0) {
            GameEvents.trigger(getTemplate().destructibleBuilding.damageEvent, attackerOrHealer, this);
        }

        var newState = getDestructibleState();

        if (goValueProtected.building.health == 0) {
            newState = GameObjectDestructibleState.destroyed;
        } else if (goValueProtected.building.health < goValueProtected.building.MaxHealth / 2) {
            newState = GameObjectDestructibleState.Damaged;
        } else if (goValueProtected.building.health == goValueProtected.building.maxHealth) {
            newState = GameObjectDestructibleState.Intact;
        }

        if (newState == getDestructibleState()) {
            return;
        }

        setDestructibleState(newState, attackerOrHealer, false);
    }

    public final void setDestructibleState(GameObjectDestructibleState state, WorldObject attackerOrHealer) {
        setDestructibleState(state, attackerOrHealer, false);
    }

    public final void setDestructibleState(GameObjectDestructibleState state, WorldObject attackerOrHealer, boolean setHealth) {
        // the user calling this must know he is already operating on destructible gameobject
        switch (state) {
            case Intact:
                removeFlag(GameObjectFlags.Damaged.getValue() | GameObjectFlags.destroyed.getValue());
                setDisplayId(goInfo.displayId);

                if (setHealth) {
                    goValueProtected.building.health = goValueProtected.building.maxHealth;
                    setGoAnimProgress(255);
                }

                enableCollision(true);

                break;
            case Damaged: {
                if (getTemplate().destructibleBuilding.damagedEvent != 0) {
                    GameEvents.trigger(getTemplate().destructibleBuilding.damagedEvent, attackerOrHealer, this);
                }

                getAI().damaged(attackerOrHealer, goInfo.destructibleBuilding.damagedEvent);

                removeFlag(GameObjectFlags.destroyed);
                setFlag(GameObjectFlags.Damaged);

                var modelId = goInfo.displayId;
                var modelData = CliDB.DestructibleModelDataStorage.get(goInfo.destructibleBuilding.destructibleModelRec);

                if (modelData != null) {
                    if (modelData.State1Wmo != 0) {
                        modelId = modelData.State1Wmo;
                    }
                }

                setDisplayId(modelId);

                if (setHealth) {
                    goValueProtected.building.health = 10000; //m_goInfo.destructibleBuilding.damagedNumHits;
                    var maxHealth = goValueProtected.building.maxHealth;

                    // in this case current health is 0 anyway so just prevent crashing here
                    if (maxHealth == 0) {
                        maxHealth = 1;
                    }

                    setGoAnimProgress(goValueProtected.building.Health * 255 / maxHealth);
                }

                break;
            }
            case Destroyed: {
                if (getTemplate().destructibleBuilding.destroyedEvent != 0) {
                    GameEvents.trigger(getTemplate().destructibleBuilding.destroyedEvent, attackerOrHealer, this);
                }

                getAI().destroyed(attackerOrHealer, goInfo.destructibleBuilding.destroyedEvent);

                var player = attackerOrHealer != null ? attackerOrHealer.getCharmerOrOwnerPlayerOrPlayerItself() : null;

                if (player) {
                    var bg = player.getBattleground();

                    if (bg != null) {
                        bg.destroyGate(player, this);
                    }
                }

                removeFlag(GameObjectFlags.Damaged);
                setFlag(GameObjectFlags.destroyed);

                var modelId = goInfo.displayId;
                var modelData = CliDB.DestructibleModelDataStorage.get(goInfo.destructibleBuilding.destructibleModelRec);

                if (modelData != null) {
                    if (modelData.State2Wmo != 0) {
                        modelId = modelData.State2Wmo;
                    }
                }

                setDisplayId(modelId);

                if (setHealth) {
                    goValueProtected.building.health = 0;
                    setGoAnimProgress(0);
                }

                enableCollision(false);

                break;
            }
            case Rebuilding: {
                if (getTemplate().destructibleBuilding.rebuildingEvent != 0) {
                    GameEvents.trigger(getTemplate().destructibleBuilding.rebuildingEvent, attackerOrHealer, this);
                }

                removeFlag(GameObjectFlags.Damaged.getValue() | GameObjectFlags.destroyed.getValue());

                var modelId = goInfo.displayId;
                var modelData = CliDB.DestructibleModelDataStorage.get(goInfo.destructibleBuilding.destructibleModelRec);

                if (modelData != null) {
                    if (modelData.State3Wmo != 0) {
                        modelId = modelData.State3Wmo;
                    }
                }

                setDisplayId(modelId);

                // restores to full health
                if (setHealth) {
                    goValueProtected.building.health = goValueProtected.building.maxHealth;
                    setGoAnimProgress(255);
                }

                enableCollision(true);

                break;
            }
        }
    }

    public final void setLootState(LootState state, Unit unit) {
        lootState = state;
        lootStateUnitGuid = unit ? unit.getGUID() : ObjectGuid.Empty;
        getAI().onLootStateChanged((int) state.getValue(), unit);

        // Start restock timer if the chest is partially looted or not looted at all
        if (getGoType() == GameObjectType.chest && state == LootState.Activated && getTemplate().chest.chestRestockTime > 0 && restockTime == 0) {
            restockTime = gameTime.GetGameTime() + getTemplate().chest.chestRestockTime;
        }

        // only set collision for doors on SetGoState
        if (getGoType() == GameObjectType.door) {
            return;
        }

        if (getModel() != null) {
            var collision = false;

            // Use the current go state
            if ((getGoState() != GOState.Ready && (state == LootState.Activated || state == LootState.JustDeactivated)) || state == LootState.Ready) {
                collision = !collision;
            }

            enableCollision(collision);
        }
    }

    public final void onLootRelease(Player looter) {
        switch (getGoType()) {
            case Chest: {
                var goInfo = getTemplate();

                if (goInfo.chest.consumable == 0 && goInfo.chest.chestPersonalLoot != 0) {
                    despawnForPlayer(looter, goInfo.chest.chestRestockTime != 0 ? duration.FromSeconds(goInfo.chest.chestRestockTime) : duration.FromSeconds(respawnDelayTime)); // not hiding this object permanently to prevent infinite growth of m_perPlayerState
                }

                // while also maintaining some sort of cheater protection (not getting rid of entries on logout)
                break;
            }
            case GatheringNode: {
                setGoStateFor(GOState.active, looter);

                ObjectFieldData objMask = new objectFieldData();
                GameObjectFieldData goMask = new gameObjectFieldData();
                objMask.markChanged(objMask.dynamicFlags);

                UpdateData udata = new UpdateData(getLocation().getMapId());
                buildValuesUpdateForPlayerWithMask(udata, objMask.getUpdateMask(), goMask.getUpdateMask(), looter);
                UpdateObject packet;
                tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
                udata.buildPacket(tempOut_packet);
                packet = tempOut_packet.outArgValue;
                looter.sendPacket(packet);

                break;
            }
        }
    }

    public final GOState getGoStateFor(ObjectGuid viewer) {
        if (perPlayerState != null) {
            var state = perPlayerState.get(viewer);

            if (state != null && state.state.HasValue) {
                return state.state.value;
            }
        }

        return getGoState();
    }

    public final byte getNameSetId() {
        switch (getGoType()) {
            case DestructibleBuilding:
                var modelData = CliDB.DestructibleModelDataStorage.get(goInfo.destructibleBuilding.destructibleModelRec);

                if (modelData != null) {
                    switch (getDestructibleState()) {
                        case Intact:
                            return modelData.State0NameSet;
                        case Damaged:
                            return modelData.State1NameSet;
                        case Destroyed:
                            return modelData.State2NameSet;
                        case Rebuilding:
                            return modelData.State3NameSet;
                        default:
                            break;
                    }
                }

                break;
            case GarrisonBuilding:
            case GarrisonPlot:
            case PhaseableMo:
                var flags = GameObjectFlags.forValue((int) getGameObjectFieldData().flags);

                return (byte) ((flags.getValue() >> 8) & 0xF);
            default:
                break;
        }

        return 0;
    }

    public final boolean isLootAllowedFor(Player player) {
        var loot = getLootForPlayer(player);

        if (loot != null) // check only if loot was already generated
        {
            if (loot.isLooted()) // nothing to loot or everything looted.
            {
                return false;
            }

            if (!loot.hasAllowedLooter(getGUID()) || (!loot.hasItemForAll() && !loot.hasItemFor(player))) // no loot in chest for this player
            {
                return false;
            }
        }

        if (getHasLootRecipient()) {
            return tapList.contains(player.getGUID()); // if go doesnt have group bound it means it was solo killed by someone else
        }

        return true;
    }

    @Override
    public Loot getLootForPlayer(Player player) {
        if (personalLoot.isEmpty()) {
            return getLoot();
        }

        return personalLoot.get(player.getGUID());
    }

    @Override
    public void buildValuesCreate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt8((byte) flags.getValue());
        getObjectData().writeCreate(buffer, flags, this, target);
        getGameObjectFieldData().writeCreate(buffer, flags, this, target);

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void buildValuesUpdate(WorldPacket data, Player target) {
        var flags = getUpdateFieldFlagsFor(target);
        WorldPacket buffer = new WorldPacket();

        buffer.writeInt32(getValues().getChangedObjectTypeMask());

        if (getValues().hasChanged(TypeId.object)) {
            getObjectData().writeUpdate(buffer, flags, this, target);
        }

        if (getValues().hasChanged(TypeId.gameObject)) {
            getGameObjectFieldData().writeUpdate(buffer, flags, this, target);
        }

        data.writeInt32(buffer.getSize());
        data.writeBytes(buffer);
    }

    @Override
    public void heartbeat() {

    }

    public final void buildValuesUpdateForPlayerWithMask(UpdateData data, UpdateMask requestedObjectMask, UpdateMask requestedGameObjectMask, Player target) {
        UpdateMask valuesMask = new UpdateMask(getObjectTypeId().max.getValue());

        if (requestedObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().object.getValue());
        }

        if (requestedGameObjectMask.isAnySet()) {
            valuesMask.set(getObjectTypeId().gameObject.getValue());
        }

        WorldPacket buffer = new WorldPacket();
        buffer.writeInt32(valuesMask.getBlock(0));

        if (valuesMask.get(getObjectTypeId().object.getValue())) {
            getObjectData().writeUpdate(buffer, requestedObjectMask, true, this, target);
        }

        if (valuesMask.get(getObjectTypeId().gameObject.getValue())) {
            getGameObjectFieldData().writeUpdate(buffer, requestedGameObjectMask, true, this, target);
        }

        WorldPacket buffer1 = new WorldPacket();
        buffer1.writeInt8((byte) UpdateType.VALUES.getValue());
        buffer1.writeGuid(getGUID());
        buffer1.writeInt32(buffer.getSize());
        buffer1.writeBytes(buffer.getByteBuf());

        data.addUpdateBlock(buffer1);
    }

    @Override
    public void clearUpdateMask(boolean remove) {
        getValues().clearChangesMask(getGameObjectFieldData());
        super.clearUpdateMask(remove);
    }

    public final IntArray getPauseTimes() {
        var transport = _goTypeImpl instanceof com.github.azeroth.game.entity.gameobjecttype.Transport ? (com.github.azeroth.game.entity.gameobjecttype.transport) _goTypeImpl : null;

        if (transport != null) {
            return transport.getPauseTimes();
        }

        return null;
    }

    public final void setPathProgressForClient(float progress) {
        doWithSuppressingObjectUpdates(() ->
        {
            ObjectFieldData dynflagMask = new objectFieldData();
            dynflagMask.markChanged(getObjectData().dynamicFlags);
            var marked = (UpdateMask.opBitwiseAnd(getObjectData().getUpdateMask(), dynflagMask.getUpdateMask())).isAnySet();

            var dynamicFlags = (int) getDynamicFlags().getValue();
            dynamicFlags &= 0xFFFF; // remove high bits
            dynamicFlags |= (int) (progress * 65535.0f) << 16;
            replaceAllDynamicFlags(GameObjectDynamicLowFlag.forValue((short) dynamicFlags));

            if (!marked) {
                getObjectData().clearChanged(getObjectData().dynamicFlags);
            }
        });
    }

    public final Position getRespawnPosition() {
        if (goData != null) {
            return goData.spawnPoint.Copy();
        } else {
            return new Position(location);
        }
    }

    public final ITransport toTransportBase() {
        switch (getGoType()) {
            case Transport:
                return (com.github.azeroth.game.entity.gameobjecttype.transport) goTypeImpl;
            case MapObjTransport:
                return (transport) this;
            default:
                break;
        }

        return null;
    }

    public final void afterRelocation() {
        updateModelPosition();
        updatePositionData();

        if (goTypeImpl != null) {
            goTypeImpl.onRelocated();
        }

        updateObjectVisibility(false);
    }

    public final float getInteractionDistance() {
        if (getTemplate().getInteractRadiusOverride() != 0) {
            return (float) getTemplate().getInteractRadiusOverride() / 100.0f;
        }

        switch (getGoType()) {
            case AreaDamage:
                return 0.0f;
            case QuestGiver:
            case Text:
            case FlagStand:
            case FlagDrop:
            case MiniGame:
                return 5.5555553f;
            case Chair:
            case BarberChair:
                return 3.0f;
            case FishingNode:
                return 100.0f;
            case FishingHole:
                return 20.0f + SharedConst.contactDistance; // max spell range
            case Camera:
            case MapObject:
            case DungeonDifficulty:
            case DestructibleBuilding:
            case Door:
                return 5.0f;
            // Following values are not blizzlike
            case GuildBank:
            case Mailbox:
                // Successful mailbox interaction is rather critical to the client, failing it will start a minute-long cooldown until the next mail query may be executed.
                // And since movement info update is not sent with mailbox interaction query, server may find the player outside of interaction range. Thus we increase it.
                return 10.0f; // 5.0f is blizzlike
            default:
                return SharedConst.InteractionDistance;
        }
    }

    public final void updateModelPosition() {
        if (getModel() == null) {
            return;
        }

        if (getMap().containsGameObjectModel(getModel())) {
            getMap().removeGameObjectModel(getModel());
            getModel().updatePosition();
            getMap().insertGameObjectModel(getModel());
        }
    }

    public final void setAnimKitId(short animKitId, boolean oneshot) {
        if (animKitId == animKitId) {
            return;
        }

        if (animKitId != 0 && !CliDB.AnimKitStorage.containsKey(animKitId)) {
            return;
        }

        if (!oneshot) {
            animKitId = animKitId;
        } else {
            animKitId = 0;
        }

        GameObjectActivateAnimKit activateAnimKit = new GameObjectActivateAnimKit();
        activateAnimKit.objectGUID = getGUID();
        activateAnimKit.animKitID = animKitId;
        activateAnimKit.maintain = !oneshot;
        sendMessageToSet(activateAnimKit, true);
    }

    public final void setSpellVisualId(int spellVisualId) {
        setSpellVisualId(spellVisualId, null);
    }

    public final void setSpellVisualId(int spellVisualId, ObjectGuid activatorGuid) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().spellVisualID), spellVisualId);

        GameObjectPlaySpellVisual packet = new GameObjectPlaySpellVisual();
        packet.objectGUID = getGUID();
        packet.activatorGUID = activatorGuid;
        packet.spellVisualID = spellVisualId;
        sendMessageToSet(packet, true);
    }

    public final void assaultCapturePoint(Player player) {
        if (!canInteractWithCapturePoint(player)) {
            return;
        }

        var ai = getAI();

        if (ai != null) {
            if (ai.onCapturePointAssaulted(player)) {
                return;
            }
        }

        // only supported in battlegrounds
        Battleground battleground = null;
        var map = getMap().toBattlegroundMap();

        if (map != null) {
            var bg = map.getBG();

            if (bg != null) {
                battleground = bg;
            }
        }

        if (!battleground) {
            return;
        }

        // Cancel current timer
        goValueProtected.capturePoint.assaultTimer = 0;

        if (player.getBgTeam() == Team.Horde) {
            if (goValueProtected.capturePoint.lastTeamCapture == TeamId.HORDE) {
                // defended. capture instantly.
                goValueProtected.capturePoint.state = BattlegroundCapturePointState.HordeCaptured;
                battleground.sendBroadcastText(getTemplate().capturePoint.defendedBroadcastHorde, ChatMsg.BgSystemHorde, player);
                updateCapturePoint();

                if (getTemplate().capturePoint.defendedEventHorde != 0) {
                    GameEvents.trigger(getTemplate().capturePoint.defendedEventHorde, player, this);
                }

                return;
            }

            switch (goValueProtected.capturePoint.state) {
                case Neutral:
                case AllianceCaptured:
                case ContestedAlliance:
                    goValueProtected.capturePoint.state = BattlegroundCapturePointState.ContestedHorde;
                    battleground.sendBroadcastText(getTemplate().capturePoint.assaultBroadcastHorde, ChatMsg.BgSystemHorde, player);
                    updateCapturePoint();

                    if (getTemplate().capturePoint.contestedEventHorde != 0) {
                        GameEvents.trigger(getTemplate().capturePoint.contestedEventHorde, player, this);
                    }

                    goValueProtected.capturePoint.assaultTimer = getTemplate().capturePoint.captureTime;

                    break;
                default:
                    break;
            }
        } else {
            if (goValueProtected.capturePoint.lastTeamCapture == TeamId.ALLIANCE) {
                // defended. capture instantly.
                goValueProtected.capturePoint.state = BattlegroundCapturePointState.AllianceCaptured;
                battleground.sendBroadcastText(getTemplate().capturePoint.defendedBroadcastAlliance, ChatMsg.BgSystemAlliance, player);
                updateCapturePoint();

                if (getTemplate().capturePoint.defendedEventAlliance != 0) {
                    GameEvents.trigger(getTemplate().capturePoint.defendedEventAlliance, player, this);
                }

                return;
            }

            switch (goValueProtected.capturePoint.state) {
                case Neutral:
                case HordeCaptured:
                case ContestedHorde:
                    goValueProtected.capturePoint.state = BattlegroundCapturePointState.ContestedAlliance;
                    battleground.sendBroadcastText(getTemplate().capturePoint.assaultBroadcastAlliance, ChatMsg.BgSystemAlliance, player);
                    updateCapturePoint();

                    if (getTemplate().capturePoint.contestedEventAlliance != 0) {
                        GameEvents.trigger(getTemplate().capturePoint.contestedEventAlliance, player, this);
                    }

                    goValueProtected.capturePoint.assaultTimer = getTemplate().capturePoint.captureTime;

                    break;
                default:
                    break;
            }
        }
    }

    public final boolean canInteractWithCapturePoint(Player target) {
        if (goInfo.type != GameObjectType.capturePoint) {
            return false;
        }

        if (goValueProtected.capturePoint.state == BattlegroundCapturePointState.Neutral) {
            return true;
        }

        if (target.getBgTeam() == Team.Horde) {
            return goValueProtected.capturePoint.state == BattlegroundCapturePointState.ContestedAlliance || goValueProtected.capturePoint.state == BattlegroundCapturePointState.AllianceCaptured;
        }

        // For Alliance players
        return goValueProtected.capturePoint.state == BattlegroundCapturePointState.ContestedHorde || goValueProtected.capturePoint.state == BattlegroundCapturePointState.HordeCaptured;
    }

    public final boolean meetsInteractCondition(Player user) {
        if (goInfo.getConditionID1() == 0) {
            return true;
        }

        var playerCondition = CliDB.PlayerConditionStorage.get(goInfo.getConditionID1());

        if (playerCondition != null) {
            if (!ConditionManager.isPlayerMeetingCondition(user, playerCondition)) {
                return false;
            }
        }

        return true;
    }

    public final boolean hasFlag(GameObjectFlags flags) {
        return (getGameObjectFieldData().flags & (int) flags.getValue()) != 0;
    }

    public final void setFlag(GameObjectFlags flags) {
        setUpdateFieldFlagValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().flags), (int) flags.getValue());
    }

    public final void removeFlag(GameObjectFlags flags) {
        removeUpdateFieldFlagValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().flags), (int) flags.getValue());
    }

    public final void replaceAllFlags(GameObjectFlags flags) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().flags), (int) flags.getValue());
    }

    public final void setLevel(int level) {
        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().level), level);
    }

    public final GameObjectDynamicLowFlag getDynamicFlags() {
        return GameObjectDynamicLowFlag.forValue((short) ((int) getObjectData().dynamicFlags));
    }

    @Override
    protected EnumFlag<UpdateFieldFlag> getUpdateFieldFlagsFor(Player target) {
        return null;
    }

    @Override
    protected void buildValuesCreate(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target) {

    }

    @Override
    protected void buildValuesUpdate(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target) {

    }

    @Override
    public void buildValuesUpdateWithFlag(WorldPacket data, EnumFlag<UpdateFieldFlag> flags, Player target) {

    }

    public final void addToSkillupList(ObjectGuid playerGuid) {
        skillupList.add(playerGuid);
    }

    public final boolean isInSkillupList(ObjectGuid playerGuid) {
        for (var i : skillupList) {
            if (Objects.equals(i, playerGuid)) {
                return true;
            }
        }

        return false;
    }

    public final void addUse() {
        ++usetimes;
    }

    @Override
    public int getLevelForTarget(WorldObject target) {
        var owner = getOwnerUnit();

        if (owner != null) {
            return owner.getLevelForTarget(target);
        }

        if (getGoType() == GameObjectType.trap) {
            var player = target.toPlayer();

            if (player != null) {
                var userLevels = global.getDB2Mgr().GetContentTuningData(getTemplate().requiredLevel, player.getPlayerData().ctrOptions.getValue().contentTuningConditionMask);

                if (userLevels != null) {
                    return (byte) Math.Clamp(player.getLevel(), userLevels.getValue().minLevel, userLevels.getValue().maxLevel);
                }
            }

            var targetUnit = target.toUnit();

            if (targetUnit != null) {
                return targetUnit.getLevel();
            }
        }

        return 1;
    }

    public final <T extends GameObjectAI> T getAI() {
        return (T) ai;
    }

    public final void relocateStationaryPosition(Position pos) {
        getStationaryPosition().relocate(pos);
    }

    //! Object distance/size - overridden from object._IsWithinDist. Needs to take in account proper GO size.
    @Override
    public boolean _IsWithinDist(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius, boolean incTargetRadius) {
        //! Following check does check 3d distance
        return isInRange(obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ(), dist2compare);
    }

    public final void createModel() {
        setModel(GameObjectModel.create(new GameObjectModelOwnerImpl(this)));

        if (getModel() != null && getModel().isMapObject()) {
            setFlag(GameObjectFlags.MapObject);
        }
    }

    private void removeFromOwner() {
        var ownerGUID = getOwnerGUID();

        if (ownerGUID.isEmpty()) {
            return;
        }

        var owner = global.getObjAccessor().GetUnit(this, ownerGUID);

        if (owner) {
            owner.removeGameObject(this, false);
            return;
        }

        // This happens when a mage portal is despawned after the caster changes map (for example using the portal)
        Log.outDebug(LogFilter.Server, "Removed gameObject (GUID: {0} Entry: {1} SpellId: {2} LinkedGO: {3}) that just lost any reference to the owner {4} GO list", getGUID().toString(), getTemplate().entry, spellId, getTemplate().getLinkedGameObjectEntry(), ownerGUID.toString());

        setOwnerGUID(ObjectGuid.Empty);
    }

    private boolean create(int entry, Map map, Position pos, Quaternion rotation, int animProgress, GOState goState, int artKit, boolean dynamic, long spawnid) {
        setMap(map);

        getLocation().relocate(pos);
        getStationaryPosition().relocate(pos);

        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.Server, "Gameobject (Spawn id: {0} Entry: {1}) not created. Suggested coordinates isn't valid (X: {2} Y: {3})", getSpawnId(), entry, pos.getX(), pos.getY());

            return false;
        }

        // Set if this object can handle dynamic spawns
        if (!dynamic) {
            setRespawnCompatibilityMode(true);
        }

        updatePositionData();

        setZoneScript();

        if (getZoneScript() != null) {
            entry = getZoneScript().getGameObjectEntry(spawnId, entry);

            if (entry == 0) {
                return false;
            }
        }

        var goInfo = global.getObjectMgr().getGameObjectTemplate(entry);

        if (goInfo == null) {
            Logs.SQL.error("Gameobject (Spawn id: {0} Entry: {1}) not created: non-existing entry in `gameobject_template`. Map: {2} (X: {3} Y: {4} Z: {5})", getSpawnId(), entry, map.getId(), pos.getX(), pos.getY(), pos.getZ());

            return false;
        }

        if (goInfo.type == GameObjectType.MapObjTransport) {
            Logs.SQL.error("Gameobject (Spawn id: {0} Entry: {1}) not created: gameobject type GAMEOBJECT_TYPE_MAP_OBJ_TRANSPORT cannot be manually created.", getSpawnId(), entry);

            return false;
        }

        ObjectGuid guid = ObjectGuid.EMPTY;

        if (goInfo.type != GameObjectType.transport) {
            guid = ObjectGuid.create(HighGuid.GameObject, map.getId(), goInfo.entry, map.generateLowGuid(HighGuid.GameObject));
        } else {
            guid = ObjectGuid.create(HighGuid.Transport, map.generateLowGuid(HighGuid.Transport));
            updateFlag.serverTime = true;
        }

        create(guid);

        this.goInfo = goInfo;
        goTemplateAddonProtected = global.getObjectMgr().getGameObjectTemplateAddon(entry);

        if (goInfo.type.getValue() >= GameObjectType.max.getValue()) {
            Logs.SQL.error("Gameobject (Spawn id: {0} Entry: {1}) not created: non-existing GO type '{2}' in `gameobject_template`. It will crash client if created.", getSpawnId(), entry, goInfo.type);

            return false;
        }

        setLocalRotation(rotation.X, rotation.Y, rotation.Z, rotation.W);
        var gameObjectAddon = global.getObjectMgr().getGameObjectAddon(getSpawnId());

        // For most of gameobjects is (0, 0, 0, 1) quaternion, there are only some transports with not standard rotation
        var parentRotation = Quaternion.Identity;

        if (gameObjectAddon != null) {
            parentRotation = gameObjectAddon.parentRotation;
        }

        setParentRotation(parentRotation);

        setObjectScale(goInfo.size);

        var goOverride = getGameObjectOverride();

        if (goOverride != null) {
            setFaction(goOverride.faction);
            replaceAllFlags(goOverride.flags);
        }

        if (goTemplateAddonProtected != null) {
            if (goTemplateAddonProtected.worldEffectId != 0) {
                updateFlag.gameObject = true;
                setWorldEffectID(goTemplateAddonProtected.worldEffectId);
            }

            if (goTemplateAddonProtected.aiAnimKitId != 0) {
                animKitId = (short) goTemplateAddonProtected.aiAnimKitId;
            }
        }

        setEntry(goInfo.entry);

        // set name for logs usage, doesn't affect anything ingame
        setName(goInfo.name);

        setDisplayId(goInfo.displayId);

        createModel();

        // GAMEOBJECT_BYTES_1, index at 0, 1, 2 and 3
        setGoType(goInfo.type);
        prevGoState = goState;
        setGoState(goState);
        setGoArtKit(artKit);

        setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().spawnTrackingStateAnimID), global.getDB2Mgr().GetEmptyAnimStateID());

        switch (goInfo.type) {
            case FishingHole:
                setGoAnimProgress(animProgress);
                goValueProtected.fishingHole.maxOpens = RandomUtil.URand(getTemplate().fishingHole.minRestock, getTemplate().fishingHole.maxRestock);

                break;
            case DestructibleBuilding:
                goValueProtected.building.health = (getTemplate().destructibleBuilding.interiorVisible != 0 ? getTemplate().destructibleBuilding.InteriorVisible : 20000);
                goValueProtected.building.maxHealth = goValueProtected.building.health;
                setGoAnimProgress(255);
                // yes, even after the updatefield rewrite this garbage hack is still in client
                setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().parentRotation), new Quaternion(goInfo.destructibleBuilding.destructibleModelRec, 0f, 0f, 0f));

                break;
            case Transport:
                goTypeImpl = new com.github.azeroth.game.entity.gameobjecttype.transport(this);

                if (goInfo.transport.startOpen != 0) {
                    setGoState(GOState.TransportStopped);
                } else {
                    setGoState(GOState.TransportActive);
                }

                setGoAnimProgress(animProgress);
                setActive(true);

                break;
            case FishingNode:
                setLevel(0);
                setGoAnimProgress(255);

                break;
            case Trap:
                if (goInfo.trap.stealthed != 0) {
                    getStealth().addFlag(StealthType.trap);
                    getStealth().addValue(StealthType.trap, 70);
                }

                if (goInfo.trap.stealthAffected != 0) {
                    getInvisibility().addFlag(InvisibilityType.trap);
                    getInvisibility().addValue(InvisibilityType.trap, 300);
                }

                break;
            case PhaseableMo:
                removeFlag(GameObjectFlags.forValue(0xF00));
                setFlag(GameObjectFlags.forValue((this.goInfo.phaseableMO.areaNameSet & 0xF) << 8));

                break;
            case CapturePoint:
                setUpdateFieldValue(getValues().modifyValue(getGameObjectFieldData()).modifyValue(getGameObjectFieldData().spellVisualID), this.goInfo.capturePoint.spellVisual1);
                goValueProtected.capturePoint.assaultTimer = 0;
                goValueProtected.capturePoint.lastTeamCapture = TeamIds.Neutral;
                goValueProtected.capturePoint.state = BattlegroundCapturePointState.Neutral;
                updateCapturePoint();

                break;
            default:
                setGoAnimProgress(animProgress);

                break;
        }

        if (gameObjectAddon != null) {
            if (gameObjectAddon.invisibilityValue != 0) {
                getInvisibility().addFlag(gameObjectAddon.invisibilityType);
                getInvisibility().addValue(gameObjectAddon.invisibilityType, gameObjectAddon.invisibilityValue);
            }

            if (gameObjectAddon.worldEffectID != 0) {
                updateFlag.gameObject = true;
                setWorldEffectID(gameObjectAddon.worldEffectID);
            }

            if (gameObjectAddon.AIAnimKitID != 0) {
                animKitId = (short) gameObjectAddon.AIAnimKitID;
            }
        }

        lastUsedScriptID = getTemplate().scriptId;
        AIM_Initialize();

        if (spawnid != 0) {
            spawnId = spawnid;
        }

        var linkedEntry = getTemplate().getLinkedGameObjectEntry();

        if (linkedEntry != 0) {
            var linkedGo = createGameObject(linkedEntry, map, pos, rotation, 255, GOState.Ready);

            if (linkedGo != null) {
                setLinkedTrap(linkedGo);

                if (!map.addToMap(linkedGo)) {
                    linkedGo.close();
                }
            }
        }

        // Check if GameObject is Infinite
        if (goInfo.isInfiniteGameObject()) {
            setVisibilityDistanceOverride(visibilityDistanceType.Infinite);
        }

        // Check if GameObject is Gigantic
        if (goInfo.isGiganticGameObject()) {
            setVisibilityDistanceOverride(visibilityDistanceType.Gigantic);
        }

        // Check if GameObject is Large
        if (goInfo.isLargeGameObject()) {
            setVisibilityDistanceOverride(visibilityDistanceType.Large);
        }

        return true;
    }

    private void despawnForPlayer(Player seer, Duration respawnTime) {
        var perPlayerState = getOrCreatePerPlayerStates(seer.getGUID());
        perPlayerState.setValidUntil(gameTime.GetSystemTime() + respawnTime);
        perPlayerState.setDespawned(true);
        seer.updateVisibilityOf(this);
    }

    private GameObject lookupFishingHoleAround(float range) {
        var u_check = new NearestGameObjectFishingHole(this, range);
        var checker = new GameObjectSearcher(this, u_check, gridType.Grid);

        Cell.visitGrid(this, checker, range);

        return checker.getTarget();
    }

    private void switchDoorOrButton(boolean activate) {
        switchDoorOrButton(activate, false);
    }

    private void switchDoorOrButton(boolean activate, boolean alternative) {
        if (activate) {
            setFlag(GameObjectFlags.InUse);
        } else {
            removeFlag(GameObjectFlags.InUse);
        }

        if (getGoState() == GOState.Ready) //if closed . open
        {
            setGoState(alternative ? GOState.Destroyed : GOState.active);
        } else //if open . close
        {
            setGoState(GOState.Ready);
        }
    }

    private boolean isAtInteractDistance(Position pos, float radius) {
        var displayInfo = CliDB.GameObjectDisplayInfoStorage.get(getTemplate().displayId);

        if (displayInfo != null) {
            var scale = getObjectScale();

            var minX = displayInfo.GeoBoxMin.X * scale - radius;
            var minY = displayInfo.GeoBoxMin.Y * scale - radius;
            var minZ = displayInfo.GeoBoxMin.Z * scale - radius;
            var maxX = displayInfo.GeoBoxMax.X * scale + radius;
            var maxY = displayInfo.GeoBoxMax.Y * scale + radius;
            var maxZ = displayInfo.GeoBoxMax.Z * scale + radius;

            var worldRotation = getWorldRotation();

            //Todo Test this. Needs checked.
            var worldSpaceBox = MathUtil.toWorldSpace(worldRotation.ToMatrix(), new Vector3(getLocation().getX(), getLocation().getY(), getLocation().getZ()), new Box(new Vector3(minX, minY, minZ), new Vector3(maxX, maxY, maxZ)));

            return worldSpaceBox.contains(new Vector3(pos.getX(), pos.getY(), pos.getZ()));
        }

        return getLocation().getExactDist(pos) <= radius;
    }

    private void clearLoot() {
        // Unlink loot objects from this GameObject before destroying to avoid accessing freed memory from Loot destructor
        setLoot(null);
        personalLoot.clear();
        uniqueUsers.clear();
        usetimes = 0;
    }

    private void setGoStateFor(GOState state, Player viewer) {
        var perPlayerState = getOrCreatePerPlayerStates(viewer.getGUID());
        perPlayerState.setValidUntil(gameTime.GetSystemTime() + duration.FromSeconds(respawnDelayTime));
        perPlayerState.state = state;

        GameObjectSetStateLocal setStateLocal = new GameObjectSetStateLocal();
        setStateLocal.objectGUID = getGUID();
        setStateLocal.state = (byte) state.getValue();
        viewer.sendPacket(setStateLocal);
    }

    private void enableCollision(boolean enable) {
        if (getModel() == null) {
            return;
        }

        getModel().enableCollision(enable);
    }

    private void updateModel() {
        if (!isInWorld()) {
            return;
        }

        if (getModel() != null) {
            if (getMap().containsGameObjectModel(getModel())) {
                getMap().removeGameObjectModel(getModel());
            }
        }

        removeFlag(GameObjectFlags.MapObject);
        setModel(null);
        createModel();

        if (getModel() != null) {
            getMap().insertGameObjectModel(getModel());
        }
    }

    private void updateCapturePoint() {
        if (getGoType() != GameObjectType.capturePoint) {
            return;
        }

        var ai = getAI();

        if (ai != null) {
            if (ai.onCapturePointUpdated(goValueProtected.capturePoint.state)) {
                return;
            }
        }

        int spellVisualId = 0;
        int customAnim = 0;

        switch (goValueProtected.capturePoint.state) {
            case Neutral:
                spellVisualId = getTemplate().capturePoint.spellVisual1;

                break;
            case ContestedHorde:
                customAnim = 1;
                spellVisualId = getTemplate().capturePoint.spellVisual2;

                break;
            case ContestedAlliance:
                customAnim = 2;
                spellVisualId = getTemplate().capturePoint.spellVisual3;

                break;
            case HordeCaptured:
                customAnim = 3;
                spellVisualId = getTemplate().capturePoint.spellVisual4;

                break;
            case AllianceCaptured:
                customAnim = 4;
                spellVisualId = getTemplate().capturePoint.spellVisual5;

                break;
            default:
                break;
        }

        if (customAnim != 0) {
            sendCustomAnim(customAnim);
        }

        setSpellVisualId(spellVisualId);
        updateDynamicFlagsForNearbyPlayers();

        var map = getMap().toBattlegroundMap();

        if (map != null) {
            var bg = map.getBG();

            if (bg != null) {
                UpdateCapturePoint packet = new updateCapturePoint();
                packet.capturePointInfo.state = goValueProtected.capturePoint.state;
                packet.capturePointInfo.pos = getLocation();
                packet.capturePointInfo.guid = getGUID();
                packet.capturePointInfo.captureTotalDuration = duration.ofSeconds(getTemplate().capturePoint.captureTime);
                packet.capturePointInfo.captureTime = goValueProtected.capturePoint.assaultTimer;
                bg.sendPacketToAll(packet);
                bg.updateWorldState((int) getTemplate().capturePoint.worldState1, (byte) goValueProtected.capturePoint.state.getValue());
            }
        }

        getMap().updateSpawnGroupConditions();
    }

    private PerPlayerState getOrCreatePerPlayerStates(ObjectGuid guid) {
        if (perPlayerState == null) {
            perPlayerState = new HashMap<ObjectGuid, PerPlayerState>();
        }

        if (!perPlayerState.containsKey(guid)) {
            perPlayerState.put(guid, new PerPlayerState());
        }

        return perPlayerState.get(guid);
    }

    private boolean hasLootMode(LootMode lootMode) {
        return (boolean) (lootMode.getValue() & lootMode.getValue());
    }

    private void addLootMode(LootMode lootMode) {
        lootMode = LootMode.forValue(lootMode.getValue() | lootMode.getValue());
    }

    private void removeLootMode(LootMode lootMode) {
        lootMode = LootMode.forValue(lootMode.getValue() & ~lootMode.getValue());
    }

    private void resetLootMode() {
        lootMode = LootMode.Default;
    }

    private void clearSkillupList() {
        skillupList.clear();
    }

    private int getUniqueUseCount() {
        return (int) uniqueUsers.size();
    }

    private void updateDynamicFlagsForNearbyPlayers() {
        getValues().modifyValue(getObjectData()).modifyValue(getObjectData().dynamicFlags);
        addToObjectUpdateIfNeeded();
    }

    private void handleCustomTypeCommand(GameObjectTypeBase.CustomCommand command) {
        if (goTypeImpl != null) {
            command.execute(goTypeImpl);
        }
    }

    @Override
    public final void setNewCellPosition(float x, float y, float z, float o) {
        this.moveState = CellMoveState.ACTIVE;
        newPosition.relocate(x, y, z, o);
    }

    private static class ValuesUpdateForPlayerWithMaskSender implements IDoWork<Player> {
        public final GameObject owner;
        public final objectFieldData objectMask = new objectFieldData();
        public final GameObjectFieldData gameObjectMask = new gameObjectFieldData();

        public ValuesUpdateForPlayerWithMaskSender(GameObject owner) {
            owner = owner;
        }

        public final void invoke(Player player) {
            UpdateData udata = new UpdateData(owner.getLocation().getMapId());

            owner.buildValuesUpdateForPlayerWithMask(udata, objectMask.getUpdateMask(), gameObjectMask.getUpdateMask(), player);

            UpdateObject packet;
            tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
            udata.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }


}

// Base class for GameObject type specific implementations


//11 GAMEOBJECT_TYPE_TRANSPORT

