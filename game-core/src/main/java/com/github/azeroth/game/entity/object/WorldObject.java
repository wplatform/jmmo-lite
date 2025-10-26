package com.github.azeroth.game.entity.object;


import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.*;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.defines.FactionTemplateFlag;
import com.github.azeroth.dbc.defines.SummonPropertyFlag;
import com.github.azeroth.dbc.domain.AreaTable;
import com.github.azeroth.dbc.domain.Faction;
import com.github.azeroth.dbc.domain.FactionTemplate;
import com.github.azeroth.dbc.domain.PowerType;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.condition.ConditionManager;
import com.github.azeroth.game.domain.map.*;
import com.github.azeroth.game.domain.object.*;
import com.github.azeroth.game.domain.object.enums.*;
import com.github.azeroth.game.domain.unit.*;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.conversation.Conversation;
import com.github.azeroth.game.entity.corpse.Corpse;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.CreatureGroup;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.gobject.Transport;
import com.github.azeroth.game.entity.object.update.UpdateData;
import com.github.azeroth.game.entity.pet.Pet;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.enums.DuelState;
import com.github.azeroth.game.entity.player.enums.PlayerCommandState;
import com.github.azeroth.game.entity.player.enums.PlayerFlag;
import com.github.azeroth.game.entity.scene.SceneObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.entity.vehicle.ITransport;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.domain.map.enums.LiquidHeaderTypeFlag;
import com.github.azeroth.game.domain.map.enums.ModelIgnoreFlags;
import com.github.azeroth.game.domain.map.enums.ZLiquidStatus;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.map.grid.visitor.GridVisitors;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.combatlog.CombatLogServerPacket;
import com.github.azeroth.game.networking.packet.combatlog.SpellLogMissEntry;
import com.github.azeroth.game.networking.packet.combatlog.SpellMissLog;
import com.github.azeroth.game.networking.packet.misc.PlayMusic;
import com.github.azeroth.game.networking.packet.misc.PlaySound;
import com.github.azeroth.game.networking.packet.misc.PlaySpeakerBoxSound;
import com.github.azeroth.game.networking.packet.spell.*;
import com.github.azeroth.game.domain.phasing.PhaseShift;
import com.github.azeroth.game.phasing.PhasingHandler;
import com.github.azeroth.game.reputation.ReputationFlag;
import com.github.azeroth.game.scenario.Scenario;
import com.github.azeroth.game.domain.creature.TempSummonType;
import com.github.azeroth.game.spell.*;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.SpellGroup;
import com.github.azeroth.game.spell.enums.SpellModOp;
import com.github.azeroth.game.spell.enums.SpellValueMod;
import com.github.azeroth.game.spell.enums.TriggerCastFlag;
import com.github.azeroth.game.world.WorldContext;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.RandomUtil;

import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Setter
@Getter
public abstract class WorldObject extends GenericObject {

    protected LocalizedString name;
    protected boolean isActive;
    protected boolean isFarVisible;
    protected Float visibilityDistanceOverride;
    protected boolean storedInWorldObjectGridContainer;
    protected ZoneScript zoneScript;
    // transports (gameobjects only)
    protected ITransport transport;

    protected int zoneId;
    protected int areaId;
    protected float staticFloorZ;
    protected boolean outdoors;
    protected EnumFlag<ZLiquidStatus> liquidStatus;
    protected WmoLocation currentWmo;


    private final EnumIntArray<StealthType> stealth = EnumIntArray.of(StealthType.class);
    private final EnumIntArray<StealthType> stealthDetect = EnumIntArray.of(StealthType.class);
    private final EnumIntArray<InvisibilityType> invisibility = EnumIntArray.of(InvisibilityType.class);
    private final EnumIntArray<InvisibilityType> invisibilityDetect = EnumIntArray.of(InvisibilityType.class);
    private final EnumIntArray<ServerSideVisibilityType> serverSideVisibility = EnumIntArray.of(ServerSideVisibilityType.class);
    private final EnumIntArray<ServerSideVisibilityType> serverSideVisibilityDetect = EnumIntArray.of(ServerSideVisibilityType.class);


    private final WorldLocation location;
    public int lastUsedScriptID;
    private boolean activeObject;


    private boolean farVisible;

    private volatile Map currMap;
    private final PhaseShift phaseShift = new PhaseShift();
    private final PhaseShift suppressedPhaseShift = new PhaseShift(); // contains phases for current area but not applied due to conditions
    private int dbPhase;
    private final EnumFlag<NotifyFlag> notifyFlags = EnumFlag.of(NotifyFlag.NONE);
    private ObjectGuid privateObjectOwner = ObjectGuid.EMPTY;
    private SmoothPhasing smoothPhasing;
    // Event handler
    private final EventProcessor events = new EventProcessor();

    private int instanceId;
    private WorldContext worldContext;
    private int faction;
    private int heartbeatTimer = ObjectDefine.HEARTBEAT_INTERVAL;


    public WorldObject() {
        serverSideVisibility.setValue(ServerSideVisibilityType.GHOST, GhostVisibilityType.ALIVE.ordinal() | GhostVisibilityType.GHOST.ordinal());
        serverSideVisibilityDetect.setValue(ServerSideVisibilityType.GHOST, GhostVisibilityType.ALIVE.ordinal());
        staticFloorZ = MapDefine.VMAP_INVALID_HEIGHT_VALUE;
        location = new WorldLocation();
    }


    public WorldObject getWorldObject(ObjectGuid guid) {
        return getWorldContext().getWorldObject(this, guid);
    }

    public GenericObject getObjectByTypeMask(ObjectGuid guid, TypeMask mask) {
        return getWorldContext().getObjectByTypeMask(this, guid, mask);
    }

    public Corpse getCorpse(ObjectGuid guid) {
        return getWorldContext().getCorpse(this, guid);
    }

    public GameObject getGameObject(ObjectGuid guid) {
        return getWorldContext().getGameObject(this, guid);
    }

    public Transport getTransport(ObjectGuid guid) {
        return getWorldContext().getTransport(this, guid);
    }

    public DynamicObject getDynamicObject(ObjectGuid guid) {
        return getCurrMap().getDynamicObject(guid);
    }

    public AreaTrigger getAreaTrigger(ObjectGuid guid) {
        return getWorldContext().getAreaTrigger(this, guid);
    }

    public SceneObject getSceneObject(ObjectGuid guid) {
        return getWorldContext().getSceneObject(this, guid);
    }

    public Conversation getConversation(ObjectGuid guid) {
        return getWorldContext().getConversation(this, guid);
    }

    public Unit getUnit(ObjectGuid guid) {
        return getWorldContext().getUnit(this, guid);
    }

    public Creature getCreature(ObjectGuid guid) {
        return getWorldContext().getCreature(this, guid);
    }

    public Pet getPet(ObjectGuid guid) {
        return getWorldContext().getPet(this, guid);
    }

    public Player getPlayer(ObjectGuid guid) {
        return getWorldContext().getPlayer(this, guid);
    }

    public Creature getCreatureOrPetOrVehicle(ObjectGuid guid) {
        return getWorldContext().getCreatureOrPetOrVehicle(this, guid);
    }

    public static boolean inSamePhase(WorldObject a, WorldObject b) {
        return a != null && b != null && a.inSamePhase(b);
    }

    private ReputationRank getFactionReactionTo(FactionTemplate factionTemplateEntry, WorldObject target) {
        // always neutral when no template entry found
        if (factionTemplateEntry == null) {
            return ReputationRank.NEUTRAL;
        }

        var targetFactionTemplateEntry = target.getFactionTemplateEntry();

        if (targetFactionTemplateEntry == null) {
            return ReputationRank.NEUTRAL;
        }

        var targetPlayerOwner = target.getAffectingPlayer();

        if (targetPlayerOwner != null) {
            // check contested flags
            if ((factionTemplateEntry.getFlags() & FactionTemplateFlag.CONTESTED_GUARD.value) != 0
                    && targetPlayerOwner.hasPlayerFlag(PlayerFlag.CONTESTED_PVP)) {
                return ReputationRank.HOSTILE;
            }

            var repRank = targetPlayerOwner.getReputationMgr().getForcedRankIfAny(factionTemplateEntry);

            if (repRank != null) {
                return repRank;
            }

            if (target.isUnit() && !target.toUnit().hasUnitFlag2(UnitFlag2.IGNORE_REPUTATION)) {
                int faction = factionTemplateEntry.getFaction().intValue();
                Faction factionEntry = worldContext.getDbcObjectManager().faction(faction);

                if (factionEntry != null) {
                    if (factionEntry.canHaveReputation()) {
                        // CvP case - check reputation, don't allow state higher than neutral when at war
                        var repRank1 = targetPlayerOwner.getReputationMgr().getRank(factionEntry);

                        if (targetPlayerOwner.getReputationMgr().isAtWar(factionEntry)) {
                            return ReputationRank.NEUTRAL.ordinal() < repRank1.ordinal() ? ReputationRank.NEUTRAL : repRank1;
                        }

                        return repRank1;
                    }
                }
            }
        }

        // common faction based check
        if (factionTemplateEntry.isHostileTo(targetFactionTemplateEntry)) {
            return ReputationRank.HOSTILE;
        }

        if (factionTemplateEntry.isFriendlyTo(targetFactionTemplateEntry)) {
            return ReputationRank.FRIENDLY;
        }

        if (targetFactionTemplateEntry.isFriendlyTo(factionTemplateEntry)) {
            return ReputationRank.FRIENDLY;
        }

        if ((factionTemplateEntry.getFlags() & (short) FactionTemplateFlag.HOSTILE_BY_DEFAULT.value) != 0) {
            return ReputationRank.HOSTILE;
        }

        // neutral by default
        return ReputationRank.NEUTRAL;
    }

    public final Scenario getScenario() {
        if (isInWorld()) {
            var instanceMap = getMap().toInstanceMap();

            if (instanceMap != null) {
                return instanceMap.getInstanceScenario();
            }
        }

        return null;
    }

    public final ObjectGuid getCharmerOrOwnerOrOwnGUID() {
        var guid = getCharmerOrOwnerGUID();

        if (!guid.isEmpty()) {
            return guid;
        }

        return getGUID();
    }

    public float getCombatReach() {
        return ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH;
    }


    public abstract short getAIAnimKitId();


    public abstract short getMovementAnimKitId();


    public abstract short getMeleeAnimKitId();

    // Watcher
    public final boolean isPrivateObject() {
        return !privateObjectOwner.isEmpty();
    }

    public final float getTransOffsetX() {
        return getMovementInfo().getTransport().getPos().getX();
    }

    public final float getTransOffsetY() {
        return getMovementInfo().getTransport().getPos().getY();
    }

    public final float getTransOffsetZ() {
        return getMovementInfo().getTransport().getPos().getZ();
    }

    public final float getTransOffsetO() {
        return getMovementInfo().getTransport().getPos().getO();
    }

    public final int getTransTime() {
        return getMovementInfo().getTransport().getTime();
    }

    public final byte getTransSeat() {
        return getMovementInfo().getTransport().getSeat();
    }

    public float getStationaryX() {
        return getLocation().getX();
    }

    public float getStationaryY() {
        return getLocation().getY();
    }

    public float getStationaryZ() {
        return getLocation().getZ();
    }

    public float getStationaryO() {
        return getLocation().getO();
    }

    public abstract float getCollisionHeight();

    public final float getMidsectionHeight() {
        return getCollisionHeight() / 2.0f;
    }

    public abstract ObjectGuid getOwnerGUID();

    public abstract ObjectGuid getCreatorGUID();

    public ObjectGuid getCharmerOrOwnerGUID() {
        return getOwnerGUID();
    }


    private Position getTransOffset() {
        return getMovementInfo().getTransport().getPos();
    }

    public Unit getCharmerOrOwner() {
        var unit = toUnit();

        if (unit != null) {
            return unit.getCharmerOrOwner();
        } else {
            var go = toGameObject();

            if (go != null) {
                return go.getOwnerUnit();
            }
        }

        return null;
    }


    public Unit getOwnerUnit() {
        return worldContext.getUnit(this, getOwnerGUID());
    }

    public final Unit getCharmerOrOwnerOrSelf() {
        var u = getCharmerOrOwner();

        if (u != null) {
            return u;
        }

        return toUnit();
    }

    public final Player getCharmerOrOwnerPlayerOrPlayerItself() {
        var guid = getCharmerOrOwnerGUID();

        if (guid.isPlayer()) {
            return getPlayer(guid);
        }

        return toPlayer();
    }

    public final Player getAffectingPlayer() {
        if (getCharmerOrOwnerGUID().isEmpty()) {
            return toPlayer();
        }

        var owner = getCharmerOrOwner();

        if (owner != null) {
            return owner.getCharmerOrOwnerPlayerOrPlayerItself();
        }

        return null;
    }

    public final boolean isInWorldPvpZone() {
        // Wintergrasp
        // Tol Barad
        // Ashran
        return switch (getZoneId()) {
            case 4197, 5095, 6941 -> true;
            default -> false;
        };
    }

    public final InstanceScript getInstanceScript() {
        var map = getMap();

        return map.isDungeon() ? ((InstanceMap) map).getInstanceScript() : null;
    }

    public final float getGridActivationRange() {
        if (isActiveObject()) {
            if (getObjectTypeId() == TypeId.PLAYER && toPlayer().getCinematicMgr().isOnCinematic()) {
                return Math.max(ObjectDefine.DEFAULT_VISIBILITY_INSTANCE, getMap().getVisibilityRange());
            }

            return getMap().getVisibilityRange();
        }

        var thisCreature = toCreature();

        if (thisCreature != null) {
            return thisCreature.getSightDistance();
        }

        return 0.0f;
    }

    public final float getVisibilityRange() {
        if (isVisibilityOverridden() && !isPlayer()) {
            return visibilityDistanceOverride;
        } else if (isFarVisible() && !isPlayer()) {
            return ObjectDefine.MAX_VISIBILITY_DISTANCE;
        } else if (getMap() != null) {
            return getMap().getVisibilityRange();
        }

        return ObjectDefine.MAX_VISIBILITY_DISTANCE;
    }

    public final Map getMap() {
        return currMap;
    }

    public final void setMap(Map value) {
        if (currMap == value) {
            return;
        }

        currMap = value;
        getLocation().setMapId(value.getId());
        this.instanceId = value.getInstanceId();

        if (isWorldObject()) {
            currMap.addWorldObject(this);
        }
    }

    public final Player getSpellModOwner() {
        var player = toPlayer();

        if (player != null) {
            return player;
        }

        if (isCreature()) {
            var creature = toCreature();

            if (creature.isPet() || creature.isTotem()) {
                var owner = creature.getOwnerUnit();

                if (owner != null) {
                    return owner.toPlayer();
                }
            }
        } else if (isGameObject()) {
            var go = toGameObject();
            var owner = go.getOwnerUnit();

            if (owner != null) {
                return owner.toPlayer();
            }
        }

        return null;
    }

    public final float getFloorZ() {
        if (!isInWorld()) {
            return staticFloorZ;
        }
        Position position = Position.of(getLocation().getX(), getLocation().getY(), getLocation().getZ() + SharedDefine.Z_OFFSET_FIND_HEIGHT);
        return Math.max(staticFloorZ, getMap().getGameObjectFloor(getPhaseShift(), position));
    }

    private boolean isFarVisible() {
        return farVisible;
    }

    public final void setFarVisible(boolean on) {
        if (isPlayer()) {
            return;
        }

        farVisible = on;
    }

    private boolean isVisibilityOverridden() {
        return visibilityDistanceOverride != null;
    }

    public void addToWorld() {
        super.addToWorld();
        if (getMap() != null) {
            ZoneAndAreaId zoneAndAreaId = getMap().getZoneAndAreaId(phaseShift, location);
            areaId = zoneAndAreaId.areaId;
            zoneId = zoneAndAreaId.zoneId;
        }

    }

    public void removeFromWorld() {
        if (!isInWorld()) {
            return;
        }

        updateObjectVisibilityOnDestroy();
        super.removeFromWorld();
    }


    public final void updatePositionData() {
        PositionFullTerrainStatus data = getMap().getFullTerrainStatusForPosition(phaseShift,
                getLocation(), LiquidHeaderTypeFlag.AllLiquids.value, getCollisionHeight());
        processPositionDataChanged(data);
    }

    public void processPositionDataChanged(PositionFullTerrainStatus data) {
        zoneId = areaId = data.getAreaId();
        AreaTable areaTable = worldContext.getDbcObjectManager().areaTable(areaId);


        if (areaTable != null) {
            if (areaTable.getParentAreaID() != 0) {
                zoneId = areaTable.getParentAreaID();
            }
        }

        outdoors = data.isOutdoors();
        staticFloorZ = data.getFloorZ();
        liquidStatus = data.getLiquidStatus();
    }

    public final void addToObjectUpdateIfNeeded() {
        if (isInWorld() && !objectUpdated) {
            objectUpdated = addToObjectUpdate();
        }
    }


    public Loot getLootForPlayer(Player player) {
        return null;
    }

    public abstract void buildValuesCreate(WorldPacket data, Player target);

    public abstract void buildValuesUpdate(WorldPacket data, Player target);

    public abstract void heartbeat();

    public final void forceUpdateFieldChange() {
        addToObjectUpdateIfNeeded();
    }

    public final void setIsStoredInWorldObjectGridContainer(boolean on) {
        if (!isInWorld()) {
            return;
        }
        getMap().addObjectToSwitchList(this, on);
    }

    public void update(int delta) {
        events.update(delta);
        heartbeatTimer -= delta;
        while (heartbeatTimer <= 0) {
            heartbeatTimer += ObjectDefine.HEARTBEAT_INTERVAL;
            heartbeat();
        }
    }

    public final boolean isAlwaysStoredInWorldObjectGridContainer() {
        return this.storedInWorldObjectGridContainer;
    }


    public final boolean isStoredInWorldObjectGridContainer() {
        if (storedInWorldObjectGridContainer)
            return true;

        return isCreature() && toCreature().isTempWorldObject();
    }


    public final void setActive(boolean on) {
        if (activeObject == on) {
            return;
        }

        if (isTypeId(TypeId.PLAYER)) {
            return;
        }

        activeObject = on;

        if (on && !isInWorld()) {
            return;
        }

        var map = getMap();

        if (map == null) {
            return;
        }

        if (on) {
            map.addToActive(this);
        } else {
            map.removeFromActive(this);
        }
    }

    public final void setVisibilityDistanceOverride(VisibilityDistanceType type) {
        if (getObjectTypeId() == TypeId.PLAYER) {
            return;
        }

        if (this instanceof Creature creature) {
            creature.removeUnitFlag2(UnitFlag2.LARGE_AOI);
            creature.removeUnitFlag2(UnitFlag2.GIGANTIC_AOI);
            creature.removeUnitFlag2(UnitFlag2.INFINITE_AOI);

            switch (type) {
                case LARGE:
                    creature.setUnitFlag2(UnitFlag2.LARGE_AOI);

                    break;
                case GIGANTIC:
                    creature.setUnitFlag2(UnitFlag2.GIGANTIC_AOI);

                    break;
                case INFINITE:
                    creature.setUnitFlag2(UnitFlag2.INFINITE_AOI);

                    break;
                default:
                    break;
            }
        }
        visibilityDistanceOverride = ObjectDefine.VISIBILITY_DISTANCES[type.ordinal()];
    }


    public void cleanupsBeforeDelete() {
        cleanupsBeforeDelete(true);
    }

    public void cleanupsBeforeDelete(boolean finalCleanup) {
        if (isInWorld()) {
            removeFromWorld();
        }
        var transport = getTransport();

        if (transport != null) {
            transport.removePassenger(this);
        }
        events.clear();
    }


    public final ZoneAndAreaId getZoneAndAreaId() {
        return ZoneAndAreaId.of(zoneId, areaId);
    }


    public final float getSightRange() {
        return getSightRange(null);
    }

    public final float getSightRange(WorldObject target) {
        if (isPlayer() || isCreature()) {
            if (isPlayer()) {
                if (target != null && target.isVisibilityOverridden() && !target.isPlayer()) {
                    return target.visibilityDistanceOverride;
                } else if (target != null && target.isFarVisible() && !target.isPlayer()) {
                    return ObjectDefine.MAX_VISIBILITY_DISTANCE;
                } else if (toPlayer().getCinematicMgr().isOnCinematic()) {
                    return ObjectDefine.DEFAULT_VISIBILITY_INSTANCE;
                } else {
                    return getMap().getVisibilityRange();
                }
            } else {
                return toCreature().getSightDistance();
            }
        }

        if (isDynObject() && isActiveObject()) {
            return getMap().getVisibilityRange();
        }

        return 0.0f;
    }

    public final boolean checkPrivateObjectOwnerVisibility(WorldObject seer) {
        if (!isPrivateObject()) {
            return true;
        }

        // Owner of this private object
        if (Objects.equals(privateObjectOwner, seer.getGUID())) {
            return true;
        }

        // Another private object of the same owner
        if (Objects.equals(privateObjectOwner, seer.getPrivateObjectOwner())) {
            return true;
        }

        var playerSeer = seer.toPlayer();

        if (playerSeer != null) {
            if (playerSeer.isInGroup(privateObjectOwner)) {
                return true;
            }
        }

        return false;
    }

    public final SmoothPhasing getOrCreateSmoothPhasing() {
        if (smoothPhasing == null) {
            smoothPhasing = new SmoothPhasing();
        }

        return smoothPhasing;
    }

    public final SmoothPhasing getSmoothPhasing() {
        return smoothPhasing;
    }


    public final boolean canSeeOrDetect(WorldObject obj, boolean ignoreStealth, boolean distanceCheck) {
        return canSeeOrDetect(obj, ignoreStealth, distanceCheck, false);
    }

    public final boolean canSeeOrDetect(WorldObject obj, boolean ignoreStealth) {
        return canSeeOrDetect(obj, ignoreStealth, false, false);
    }

    public final boolean canSeeOrDetect(WorldObject obj) {
        return canSeeOrDetect(obj, false, false, false);
    }

    public final boolean canSeeOrDetect(WorldObject obj, boolean ignoreStealth, boolean distanceCheck, boolean checkAlert) {
        if (this == obj) {
            return true;
        }

        if (obj.isNeverVisibleFor(this) || canNeverSee(obj)) {
            return false;
        }

        if (obj.isAlwaysVisibleFor(this) || canAlwaysSee(obj)) {
            return true;
        }

        if (!obj.checkPrivateObjectOwnerVisibility(this)) {
            return false;
        }

        var smoothPhasing = obj.getSmoothPhasing();

        if (smoothPhasing != null && smoothPhasing.isBeingReplacedForSeer(getGUID())) {
            return false;
        }

        ConditionManager conditionManager = worldContext.getConditionManager();
        if (!obj.isPrivateObject() && !conditionManager.isObjectMeetingVisibilityByObjectIdConditions(obj.getObjectTypeId(), obj.getEntry(), this)) {
            return false;
        }

        var corpseVisibility = false;

        if (distanceCheck) {
            var corpseCheck = false;
            var thisPlayer = toPlayer();

            if (thisPlayer != null) {
                if (thisPlayer.isDead() && thisPlayer.getHealth() > 0 && (obj.serverSideVisibility.getValue(ServerSideVisibilityType.GHOST) & serverSideVisibility.getValue(ServerSideVisibilityType.GHOST) & GhostVisibilityType.GHOST.ordinal()) == 0) {
                    var corpse = thisPlayer.getCorpse();

                    if (corpse != null) {
                        corpseCheck = true;

                        if (corpse.isWithinDist(thisPlayer, getSightRange(obj), false)) {
                            if (corpse.isWithinDist(obj, getSightRange(obj), false)) {
                                corpseVisibility = true;
                            }
                        }
                    }
                }

                var target = obj.toUnit();

                if (target != null) {
                    // Don't allow to detect vehicle accessories if you can't see vehicle
                    var vehicle = target.getVehicleBase();

                    if (vehicle != null) {
                        if (!thisPlayer.haveAtClient(vehicle)) {
                            return false;
                        }
                    }
                }
            }

            var viewpoint = this;
            var player = toPlayer();

            if (player != null) {
                viewpoint = player.getViewpoint();
            }

            if (viewpoint == null) {
                viewpoint = this;
            }

            if (!corpseCheck && !viewpoint.isWithinDist(obj, getSightRange(obj), false)) {
                return false;
            }
        }

        // GM visibility off or hidden NPC
        if (obj.serverSideVisibility.getValue(ServerSideVisibilityType.GM) == 0) {
            // Stop checking other things for GMs
            if (getServerSideVisibilityDetect().getValue(ServerSideVisibilityType.GM) != 0) {
                return true;
            }
        } else {
            return getServerSideVisibilityDetect().getValue(ServerSideVisibilityType.GM) >= obj.serverSideVisibility.getValue(ServerSideVisibilityType.GM);
        }

        // Ghost players, Spirit Healers, and some other NPCs
        if (!corpseVisibility && (obj.serverSideVisibility.getValue(ServerSideVisibilityType.GHOST) & getServerSideVisibilityDetect().getValue(ServerSideVisibilityType.GHOST)) != 0) {
            // Alive players can see dead players in some cases, but other objects can't do that
            var thisPlayer = toPlayer();

            if (thisPlayer != null) {
                var objPlayer = obj.toPlayer();

                if (objPlayer != null) {
                    if (!thisPlayer.isGroupVisibleFor(objPlayer)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (obj.isInvisibleDueToDespawn(this)) {
            return false;
        }

        if (!canDetect(obj, ignoreStealth, checkAlert)) {
            return false;
        }

        return true;
    }

    public boolean canNeverSee(WorldObject obj) {
        return getMap() != obj.getMap() || !inSamePhase(obj);
    }

    public boolean canAlwaysSee(WorldObject obj) {
        return false;
    }

    public void sendMessageToSet(ServerPacket packet, boolean self) {
        if (isInWorld()) {
            sendMessageToSetInRange(packet, getVisibilityRange(), self);
        }
    }

    public void sendMessageToSetInRange(ServerPacket data, float dist, boolean self) {
        Consumer<Player> sender = (player) -> player.sendPacket(data);
        var notifier = GridVisitors.newMessageDistDeliverer(this, sender, dist);
        Cell.visitGrid(this, notifier, dist);
    }

    public void sendMessageToSet(ServerPacket data, Player skip) {
        Consumer<Player> sender = (player) -> player.sendPacket(data);
        var notifier = GridVisitors.newMessageDistDeliverer(this, sender, getVisibilityRange(), false, skip, false);
        Cell.visitGrid(this, notifier, getVisibilityRange());
    }

    public final void sendCombatLogMessage(CombatLogServerPacket combatLog) {
        Consumer<Player> sender = (player) -> {
            combatLog.clear();
            combatLog.setAdvancedCombatLogging(player.isAdvancedCombatLoggingEnabled());
            player.sendPacket(combatLog);
        };

        var self = toPlayer();
        if (self != null) {
            sender.accept(self);
        }
        var notifier = GridVisitors.newMessageDistDeliverer(this, sender, getVisibilityRange());
        Cell.visitGrid(this, notifier, getVisibilityRange());
    }

    public void resetMap() {
        if (currMap == null) {
            return;
        }

        if (isWorldObject()) {
            currMap.removeWorldObject(this);
        }

        currMap = null;
    }

    public final void addObjectToRemoveList() {
        var map = getMap();

        if (map == null) {
            Logs.MISC.error("Object {} at attempt add to move list not have valid map (Id: {}).", getGUID(), getLocation().getMapId());

            return;
        }

        map.addObjectToRemoveList(this);
    }

    public final ZoneScript findZoneScript() {
        var map = getMap();

        if (map != null) {
            var instanceMap = map.toInstanceMap();

            if (instanceMap != null) {
                return instanceMap.getInstanceScript();
            }

            var bgMap = map.toBattlegroundMap();

            if (bgMap != null) {
                return bgMap.getBattlegroundScript();
            }

            if (!map.isBattlegroundOrArena()) {
                var bf = worldContext.getBattleFieldManager().getBattlefieldToZoneId(map, getZoneId());

                if (bf != null) {
                    return bf;
                }

                return worldContext.getOutdoorPvpManager().getOutdoorPvPToZoneId(map, getZoneId());
            }
        }

        return null;
    }

    public final void setZoneScript() {
        setZoneScript(findZoneScript());
    }


    public final TempSummon summonCreature(int entry, float x, float y, float z, float o, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId) {
        return summonCreature(entry, x, y, z, o, despawnType, despawnTime, vehId, spellId, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z, float o, TempSummonType despawnType, Duration despawnTime, int vehId) {
        return summonCreature(entry, x, y, z, o, despawnType, despawnTime, vehId, 0, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z, float o, TempSummonType despawnType, Duration despawnTime) {
        return summonCreature(entry, x, y, z, o, despawnType, despawnTime, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z, float o, TempSummonType despawnType) {
        return summonCreature(entry, x, y, z, o, despawnType, null, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z, float o) {
        return summonCreature(entry, x, y, z, o, TempSummonType.MANUAL_DESPAWN, null, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z) {
        return summonCreature(entry, x, y, z, 0, TempSummonType.MANUAL_DESPAWN, null, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, float x, float y, float z, float o, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId, ObjectGuid privateObjectOwner) {
        return summonCreature(entry, new Position(x, y, z, o), despawnType, despawnTime, vehId, spellId, privateObjectOwner);
    }


    public final TempSummon summonCreature(int entry, Position pos, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId) {
        return summonCreature(entry, pos, despawnType, despawnTime, vehId, spellId, null);
    }

    public final TempSummon summonCreature(int entry, Position pos, TempSummonType despawnType, Duration despawnTime, int vehId) {
        return summonCreature(entry, pos, despawnType, despawnTime, vehId, 0, null);
    }

    public final TempSummon summonCreature(int entry, Position pos, TempSummonType despawnType, Duration despawnTime) {
        return summonCreature(entry, pos, despawnType, despawnTime, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, Position pos, TempSummonType despawnType) {
        return summonCreature(entry, pos, despawnType, null, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, Position pos) {
        return summonCreature(entry, pos, TempSummonType.MANUAL_DESPAWN, null, 0, 0, null);
    }

    public final TempSummon summonCreature(int entry, Position pos, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId, ObjectGuid privateObjectOwner) {
        if (pos.isDefaultValue()) {
            getClosePoint(pos, getCombatReach());
        }

        if (pos.getO() == 0.0f) {
            pos.setO(getLocation().getO());
        }

        var map = getMap();

        if (map != null) {
            var summon = map.summonCreature(entry, pos, null, (int) despawnTime.toMillis(), this, spellId, vehId, privateObjectOwner);

            if (summon != null) {
                summon.setTempSummonType(despawnType);

                return summon;
            }
        }

        return null;
    }


    public final TempSummon summonPersonalClone(Position pos, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId) {
        return summonPersonalClone(pos, despawnType, despawnTime, vehId, spellId, null);
    }

    public final TempSummon summonPersonalClone(Position pos, TempSummonType despawnType, Duration despawnTime, int vehId) {
        return summonPersonalClone(pos, despawnType, despawnTime, vehId, 0, null);
    }

    public final TempSummon summonPersonalClone(Position pos, TempSummonType despawnType, Duration despawnTime) {
        return summonPersonalClone(pos, despawnType, despawnTime, 0, 0, null);
    }

    public final TempSummon summonPersonalClone(Position pos, TempSummonType despawnType) {
        return summonPersonalClone(pos, despawnType, null, 0, 0, null);
    }

    public final TempSummon summonPersonalClone(Position pos) {
        return summonPersonalClone(pos, TempSummonType.MANUAL_DESPAWN, null, 0, 0, null);
    }

    public final TempSummon summonPersonalClone(Position pos, TempSummonType despawnType, Duration despawnTime, int vehId, int spellId, Player privateObjectOwner) {
        var map = getMap();

        if (map != null) {
            var summon = map.summonCreature(getEntry(), pos, null, (int) despawnTime.toMillis(), privateObjectOwner, spellId, vehId, privateObjectOwner.getGUID(), new SmoothPhasingInfo(getGUID(), true, true));

            if (summon != null) {
                summon.setTempSummonType(despawnType);

                return summon;
            }
        }

        return null;
    }


    public final GameObject summonGameObject(int entry, float x, float y, float z, float ang, Quaternion rotation, Duration respawnTime) {
        return summonGameObject(entry, x, y, z, ang, rotation, respawnTime, GOSummonType.TIMED_OR_CORPSE_DESPAWN);
    }

    public final GameObject summonGameObject(int entry, float x, float y, float z, float ang, Quaternion rotation, Duration respawnTime, GOSummonType summonType) {
        return summonGameObject(entry, new Position(x, y, z, ang), rotation, respawnTime, summonType);
    }


    public final GameObject summonGameObject(int entry, Position pos, Quaternion rotation, Duration respawnTime) {
        return summonGameObject(entry, pos, rotation, respawnTime, GOSummonType.TIMED_OR_CORPSE_DESPAWN);
    }

    public final GameObject summonGameObject(int entry, Position pos, Quaternion rotation, Duration respawnTime, GOSummonType summonType) {
        if (pos.getX() != 0 && pos.getY() != 0 && pos.getZ() != 0) {
            getClosePoint(pos, getCombatReach());
            pos.setO(getLocation().getO());
        }

        if (!isInWorld()) {
            return null;
        }

        var goinfo = worldContext.getObjectManager().getGameObjectTemplate(entry);

        if (goinfo == null) {
            Logs.SQL.error("GameObject template {} not found in database!", entry);
            return null;
        }

        var map = getMap();
        var go = GameObject.createGameObject(entry, map, pos, rotation, 255, GOState.READY);

        if (go == null) {
            return null;
        }

        PhasingHandler.inheritPhaseShift(go, this);

        go.setRespawnTime((int) respawnTime.toSeconds());

        if (isPlayer() || (isCreature() && summonType == GOSummonType.TIMED_OR_CORPSE_DESPAWN)) //not sure how to handle this
        {
            toUnit().addGameObject(go);
        } else {
            go.setSpawnedByDefault(false);
        }

        map.addToMap(go);

        return go;
    }


    public final Creature summonTrigger(Position pos, Duration despawnTime) {
        return summonTrigger(pos, despawnTime, null);
    }

    public final Creature summonTrigger(Position pos, Duration despawnTime, CreatureAI AI) {
        var summonType = despawnTime.isZero() ? TempSummonType.DEAD_DESPAWN : TempSummonType.TIMED_DESPAWN;
        Creature summon = summonCreature(ObjectDefine.WORLD_TRIGGER, pos, summonType, despawnTime);

        if (summon == null) {
            return null;
        }

        if (isTypeId(TypeId.PLAYER) || isTypeId(TypeId.UNIT)) {
            summon.setFaction(toUnit().getFaction());
            summon.setLevel(toUnit().getLevel());
        }

        if (AI != null) {
            summon.initializeAI(new CreatureAI(summon));
        }

        return summon;
    }


    public final void summonCreatureGroup(byte group, ArrayList<TempSummon> summoned) {

        Assert.isTrue(getObjectTypeId() == TypeId.GAME_OBJECT || getObjectTypeId() == TypeId.UNIT, "Only GOs and creatures can summon npc groups!");

        var data = worldContext.getObjectManager().getSummonGroup(getEntry(), isTypeId(TypeId.GAME_OBJECT) ? SummonerType.GAME_OBJECT : SummonerType.CREATURE, group);

        if (data.isEmpty()) {

            Logs.SCRIPTS.warn("{} ({}) tried to summon non-existing summon group {}.", getName(), getGUID(), group);

            return;
        }

        List<TempSummon> summons = new ArrayList<>(data.size());
        for (var item : data) {
            var summon = summonCreature(item.entry, item.positionX, item.positionY, item.positionZ,
                    item.orientation, null, Duration.ofMillis(item.summonTime));

            if (summon != null) {
                summons.add(summon);
            }
        }


        CreatureGroup creatureGroup = new CreatureGroup(0);
        for (TempSummon summon : summons) {
            if (!summon.isInWorld())   // evil script might despawn a summon
                continue;

            creatureGroup.addMember(summon);
            if (summoned != null)
                summoned.add(summon);
        }
    }


    public final Creature findNearestCreature(int entry, float range) {
        return findNearestCreature(entry, range, true);
    }

    public final Creature findNearestCreature(int entry, float range, boolean alive) {
        YieldResult<Creature> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newCreatureLastSearcher(this, entry, range, alive, yieldResult);
        Cell.visitGrid(this, searcher, range);
        return yieldResult.get();
    }

    public final Creature findNearestCreatureWithOptions(float range, FindCreatureOptions options) {
        YieldResult<Creature> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newCreatureLastSearcher(this, range, options, yieldResult);
        Cell.visitGrid(this, searcher, range);
        return yieldResult.get();
    }


    public final GameObject findNearestGameObject(int entry, float range) {
        return findNearestGameObject(entry, range, true);
    }

    public final GameObject findNearestGameObject(int entry, float range, boolean spawnedOnly) {
        YieldResult<GameObject> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newGameObjectLastSearcher(this, entry, range, spawnedOnly, yieldResult);
        Cell.visitGrid(this, searcher, range);
        return yieldResult.get();
    }


    public final GameObject findNearestUnspawnedGameObject(int entry, float range) {
        YieldResult<GameObject> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newGameObjectLastSearcher(this, entry, range, yieldResult);
        Cell.visitGrid(this, searcher, range);
        return yieldResult.get();
    }

    public final GameObject findNearestGameObjectOfType(GameObjectType type, float range) {
        YieldResult<GameObject> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newGameObjectLastSearcher(this, type, range, yieldResult);
        Cell.visitGrid(this, searcher, range);
        return yieldResult.get();
    }

    public final Player selectNearestPlayer(float distance) {
        YieldResult<Player> yieldResult = YieldResult.ofNull();
        var searcher = GridVisitors.newPlayerLastSearcher(this, distance, yieldResult);
        Cell.visitGrid(this, searcher, distance);
        return yieldResult.get();
    }


    public final double calculateSpellDamage(Unit target, SpellEffectInfo spellEffectInfo, YieldResult<Integer> basePoints, YieldResult<Float> variance) {
        if (!variance.wasNull()) {
            variance.set(0.0f);
        }
        return spellEffectInfo.calcValue(this, basePoints, target, variance);
    }

    public final float getSpellMaxRangeForTarget(Unit target, SpellInfo spellInfo) {
        if (spellInfo.getRangeEntry() == null) {
            return 0.0f;
        }

        if (spellInfo.getRangeEntry().getRangeMax()[0] == spellInfo.getRangeEntry().getRangeMax()[1]) {
            return spellInfo.getMaxRange();
        }

        if (target == null) {
            return spellInfo.getMaxRange(true);
        }

        return spellInfo.getMaxRange(!isHostileTo(target));
    }

    public final float getSpellMinRangeForTarget(Unit target, SpellInfo spellInfo) {
        if (spellInfo.getRangeEntry() == null) {
            return 0.0f;
        }

        if (spellInfo.getRangeEntry().getRangeMin()[0] == spellInfo.getRangeEntry().getRangeMin()[1]) {
            return spellInfo.getMinRange();
        }

        if (target == null) {
            return spellInfo.getMinRange(true);
        }

        return spellInfo.getMinRange(!isHostileTo(target));
    }

    public final float applyEffectModifiers(SpellInfo spellInfo, SpellEffIndex effIndex, float value) {
        var modOwner = getSpellModOwner();

        if (modOwner != null) {

            value = modOwner.applySpellMod(spellInfo, SpellModOp.Points, value);

            value = switch (effIndex) {
                case EFFECT_0 -> modOwner.applySpellMod(spellInfo, SpellModOp.PointsIndex0, value);
                case EFFECT_1 -> modOwner.applySpellMod(spellInfo, SpellModOp.PointsIndex1, value);
                case EFFECT_2 -> modOwner.applySpellMod(spellInfo, SpellModOp.PointsIndex2, value);
                case EFFECT_3 -> modOwner.applySpellMod(spellInfo, SpellModOp.PointsIndex3, value);
                case EFFECT_4 -> modOwner.applySpellMod(spellInfo, SpellModOp.PointsIndex4, value);
                default -> value;
            };
        }

        return value;
    }

    public final int calcSpellDuration(SpellInfo spellInfo) {
        return calcSpellDuration(spellInfo, 0);
    }

    public final int calcSpellDuration(SpellInfo spellInfo, int spentComboPoints) {
        var minduration = spellInfo.getDuration();
        if (minduration <= 0)
            return minduration;

        var maxduration = spellInfo.getMaxDuration();
        if (minduration == maxduration)
            return minduration;

        var unit = toUnit();
        if (unit == null)
            return minduration;

        if (spentComboPoints == 0 || !spellInfo.isFinishingMove())
            return minduration;

        // Combo Points increase an aura's duration per consumed point
        int baseComboCost = 0;
        PowerType powerType = worldContext.getDbcObjectManager().getPowerType(Power.COMBO_POINTS);
        if (powerType == null)
            baseComboCost += powerType.getMaxBasePower();

        if (baseComboCost == 0)
            return minduration;

        float durationPerComboPoint = (float) (maxduration - minduration) / baseComboCost;
        return minduration + (int) (durationPerComboPoint * spentComboPoints);
    }


    public final int modSpellDuration(SpellInfo spellInfo, WorldObject target, int duration, boolean positive, int effectMask) {
        // don't mod permanent auras duration
        if (duration < 0) {
            return duration;
        }

        // some auras are not affected by duration modifiers
        if (spellInfo.hasAttribute(SpellAttr7.NO_TARGET_DURATION_MOD)) {
            return duration;
        }

        // cut duration only of negative effects
        var unitTarget = target.toUnit();

        if (unitTarget == null) {
            return duration;
        }

        if (!positive) {
            var mechanicMask = spellInfo.getSpellMechanicMaskByEffectMask(effectMask);

            Predicate<AuraEffect> mechanicCheck = (aurEff) -> (mechanicMask & (1L << aurEff.getMiscValue())) != 0;
            // Find total mod value (negative bonus)
            var durationMod_always = unitTarget.getTotalAuraModifier(AuraType.MECHANIC_DURATION_MOD, mechanicCheck);
            // Find max mod (negative bonus)
            var durationMod_not_stack = unitTarget.getMaxNegativeAuraModifier(AuraType.MECHANIC_DURATION_MOD_NOT_STACK, mechanicCheck);

            // Select strongest negative mod
            var durationMod = Math.min(durationMod_always, durationMod_not_stack);

            if (durationMod != 0) {
                duration = MathUtil.addPct(duration, durationMod);
            }

            // there are only negative mods currently
            durationMod_always = unitTarget.getTotalAuraModifierByMiscValue(AuraType.MOD_AURA_DURATION_BY_DISPEL, spellInfo.getDispel().ordinal());
            durationMod_not_stack = unitTarget.getMaxNegativeAuraModifierByMiscValue(AuraType.MOD_AURA_DURATION_BY_DISPEL_NOT_STACK, spellInfo.getDispel().ordinal());

            durationMod = Math.min(durationMod_always, durationMod_not_stack);

            if (durationMod != 0) {
                duration = MathUtil.addPct(duration, durationMod);
            }
        } else {
            // else positive mods here, there are no currently
            // when there will be, change GetTotalAuraModifierByMiscValue to GetMaxPositiveAuraModifierByMiscValue

            // Mixology - duration boost
            if (unitTarget.isPlayer()) {
                if (spellInfo.getSpellFamilyName() == SpellFamilyName.POTION
                        && (worldContext.getSpellManager().isSpellMemberOfSpellGroup(spellInfo.getId(), SpellGroup.ELIXIR_BATTLE)
                        || worldContext.getSpellManager().isSpellMemberOfSpellGroup(spellInfo.getId(), SpellGroup.ELIXIR_GUARDIAN))) {
                    var effect = spellInfo.getEffect(SpellEffIndex.EFFECT_0);

                    if (unitTarget.hasAura(53042) && effect != null && unitTarget.hasSpell(effect.triggerSpell)) {
                        duration *= 2;
                    }
                }
            }
        }

        return Math.max(duration, 0);
    }


    public final int modSpellCastTime(SpellInfo spellInfo, int castTime) {
        return modSpellCastTime(spellInfo, castTime, null);
    }

    public final int modSpellCastTime(SpellInfo spellInfo, int castTime, Spell spell) {
        if (spellInfo == null || castTime < 0) {
            return castTime;
        }

        // called from caster
        var modOwner = getSpellModOwner();

        if (modOwner != null) {
            float spellModVal = modOwner.applySpellMod(spellInfo, SpellModOp.ChangeCastTime, castTime, spell);
            castTime = Math.round(spellModVal);
        }

        var unitCaster = toUnit();

        if (unitCaster == null) {
            return castTime;
        }

        if (unitCaster.isPlayer() && unitCaster.toPlayer().getCommandStatus(PlayerCommandState.CAST_TIME)) {
            castTime = 0;
        } else if (!(spellInfo.hasAttribute(SpellAttr0.IS_ABILITY)
                || spellInfo.hasAttribute(SpellAttr0.IS_TRADESKILL)
                || spellInfo.hasAttribute(SpellAttr3.IGNORE_CASTER_MODIFIERS))
                && ((isPlayer() && spellInfo.getSpellFamilyName() != SpellFamilyName.GENERIC) || isCreature())) {
            castTime = Math.round(unitCaster.getCanInstantCast() ? 0 : (castTime * unitCaster.getUnitData().getModCastingSpeed()));
        } else if (spellInfo.hasAttribute(SpellAttr0.USES_RANGED_SLOT) && !spellInfo.hasAttribute(SpellAttr2.AUTO_REPEAT)) {
            castTime = Math.round(castTime * unitCaster.getModAttackSpeedPct()[WeaponAttackType.RANGED_ATTACK.ordinal()]);
        } else if (worldContext.getSpellManager().isPartOfSkillLine(SkillType.COOKING, spellInfo.getId()) && unitCaster.hasAura(67556)) // cooking with Chef Hat.
        {
            castTime = 500;
        }
        return castTime;
    }

    public final int modSpellDurationTime(SpellInfo spellInfo, int duration) {
        return modSpellDurationTime(spellInfo, duration, null);
    }

    public final int modSpellDurationTime(SpellInfo spellInfo, int duration, Spell spell) {
        if (spellInfo == null || duration < 0) {
            return duration;
        }

        if (spellInfo.isChanneled() && !spellInfo.hasAttribute(SpellAttr5.SPELL_HASTE_AFFECTS_PERIODIC)) {
            return duration;
        }

        // called from caster
        var modOwner = getSpellModOwner();

        if (modOwner != null) {
            modOwner.applySpellMod(spellInfo, SpellModOp.ChangeCastTime, duration, spell);
        }

        var unitCaster = toUnit();

        if (unitCaster == null) {
            return duration;
        }

        if (!(spellInfo.hasAttribute(SpellAttr0.IS_ABILITY)
                || spellInfo.hasAttribute(SpellAttr0.IS_TRADESKILL)
                || spellInfo.hasAttribute(SpellAttr3.IGNORE_CASTER_MODIFIERS))
                && ((isPlayer() && spellInfo.getSpellFamilyName() != SpellFamilyName.GENERIC) || isCreature())) {
            duration = Math.round(duration * unitCaster.getUnitData().getModCastingSpeed());
        } else if (spellInfo.hasAttribute(SpellAttr0.USES_RANGED_SLOT) && !spellInfo.hasAttribute(SpellAttr2.AUTO_REPEAT)) {
            duration = Math.round(duration * unitCaster.getModAttackSpeedPct()[WeaponAttackType.RANGED_ATTACK.ordinal()]);
        }
        return duration;
    }

    public float meleeSpellMissChance(Unit victim, WeaponAttackType attType, SpellInfo spellInfo) {
        return 0.0f;
    }

    public SpellMissInfo meleeSpellHitResult(Unit victim, SpellInfo spellInfo) {
        return SpellMissInfo.NONE;
    }

    // Calculate spell hit result can be:
    // Every spell can: Evade/Immune/Reflect/Sucesful hit
    // For melee based spells:
    //   Miss
    //   Dodge
    //   Parry
    // For spells
    //   Resist

    public final SpellMissInfo spellHitResult(Unit victim, SpellInfo spellInfo) {
        return spellHitResult(victim, spellInfo, false);
    }

    public final SpellMissInfo spellHitResult(Unit victim, SpellInfo spellInfo, boolean canReflect) {
        // Check for immune
        if (victim.isImmunedToSpell(spellInfo, this)) {
            return SpellMissInfo.IMMUNE;
        }

        // Damage immunity is only checked if the spell has damage effects, this immunity must not prevent aura apply
        // returns SPELL_MISS_IMMUNE in that case, for other spells, the SMSG_SPELL_GO must show hit
        if (spellInfo.getHasOnlyDamageEffects() && victim.isImmunedToDamage(spellInfo)) {
            return SpellMissInfo.IMMUNE;
        }

        // All positive spells can`t miss
        /// @todo client not show miss log for this spells - so need find info for this in dbc and use it!
        if (spellInfo.isPositive() && !isHostileTo(victim)) // prevent from affecting enemy by "positive" spell
        {
            return SpellMissInfo.NONE;
        }

        if (this == victim) {
            return SpellMissInfo.NONE;
        }

        // Return evade for units in evade mode
        if (victim.isCreature() && victim.toCreature().isEvadingAttacks()) {
            return SpellMissInfo.EVADE;
        }

        // Try victim reflect spell
        if (canReflect) {
            var reflectchance = victim.getTotalAuraModifier(AuraType.REFLECT_SPELLS);
            reflectchance += victim.getTotalAuraModifierByMiscMask(AuraType.REFLECT_SPELLS_SCHOOL, spellInfo.getSchoolMask());

            if (reflectchance > 0 && RandomUtil.randChance(Math.round(reflectchance))) {
                return SpellMissInfo.REFLECT;
            }
        }

        if (spellInfo.hasAttribute(SpellAttr3.ALWAYS_HIT)) {
            return SpellMissInfo.NONE;
        }

        return switch (spellInfo.getDmgClass()) {
            case RANGED, MELEE -> meleeSpellHitResult(victim, spellInfo);
            case NONE -> SpellMissInfo.NONE;
            case MAGIC -> magicSpellHitResult(victim, spellInfo);
        };

    }


    public final void sendSpellMiss(Unit target, int spellID, SpellMissInfo missInfo) {
        SpellMissLog spellMissLog = new SpellMissLog();
        spellMissLog.spellID = spellID;
        spellMissLog.caster = getGUID();
        spellMissLog.entries.add(new SpellLogMissEntry(target.getGUID(), (byte) missInfo.ordinal()));
        sendMessageToSet(spellMissLog, true);
    }

    public final FactionTemplate getFactionTemplateEntry() {
        var factionId = getFaction();
        var entry = worldContext.getDbcObjectManager().factionTemplate(factionId);
        if (entry == null) {
            switch (getObjectTypeId()) {
                case PLAYER:
                    Logs.UNIT.error("Player {} has invalid faction (faction template id) #{}", toPlayer().getName(), factionId);
                    break;
                case UNIT:
                    Logs.UNIT.error("Creature (template id: {}) has invalid faction (faction template Id) #{}", toCreature().getCreatureTemplate().entry, factionId);

                    break;
                case GAME_OBJECT:
                    if (factionId != 0) // Gameobjects may have faction template id = 0
                    {
                        Logs.UNIT.error("GameObject (template id: {}) has invalid faction (faction template Id) #{}", toGameObject().getTemplate().entry, factionId);
                    }

                    break;
                default:
                    Logs.UNIT.error("Object (name={}, type={}) has invalid faction (faction template Id) #{}", getName(), getObjectTypeId(), factionId);

                    break;
            }
        }

        return entry;
    }

    // function based on function Unit::UnitReaction from 13850 client
    public final ReputationRank getReactionTo(WorldObject target) {
        // always friendly to self
        if (this == target) {
            return ReputationRank.FRIENDLY;
        }


        BiFunction<Unit, ObjectGuid, Boolean> isAttackBySummoner = (me, targetGuid) -> {
            if (me == null)
                return false;

            var tempSummon = me.toTempSummon();

            if (tempSummon == null || tempSummon.summonProperty == null)
                return false;

            return (tempSummon.summonProperty.getFlags().hasFlag(SummonPropertyFlag.AttackableBySummoner))
                    && Objects.equals(targetGuid, tempSummon.getSummonerGUID());
        };

        if (isAttackBySummoner.apply(toUnit(), target.getGUID()) || isAttackBySummoner.apply(target.toUnit(), getGUID())) {
            return ReputationRank.NEUTRAL;
        }

        // always friendly to charmer or owner
        if (getCharmerOrOwnerOrSelf() == target.getCharmerOrOwnerOrSelf()) {
            return ReputationRank.FRIENDLY;
        }

        var selfPlayerOwner = getAffectingPlayer();
        var targetPlayerOwner = target.getAffectingPlayer();

        // check forced reputation to support SPELL_AURA_FORCE_REACTION
        if (selfPlayerOwner != null) {
            var targetFactionTemplateEntry = target.getFactionTemplateEntry();

            if (targetFactionTemplateEntry != null) {
                var repRank = selfPlayerOwner.getReputationMgr().getForcedRankIfAny(targetFactionTemplateEntry);

                if (repRank != null) {
                    return repRank;
                }
            }
        } else if (targetPlayerOwner != null) {
            var selfFactionTemplateEntry = getFactionTemplateEntry();

            if (selfFactionTemplateEntry != null) {
                var repRank = targetPlayerOwner.getReputationMgr().getForcedRankIfAny(selfFactionTemplateEntry);

                if (repRank != null) {
                    return repRank;
                }
            }
        }

        if (this instanceof Unit unit && unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
            if (target instanceof Unit targetUnit && targetUnit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
                if (selfPlayerOwner != null && targetPlayerOwner != null) {
                    // always friendly to other unit controlled by player, or to the player himself
                    if (selfPlayerOwner == targetPlayerOwner) {
                        return ReputationRank.FRIENDLY;
                    }

                    // duel - always hostile to opponent
                    if (selfPlayerOwner.getDuel() != null && selfPlayerOwner.getDuel().getOpponent() == targetPlayerOwner && selfPlayerOwner.getDuel().getState() == DuelState.IN_PROGRESS) {
                        return ReputationRank.HOSTILE;
                    }

                    // same group - checks dependant only on our faction - skip FFA_PVP for example
                    if (selfPlayerOwner.isInRaidWith(targetPlayerOwner)) {
                        return ReputationRank.FRIENDLY; // return true to allow config option AllowTwoSide.Interaction.Group to work
                    }
                    // however client seems to allow mixed group parties, because in 13850 client it works like:
                    // return getFactionReactionTo(getFactionTemplateEntry(), target);
                }

                // check FFA_PVP
                if (unit.isFFAPvP() && targetUnit.isFFAPvP()) {
                    return ReputationRank.HOSTILE;
                }

                if (selfPlayerOwner != null) {
                    var targetFactionTemplateEntry = targetUnit.getFactionTemplateEntry();

                    if (targetFactionTemplateEntry != null) {
                        var repRank = selfPlayerOwner.getReputationMgr().getForcedRankIfAny(targetFactionTemplateEntry);

                        if (repRank != null) {
                            return repRank;
                        }

                        if (!selfPlayerOwner.hasUnitFlag2(UnitFlag2.IGNORE_REPUTATION)) {
                            var targetFactionEntry = worldContext.getDbcObjectManager().faction(targetFactionTemplateEntry.getFaction().intValue());

                            if (targetFactionEntry != null) {
                                if (targetFactionEntry.canHaveReputation()) {
                                    // check contested flags
                                    if ((targetFactionTemplateEntry.getFlags() & FactionTemplateFlag.CONTESTED_GUARD.value) != 0
                                            && selfPlayerOwner.hasPlayerFlag(PlayerFlag.CONTESTED_PVP)) {
                                        return ReputationRank.HOSTILE;
                                    }

                                    // if faction has reputation, hostile state depends only from AtWar state
                                    if (selfPlayerOwner.getReputationMgr().isAtWar(targetFactionEntry)) {
                                        return ReputationRank.HOSTILE;
                                    }

                                    return ReputationRank.FRIENDLY;
                                }
                            }
                        }
                    }
                }
            }
        }

        // do checks dependant only on our faction
        return getFactionReactionTo(getFactionTemplateEntry(), target);
    }

    public final boolean isHostileTo(WorldObject target) {
        return getReactionTo(target).ordinal() <= ReputationRank.HOSTILE.ordinal();
    }

    public final boolean isFriendlyTo(WorldObject target) {
        return getReactionTo(target).ordinal() >= ReputationRank.FRIENDLY.ordinal();
    }

    public final boolean isHostileToPlayers() {
        var my_faction = getFactionTemplateEntry();

        if (my_faction.getFaction() == 0) {
            return false;
        }

        var raw_faction = worldContext.getDbcObjectManager().faction(my_faction.getFaction().intValue());

        if (raw_faction != null && raw_faction.canHaveReputation()) {
            return false;
        }

        return my_faction.isHostileToPlayers();
    }

    public final boolean isNeutralToAll() {
        var my_faction = getFactionTemplateEntry();

        if (my_faction.getFaction() == 0) {
            return true;
        }

        var raw_faction = worldContext.getDbcObjectManager().faction(my_faction.getFaction().intValue());

        if (raw_faction != null && raw_faction.canHaveReputation()) {
            return false;
        }

        return my_faction.isNeutralToAll();
    }

    public final SpellCastResult castSpell(int spellId, boolean triggered) {
        return castSpell(spellId, triggered, null);
    }

    public final SpellCastResult castSpell(int spellId) {
        return castSpell(spellId, false, null);
    }

    public final SpellCastResult castSpell(int spellId, boolean triggered, Byte empowerStage) {
        return castSpell(null, spellId, triggered, empowerStage);
    }

    public final SpellCastResult castSpell(WorldObject target, EnumFlag.FlagValue spellId) {
        return castSpell(target, spellId.getValue(), false);
    }


    public final SpellCastResult castSpell(WorldObject target, EnumFlag.FlagValue spellId, boolean triggered) {
        return castSpell(target, spellId.getValue(), triggered);
    }


    public final SpellCastResult castSpell(WorldObject target, int spellId, Spell triggeringSpell) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(true);
        args.triggeringSpell = triggeringSpell;

        return castSpell(target, spellId, args);
    }


    public final SpellCastResult castSpell(WorldObject target, int spellId, AuraEffect triggeringAura) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(true);
        args.triggeringAura = triggeringAura;

        return castSpell(target, spellId, args);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, boolean triggered) {
        return castSpell(target, spellId, triggered, null);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId) {
        return castSpell(target, spellId, false, null);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, boolean triggered, Byte empowerStage) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(triggered);
        args.empowerStage = empowerStage;

        return castSpell(target, spellId, args);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, TriggerCastFlag triggerCastFlag) {
        return castSpell(target, spellId, triggerCastFlag, false);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, TriggerCastFlag triggerCastFlags, boolean triggered) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(triggered);
        args.triggerFlags = triggerCastFlags;

        return castSpell(target, spellId, args);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, float bp0Val) {
        return castSpell(target, spellId, bp0Val, false);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, float bp0Val, boolean triggered) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(triggered);
        args.spellValueOverrides.put(SpellValueMod.BASE_POINT0, bp0Val);

        return castSpell(target, spellId, args);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, SpellValueMod spellValueMod, float bp0Val) {
        return castSpell(target, spellId, spellValueMod, bp0Val, false);
    }

    public final SpellCastResult castSpell(WorldObject target, int spellId, SpellValueMod spellValueMod, float bp0Val, boolean triggered) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(triggered);
        args.spellValueOverrides.put(spellValueMod, bp0Val);

        return castSpell(target, spellId, args);
    }


    public final SpellCastResult castSpell(SpellCastTargets targets, int spellId, CastSpellExtraArgs args) {
        return castSpell(new CastSpellTargetArg(targets), spellId, args);
    }


    public final SpellCastResult castSpell(WorldObject target, int spellId, CastSpellExtraArgs args) {
        return castSpell(new CastSpellTargetArg(target), spellId, args);
    }

    public final SpellCastResult castSpell(float x, float y, float z, int spellId) {
        return castSpell(x, y, z, spellId, false);
    }

    public final SpellCastResult castSpell(float x, float y, float z, int spellId, boolean triggered) {
        return castSpell(new Position(x, y, z), spellId, triggered);
    }


    public final SpellCastResult castSpell(float x, float y, float z, int spellId, CastSpellExtraArgs args) {
        return castSpell(new Position(x, y, z), spellId, args);
    }

    public final SpellCastResult castSpell(Position dest, int spellId) {
        return castSpell(dest, spellId, false);
    }

    public final SpellCastResult castSpell(Position dest, int spellId, boolean triggered) {
        CastSpellExtraArgs args = new CastSpellExtraArgs(triggered);
        return castSpell(new CastSpellTargetArg(dest), spellId, args);
    }


    public final SpellCastResult castSpell(Position dest, int spellId, CastSpellExtraArgs args) {
        return castSpell(new CastSpellTargetArg(dest), spellId, args);
    }


    public final SpellCastResult castSpell(CastSpellTargetArg targets, int spellId, CastSpellExtraArgs args) {


        var info = worldContext.getSpellManager().getSpellInfo(spellId, args.castDifficulty != Difficulty.NONE ? args.castDifficulty : getMap().getDifficultyID());

        if (info == null) {
            Logs.UNIT.error("CastSpell: unknown spell {} by caster {}", spellId, getGUID());
            ;

            return SpellCastResult.FAILED_SPELL_UNAVAILABLE;
        }

        if (targets.targets == null) {
            Logs.UNIT.error("CastSpell: unknown spell {} by caster {}", spellId, getGUID());

            return SpellCastResult.FAILED_BAD_TARGETS;
        }

        Spell spell = new Spell(this, info, args.triggerFlags, args.originalCaster, args.originalCastId, args.empowerStage);

        for (var pair : args.spellValueOverrides.entrySet()) {
            spell.setSpellValue(pair.getKey(), pair.getValue());
        }

        spell.castItem = args.castItem;

        if (args.originalCastItemLevel != null) {
            spell.castItemLevel = args.originalCastItemLevel;
        }

        if (spell.castItem == null && info.hasAttribute(SpellAttr2.RETAIN_ITEM_CAST)) {
            if (args.triggeringSpell != null) {
                spell.castItem = args.triggeringSpell.castItem;
            } else if (args.triggeringAura != null && !args.triggeringAura.getBase().getCastItemGuid().isEmpty()) {
                var triggeringAuraCaster = args.triggeringAura.getCaster() == null ? null : args.triggeringAura.getCaster().toPlayer();

                if (triggeringAuraCaster != null) {
                    spell.castItem = triggeringAuraCaster.getItemByGuid(args.triggeringAura.getBase().getCastItemGuid());
                }
            }
        }

        spell.customArg = args.customArg;

        return spell.prepare(targets.targets, args.triggeringAura);
    }

    public final void sendPlaySpellVisual(WorldObject target, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime) {
        sendPlaySpellVisual(target, spellVisualId, missReason, reflectStatus, travelSpeed, speedAsTime, 0);
    }

    public final void sendPlaySpellVisual(WorldObject target, int spellVisualId, short missReason, short reflectStatus, float travelSpeed) {
        sendPlaySpellVisual(target, spellVisualId, missReason, reflectStatus, travelSpeed, false, 0);
    }

    public final void sendPlaySpellVisual(WorldObject target, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime, float launchDelay) {
        PlaySpellVisual playSpellVisual = new PlaySpellVisual();
        playSpellVisual.source = getGUID();
        playSpellVisual.target = target.getGUID();
        playSpellVisual.targetPosition = target.getLocation();
        playSpellVisual.spellVisualID = spellVisualId;
        playSpellVisual.travelSpeed = travelSpeed;
        playSpellVisual.missReason = missReason;
        playSpellVisual.reflectStatus = reflectStatus;
        playSpellVisual.speedAsTime = speedAsTime;
        playSpellVisual.launchDelay = launchDelay;
        sendMessageToSet(playSpellVisual, true);
    }

    public final void sendPlaySpellVisual(Position targetPosition, float launchDelay, int spellVisualId, short missReason, short reflectStatus, float travelSpeed) {
        sendPlaySpellVisual(targetPosition, launchDelay, spellVisualId, missReason, reflectStatus, travelSpeed, false);
    }

    public final void sendPlaySpellVisual(Position targetPosition, float launchDelay, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime) {
        PlaySpellVisual playSpellVisual = new PlaySpellVisual();
        playSpellVisual.source = getGUID();
        playSpellVisual.targetPosition = targetPosition;
        playSpellVisual.launchDelay = launchDelay;
        playSpellVisual.spellVisualID = spellVisualId;
        playSpellVisual.travelSpeed = travelSpeed;
        playSpellVisual.missReason = missReason;
        playSpellVisual.reflectStatus = reflectStatus;
        playSpellVisual.speedAsTime = speedAsTime;
        sendMessageToSet(playSpellVisual, true);
    }

    public final void sendPlaySpellVisual(Position targetPosition, int spellVisualId, short missReason, short reflectStatus, float travelSpeed) {
        sendPlaySpellVisual(targetPosition, spellVisualId, missReason, reflectStatus, travelSpeed, false);
    }

    public final void sendPlaySpellVisual(Position targetPosition, int spellVisualId, short missReason, short reflectStatus, float travelSpeed, boolean speedAsTime) {
        PlaySpellVisual playSpellVisual = new PlaySpellVisual();
        playSpellVisual.source = getGUID();
        playSpellVisual.targetPosition = targetPosition;
        playSpellVisual.spellVisualID = spellVisualId;
        playSpellVisual.travelSpeed = travelSpeed;
        playSpellVisual.missReason = missReason;
        playSpellVisual.reflectStatus = reflectStatus;
        playSpellVisual.speedAsTime = speedAsTime;
        sendMessageToSet(playSpellVisual, true);
    }


    public final void sendPlaySpellVisualKit(int id, int type, int duration) {
        PlaySpellVisualKit playSpellVisualKit = new PlaySpellVisualKit();
        playSpellVisualKit.unit = getGUID();
        playSpellVisualKit.kitRecID = id;
        playSpellVisualKit.kitType = type;
        playSpellVisualKit.duration = duration;

        sendMessageToSet(playSpellVisualKit, true);
    }

    public final boolean isValidAttackTarget(WorldObject target) {
        return isValidAttackTarget(target, null);
    }

    // function based on function Unit::CanAttack from 13850 client

    public final boolean isValidAttackTarget(WorldObject target, SpellInfo bySpell) {
        // some positive spells can be casted at hostile target
        var isPositiveSpell = bySpell != null && bySpell.isPositive();

        // can't attack self (spells can, attribute check)
        if (bySpell == null && this == target) {
            return false;
        }

        // can't attack unattackable units
        var unitTarget = target.toUnit();

        if (unitTarget != null && unitTarget.hasUnitState(UnitState.UNATTACKABLE)) {
            return false;
        }

        // can't attack GMs
        if (target.isPlayer() && target.toPlayer().isGameMaster()) {
            return false;
        }

        var unit = toUnit();

        // visibility checks (only units)
        if (unit != null) {
            // can't attack invisible
            if (bySpell == null || !bySpell.hasAttribute(SpellAttr6.IGNORE_PHASE_SHIFT)) {
                if (!unit.canSeeOrDetect(target, bySpell != null && bySpell.isAffectingArea())) {
                    return false;
                }
            }
        }

        // can't attack dead
        if ((bySpell == null || !bySpell.isAllowingDeadTarget()) && unitTarget != null && !unitTarget.isAlive()) {
            return false;
        }

        // can't attack untargetable
        if ((bySpell == null || !bySpell.hasAttribute(SpellAttr6.CAN_TARGET_UNTARGETABLE)) && unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.NON_ATTACKABLE_2)) {
            return false;
        }

        if (unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.UNINTERACTIBLE)) {
            return false;
        }

        var playerAttacker = toPlayer();

        if (playerAttacker != null) {
            if (playerAttacker.hasPlayerFlag(PlayerFlag.UBER)) {
                return false;
            }
        }

        // check flags
        if (unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.NON_ATTACKABLE, UnitFlag.ON_TAXI, UnitFlag.NOT_ATTACKABLE_1)) {
            return false;
        }

        var unitOrOwner = unit;
        var go = toGameObject();

        if ((go == null ? null : go.getGoType()) == GameObjectType.TRAP) {
            unitOrOwner = go.getOwnerUnit();
        }

        // ignore immunity flags when assisting
        if (unitOrOwner != null && unitTarget != null && !(isPositiveSpell && bySpell.hasAttribute(SpellAttr6.CAN_ASSIST_IMMUNE_PC))) {
            if (!unitOrOwner.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) && unitTarget.isImmuneToNPC()) {
                return false;
            }

            if (!unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) && unitOrOwner.isImmuneToNPC()) {
                return false;
            }

            if (bySpell == null || !bySpell.hasAttribute(SpellAttr8.ATTACK_IGNORE_IMMUNE_TO_PC_FLAG)) {
                if (unitOrOwner.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) && unitTarget.isImmuneToPC()) {
                    return false;
                }

                if (unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) && unitOrOwner.isImmuneToPC()) {
                    return false;
                }
            }
        }

        // CvC case - can attack each other only when one of them is hostile
        if (unit != null && !unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) && unitTarget != null && !unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
            return isHostileTo(unitTarget) || unitTarget.isHostileTo(this);
        }

        // Traps without owner or with NPC owner versus Creature case - can attack to creature only when one of them is hostile
        if ((go == null ? null : go.getGoType()) == GameObjectType.TRAP) {
            var goOwner = go.getOwnerUnit();

            if (goOwner == null || !goOwner.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
                if (unitTarget != null && !unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
                    return isHostileTo(unitTarget) || unitTarget.isHostileTo(this);
                }
            }
        }

        // pvP, PvC, CvP case
        // can't attack friendly targets
        if (isFriendlyTo(target) || target.isFriendlyTo(this)) {
            return false;
        }

        var playerAffectingAttacker = unit != null && unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) ? getAffectingPlayer() : go != null ? getAffectingPlayer() : null;
        var playerAffectingTarget = unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED) ? unitTarget.getAffectingPlayer() : null;

        // Not all neutral creatures can be attacked (even some unfriendly faction does not react aggresive to you, like Sporaggar)
        if ((playerAffectingAttacker != null && playerAffectingTarget == null) || (playerAffectingAttacker == null && playerAffectingTarget != null)) {
            var player = playerAffectingAttacker != null ? playerAffectingAttacker : playerAffectingTarget;
            var creature = playerAffectingAttacker != null ? unitTarget : unit;

            if (creature != null) {
                if (creature.isContestedGuard() && player.hasPlayerFlag(PlayerFlag.CONTESTED_PVP)) {
                    return true;
                }

                var factionTemplate = creature.getFactionTemplateEntry();

                if (factionTemplate != null) {
                    if (player.getReputationMgr().getForcedRankIfAny(factionTemplate) == null) {
                        var factionEntry = worldContext.getDbcObjectManager().faction(factionTemplate.getFaction().intValue());

                        if (factionEntry != null) {
                            var repState = player.getReputationMgr().getState(factionEntry);

                            if (repState != null) {
                                if ((repState.flags.value & ReputationFlag.AtWar.value) == 0) {
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
        }

        var creatureAttacker = toCreature();

        if (creatureAttacker != null && creatureAttacker.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TREAT_AS_RAID_UNIT)) {
            return false;
        }

        if (playerAffectingAttacker != null && playerAffectingTarget != null) {
            if (playerAffectingAttacker.getDuel() != null
                    && playerAffectingAttacker.getDuel().getOpponent() == playerAffectingTarget
                    && playerAffectingAttacker.getDuel().getState() == DuelState.IN_PROGRESS) {
                return true;
            }
        }

        // PvP case - can't attack when attacker or target are in sanctuary
        // however, 13850 client doesn't allow to attack when one of the unit's has sanctuary flag and is pvp
        if (unitTarget != null && unitOrOwner != null
                && unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)
                && unitOrOwner.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)
                && (unitTarget.isInSanctuary() || unitOrOwner.isInSanctuary())) {
            return false;
        }

        // additional checks - only PvP case
        if (playerAffectingAttacker != null && playerAffectingTarget != null) {
            if (playerAffectingTarget.isPvP() || (bySpell != null && bySpell.hasAttribute(SpellAttr5.IGNORE_AREA_EFFECT_PVP_CHECK))) {
                return true;
            }

            if (playerAffectingAttacker.isFFAPvP() && playerAffectingTarget.isFFAPvP()) {
                return true;
            }

            return playerAffectingAttacker.hasPvpFlag(UnitPVPStateFlag.UNIT_BYTE2_FLAG_UNK1)
                    || playerAffectingTarget.hasPvpFlag(UnitPVPStateFlag.UNIT_BYTE2_FLAG_UNK1);
        }

        return true;
    }

    public final boolean isValidAssistTarget(WorldObject target, SpellInfo bySpell) {
        return isValidAssistTarget(target, bySpell, true);
    }

    // function based on function Unit::CanAssist from 13850 client

    public final boolean isValidAssistTarget(WorldObject target) {
        return isValidAssistTarget(target, null, true);
    }

    public final boolean isValidAssistTarget(WorldObject target, SpellInfo bySpell, boolean spellCheck) {
        // some negative spells can be casted at friendly target
        var isNegativeSpell = bySpell != null && !bySpell.isPositive();

        // can assist to self
        if (this == target) {
            return true;
        }

        // can't assist unattackable units
        var unitTarget = target.toUnit();

        if (unitTarget != null && unitTarget.hasUnitState(UnitState.UNATTACKABLE)) {
            return false;
        }

        // can't assist GMs
        if (target.isPlayer() && target.toPlayer().isGameMaster()) {
            return false;
        }

        // can't assist own vehicle or passenger
        var unit = toUnit();

        if (unit != null && unitTarget != null && unit.getVehicle() != null) {
            if (unit.isOnVehicle(unitTarget)) {
                return false;
            }

            Unit vehicleBase = unit.getVehicleBase();
            if (vehicleBase != null && vehicleBase.isOnVehicle(unitTarget)) {
                return false;
            }
        }

        // can't assist invisible
        if ((bySpell == null || !bySpell.hasAttribute(SpellAttr6.IGNORE_PHASE_SHIFT)) && !canSeeOrDetect(target, bySpell != null && bySpell.isAffectingArea())) {
            return false;
        }

        // can't assist dead
        if ((bySpell == null || !bySpell.isAllowingDeadTarget()) && unitTarget != null && !unitTarget.isAlive()) {
            return false;
        }

        // can't assist untargetable
        if ((bySpell == null || !bySpell.hasAttribute(SpellAttr6.CAN_TARGET_UNTARGETABLE)) && unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.NON_ATTACKABLE_2)) {
            return false;
        }

        if (unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.UNINTERACTIBLE)) {
            return false;
        }

        // check flags for negative spells
        if (isNegativeSpell && unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.NON_ATTACKABLE, UnitFlag.ON_TAXI, UnitFlag.NOT_ATTACKABLE_1)) {
            return false;
        }

        if (isNegativeSpell || bySpell == null || !bySpell.hasAttribute(SpellAttr6.CAN_ASSIST_IMMUNE_PC)) {
            if (unit != null && unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
                if (bySpell == null || !bySpell.hasAttribute(SpellAttr8.ATTACK_IGNORE_IMMUNE_TO_PC_FLAG)) {
                    if (unitTarget != null && unitTarget.isImmuneToPC()) {
                        return false;
                    }
                }
            } else {
                if (unitTarget != null && unitTarget.isImmuneToNPC()) {
                    return false;
                }
            }
        }

        // can't assist non-friendly targets
        if (ReputationRank.NEUTRAL.compareTo(getReactionTo(target)) > 0
                && ReputationRank.NEUTRAL.compareTo(target.getReactionTo(this)) > 0
                && (toCreature() == null || !toCreature().getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TREAT_AS_RAID_UNIT))) {
            return false;
        }

        // PvP case
        if (unitTarget != null && unitTarget.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
            if (unit != null && unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
                var selfPlayerOwner = getAffectingPlayer();
                var targetPlayerOwner = unitTarget.getAffectingPlayer();

                if (selfPlayerOwner != null && targetPlayerOwner != null) {
                    // can't assist player which is dueling someone
                    if (selfPlayerOwner != targetPlayerOwner && targetPlayerOwner.getDuel() != null) {
                        return false;
                    }
                }

                // can't assist player in ffa_pvp zone from outside
                if (unitTarget.isFFAPvP() && !unit.isFFAPvP()) {
                    return false;
                }

                // can't assist player out of sanctuary from sanctuary if has pvp enabled
                if (unitTarget.isPvP()) {
                    if (unit.isInSanctuary() && !unitTarget.isInSanctuary()) {
                        return false;
                    }
                }
            }
        }
        // PvC case - player can assist creature only if has specific type flags
        // !target.hasFlag(UNIT_FIELD_FLAGS, UnitFlag.PvpAttackable) &&
        else if (unit != null && unit.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
            if (bySpell == null || !bySpell.hasAttribute(SpellAttr6.CAN_ASSIST_IMMUNE_PC)) {
                if (unitTarget != null && !unitTarget.isPvP()) {
                    var creatureTarget = target.toCreature();

                    if (creatureTarget != null) {
                        return (creatureTarget.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.TREAT_AS_RAID_UNIT) || creatureTarget.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlag.CAN_ASSIST));
                    }
                }
            }
        }

        return true;
    }

    public final Unit getMagicHitRedirectTarget(Unit victim, SpellInfo spellInfo) {
        // Patch 1.2 notes: Spell Reflection no longer reflects abilities
        if (spellInfo.hasAttribute(SpellAttr0.IS_ABILITY) || spellInfo.hasAttribute(SpellAttr1.NO_REDIRECTION) || spellInfo.hasAttribute(SpellAttr0.NO_IMMUNITIES)) {
            return victim;
        }

        var magnetAuras = victim.getAuraEffectsByType(AuraType.SPELL_MAGNET);

        for (var aurEff : magnetAuras) {
            var magnet = aurEff.getBase().getCaster();

            if (magnet != null) {
                if (spellInfo.checkExplicitTarget(this, magnet) == SpellCastResult.CAST_OK && isValidAttackTarget(magnet, spellInfo)) {
                    /** @todo handle this charge drop by proc in cast phase on explicit target
                     */
                    if (spellInfo.getHasHitDelay()) {
                        // Set up missile speed based delay
                        var hitDelay = spellInfo.getLaunchDelay();

                        if (spellInfo.hasAttribute(SpellAttr9.SPECIAL_DELAY_CALCULATION)) {
                            hitDelay += spellInfo.getSpeed();
                        } else if (spellInfo.getSpeed() > 0.0f) {
                            hitDelay += Math.max(victim.getDistance(this), 5.0f) / spellInfo.getSpeed();
                        }

                        var delay = (int) Math.floor(hitDelay * 1000.0f);
                        // Schedule charge drop
                        aurEff.getBase().dropChargeDelayed(delay, AuraRemoveMode.Expire);
                    } else {
                        aurEff.getBase().dropCharge(AuraRemoveMode.Expire);
                    }

                    return magnet;
                }
            }
        }

        return victim;
    }


    public int getCastSpellXSpellVisualId(SpellInfo spellInfo) {
        return spellInfo.getSpellXSpellVisualId(this);
    }

    public final ArrayList<GameObject> getGameObjectListWithEntryInGrid(int entry) {
        return getGameObjectListWithEntryInGrid(entry, 250.0f);
    }

    public final ArrayList<GameObject> getGameObjectListWithEntryInGrid() {
        return getGameObjectListWithEntryInGrid(0, 250.0f);
    }

    public final ArrayList<GameObject> getGameObjectListWithEntryInGrid(int entry, float maxSearchRange) {
        ArrayList<GameObject> gameObjects = new ArrayList<>();
        var searcher = GridVisitors.newGameObjectListSearcher(this, entry, maxSearchRange, gameObjects);
        Cell.visitGrid(this, searcher, maxSearchRange, true);
        return gameObjects;
    }

    public final ArrayList<Creature> getCreatureListWithEntryInGrid(int entry) {
        return getCreatureListWithEntryInGrid(entry, 250.0f);
    }

    public final ArrayList<Creature> getCreatureListWithEntryInGrid() {
        return getCreatureListWithEntryInGrid(0, 250.0f);
    }

    public final ArrayList<Creature> getCreatureListWithEntryInGrid(int entry, float maxSearchRange) {
        ArrayList<Creature> creatureList = new ArrayList<>();
        var searcher = GridVisitors.newCreatureListSearcher(this, entry, maxSearchRange, creatureList);
        Cell.visitGrid(this, searcher, maxSearchRange, true);
        return creatureList;
    }

    public final ArrayList<Creature> getCreatureListWithEntryInGrid(int[] entry) {
        return getCreatureListWithEntryInGrid(entry, 250.0f);
    }

    public final ArrayList<Creature> getCreatureListWithEntryInGrid(int[] entry, float maxSearchRange) {
        ArrayList<Creature> creatureList = new ArrayList<>();
        var searcher = GridVisitors.newCreatureListSearcher(this, entry, maxSearchRange, creatureList);
        Cell.visitGrid(this, searcher, maxSearchRange, true);
        return creatureList;
    }

    public final ArrayList<Creature> getCreatureListWithOptionsInGrid(float maxSearchRange, FindCreatureOptions options) {
        ArrayList<Creature> creatureList = new ArrayList<>();
        var searcher = GridVisitors.newCreatureListSearcher(this, maxSearchRange, options, creatureList);
        Cell.visitGrid(this, searcher, maxSearchRange, true);
        return creatureList;
    }

    public final ArrayList<Player> getPlayerListInGrid(float maxSearchRange) {
        return getPlayerListInGrid(maxSearchRange, true);
    }

    public final ArrayList<Player> getPlayerListInGrid(float maxSearchRange, boolean alive) {
        ArrayList<Player> playerList = new ArrayList<>();
        var searcher = GridVisitors.newPlayerListSearcher(this, maxSearchRange, alive, playerList);
        Cell.visitGrid(this, searcher, maxSearchRange);

        return playerList;
    }

    public final boolean inSamePhase(PhaseShift phaseShift) {
        return getPhaseShift().canSee(phaseShift);
    }

    public final boolean inSamePhase(WorldObject obj) {
        return getPhaseShift().canSee(obj.getPhaseShift());
    }

    public final void playDistanceSound(int soundId) {
        playDistanceSound(soundId, null);
    }

    public final void playDistanceSound(int soundId, Player target) {
        PlaySpeakerBoxSound playSpeakerBoxSound = new PlaySpeakerBoxSound(getGUID(), soundId);
        if (target != null) {
            target.sendPacket(playSpeakerBoxSound);
        } else {
            sendMessageToSet(playSpeakerBoxSound, true);
        }
    }

    public final void playDirectSound(int soundId, Player target) {
        playDirectSound(soundId, target, 0);
    }

    public final void playDirectSound(int soundId) {
        playDirectSound(soundId, null, 0);
    }

    public final void playDirectSound(int soundId, Player target, int broadcastTextId) {
        PlaySound sound = new PlaySound(getGUID(), soundId, broadcastTextId);

        if (target != null) {
            target.sendPacket(sound);
        } else {
            sendMessageToSet(sound, true);
        }
    }

    public final void playDirectMusic(int musicId) {
        playDirectMusic(musicId, null);
    }

    public final void playDirectMusic(int musicId, Player target) {
        if (target != null) {
            target.sendPacket(new PlayMusic(musicId));
        } else {
            sendMessageToSet(new PlayMusic(musicId), true);
        }
    }

    public final void destroyForNearbyPlayers() {
        if (!isInWorld()) {
            return;
        }

        ArrayList<Player> targets = new ArrayList<>();

        var searcher = GridVisitors.newPlayerListSearcher(this, getVisibilityRange(), false, targets);
        Cell.visitGrid(this, searcher, getVisibilityRange());
        for (Player player : targets) {
            if (player == this) {
                continue;
            }

            if (!player.haveAtClient(this)) {
                continue;
            }

            if (isType(TypeMask.UNIT) && (Objects.equals(toUnit().getCharmerGUID(), player.getGUID()))) // @todo this is for puppet
            {
                continue;
            }
            destroyForPlayer(player);
            player.getClientGuiDs().remove(getGUID());
        }
    }

    public void updateObjectVisibility() {
        updateObjectVisibility(true);
    }

    public void updateObjectVisibility(boolean force) {
        //updates object's visibility for nearby players
        var notifier = GridVisitors.newVisibleChangesNotifier(new WorldObject[]{this});
        Cell.visitGrid(this, notifier, getVisibilityRange());
    }

    public void updateObjectVisibilityOnCreate() {
        updateObjectVisibility(true);
    }

    public void updateObjectVisibilityOnDestroy() {
        destroyForNearbyPlayers();
    }

    public void buildUpdate(HashMap<Player, UpdateData> data) {
        var notifier = GridVisitors.newWorldObjectChangeAccumulator(this, data);
        Cell.visitGrid(this, notifier, getVisibilityRange());
        clearUpdateMask(false);
    }

    public boolean addToObjectUpdate() {
        getMap().addUpdateObject(this);

        return true;
    }

    public void removeFromObjectUpdate() {
        getMap().removeUpdateObject(this);
    }

    public final String getName() {
        return name.get(worldContext.getDbcLocale());
    }

    public final boolean isTypeId(TypeId typeId) {
        return getObjectTypeId() == typeId;
    }


    public boolean hasQuest(int questId) {
        return false;
    }


    public boolean hasInvolvedQuest(int questId) {
        return false;
    }


    public int getLevelForTarget(WorldObject target) {
        return 1;
    }

    public final void addToNotify(NotifyFlag f) {
        notifyFlags.addFlag(f);
    }

    public final boolean isNeedNotify(NotifyFlag f) {
        return notifyFlags.hasFlag(f);
    }

    public final void resetAllNotifies() {
        notifyFlags.set(NotifyFlag.NONE);
    }

    public final ITransport getTransport() {
        return transport;
    }

    public final void setTransport(ITransport t) {
        transport = t;
    }

    public ObjectGuid getTransGUID() {
        if (getTransport() != null) {
            return getTransport().getTransportGUID();
        }

        return ObjectGuid.EMPTY;
    }

    public boolean isNeverVisibleFor(WorldObject seer) {
        return !isInWorld() || isDestroyedObject();
    }

    public boolean isAlwaysVisibleFor(WorldObject seer) {
        return false;
    }

    public boolean isInvisibleDueToDespawn(WorldObject seer) {
        return false;
    }

    public boolean isAlwaysDetectableFor(WorldObject seer) {
        return false;
    }


    public boolean loadFromDB(long spawnId, Map map, boolean addToMap, boolean allowDuplicate) {
        return true;
    }

    public final float getDistanceZ(WorldObject obj) {
        var dz = Math.abs(getLocation().getZ() - obj.getLocation().getZ());
        var sizefactor = getCombatReach() + obj.getCombatReach();
        var dist = dz - sizefactor;

        return (dist > 0 ? dist : 0);
    }

    public boolean _IsWithinDist(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius) {
        return _IsWithinDist(obj, dist2compare, is3D, incOwnRadius, true);
    }

    //Position

    public boolean _IsWithinDist(WorldObject obj, float dist2compare, boolean is3D) {
        return _IsWithinDist(obj, dist2compare, is3D, true, true);
    }

    public boolean _IsWithinDist(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius, boolean incTargetRadius) {
        float sizefactor = 0;
        sizefactor += incOwnRadius ? getCombatReach() : 0.0f;
        sizefactor += incTargetRadius ? obj.getCombatReach() : 0.0f;
        var maxdist = dist2compare + sizefactor;

        Position thisOrTransport = getLocation();
        Position objOrObjTransport = obj.getLocation();

        if (getTransport() != null && obj.getTransport() != null && Objects.equals(obj.getTransport().getTransportGUID(), getTransport().getTransportGUID())) {
            thisOrTransport = getMovementInfo().getTransport().getPos();
            objOrObjTransport = obj.getMovementInfo().getTransport().getPos();
        }


        if (is3D) {
            return thisOrTransport.isInDist(objOrObjTransport, maxdist);
        } else {
            return thisOrTransport.isInDist2D(objOrObjTransport, maxdist);
        }
    }

    public final float getDistance(WorldObject obj) {
        var d = getLocation().getExactDist(obj.getLocation()) - getCombatReach() - obj.getCombatReach();

        return Math.max(d, 0.0f);
    }

    public final float getDistance(Position pos) {
        var d = getLocation().getExactDist(pos) - getCombatReach();

        return Math.max(d, 0.0f);
    }

    public final float getDistance(float x, float y, float z) {
        var d = getLocation().getExactDist(x, y, z) - getCombatReach();

        return Math.max(d, 0.0f);
    }

    public final float getDistance2d(WorldObject obj) {
        var d = getLocation().getExactDist2D(obj.getLocation()) - getCombatReach() - obj.getCombatReach();
        return Math.max(d, 0.0f);
    }

    public final float getDistance2d(float x, float y) {
        var d = getLocation().getExactDist2D(x, y) - getCombatReach();
        return Math.max(d, 0.0f);
    }

    public final boolean isSelfOrInSameMap(WorldObject obj) {
        if (this == obj) {
            return true;
        }

        return isInMap(obj);
    }

    public final boolean isInMap(WorldObject obj) {
        if (obj != null) {
            return isInWorld() && obj.isInWorld() && getMap().getId() == obj.getMap().getId();
        }

        return false;
    }

    public final boolean isWithinDist3D(float x, float y, float z, float dist) {
        return getLocation().isInDist(x, y, z, dist + getCombatReach());
    }

    public final boolean isWithinDist3D(Position pos, float dist) {
        return getLocation().isInDist(pos, dist + getCombatReach());
    }

    public final boolean isWithinDist2D(float x, float y, float dist) {
        return getLocation().isInDist2D(x, y, dist + getCombatReach());
    }

    public final boolean isWithinDist2D(Position pos, float dist) {
        return getLocation().isInDist2D(pos, dist + getCombatReach());
    }

    public final boolean isWithinDist(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius) {
        return isWithinDist(obj, dist2compare, is3D, incOwnRadius, true);
    }

    public final boolean isWithinDist(WorldObject obj, float dist2compare, boolean is3D) {
        return isWithinDist(obj, dist2compare, is3D, true, true);
    }

    public final boolean isWithinDist(WorldObject obj, float dist2compare) {
        return isWithinDist(obj, dist2compare, true, true, true);
    }

    public final boolean isWithinDist(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius, boolean incTargetRadius) {
        return obj != null && _IsWithinDist(obj, dist2compare, is3D, incOwnRadius, incTargetRadius);
    }

    public final boolean isWithinDistInMap(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius) {
        return isWithinDistInMap(obj, dist2compare, is3D, incOwnRadius, true);
    }

    public final boolean isWithinDistInMap(WorldObject obj, float dist2compare, boolean is3D) {
        return isWithinDistInMap(obj, dist2compare, is3D, true, true);
    }

    public final boolean isWithinDistInMap(WorldObject obj, float dist2compare) {
        return isWithinDistInMap(obj, dist2compare, true, true, true);
    }

    public final boolean isWithinDistInMap(WorldObject obj, float dist2compare, boolean is3D, boolean incOwnRadius, boolean incTargetRadius) {
        return isInMap(obj) && inSamePhase(obj) && _IsWithinDist(obj, dist2compare, is3D, incOwnRadius, incTargetRadius);
    }

    public final boolean isWithinLOS(Position pos, LineOfSightChecks checks) {
        return isWithinLOS(pos, checks, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOS(Position pos) {
        return isWithinLOS(pos, LineOfSightChecks.ALL, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOS(Position pos, LineOfSightChecks checks, ModelIgnoreFlags ignoreFlags) {
        return isWithinLOS(pos.getX(), pos.getY(), pos.getZ(), checks, ignoreFlags);
    }

    public final boolean isWithinLOS(float ox, float oy, float oz, LineOfSightChecks checks) {
        return isWithinLOS(ox, oy, oz, checks, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOS(float ox, float oy, float oz) {
        return isWithinLOS(ox, oy, oz, LineOfSightChecks.ALL, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOS(float ox, float oy, float oz, LineOfSightChecks checks, ModelIgnoreFlags ignoreFlags) {
        if (isInWorld()) {
            oz += getCollisionHeight();
            var pos = new Position();

            if (isTypeId(TypeId.PLAYER)) {
                pos = new Position(location);
                pos.setZ(pos.getZ() + getCollisionHeight());
            } else {
                getHitSpherePointFor(new Position(ox, oy, oz), pos);
            }

            return getMap().isInLineOfSight(getPhaseShift(), pos, Position.of(ox, oy, oz), checks, ignoreFlags);
        }

        return true;
    }

    public final boolean isWithinLOSInMap(WorldObject obj, LineOfSightChecks checks) {
        return isWithinLOSInMap(obj, checks, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOSInMap(WorldObject obj) {
        return isWithinLOSInMap(obj, LineOfSightChecks.ALL, ModelIgnoreFlags.Nothing);
    }

    public final boolean isWithinLOSInMap(WorldObject obj, LineOfSightChecks checks, ModelIgnoreFlags ignoreFlags) {
        if (!isInMap(obj)) {
            return false;
        }

        var pos = new Position();

        if (obj.isTypeId(TypeId.PLAYER)) {
            pos = new Position(location);
            pos.setZ(pos.getZ() + getCollisionHeight());
        } else {
            obj.getHitSpherePointFor(new Position(getLocation().getX(), getLocation().getY(), getLocation().getZ() + getCollisionHeight()), pos);
        }

        var pos2 = new Position();

        if (isPlayer()) {
            pos2 = new Position(location);
            pos2.setZ(pos2.getZ() + getCollisionHeight());
        } else {
            getHitSpherePointFor(new Position(obj.getLocation().getX(), obj.getLocation().getY(), obj.getLocation().getZ() + obj.getCollisionHeight()), pos2);
        }

        return getMap().isInLineOfSight(getPhaseShift(), pos2, pos, checks, ignoreFlags);
    }

    public final Position getHitSpherePointFor(Position dest) {

        Vector3 vThis = new Vector3(getLocation().getX(), getLocation().getY(), getLocation().getZ() + getCollisionHeight());
        Vector3 vObj = new Vector3(dest.getX(), dest.getY(), dest.getZ());
        var contactPoint = vThis.add(vObj.sub(vThis).nor().scl(Math.min(dest.getExactDist(getLocation()), getCombatReach())));
        return new Position(contactPoint.x, contactPoint.y, contactPoint.z, getLocation().getAbsoluteAngle(contactPoint.x, contactPoint.y));
    }

    public final void getHitSpherePointFor(Position dest, Position refDest) {
        var pos = getHitSpherePointFor(dest);
        refDest.setX(pos.getX());
        refDest.setY(pos.getY());
        refDest.setZ(pos.getZ());
    }

    public final boolean getDistanceOrder(WorldObject obj1, WorldObject obj2) {
        return getDistanceOrder(obj1, obj2, true);
    }

    public final boolean getDistanceOrder(WorldObject obj1, WorldObject obj2, boolean is3D) {
        var dx1 = getLocation().getX() - obj1.getLocation().getX();
        var dy1 = getLocation().getY() - obj1.getLocation().getY();
        var distsq1 = dx1 * dx1 + dy1 * dy1;

        if (is3D) {
            var dz1 = getLocation().getZ() - obj1.getLocation().getZ();
            distsq1 += dz1 * dz1;
        }

        var dx2 = getLocation().getX() - obj2.getLocation().getX();
        var dy2 = getLocation().getY() - obj2.getLocation().getY();
        var distsq2 = dx2 * dx2 + dy2 * dy2;

        if (is3D) {
            var dz2 = getLocation().getZ() - obj2.getLocation().getZ();
            distsq2 += dz2 * dz2;
        }

        return distsq1 < distsq2;
    }

    public final boolean isInRange(WorldObject obj, float minRange, float maxRange) {
        return isInRange(obj, minRange, maxRange, true);
    }

    public final boolean isInRange(WorldObject obj, float minRange, float maxRange, boolean is3D) {
        var dx = getLocation().getX() - obj.getLocation().getX();
        var dy = getLocation().getY() - obj.getLocation().getY();
        var distsq = dx * dx + dy * dy;

        if (is3D) {
            var dz = getLocation().getZ() - obj.getLocation().getZ();
            distsq += dz * dz;
        }

        var sizefactor = getCombatReach() + obj.getCombatReach();

        // check only for real range
        if (minRange > 0.0f) {
            var mindist = minRange + sizefactor;

            if (distsq < mindist * mindist) {
                return false;
            }
        }

        var maxdist = maxRange + sizefactor;

        return distsq < maxdist * maxdist;
    }

    public final boolean isInBetween(WorldObject obj1, WorldObject obj2) {
        return isInBetween(obj1, obj2, 0);
    }

    public final boolean isInBetween(WorldObject obj1, WorldObject obj2, float size) {
        return obj1 != null && obj2 != null && isInBetween(obj1.getLocation(), obj2.getLocation(), size);
    }

    public final boolean isInFront(WorldObject target) {
        return isInFront(target, MathUtil.PI);
    }

    public final boolean isInFront(WorldObject target, float arc) {
        return getLocation().hasInArc(arc, target.getLocation());
    }

    public final boolean isInBack(WorldObject target) {
        return isInBack(target, MathUtil.PI);
    }

    public final boolean isInBack(WorldObject target, float arc) {
        return !getLocation().hasInArc(2 * MathUtil.PI - arc, target.getLocation());
    }


    public final Position getRandomPoint(Position srcPos, float distance) {

        if (distance == 0) {

            return new Position(srcPos.getX(), srcPos.getY(), srcPos.getZ(), getLocation().getO());
        }
        Position randomPoint = new Position();

        // angle to face `obj` to `this`
        var angle = RandomUtil.randomFloat() * (2 * MathUtil.PI);
        var new_dist = RandomUtil.randomFloat() + RandomUtil.randomFloat();
        new_dist = distance * (new_dist > 1 ? new_dist - 2 : new_dist);

        float x = (float) (srcPos.getX() + new_dist * Math.cos(angle));
        float y = (float) (srcPos.getY() + new_dist * Math.sin(angle));
        float z = srcPos.getZ();

        x = MapDefine.normalizeMapCoordinate(x);
        y = MapDefine.normalizeMapCoordinate(y);
        z = updateGroundPositionZ(x, y, z); // update to LOS height if available


        randomPoint.setX(x);
        randomPoint.setY(y);
        randomPoint.setZ(z);
        randomPoint.setO(getLocation().getO());
        return randomPoint;
    }

    public final float updateGroundPositionZ(float x, float y, float z) {
        var newZ = getMapHeight(x, y, z);

        if (newZ > MapDefine.INVALID_HEIGHT) {
            z = newZ + (isUnit() ? toUnit().getHoverOffset() : 0.0f);
        }

        return z;
    }

    public final float updateAllowedPositionZ(float x, float y, float z) {
        return updateAllowedPositionZ(x, y, z, null);
    }

    public final float updateAllowedPositionZ(float x, float y, float z, YieldResult<Float> groundZ) {
        Position newPos = Position.of(x, y, z);
        updateAllowedPositionZ(newPos, groundZ);
        return newPos.getZ();
    }

    public final void updateAllowedPositionZ(Position pos) {
        updateAllowedPositionZ(pos, null);
    }


    public final void updateAllowedPositionZ(Position pos, YieldResult<Float> groundZ) {
        // TODO: Allow transports to be part of dynamic vmap tree
        if (getTransport() != null) {
            if(groundZ != null) {
                groundZ.set(pos.getZ());
            }
            return;
        }


        if (this instanceof Unit unit) {
            if (!unit.canFly()) {
                var canSwim = unit.canSwim();
                var ground_z = pos.getZ();
                float max_z;

                if (canSwim) {
                    max_z = getMapWaterOrGroundLevel(pos, groundZ);
                } else {
                    max_z = ground_z = getMapHeight(pos.getX(), pos.getY(), pos.getZ());
                }

                if (max_z > MapDefine.INVALID_HEIGHT) {
                    // hovering units cannot go below their hover height
                    var hoverOffset = unit.getHoverOffset();
                    max_z += hoverOffset;
                    ground_z += hoverOffset;

                    if (pos.getZ() > max_z) {
                        pos.setZ(max_z);
                    } else if (pos.getZ() < ground_z) {
                        pos.setZ(ground_z);
                    }
                }
                if(groundZ != null) {
                    groundZ.set(ground_z);
                }
            } else {
                var ground_z = getMapHeight(pos.getX(), pos.getY(), pos.getZ()) + unit.getHoverOffset();

                if (pos.getZ() < ground_z) {
                    pos.setZ(ground_z);
                }

                if(groundZ != null) {
                    groundZ.set(ground_z);
                }
            }
        } else {
            var ground_z = getMapHeight(pos.getX(), pos.getY(), pos.getZ());

            if (ground_z > MapDefine.INVALID_HEIGHT) {
                pos.setZ(ground_z);
            }

            if(groundZ != null) {
                groundZ.set(ground_z);
            }
        }

    }

    public final Position getNearPoint2D(WorldObject searcher, float distance2d, float absAngle) {
        var effectiveReach = getCombatReach();

        if (searcher != null) {
            effectiveReach += searcher.getCombatReach();

            if (this != searcher) {
                var myHover = 0.0f;
                var searcherHover = 0.0f;

                var unit = toUnit();

                if (unit != null) {
                    myHover = unit.getHoverOffset();
                }

                var searchUnit = searcher.toUnit();

                if (searchUnit != null) {
                    searcherHover = searchUnit.getHoverOffset();
                }

                var hoverDelta = myHover - searcherHover;

                if (hoverDelta != 0.0f) {
                    effectiveReach = (float) Math.sqrt(Math.max(effectiveReach * effectiveReach - hoverDelta * hoverDelta, 0.0f));
                }
            }
        }

        float x = getLocation().getX() + (effectiveReach + distance2d) * (float) Math.cos(absAngle);
        float y = getLocation().getY() + (effectiveReach + distance2d) * (float) Math.sin(absAngle);

        x = MapDefine.normalizeMapCoordinate(x);
        y = MapDefine.normalizeMapCoordinate(y);

        return new Position(x, y);
    }

    public final void getNearPoint(WorldObject searcher, Position pos, float distance2d, float absAngle) {
        Position nearPoint2D = getNearPoint2D(searcher, distance2d, absAngle);
        var x = nearPoint2D.getX();
        var y = nearPoint2D.getY();
        pos.setX(x);
        pos.setY(y);
        pos.setZ(getLocation().getZ());

        pos.setZ((searcher != null ? searcher : this).updateAllowedPositionZ(x, y, pos.getZ()));


        // if detection disabled, return first point
        if (!worldContext.getWorldSettings().detectPosCollision) {
            return;
        }

        // return if the point is already in LoS
        if (isWithinLOS(pos.getX(), pos.getY(), pos.getZ())) {
            return;
        }

        // remember first point
        var first_x = pos.getX();
        var first_y = pos.getY();
        var first_z = pos.getZ();

        // loop in a circle to look for a point in LoS using small steps
        for (var angle = MathUtil.PI / 8; angle < Math.PI * 2; angle += MathUtil.PI / 8) {


            Position nearPoint2D1 = getNearPoint2D(searcher, distance2d, absAngle + angle);
            pos.setX(nearPoint2D1.getX());
            pos.setY(nearPoint2D1.getY());
            pos.setZ(getLocation().getZ());

            (searcher != null ? searcher : this).updateAllowedPositionZ(pos);

            if (isWithinLOS(pos.getX(), pos.getY(), pos.getZ())) {
                return;
            }
        }

        // still not in LoS, give up and return first position found
        pos.setX(first_x);
        pos.setY(first_y);
        pos.setZ(first_z);
    }

    public final void getClosePoint(Position pos, float size, float distance2d) {
        getClosePoint(pos, size, distance2d, 0);
    }

    public final void getClosePoint(Position pos, float size) {
        getClosePoint(pos, size, 0, 0);
    }

    public final void getClosePoint(Position pos, float size, float distance2d, float relAngle) {
        // angle calculated from current orientation
        getNearPoint(null, pos, distance2d + size, getLocation().getO() + relAngle);
    }

    public final Position getNearPosition(float dist, float angle) {
        var pos = getLocation();
        movePosition(pos, dist, angle);

        return pos;
    }

    public final Position getFirstCollisionPosition(float dist, float angle) {
        var pos = new Position(getLocation());
        movePositionToFirstCollision(pos, dist, angle);

        return pos;
    }

    public final Position getRandomNearPosition(float radius) {
        var pos = getLocation();
        movePosition(pos, radius * (float) RandomUtil.randomFloat(), (float) RandomUtil.randomFloat() * MathUtil.PI * 2);

        return pos;
    }


    public final void getContactPoint(WorldObject obj, Position pos) {
        getContactPoint(obj, pos, 0.5f);
    }

    public final void getContactPoint(WorldObject obj, Position pos, float distance2d) {
        // angle to face `obj` to `this` using distance includes size of `obj`
        getNearPoint(obj, pos, distance2d, getLocation().getAbsoluteAngle(obj.getLocation()));
    }

    public final void movePosition(Position pos, float dist, float angle) {
        angle += getLocation().getO();
        var destx = pos.getX() + dist * (float) Math.cos(angle);
        var desty = pos.getY() + dist * (float) Math.sin(angle);

        // Prevent invalid coordinates here, position is unchanged
        if (!MapDefine.isValidMapCoordinate(destx, desty, pos.getZ())) {
            Logs.MISC.error("WorldObject::MovePosition: Object {} has invalid coordinates X: {} and Y: {} were passed!",
                    getGUID(), destx, desty);

            return;
        }

        var ground = getMapHeight(destx, desty, MapDefine.MAX_HEIGHT);
        var floor = getMapHeight(destx, desty, pos.getZ());
        var destz = Math.abs(ground - pos.getZ()) <= Math.abs(floor - pos.getZ()) ? ground : floor;

        var step = dist / 10.0f;

        for (byte j = 0; j < 10; ++j) {
            // do not allow too big z changes
            if (Math.abs(pos.getZ() - destz) > 6) {
                destx -= step * (float) Math.cos(angle);
                desty -= step * (float) Math.sin(angle);
                ground = getMap().getHeight(getPhaseShift(), Position.of(destx, desty, MapDefine.MAX_HEIGHT), true);
                floor = getMap().getHeight(getPhaseShift(), Position.of(destx, desty, pos.getZ()), true);
                destz = Math.abs(ground - pos.getZ()) <= Math.abs(floor - pos.getZ()) ? ground : floor;
            }
            // we have correct destz now
            else {
                pos.relocate(destx, desty, destz);

                break;
            }
        }

        pos.setX(MapDefine.normalizeMapCoordinate(pos.getX()));
        pos.setY(MapDefine.normalizeMapCoordinate(pos.getY()));
        pos.setZ(updateGroundPositionZ(pos.getX(), pos.getY(), pos.getZ()));
        pos.setO(getLocation().getO());
    }

    public final void movePositionToFirstCollision(Position pos, float dist, float angle) {
        angle += getLocation().getO();
        var destx = pos.getX() + dist * (float) Math.cos(angle);
        var desty = pos.getY() + dist * (float) Math.sin(angle);
        var destz = pos.getZ();

        // Prevent invalid coordinates here, position is unchanged
        if (!MapDefine.isValidMapCoordinate(destx, desty)) {
            Logs.MISC.error("WorldObject::MovePositionToFirstCollision invalid coordinates X: {} and Y: {} were passed!", destx, desty);

            return;
        }

        // Use a detour raycast to get our first collision point
        PathGenerator path = new PathGenerator(this);
        path.setUseRaycast(true);
        path.calculatePath(Position.of(destx, desty, destz), false);

        // We have a invalid path result. Skip further processing.
        if (!path.getPathType().hasFlag(PathType.NOT_USING_PATH)) {
            if ((path.getPathType().hasNotFlag(PathType.NORMAL, PathType.SHORTCUT, PathType.INCOMPLETE, PathType.FARFROMPOLY_END))) {
                return;
            }
        }

        var result = path.getPath()[path.getPath().length - 1];
        destx = result.x;
        desty = result.y;
        destz = result.z;

        // check static LOS
        var halfHeight = getCollisionHeight() * 0.5f;
        var col = false;

        Position outXYZ = new Position();
        Position xyz1 = new Position(pos.getX(), pos.getY(), pos.getZ() + halfHeight);
        Position xyz2 = new Position(destx, desty, destz + halfHeight);
        // Unit is flying, check for potential collision via vmaps
        if (path.getPathType().hasFlag(PathType.NOT_USING_PATH)) {
            int terrainMapId = PhasingHandler.getTerrainMapId(getPhaseShift(), getLocation().getMapId(), getMap().getTerrain(), pos.getX(), pos.getY());
            col = worldContext.getVMapManager().getObjectHitPos(terrainMapId, xyz1, xyz2, outXYZ, -0.5f);
            destz = outXYZ.getZ();
            desty = outXYZ.getY();
            destx = outXYZ.getX();

            destz -= halfHeight;

            // Collided with static LOS object, move back to collision point
            if (col) {
                destx -= ObjectDefine.CONTACT_DISTANCE * (float) Math.cos(angle);
                desty -= ObjectDefine.CONTACT_DISTANCE * (float) Math.sin(angle);
                dist = (float) Math.sqrt((pos.getX() - destx) * (pos.getX() - destx) + (pos.getY() - desty) * (pos.getY() - desty));
            }
        }

        // check dynamic collision

        col = getMap().getObjectHitPos(getPhaseShift(), xyz1, xyz2, outXYZ, -0.5f);
        destz = outXYZ.getZ();
        desty = outXYZ.getY();
        destx = outXYZ.getX();

        destz -= halfHeight;

        // Collided with a gameobject, move back to collision point
        if (col) {
            destx -= ObjectDefine.CONTACT_DISTANCE * (float) Math.cos(angle);
            desty -= ObjectDefine.CONTACT_DISTANCE * (float) Math.sin(angle);
            dist = (float) Math.sqrt((pos.getX() - destx) * (pos.getX() - destx) + (pos.getY() - desty) * (pos.getY() - desty));
        }

        pos.setX(MapDefine.normalizeMapCoordinate(pos.getX()));
        pos.setY(MapDefine.normalizeMapCoordinate(pos.getY()));

        YieldResult<Float> groundZ = YieldResult.of(MapDefine.VMAP_INVALID_HEIGHT_VALUE);
        destz = updateAllowedPositionZ(destx, desty, destz, groundZ);


        pos.setO(getLocation().getO());
        pos.relocate(destx, desty, destz);

        // position has no ground under it (or is too far away)
        if (groundZ.get() <= MapDefine.INVALID_HEIGHT) {
            var unit = toUnit();

            if (unit != null) {
                // unit can fly, ignore.
                if (unit.canFly()) {
                    return;
                }

                // fall back to gridHeight if any
                var gridHeight = getMap().getGridHeight(getPhaseShift(), pos.getX(), pos.getY());

                if (gridHeight > MapDefine.INVALID_HEIGHT) {
                    pos.setZ(gridHeight + unit.getHoverOffset());
                }
            }
        }
    }

    public final float getMapWaterOrGroundLevel(Position position) {
        return getMapWaterOrGroundLevel(position, null);
    }

    public final float getMapWaterOrGroundLevel(Position position, YieldResult<Float> ground) {

        boolean swimming = true;
        Creature c = toCreature();
        Unit u = toUnit();
        if (c != null) {
            swimming = (!c.cannotPenetrateWater() && !c.hasAuraType(AuraType.WATER_WALK));
        } else if (u != null) {
            swimming = !u.hasAuraType(AuraType.WATER_WALK);
        }

        return getMap().getWaterOrGroundLevel(getPhaseShift(), position, ground, swimming, getCollisionHeight());
    }

    public final float getMapHeight(float x, float y, float z, boolean vmap) {
        return getMapHeight(x, y, z, vmap, MapDefine.DEFAULT_HEIGHT_SEARCH);
    }

    public final float getMapHeight(float x, float y, float z) {
        return getMapHeight(x, y, z, true, MapDefine.DEFAULT_HEIGHT_SEARCH);
    }

    public final float getMapHeight(float x, float y, float z, boolean vmap, float distanceToSearch) {

        if (z != MapDefine.MAX_HEIGHT) {
            z += SharedDefine.Z_OFFSET_FIND_HEIGHT;
        }
        return getMap().getHeight(getPhaseShift(), Position.of(x, y, z), vmap, distanceToSearch);
    }



    public final void setLocationInstanceId(int instanceId) {
        this.instanceId = instanceId;
    }

    private boolean canDetect(WorldObject obj, boolean ignoreStealth) {
        return canDetect(obj, ignoreStealth, false);
    }

    private boolean canDetect(WorldObject obj, boolean ignoreStealth, boolean checkAlert) {
        var seer = this;

        // If a unit is possessing another one, it uses the detection of the latter
        // Pets don't have detection, they use the detection of their masters
        var thisUnit = toUnit();

        if (thisUnit != null) {
            if (thisUnit.isPossessing()) {
                var charmed = thisUnit.getCharmed();

                if (charmed != null) {
                    seer = charmed;
                }
            } else {
                var controller = thisUnit.getCharmerOrOwner();

                if (controller != null) {
                    seer = controller;
                }
            }
        }

        if (obj.isAlwaysDetectableFor(seer)) {
            return true;
        }

        if (!ignoreStealth && !seer.canDetectInvisibilityOf(obj)) {
            return false;
        }

        if (!ignoreStealth && !seer.canDetectStealthOf(obj, checkAlert)) {
            return false;
        }

        return true;
    }


    private boolean canDetectInvisibilityOf(WorldObject obj) {
        var mask = obj.getInvisibility().getFlags() & getInvisibilityDetect().getFlags();

        // Check for not detected types
        if (mask != obj.getInvisibility().getFlags()) {
            return false;
        }

        for (InvisibilityType value : InvisibilityType.values()) {
            if ((mask & (1 << value.ordinal())) == 0) {
                continue;
            }

            var objInvisibilityValue = obj.getInvisibility().getValue(value);
            var ownInvisibilityDetectValue = getInvisibilityDetect().getValue(value);

            // Too low value to detect
            if (ownInvisibilityDetectValue < objInvisibilityValue) {
                return false;
            }
        }


        return true;
    }

    private boolean canDetectStealthOf(WorldObject obj) {
        return canDetectStealthOf(obj, false);
    }

    private boolean canDetectStealthOf(WorldObject obj, boolean checkAlert) {
        // Combat reach is the minimal distance (both in front and behind),
        //   and it is also used in the range calculation.
        // One stealth point increases the visibility range by 0.3 yard.

        if (obj.getStealth().getFlags() == 0) {
            return true;
        }

        var distance = getLocation().getExactDist(obj.getLocation());
        var combatReach = getCombatReach();

        if (distance < combatReach) {
            return true;
        }

        // Only check back for units, it does not make sense for gameobjects
        if (!getLocation().hasInArc(MathUtil.PI, obj.getLocation())) {
            return false;
        }

        // Traps should detect stealth always
        var go = toGameObject();

        if (go != null) {
            if (go.getGoType() == GameObjectType.TRAP) {
                return true;
            }
        }

        go = obj.toGameObject();


        for (StealthType stealthType : StealthType.values()) {
            if ((obj.getStealth().getFlags() & (1 << stealthType.ordinal())) == 0) {
                continue;
            }

            if (this instanceof Unit unit && unit.hasAuraTypeWithMiscvalue(AuraType.DETECT_STEALTH, stealthType.ordinal())) {
                return true;
            }

            // Starting points
            var detectionValue = 30;

            // Level difference: 5 point / level, starting from level 1.
            // There may be spells for this and the starting points too, but
            // not in the DBCs of the client.
            detectionValue += (int) (getLevelForTarget(obj) - 1) * 5;

            // Apply modifiers
            detectionValue += getStealthDetect().getValue(stealthType);

            if (go != null) {
                var owner = go.getOwnerUnit();

                if (owner != null) {
                    detectionValue -= (owner.getLevelForTarget(this) - 1) * 5;
                }
            }

            detectionValue -= obj.getStealth().getValue(stealthType);

            // Calculate max distance
            var visibilityRange = detectionValue * 0.3f + combatReach;

            // If this unit is an NPC then player detect range doesn't apply
            if (isTypeId(TypeId.PLAYER) && visibilityRange > ObjectDefine.MAX_PLAYER_STEALTH_DETECT_RANGE) {
                visibilityRange = ObjectDefine.MAX_PLAYER_STEALTH_DETECT_RANGE;
            }

            // When checking for alert state, look 8% further, and then 1.5 yards more than that.
            if (checkAlert) {
                visibilityRange += (visibilityRange * 0.08f) + 1.5f;
            }

            // If checking for alert, and creature's visibility range is greater than aggro distance, No alert
            var tunit = obj.toUnit();

            if (this instanceof Creature creature && visibilityRange >= creature.getAttackDistance(tunit) + creature.getCombatDistance()) {
                return false;
            }

            if (distance > visibilityRange) {
                return false;
            }
        }

        return true;
    }

    private SpellMissInfo magicSpellHitResult(Unit victim, SpellInfo spellInfo) {
        // Can`t miss on dead target (on skinning for example)
        if (!victim.isAlive() && !victim.isPlayer()) {
            return SpellMissInfo.NONE;
        }

        if (spellInfo.hasAttribute(SpellAttr3.NO_AVOIDANCE)) {
            return SpellMissInfo.NONE;
        }

        double missChance;

        if (spellInfo.hasAttribute(SpellAttr7.NO_ATTACK_MISS)) {
            missChance = 0.0f;
        } else {
            var schoolMask = spellInfo.getSchoolMask();
            // PvP - PvE spell misschances per leveldif > 2
            var lchance = victim.isPlayer() ? 7 : 11;
            var thisLevel = getLevelForTarget(victim);

            if (isCreature() && toCreature().isTrigger()) {
                thisLevel = Math.max(thisLevel, spellInfo.getSpellLevel());
            }

            var leveldif = victim.getLevelForTarget(this) - thisLevel;
            var levelBasedHitDiff = leveldif;

            // Base hit chance from attacker and victim levels
            float modHitChance = 100;

            if (levelBasedHitDiff >= 0) {
                if (!victim.isPlayer()) {
                    modHitChance = 94 - 3 * Math.min(levelBasedHitDiff, 3);
                    levelBasedHitDiff -= 3;
                } else {
                    modHitChance = 96 - Math.min(levelBasedHitDiff, 2);
                    levelBasedHitDiff -= 2;
                }

                if (levelBasedHitDiff > 0) {
                    modHitChance -= lchance * Math.min(levelBasedHitDiff, 7);
                }
            } else {
                modHitChance = 97 - levelBasedHitDiff;
            }

            // Spellmod from SpellModOp::hitChance
            var modOwner = getSpellModOwner();

            if (modOwner != null) {
                modHitChance = modOwner.applySpellMod(spellInfo, SpellModOp.HitChance, modHitChance);
            }

            // Spells with SPELL_ATTR3_IGNORE_HIT_RESULT will ignore target's avoidance effects
            if (!spellInfo.hasAttribute(SpellAttr3.ALWAYS_HIT)) {
                // Chance hit from victim SPELL_AURA_MOD_ATTACKER_SPELL_HIT_CHANCE auras
                modHitChance += victim.getTotalAuraModifierByMiscMask(AuraType.MOD_ATTACKER_SPELL_HIT_CHANCE, schoolMask);
            }

            var hitChance = modHitChance;
            // Increase hit chance from attacker SPELL_AURA_MOD_SPELL_HIT_CHANCE and attacker ratings
            var unit = toUnit();

            if (unit != null) {
                hitChance += unit.getModSpellHitChance();
            }

            hitChance = MathUtil.roundToInterval(hitChance, 0.0f, 100.0f);

            missChance = 100.0f - hitChance;
        }

        var tmp = missChance * 100.0f;

        var rand = RandomUtil.randomInt(0, 10000);

        if (tmp > 0 && rand < tmp) {
            return SpellMissInfo.MISS;
        }

        // Chance resist mechanic (select max value from every mechanic spell effect)
        var resist_chance = victim.getMechanicResistChance(spellInfo) * 100;

        // Roll chance
        if (resist_chance > 0 && rand < (tmp += resist_chance)) {
            return SpellMissInfo.RESIST;
        }

        // cast by caster in front of victim
        if (!victim.hasUnitState(UnitState.CONTROLLED) && (victim.getLocation().hasInArc(MathUtil.PI, getLocation()) || victim.hasAuraType(AuraType.IGNORE_HIT_DIRECTION))) {
            var deflect_chance = victim.getTotalAuraModifier(AuraType.DEFLECT_SPELLS) * 100;

            if (deflect_chance > 0 && rand < (tmp += deflect_chance)) {
                return SpellMissInfo.DEFLECT;
            }
        }

        return SpellMissInfo.NONE;
    }


    private void sendCancelSpellVisual(int id) {
        CancelSpellVisual cancelSpellVisual = new CancelSpellVisual();
        cancelSpellVisual.source = getGUID();
        cancelSpellVisual.spellVisualID = id;
        sendMessageToSet(cancelSpellVisual, true);
    }

    private void sendPlayOrphanSpellVisual(ObjectGuid target, int spellVisualId, float travelSpeed, boolean speedAsTime) {
        sendPlayOrphanSpellVisual(target, spellVisualId, travelSpeed, speedAsTime, false);
    }

    private void sendPlayOrphanSpellVisual(ObjectGuid target, int spellVisualId, float travelSpeed) {
        sendPlayOrphanSpellVisual(target, spellVisualId, travelSpeed, false, false);
    }

    private void sendPlayOrphanSpellVisual(ObjectGuid target, int spellVisualId, float travelSpeed, boolean speedAsTime, boolean withSourceOrientation) {
        PlayOrphanSpellVisual playOrphanSpellVisual = new PlayOrphanSpellVisual();
        playOrphanSpellVisual.sourceLocation = getLocation();

        if (withSourceOrientation) {
            if (isGameObject()) {
                var rotation = toGameObject().getWorldRotation();
                rotation.toEulerAnglesZYX(playOrphanSpellVisual.sourceRotation);
            } else {
                playOrphanSpellVisual.sourceRotation = new Vector3(0.0f, 0.0f, getLocation().getO());
            }
        }

        playOrphanSpellVisual.target = target; // exclusive with TargetLocation
        playOrphanSpellVisual.spellVisualID = spellVisualId;
        playOrphanSpellVisual.travelSpeed = travelSpeed;
        playOrphanSpellVisual.speedAsTime = speedAsTime;
        playOrphanSpellVisual.launchDelay = 0.0f;
        sendMessageToSet(playOrphanSpellVisual, true);
    }

    private void sendPlayOrphanSpellVisual(Position targetLocation, int spellVisualId, float travelSpeed, boolean speedAsTime) {
        sendPlayOrphanSpellVisual(targetLocation, spellVisualId, travelSpeed, speedAsTime, false);
    }

    private void sendPlayOrphanSpellVisual(Position targetLocation, int spellVisualId, float travelSpeed) {
        sendPlayOrphanSpellVisual(targetLocation, spellVisualId, travelSpeed, false, false);
    }

    private void sendPlayOrphanSpellVisual(Position targetLocation, int spellVisualId, float travelSpeed, boolean speedAsTime, boolean withSourceOrientation) {
        PlayOrphanSpellVisual playOrphanSpellVisual = new PlayOrphanSpellVisual();
        playOrphanSpellVisual.sourceLocation = getLocation();

        if (withSourceOrientation) {
            if (isGameObject()) {
                var rotation = toGameObject().getWorldRotation();
                rotation.toEulerAnglesZYX(playOrphanSpellVisual.sourceRotation);
            } else {
                playOrphanSpellVisual.sourceRotation = new Vector3(0.0f, 0.0f, getLocation().getO());
            }
        }

        playOrphanSpellVisual.targetLocation = targetLocation.toVector3(); // exclusive with Target
        playOrphanSpellVisual.spellVisualID = spellVisualId;
        playOrphanSpellVisual.travelSpeed = travelSpeed;
        playOrphanSpellVisual.speedAsTime = speedAsTime;
        playOrphanSpellVisual.launchDelay = 0.0f;
        sendMessageToSet(playOrphanSpellVisual, true);
    }


    private void sendCancelOrphanSpellVisual(int id) {
        CancelOrphanSpellVisual cancelOrphanSpellVisual = new CancelOrphanSpellVisual();
        cancelOrphanSpellVisual.spellVisualID = id;
        sendMessageToSet(cancelOrphanSpellVisual, true);
    }


    private void sendCancelSpellVisualKit(int id) {
        CancelSpellVisualKit cancelSpellVisualKit = new CancelSpellVisualKit();
        cancelSpellVisualKit.source = getGUID();
        cancelSpellVisualKit.spellVisualKitID = id;
        sendMessageToSet(cancelSpellVisualKit, true);
    }

    private boolean isInBetween(Position pos1, Position pos2, float size) {
        var dist = getLocation().getExactDist2D(pos1);

        // not using sqrt() for performance
        if ((dist * dist) >= pos1.getExactDist2DSq(pos2)) {
            return false;
        }

        if (size == 0) {
            size = getCombatReach() / 2;
        }

        var angle = pos1.getAbsoluteAngle(pos2);

        // not using sqrt() for performance
        return (size * size) >= getLocation().getExactDist2DSq(pos1.getX() + (float) Math.cos(angle) * dist, pos1.getY() + (float) Math.sin(angle) * dist);
    }


}
