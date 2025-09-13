package com.github.azeroth.game.entity.creature;


import com.github.azeroth.common.Logs;
import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.domain.SandboxScaling;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.ai.AISelector;
import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.ai.IUnitAI;
import com.github.azeroth.game.domain.creature.*;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.map.PositionFullTerrainStatus;
import com.github.azeroth.game.domain.map.enums.LiquidHeaderTypeFlag;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.object.WorldLocation;
import com.github.azeroth.game.domain.spawn.RespawnInfo;
import com.github.azeroth.game.domain.unit.*;
import com.github.azeroth.game.entity.object.*;
import com.github.azeroth.game.domain.object.enums.CellMoveState;
import com.github.azeroth.game.domain.object.enums.HighGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.map.*;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.spell.SpellEffectInfo;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.world.setting.WorldSetting;
import com.github.azeroth.utils.MathUtil;
import com.github.azeroth.utils.RandomUtil;

import game.PhasingHandler;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;



@Getter
@Setter
public class Creature extends Unit implements GirdObject {

    private final MultiMap<Byte, Byte> textRepeat = new MultiMap<Byte, Byte>();
    private final Position homePosition;
    private final Position transportHomeposition = new Position();
    // vendor items
    private final ArrayList<VendorItemCount> vendorItemCounts = new ArrayList<>();
    private final String[] stringIds = new String[3];
    private final SpellFocusInfo spellFocusInfo = new SpellFocusInfo();
    int nodeId;
    int pathId;
    private String scriptStringId;
    // Regenerate health
    private boolean regenerateHealth; // Set on creation
    private boolean isMissingCanSwimFlagOutOfCombat;
    private boolean alreadyCallAssistance;

    private int cannotReachTimer;
    private SpellSchoolMask meleeDamageSchoolMask = SpellSchoolMask.NONE;
    private LootMode lootMode = LootMode.DEFAULT; // Bitmask (default: LOOT_MODE_DEFAULT) that determines what loot will be lootable
    private boolean triggerJustAppeared;
    // Timers
    private long pickpocketLootRestore;
    private boolean ignoreCorpseDecayRatio;

    private int boundaryCheckTime; // (msecs) remaining time for next evade boundary check

    private int combatPulseTime; // (msecs) remaining time for next zone-in-combat pulse

    private int combatPulseDelay; // (secs) how often the creature puts the entire zone in combat (only works in dungeons)

    private Integer gossipMenuId = null;

    private Integer lootid = null;

    private long playerDamageReq;
    private float sightDistance;
    private float combatDistance;
    private boolean isTempWorldObject;

    private int originalEntry;
    private HashMap<ObjectGuid, Loot> personalLoot = new HashMap<ObjectGuid, Loot>();
    private MovementGeneratorType defaultMovementType = MovementGeneratorType.values()[0];

    private int spawnId;
    private StaticCreatureFlags staticFlags = new StaticCreatureFlags();

    private int[] spells = new int[SharedConst.MaxCreatureSpells];
    private long corpseRemoveTime;
    private Loot loot;
    private HashSet<ObjectGuid> tapList = new HashSet<ObjectGuid>();

    private int corpseDelay;
    private ReactState reactState = ReactState.values()[0];
    private byte originalEquipmentId;

    private byte currentEquipmentId;
    private CreatureTemplate creatureTemplate;
    private CreatureData creatureData;
    private boolean isReputationGainDisabled;
    private boolean cannotReachTarget;
    // Part of Evade mechanics
    private long lastDamagedTime;
    private boolean hasSearchedAssistance;
    private long respawnTime;

    private int respawnDelay;
    private float wanderDistance;

    private int waypointPath;
    private CreatureGroup formation;
    // There's many places not ready for dynamic spawns. This allows them to live on for now.
    private boolean respawnCompatibilityMode;

    private Cell currentCell;
    private CellMoveState moveState;
    private Position newPosition = new Position();

    CreatureDifficulty creatureDifficulty;

    public Creature() {
        this(false);
    }

    public Creature(boolean worldObject) {
        super(worldObject);
        setRespawnDelay(300);
        setCorpseDelay(60);
        boundaryCheckTime = 2500;
        setReactState(ReactState.Aggressive);
        setDefaultMovementType(MovementGeneratorType.IDLE);
        regenerateHealth = true;
        meleeDamageSchoolMask = SpellSchoolMask.NORMAL;
        triggerJustAppeared = true;

        setRegenTimer(CreatureTemplate.CREATURE_REGEN_INTERVAL);

        setSightDistance(ObjectDefine.SIGHT_RANGE_UNIT);

        resetLootMode(); // restore default loot mode

        homePosition = new WorldLocation();

        _currentWaypointNodeInfo = new ValueTuple<Integer, Integer>();
    }

    public static Creature createCreature(int entry, Map map, Position pos) {
        return createCreature(entry, map, pos, 0);
    }

    public static Creature createCreature(int entry, Map map, Position pos, int vehId) {
        var cInfo = global.getObjectMgr().getCreatureTemplate(entry);

        if (cInfo == null) {
            return null;
        }

        long lowGuid;

        if (vehId != 0 || cInfo.vehicleId != 0) {
            lowGuid = map.generateLowGuid(HighGuid.Vehicle);
        } else {
            lowGuid = map.generateLowGuid(HighGuid.Creature);
        }

        Creature creature = new Creature();

        if (!creature.create(lowGuid, map, entry, pos, null, vehId)) {
            return null;
        }

        return creature;
    }

    public static Creature createCreatureFromDB(long spawnId, Map map, boolean addToMap) {
        return createCreatureFromDB(spawnId, map, addToMap, false);
    }

    public static Creature createCreatureFromDB(long spawnId, Map map) {
        return createCreatureFromDB(spawnId, map, true, false);
    }

    public static Creature createCreatureFromDB(long spawnId, Map map, boolean addToMap, boolean allowDuplicate) {
        Creature creature = new Creature();

        if (!creature.loadFromDB(spawnId, map, addToMap, allowDuplicate)) {
            return null;
        }

        return creature;
    }

    public static boolean deleteFromDB(long spawnId) {
        var data = global.getObjectMgr().getCreatureData(spawnId);

        if (data == null) {
            return false;
        }

        SQLTransaction trans = new SQLTransaction();

        global.getMapMgr().DoForAllMapsWithMapId(data.getMapId(), map ->
        {
            // despawn all active creatures, and remove their respawns
            ArrayList<Creature> toUnload = new ArrayList<>();

            for (var creature : map.CreatureBySpawnIdStore.get(spawnId)) {
                toUnload.add(creature);
            }

            for (var creature : toUnload) {
                map.addObjectToRemoveList(creature);
            }

            map.removeRespawnTime(SpawnObjectType.CREATURE, spawnId, trans);
        });

        // delete data from memory ...
        global.getObjectMgr().deleteCreatureData(spawnId);

        DB.characters.CommitTransaction(trans);

        // ... and the database
        trans = new SQLTransaction();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_CREATURE);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_SPAWNGROUP_MEMBER);
        stmt.AddValue(0, (byte) SpawnObjectType.CREATURE.getValue());
        stmt.AddValue(1, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_CREATURE_ADDON);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_GAME_EVENT_CREATURE);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_GAME_EVENT_MODEL_EQUIP);
        stmt.AddValue(0, spawnId);
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.CreatureToCreature.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.CreatureToGO.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN_MASTER);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.CreatureToCreature.getValue());
        trans.append(stmt);

        stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_LINKED_RESPAWN_MASTER);
        stmt.AddValue(0, spawnId);
        stmt.AddValue(1, (int) CreatureLinkedRespawnType.GOToCreature.getValue());
        trans.append(stmt);

        DB.World.CommitTransaction(trans);

        return true;
    }



    public CreatureAI getAI() {
        IUnitAI tempVar = super.getAI();
        return tempVar instanceof CreatureAI ? (CreatureAI) tempVar : null;
    }

    @Override
    public void addToWorld() {
        // Register the creature for guid lookup
        if (!isInWorld()) {

            getMap().getObjectsStore().TryAdd(getGUID(), this);

            if (getSpawnId() != 0) {
                getMap().getCreatureBySpawnIdStore().add(getSpawnId(), this);
            }

            super.addToWorld();
            searchFormation();
            initializeAI();

            if (isVehicle()) {
                getVehicleKit1().Install();
            }

            if (getZoneScript() != null) {
                getZoneScript().onCreatureCreate(this);
            }
        }
    }

    @Override
    public void removeFromWorld() {
        if (isInWorld()) {
            try {
                if (getZoneScript() != null) {
                    getZoneScript().onCreatureRemove(this);
                }

                if (getFormation() != null) {
                    CreatureGroup.removeCreatureFromGroup(getFormation(), this);
                }

                super.removeFromWorld();

                if (getSpawnId() != 0) {
                    getMap().getCreatureBySpawnIdStore().remove(getSpawnId(), this);
                }

                tangible.OutObject<WorldObject> tempOut__ = new tangible.OutObject<WorldObject>();

                getMap().getObjectsStore().TryRemove(getGUID(), tempOut__);
                _ = tempOut__.outArgValue;
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void disappearAndDie() {
        forcedDespawn(0);
    }

    public final void searchFormation() {
        if (isSummon()) {
            return;
        }

        var lowguid = getSpawnId();

        if (lowguid == 0) {
            return;
        }

        var formationInfo = getWorldContext().getObjectManager().getFormationInfo(lowguid);

        if (formationInfo != null) {
            CreatureGroup.addCreatureToGroup(formationInfo.leaderSpawnId, this);
        }
    }

    public final void signalFormationMovement() {
        if (getFormation() == null) {
            return;
        }

        if (!getFormation().isLeader(this)) {
            return;
        }

        getFormation().leaderStartedMoving();
    }

    public final void removeCorpse(boolean setSpawnTime) {
        removeCorpse(setSpawnTime, true);
    }

    public final void removeCorpse() {
        removeCorpse(true, true);
    }

    public final void removeCorpse(boolean setSpawnTime, boolean destroyForNearbyPlayers) {
        if (getDeathState() != deathState.Corpse) {
            return;
        }

        if (getRespawnCompatibilityMode()) {
            setCorpseRemoveTime(gameTime.GetGameTime());
            setDeathState(deathState.Dead);
            removeAllAuras();
            //DestroyForNearbyPlayers(); // old updateObjectVisibility()
            setLoot(null);
            var respawnDelay = getRespawnDelay();
            var ai = getAI();

            if (ai != null) {
                ai.corpseRemoved(respawnDelay);
            }

            if (destroyForNearbyPlayers) {
                updateObjectVisibilityOnDestroy();
            }

            // Should get removed later, just keep "compatibility" with scripts
            if (setSpawnTime) {
                setRespawnTime(Math.max(gameTime.GetGameTime() + respawnDelay, getRespawnTime()));
            }

            // if corpse was removed during falling, the falling will continue and override relocation to respawn position
            if (isFalling()) {
                stopMoving();
            }

            var respawn = getRespawnPosition();

            // We were spawned on transport, calculate real position
            if (isSpawnedOnTransport()) {
                getMovementInfo().transport.pos.relocate(respawn);

                var transport = getDirectTransport();

                if (transport != null) {
                    transport.calculatePassengerPosition(respawn);
                }
            }

            respawn.setZ(updateAllowedPositionZ(respawn.getX(), respawn.getY(), respawn.getZ()));
            setHomePosition(respawn);
            getMap().creatureRelocation(this, respawn);
        } else {
            var ai = getAI();

            if (ai != null) {
                ai.corpseRemoved(getRespawnDelay());
            }

            // In case this is called directly and normal respawn timer not set
            // Since this timer will be longer than the already present time it
            // will be ignored if the correct place added a respawn timer
            if (setSpawnTime) {
                var respawnDelay = getRespawnDelay();
                setRespawnTime(Math.max(gameTime.GetGameTime() + respawnDelay, getRespawnTime()));

                saveRespawnTime();
            }

            var summon = toTempSummon();

            if (summon != null) {
                summon.unSummon();
            } else {
                addObjectToRemoveList();
            }
        }
    }

    public final boolean initEntry(int entry) {
        return initEntry(entry, null);
    }

    public final boolean initEntry(int entry, CreatureData data) {
        var normalInfo = global.getObjectMgr().getCreatureTemplate(entry);

        if (normalInfo == null) {
            Logs.SQL.error("Creature.InitEntry creature entry {0} does not exist.", entry);

            return false;
        }

        // get difficulty 1 mode entry
        CreatureTemplate cInfo = null;
        var difficultyEntry = CliDB.DifficultyStorage.get(getMap().getDifficultyID());

        while (cInfo == null && difficultyEntry != null) {
            var idx = CreatureTemplate.difficultyIDToDifficultyEntryIndex(difficultyEntry.id);

            if (idx == -1) {
                break;
            }

            if (normalInfo.DifficultyEntry[idx] != 0) {
                cInfo = global.getObjectMgr().getCreatureTemplate(normalInfo.DifficultyEntry[idx]);

                break;
            }

            if (difficultyEntry.FallbackDifficultyID == 0) {
                break;
            }

            difficultyEntry = CliDB.DifficultyStorage.get(difficultyEntry.FallbackDifficultyID);
        }

        if (cInfo == null) {
            cInfo = normalInfo;
        }

        setEntry(entry); // normal entry always
        setCreatureTemplate(cInfo); // map mode related always

        // equal to player Race field, but creature does not have race
        setRace(race.forValue(0));
        setClass(playerClass.forValue(cInfo.unitClass));

        // Cancel load if no model defined
        if (cInfo.getFirstValidModel() == null) {
            Logs.SQL.error("Creature (Entry: {0}) has no model defined in table `creature_template`, can't load. ", entry);

            return false;
        }

        var model = ObjectManager.chooseDisplayId(cInfo, data);
        tangible.RefObject<CreatureModel> tempRef_model = new tangible.RefObject<CreatureModel>(model);
        var minfo = global.getObjectMgr().getCreatureModelRandomGender(tempRef_model, cInfo);
        model = tempRef_model.refArgValue;

        if (minfo == null) // Cancel load if no model defined
        {
            Logs.SQL.error("Creature (Entry: {0}) has invalid model {1} defined in table `creature_template`, can't load.", entry, model.creatureDisplayId);

            return false;
        }

        setDisplayId(model.creatureDisplayId, model.displayScale);
        setNativeDisplayId(model.creatureDisplayId, model.displayScale);

        // Load creature equipment
        if (data == null) {
            loadEquipment(); // use default equipment (if available) for summons
        } else if (data.equipmentId == 0) {
            loadEquipment(0); // 0 means no equipment for creature table
        } else {
            setOriginalEquipmentId(data.equipmentId);
            loadEquipment(data.equipmentId);
        }

        setName(normalInfo.name); // at normal entry always

        setModCastingSpeed(1.0f);
        setModSpellHaste(1.0f);
        setModHaste(1.0f);
        setModRangedHaste(1.0f);
        setModHasteRegen(1.0f);
        setModTimeRate(1.0f);

        setSpeedRate(UnitMoveType.Walk, cInfo.speedWalk);
        setSpeedRate(UnitMoveType.run, cInfo.speedRun);
        setSpeedRate(UnitMoveType.swim, 1.0f); // using 1.0 rate
        setSpeedRate(UnitMoveType.flight, 1.0f); // using 1.0 rate

        setObjectScale(getNativeObjectScale());

        setHoverHeight(cInfo.hoverHeight);

        setCanDualWield(cInfo.flagsExtra.hasFlag(CreatureFlagExtra.UseOffhandAttack));

        // checked at loading
        setDefaultMovementType(MovementGeneratorType.forValue(data != null ? data.MovementType : cInfo.movementType));

        if (getWanderDistance() == 0 && getDefaultMovementType() == MovementGeneratorType.random) {
            setDefaultMovementType(MovementGeneratorType.IDLE);
        }

        for (byte i = 0; i < SharedConst.MaxCreatureSpells; ++i) {
            getSpells()[i] = getCreatureTemplate().Spells[i];
        }

        getStaticFlags().modifyFlag(CreatureStaticFlags.NO_XP, isCritter() && isPet() && isTotem() && getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoXP));

        return true;
    }

    public final boolean updateEntry(int entry, CreatureData data) {
        return updateEntry(entry, data, true);
    }

    public final boolean updateEntry(int entry) {
        return updateEntry(entry, null, true);
    }

    public final boolean updateEntry(int entry, CreatureData data, boolean updateLevel) {
        if (!initEntry(entry, data)) {
            return false;
        }

        var cInfo = getCreatureTemplate();

        regenerateHealth = cInfo.regenHealth;

        // creatures always have melee weapon ready if any unless specified otherwise
        if (getCreatureAddon() == null) {
            setSheath(sheathState.Melee);
        }

        setFaction(cInfo.faction);

        long npcFlags;
        tangible.OutObject<Long> tempOut_npcFlags = new tangible.OutObject<Long>();
        int unitFlags;
        tangible.OutObject<Integer> tempOut_unitFlags = new tangible.OutObject<Integer>();
        int unitFlags2;
        tangible.OutObject<Integer> tempOut_unitFlags2 = new tangible.OutObject<Integer>();
        int unitFlags3;
        tangible.OutObject<Integer> tempOut_unitFlags3 = new tangible.OutObject<Integer>();
        int dynamicFlags;
        tangible.OutObject<Integer> tempOut_dynamicFlags = new tangible.OutObject<Integer>();
        ObjectManager.chooseCreatureFlags(cInfo, tempOut_npcFlags, tempOut_unitFlags, tempOut_unitFlags2, tempOut_unitFlags3, tempOut_dynamicFlags, data);
        dynamicFlags = tempOut_dynamicFlags.outArgValue;
        unitFlags3 = tempOut_unitFlags3.outArgValue;
        unitFlags2 = tempOut_UnitFlag2.outArgValue;
        unitFlags = tempOut_UnitFlag.outArgValue;
        npcFlags = tempOut_npcFlags.outArgValue;

        if (cInfo.flagsExtra.hasFlag(CreatureFlagExtra.Worldevent)) {
            npcFlags |= global.getGameEventMgr().getNPCFlag(this);
        }

        replaceAllNpcFlags(NPCFlags.forValue(npcFlags & 0xFFFFFFFF));
        replaceAllNpcFlags2(NPCFlags2.forValue(npcFlags >>> 32));

        // if unit is in combat, keep this flag
        unitFlags &= ~(int) UnitFlag.IN_COMBAT.getValue();

        if (isInCombat()) {
            unitFlags |= (int) UnitFlag.IN_COMBAT.getValue();
        }

        replaceAllUnitFlags(UnitFlag.forValue(unitFlags));
        replaceAllUnitFlags2(UnitFlag2.forValue(unitFlags2));
        replaceAllUnitFlags3(unitFlags3.forValue(unitFlags3));

        replaceAllDynamicFlags(UnitDynFlags.forValue(dynamicFlags));

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().stateAnimID), global.getDB2Mgr().GetEmptyAnimStateID());

        setCanDualWield(cInfo.flagsExtra.hasFlag(CreatureFlagExtra.USE_OFFHAND_ATTACK));

        setBaseAttackTime(WeaponAttackType.BASE_ATTACK, cInfo.baseAttackTime);
        setBaseAttackTime(WeaponAttackType.OFF_ATTACK, cInfo.baseAttackTime);
        setBaseAttackTime(WeaponAttackType.RANGED_ATTACK, cInfo.rangeAttackTime);

        if (updateLevel) {
            selectLevel();
        } else if (!isGuardian()) {
            var previousHealth = getHealth();
            updateLevelDependantStats(); // We still re-initialize level dependant stats on entry update

            if (previousHealth > 0) {
                setHealth(previousHealth);
            }
        }

        // Do not update guardian stats here - they are handled in Guardian::InitStatsForLevel()
        if (!isGuardian()) {
            setMeleeDamageSchool(SpellSchool.forValue(cInfo.dmgSchool));
            setStatFlatModifier(UnitMod.RESISTANCE_HOLY, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.HOLY.ordinal()]);
            setStatFlatModifier(UnitMod.RESISTANCE_FIRE, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.FIRE.ordinal()]);
            setStatFlatModifier(UnitMod.RESISTANCE_NATURE, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.NATURE.ordinal()]);
            setStatFlatModifier(UnitMod.RESISTANCE_FROST, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.FROST.ordinal()]);
            setStatFlatModifier(UnitMod.RESISTANCE_SHADOW, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.SHADOW.ordinal()]);
            setStatFlatModifier(UnitMod.RESISTANCE_ARCANE, UnitModifierFlatType.BASE_VALUE, cInfo.resistance[SpellSchool.ARCANE.ordinal()]);

            setCanModifyStats(true);
            updateAllStats();
        }

        // checked and error show at loading templates
        var factionTemplate = CliDB.FactionTemplateStorage.get(cInfo.faction);

        if (factionTemplate != null) {
            setPvP(factionTemplate.flags.hasFlag((short) FactionTemplateFlags.PVP.getValue()));
        }

        // updates spell bars for vehicles and set player's faction - should be called here, to overwrite faction that is set from the new template
        if (isVehicle()) {
            var owner = getCharmerOrOwnerPlayerOrPlayerItself();

            if (owner != null) // this check comes in case we don't have a player
            {
                setFaction(owner.getFaction()); // vehicles should have same as owner faction
                owner.vehicleSpellInitialize();
            }
        }

        // trigger creature is always not selectable and can not be attacked
        if (isTrigger()) {
            setUnitFlag(UnitFlag.Uninteractible);
        }

        initializeReactState();

        if ((boolean) (cInfo.flagsExtra.getValue() & CreatureFlagExtra.NoTaunt.getValue())) {
            applySpellImmune(0, SpellImmunity.state, AuraType.ModTaunt, true);
            applySpellImmune(0, SpellImmunity.effect, SpellEffectName.AttackMe, true);
        }

        setIsCombatDisallowed(cInfo.flagsExtra.hasFlag(CreatureFlagExtra.CannotEnterCombat));

        loadTemplateRoot();
        initializeMovementFlags();

        loadCreaturesAddon();

        loadTemplateImmunities();
        getThreatManager().evaluateSuppressed();

        //We must update last scriptId or it looks like we reloaded a script, breaking some things such as gossip temporarily
        lastUsedScriptID = getScriptId();

        getStringIds()[0] = cInfo.stringId;

        return true;
    }


    @Override
    public void update(int diff) {
        if (isAIEnabled() && triggerJustAppeared && getDeathState() != deathState.Dead) {
            if (getRespawnCompatibilityMode() && getVehicleKit() != null) {
                getVehicleKit().reset();
            }

            triggerJustAppeared = false;
            getAI().justAppeared();
        }

        updateMovementFlags();

        switch (getDeathState()) {
            case JustRespawned:
            case JustDied:
                Log.outError(LogFilter.unit, String.format("Creature (%1$s) in wrong state: %2$s", getGUID(), getDeathState()));

                break;
            case Dead: {
                if (!getRespawnCompatibilityMode()) {
                    Log.outError(LogFilter.unit, String.format("Creature (GUID: %1$s Entry: %2$s) in wrong state: DEAD (3)", getGUID().getCounter(), getEntry()));

                    break;
                }

                var now = gameTime.GetGameTime();

                if (getRespawnTime() <= now) {
                    // Delay respawn if spawn group is not active
                    if (getCreatureData() != null && !getMap().isSpawnGroupActive(getCreatureData().getSpawnGroupData().getGroupId())) {
                        setRespawnTime(now + RandomUtil.LRand(4, 7));

                        break; // Will be rechecked on next Update call after delay expires
                    }

                    var dbtableHighGuid = ObjectGuid.create(HighGuid.Creature, getLocation().getMapId(), getEntry(), getSpawnId());
                    var linkedRespawnTime = getMap().getLinkedRespawnTime(dbtableHighGuid);

                    if (linkedRespawnTime == 0) // Can respawn
                    {
                        respawn();
                    } else // the master is dead
                    {
                        var targetGuid = global.getObjectMgr().getLinkedRespawnGuid(dbtableHighGuid);

                        if (Objects.equals(targetGuid, dbtableHighGuid)) // if linking self, never respawn (check delayed to next day)
                        {
                            setRespawnTime(time.Week);
                        } else {
                            // else copy time from master and add a little
                            var baseRespawnTime = Math.max(linkedRespawnTime, now);
                            var offset = RandomUtil.LRand(5, time.Minute);

                            // linked guid can be a boss, uses std::numeric_limits<time_t>::max to never respawn in that instance
                            // we shall inherit it instead of adding and causing an overflow
                            if (baseRespawnTime <= Long.MAX_VALUE - offset) {
                                setRespawnTime(baseRespawnTime + offset);
                            } else {
                                setRespawnTime(Long.MAX_VALUE);
                            }
                        }

                        saveRespawnTime(); // also save to DB immediately
                    }
                }

                break;
            }
            case Corpse:
                super.update(diff);

                if (getDeathState() != deathState.Corpse) {
                    break;
                }

                if (isEngaged()) {
                    AIUpdateTick(diff);
                }

                if (getLoot() != null) {
                    getLoot().update();
                }


                for (var(playerOwner, loot) : getPersonalLoot()) {
                    if (loot != null) {
                        loot.update();
                    }
                }

                if (getCorpseRemoveTime() <= gameTime.GetGameTime()) {
                    removeCorpse(false);
                    Log.outDebug(LogFilter.unit, "Removing corpse... {0} ", getEntry());
                }

                break;
            case Alive:
                super.update(diff);

                if (!isAlive()) {
                    break;
                }

                getThreatManager().update(diff);

                if (isFeared()) {
                    return;
                }

                if (spellFocusInfo.delay != 0) {
                    if (spellFocusInfo.delay <= diff) {
                        reacquireSpellFocusTarget();
                    } else {
                        spellFocusInfo.Delay -= diff;
                    }
                }

                // periodic check to see if the creature has passed an evade boundary
                if (isAIEnabled() && !isInEvadeMode() && isEngaged()) {
                    if (diff >= boundaryCheckTime) {
                        getAI().checkInRoom();
                        boundaryCheckTime = 2500;
                    } else {
                        _boundaryCheckTime -= diff;
                    }
                }

                // if periodic combat pulse is enabled and we are both in combat and in a dungeon, do this now
                if (combatPulseDelay > 0 && isEngaged() && getMap().isDungeon()) {
                    if (diff > combatPulseTime) {
                        combatPulseTime = 0;
                    } else {
                        _combatPulseTime -= diff;
                    }

                    if (combatPulseTime == 0) {
                        var players = getMap().getPlayers();

                        for (var player : players) {
                            if (player.isGameMaster()) {
                                continue;
                            }

                            if (player.isAlive() && isHostileTo(player)) {
                                engageWithTarget(player);
                            }
                        }

                        combatPulseTime = _combatPulseDelay * time.InMilliseconds;
                    }
                }

                AIUpdateTick(diff);

                if (!isAlive()) {
                    break;
                }

                if (getRegenTimer() > 0) {
                    if (diff >= getRegenTimer()) {
                        setRegenTimer(0);
                    } else {
                        setRegenTimer(getRegenTimer() - diff);
                    }
                }

                if (getRegenTimer() == 0) {
                    if (!isInEvadeMode()) {
                        // regenerate health if not in combat or if polymorphed)
                        if (!isEngaged() || isPolymorphed()) {
                            regenerateHealth();
                        } else if (getCannotReachTarget()) {
                            // regenerate health if cannot reach the target and the setting is set to do so.
                            // this allows to disable the health regen of raid bosses if pathfinding has issues for whatever reason
                            if (WorldConfig.getBoolValue(WorldCfg.RegenHpCannotReachTargetInRaid) || !getMap().isRaid()) {
                                regenerateHealth();
                                Log.outDebug(LogFilter.unit, String.format("RegenerateHealth() enabled because Creature cannot reach the target. Detail: %1$s", getDebugInfo()));
                            } else {
                                Log.outDebug(LogFilter.unit, String.format("RegenerateHealth() disabled even if the Creature cannot reach the target. Detail: %1$s", getDebugInfo()));
                            }
                        }
                    }

                    if (getDisplayPowerType() == powerType.Energy) {
                        regenerate(powerType.Energy);
                    } else {
                        regenerate(powerType.mana);
                    }

                    setRegenTimer(SharedConst.CreatureRegenInterval);
                }

                if (getCannotReachTarget() && !isInEvadeMode() && !getMap().isRaid()) {
                    cannotReachTimer += diff;

                    if (cannotReachTimer >= SharedConst.CreatureNoPathEvadeTime) {
                        var ai = getAI();

                        if (ai != null) {
                            ai.enterEvadeMode(EvadeReason.NOPATH);
                        }
                    }
                }

                break;
        }
    }

    public final void regenerate(Power power) {
        var curValue = getPower(power);
        var maxValue = getMaxPower(power);

        if (!hasUnitFlag2(UnitFlag2.RegeneratePower)) {
            return;
        }

        if (curValue >= maxValue) {
            return;
        }

        double addvalue;

        switch (power) {
            case Focus: {
                // For hunter pets.
                addvalue = 24 * WorldConfig.getFloatValue(WorldCfg.RatePowerFocus);

                break;
            }
            case Energy: {
                // For deathknight's ghoul.
                addvalue = 20;

                break;
            }
            case Mana: {
                // Combat and any controlled creature
                if (isInCombat() || getCharmerOrOwnerGUID().isEmpty()) {
                    var ManaIncreaseRate = WorldConfig.getFloatValue(WorldCfg.RatePowerMana);
                    addvalue = (27.0f / 5.0f + 17.0f) * ManaIncreaseRate;
                } else {
                    addvalue = maxValue / 3;
                }

                break;
            }
            default:
                return;
        }

        // Apply modifiers (if any).
        addvalue *= getTotalAuraMultiplierByMiscValue(AuraType.ModPowerRegenPercent, power.getValue());
        addvalue += getTotalAuraModifierByMiscValue(AuraType.ModPowerRegen, power.getValue()) * (isHunterPet() ? SharedConst.PetFocusRegenInterval : SharedConst.CreatureRegenInterval) / (5 * time.InMilliseconds);

        modifyPower(power, (int) addvalue);
    }

    public final void doFleeToGetAssistance() {
        if (!getVictim()) {
            return;
        }

        if (hasAuraType(AuraType.PreventsFleeing)) {
            return;
        }

        var radius = WorldConfig.getFloatValue(WorldCfg.CreatureFamilyFleeAssistanceRadius);

        if (radius > 0) {
            var u_check = new NearestAssistCreatureInCreatureRangeCheck(this, getVictim(), radius);
            var searcher = new CreatureLastSearcher(this, u_check, gridType.Grid);
            Cell.visitGrid(this, searcher, radius);

            var creature = searcher.getTarget();

            setNoSearchAssistance(true);

            if (!creature) {
                setControlled(true, UnitState.Fleeing);
            } else {
                getMotionMaster().moveSeekAssistance(creature.getLocation().getX(), creature.getLocation().getY(), creature.getLocation().getZ());
            }
        }
    }

    public final boolean initializeAI() {
        return initializeAI(null);
    }

    public final boolean initializeAI(CreatureAI ai) {
        initializeMovementAI();

        AI = ai != null ? ai : AISelector.selectAI(this);

        getAi().initializeAI();

        // Initialize vehicle
        if (getVehicleKit1() != null) {
            getVehicleKit1().reset();
        }

        return true;
    }

    public final boolean create(long guidlow, Map map, int entry, Position pos, CreatureData data, int vehId) {
        return create(guidlow, map, entry, pos, data, vehId, false);
    }

    public final boolean create(long guidlow, Map map, int entry, Position pos, CreatureData data) {
        return create(guidlow, map, entry, pos, data, 0, false);
    }

    public final boolean create(long guidlow, Map map, int entry, Position pos) {
        return create(guidlow, map, entry, pos, null, 0, false);
    }

    public final boolean create(long guidlow, Map map, int entry, Position pos, CreatureData data, int vehId, boolean dynamic) {
        setMap(map);

        if (data != null) {
            PhasingHandler.initDbPhaseShift(getPhaseShift(), data.phaseUseFlags, data.phaseId, data.phaseGroup);
            PhasingHandler.initDbVisibleMapId(getPhaseShift(), data.terrainSwapMap);
        }

        // Set if this creature can handle dynamic spawns
        if (!dynamic) {
            setRespawnCompatibilityMode(true);
        }

        var cinfo = global.getObjectMgr().getCreatureTemplate(entry);

        if (cinfo == null) {
            Logs.SQL.error("Creature.Create: creature template (guidlow: {0}, entry: {1}) does not exist.", guidlow, entry);

            return false;
        }

        //! Relocate before CreateFromProto, to initialize coords and allow
        //! returning correct zone id for selecting OutdoorPvP/Battlefield script
        getLocation().relocate(pos);

        // Check if the position is valid before calling createFromProto(), otherwise we might add Auras to Creatures at
        // invalid position, triggering a crash about Auras not removed in the destructor
        if (!getLocation().isPositionValid()) {
            Log.outError(LogFilter.unit, String.format("Creature.Create: given coordinates for creature (guidlow %1$s, entry %2$s) are not valid (%3$s)", guidlow, entry, pos));

            return false;
        }

        {
            // area/zone id is needed immediately for ZoneScript::GetCreatureEntry hook before it is known which creature template to load (no model/scale available yet)
            PositionFullTerrainStatus positionData = getMap().getFullTerrainStatusForPosition(getPhaseShift(), getLocation(), LiquidHeaderTypeFlag.AllLiquids.value);
            processPositionDataChanged(positionData);
        }

        // Allow players to see those units while dead, do it here (mayby altered by addon auras)
        if (cinfo.typeFlags.hasFlag(CreatureTypeFlags.VisibleToGhosts)) {
            getServerSideVisibility().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Alive.getValue() | GhostVisibilityType.Ghost.getValue());
        }

        if (!createFromProto(guidlow, entry, data, vehId)) {
            return false;
        }

        cinfo = getCreatureTemplate(); // might be different than initially requested

        if (cinfo.flagsExtra.hasFlag(CreatureFlagExtra.DungeonBoss) && map.isDungeon()) {
            setRespawnDelay(0); // special value, prevents respawn for dungeon bosses unless overridden
        }

        switch (cinfo.rank) {
            case Rare:
                setCorpseDelay(WorldConfig.getUIntValue(WorldCfg.CorpseDecayRare));

                break;
            case Elite:
                setCorpseDelay(WorldConfig.getUIntValue(WorldCfg.CorpseDecayElite));

                break;
            case RareElite:
                setCorpseDelay(WorldConfig.getUIntValue(WorldCfg.CorpseDecayRareelite));

                break;
            case WorldBoss:
                setCorpseDelay(WorldConfig.getUIntValue(WorldCfg.CorpseDecayWorldboss));

                break;
            default:
                setCorpseDelay(WorldConfig.getUIntValue(WorldCfg.CorpseDecayNormal));

                break;
        }

        loadCreaturesAddon();

        //! Need to be called after LoadCreaturesAddon - MOVEMENTFLAG_HOVER is set there
        getLocation().setZ(getLocation().getZ() + getHoverOffset());

        lastUsedScriptID = getScriptId();

        if (isSpiritHealer() || isSpiritGuide() || getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.GhostVisibility)) {
            getServerSideVisibility().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Ghost);
            getServerSideVisibilityDetect().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Ghost);
        }

        if (cinfo.flagsExtra.hasFlag(CreatureFlagExtra.IgnorePathfinding)) {
            addUnitState(UnitState.IgnorePathfinding);
        }

        if (cinfo.flagsExtra.hasFlag(CreatureFlagExtra.ImmunityKnockback)) {
            applySpellImmune(0, SpellImmunity.effect, SpellEffectName.knockBack, true);
            applySpellImmune(0, SpellImmunity.effect, SpellEffectName.KnockBackDest, true);
        }

        getThreatManager().initialize();

        return true;
    }

    // select nearest hostile unit within the given distance (regardless of threat list).

    public final Unit selectVictim() {
        Unit target;

        if (getCanHaveThreatList()) {
            target = getThreatManager().getCurrentVictim();
        } else if (!hasReactState(ReactState.Passive)) {
            // We're a player pet, probably
            target = getAttackerForHelper();

            if (!target && isSummon()) {
                var owner = toTempSummon().getOwnerUnit();

                if (owner != null) {
                    if (owner.isInCombat()) {
                        target = owner.getAttackerForHelper();
                    }

                    if (!target) {
                        for (var itr : owner.getControlled()) {
                            if (itr.isInCombat()) {
                                target = itr.getAttackerForHelper();

                                if (target) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        } else {
            return null;
        }

        if (target && _IsTargetAcceptable(target) && canCreatureAttack(target)) {
            if (!hasSpellFocus()) {
                setInFront(target);
            }

            return target;
        }

        /** @todo a vehicle may eat some mob, so mob should not evade
         */
        if (getVehicle1()) {
            return null;
        }

        var iAuras = getAuraEffectsByType(AuraType.ModInvisibility);

        if (!iAuras.isEmpty()) {
            for (var itr : iAuras) {
                if (itr.getBase().isPermanent()) {
                    getAI().enterEvadeMode(EvadeReason.other);

                    break;
                }
            }

            return null;
        }

        // enter in evade mode in other case
        getAI().enterEvadeMode(EvadeReason.NoHostiles);

        return null;
    }

    public final void initializeReactState() {
        if (isTotem() || isTrigger() || isCritter() || isSpiritService()) {
            setReactState(ReactState.Passive);
        } else {
            setReactState(ReactState.Aggressive);
        }
    }

    // select nearest hostile unit within the given attack distance (i.e. distance is ignored if > than ATTACK_DISTANCE), regardless of threat list.

    public final boolean canInteractWithBattleMaster(Player player, boolean msg) {
        if (!isBattleMaster()) {
            return false;
        }

        var bgTypeId = global.getBattlegroundMgr().getBattleMasterBG(getEntry());

        if (!msg) {
            return player.getBgAccessByLevel(bgTypeId);
        }

        if (!player.getBgAccessByLevel(bgTypeId)) {
            player.getPlayerTalkClass().clearMenus();

            switch (bgTypeId) {
                case AV:
                    player.getPlayerTalkClass().sendGossipMenu(7616, getGUID());

                    break;
                case WS:
                    player.getPlayerTalkClass().sendGossipMenu(7599, getGUID());

                    break;
                case AB:
                    player.getPlayerTalkClass().sendGossipMenu(7642, getGUID());

                    break;
                case EY:
                case NA:
                case BE:
                case AA:
                case RL:
                case SA:
                case DS:
                case RV:
                    player.getPlayerTalkClass().sendGossipMenu(10024, getGUID());

                    break;
                default:
                    break;
            }

            return false;
        }

        return true;
    }

    public final boolean canResetTalents(Player player) {
        return player.getLevel() >= 15 && player.getClass() == getCreatureTemplate().trainerClass;
    }


    public final void setTextRepeatId(byte textGroup, byte id) {
        if (!textRepeat.ContainsKey(textGroup)) {
            textRepeat.add(textGroup, id);

            return;
        }

        var repeats = textRepeat.get(textGroup);

        if (!repeats.contains(id)) {
            repeats.add(id);
        } else {
            Logs.SQL.error("CreatureTextMgr: TextGroup {0} for ({1}) {2}, id {3} already added", textGroup, getName(), getGUID().toString(), id);
        }
    }


    public final ArrayList<Byte> getTextRepeatGroup(byte textGroup) {
        return textRepeat.get(textGroup);
    }


    public final void clearTextRepeatGroup(byte textGroup) {
        var groupList = textRepeat.get(textGroup);

        if (groupList != null) {
            groupList.clear();
        }
    }

    @Override
    public void atEngage(Unit target) {
        super.atEngage(target);

        if (!getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlags.AllowMountedCombat)) {
            dismount();
        }

        refreshCanSwimFlag();

        if (isPet() || isGuardian()) // update pets' speed for catchup OOC speed
        {
            updateSpeed(UnitMoveType.run);
            updateSpeed(UnitMoveType.swim);
            updateSpeed(UnitMoveType.flight);
        }

        var movetype = getMotionMaster().getCurrentMovementGeneratorType();

        if (movetype == MovementGeneratorType.Waypoint || movetype == MovementGeneratorType.Point || (isAIEnabled() && getAI().isEscorted())) {
            setHomePosition(getLocation());
            // if its a vehicle, set the home positon of every creature passenger at engage
            // so that they are in combat range if hostile
            var vehicle = getVehicleKit1();

            if (vehicle != null) {

                for (var(_, seat) : vehicle.Seats) {
                    var passenger = global.getObjAccessor().GetUnit(this, seat.passenger.guid);

                    if (passenger != null) {
                        var creature = passenger.toCreature();

                        if (creature != null) {
                            creature.setHomePosition(getLocation());
                        }
                    }
                }
            }
        }

        var ai = getAI();

        if (ai != null) {
            ai.justEngagedWith(target);
        }

        var formation = getFormation();

        if (formation != null) {
            formation.memberEngagingTarget(this, target);
        }
    }

    @Override
    public void atDisengage() {
        super.atDisengage();

        clearUnitState(UnitState.AttackPlayer);

        if (isAlive() && hasDynamicFlag(UnitDynFlags.Tapped)) {
            replaceAllDynamicFlags(UnitDynFlags.forValue(getCreatureTemplate().dynamicFlags));
        }

        if (isPet() || isGuardian()) // update pets' speed for catchup OOC speed
        {
            updateSpeed(UnitMoveType.run);
            updateSpeed(UnitMoveType.swim);
            updateSpeed(UnitMoveType.flight);
        }
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nAIName: %2$s ScriptName: %3$s WaypointPath: %4$s SpawnId: %5$s", super.getDebugInfo(), getAIName(), getScriptName(), getWaypointPath(), getSpawnId());
    }

    @Override
    public void exitVehicle() {
        exitVehicle(null);
    }

    @Override
    public void exitVehicle(Position exitPosition) {
        super.exitVehicle();

        // if the creature exits a vehicle, set it's home position to the
        // exited position so it won't run away (home) and evade if it's hostile
        setHomePosition(getLocation());
    }

    @Override
    public boolean isMovementPreventedByCasting() {
        // first check if currently a movement allowed channel is active and we're not casting
        var spell = getCurrentSpell(CurrentSpellTypes.Channeled);

        if (spell != null) {
            if (spell.getState() != SpellState.Finished && spell.isChannelActive()) {
                if (spell.checkMovement() != SpellCastResult.SpellCastOk) {
                    return true;
                }
            }
        }

        if (hasSpellFocus()) {
            return true;
        }

        return hasUnitState(UnitState.Casting);
    }

    public final void startPickPocketRefillTimer() {
        pickpocketLootRestore = gameTime.GetGameTime() + WorldConfig.getIntValue(WorldCfg.CreaturePickpocketRefill);
    }

    public final void resetPickPocketRefillTimer() {
        pickpocketLootRestore = 0;
    }

    public final void setTappedBy(Unit unit) {
        setTappedBy(unit, true);
    }

    public final void setTappedBy(Unit unit, boolean withGroup) {
        // set the player whose group should receive the right
        // to loot the creature after it dies
        // should be set to NULL after the loot disappears

        if (unit == null) {
            getTapList().clear();
            removeDynamicFlag(UnitDynFlags.Lootable.getValue() | UnitDynFlags.Tapped.getValue());

            return;
        }

        if (getTapList().size() >= SharedConst.CreatureTappersSoftCap) {
            return;
        }

        if (!unit.isTypeId(TypeId.PLAYER) && !unit.isVehicle()) {
            return;
        }

        var player = unit.getCharmerOrOwnerPlayerOrPlayerItself();

        if (player == null) // normal creature, no player involved
        {
            return;
        }

        getTapList().add(player.getGUID());

        if (withGroup) {
            var group = player.getGroup();

            if (group != null) {
                for (var itr = group.getFirstMember(); itr != null; itr = itr.next()) {
                    if (getMap().isRaid() || group.sameSubGroup(player, itr.getSource())) {
                        getTapList().add(itr.getSource().getGUID());
                    }
                }
            }
        }

        if (getTapList().size() >= SharedConst.CreatureTappersSoftCap) {
            setDynamicFlag(UnitDynFlags.Tapped);
        }
    }

    public final boolean isTappedBy(Player player) {
        return getTapList().contains(player.getGUID());
    }

    @Override
    public Loot getLootForPlayer(Player player) {
        if (getPersonalLoot().isEmpty()) {
            return getLoot();
        }

        var loot = getPersonalLoot().get(player.getGUID());

        return loot;
    }

    public final boolean isSkinnedBy(Player player) {
        var loot = getLootForPlayer(player);

        if (loot != null) {
            return loot.loot_type == LootType.SKINNING;
        }

        return false;
    }

    public final void saveToDB() {
        // this should only be used when the creature has already been loaded
        // preferably after adding to map, because mapid may not be valid otherwise
        var data = global.getObjectMgr().getCreatureData(getSpawnId());

        if (data == null) {
            Log.outError(LogFilter.unit, "Creature.SaveToDB failed, cannot get creature data!");

            return;
        }

        var mapId = getLocation().getMapId();
        var transport = getTransport();

        if (transport != null) {
            if (transport.getMapIdForSpawning() >= 0) {
                mapId = transport.getMapIdForSpawning();
            }
        }

        saveToDB(mapId, data.spawnDifficulties);
    }

    public void saveToDB(int mapid, ArrayList<Difficulty> spawnDifficulties) {
        // update in loaded data
        if (getSpawnId() == 0) {
            setSpawnId(global.getObjectMgr().generateCreatureSpawnId());
        }

        var data = global.getObjectMgr().newOrExistCreatureData(getSpawnId());

        var displayId = getNativeDisplayId();
        var npcflag = ((long) getUnitData().npcFlags.get(1) << 32) | getUnitData().npcFlags.get(0);
        int unitFlags = getUnitData().flags;
        int unitFlags2 = getUnitData().flags2;
        int unitFlags3 = getUnitData().flags3;
        var dynamicflags = UnitDynFlags.forValue((int) getObjectData().dynamicFlags);

        // check if it's a custom model and if not, use 0 for displayId
        var cinfo = getCreatureTemplate();

        if (cinfo != null) {
            for (var model : cinfo.models) {
                if (displayId != 0 && displayId == model.creatureDisplayId) {
                    displayId = 0;
                    break;
                }
            }

            if (npcflag == (int) cinfo.npcFlag) {
                npcflag = 0;
            }

            if (unitFlags == (int) cinfo.UnitFlag.getValue()) {
                unitFlags = 0;
            }

            if (unitFlags2 == cinfo.unitFlags2) {
                unitFlags2 = 0;
            }

            if (unitFlags3 == cinfo.unitFlags3) {
                unitFlags3 = 0;
            }

            if (dynamicflags == UnitDynFlags.forValue(cinfo.dynamicFlags)) {
                dynamicflags = UnitDynFlags.forValue(0);
            }
        }

        if (data.getSpawnId() == 0) {
            data.setSpawnId(getSpawnId());
        }

        data.id = getEntry();
        data.displayid = displayId;
        data.equipmentId = (byte) getCurrentEquipmentId();

        if (getTransport() == null) {
            data.setMapId(getLocation().getMapId());
            data.spawnPoint.relocate(getLocation());
        } else {
            data.setMapId(mapid);
            data.spawnPoint.relocate(getTransOffsetX(), getTransOffsetY(), getTransOffsetZ(), getTransOffsetO());
        }

        data.spawntimesecs = (int) getRespawnDelay();
        // prevent add data integrity problems
        data.wanderDistance = getDefaultMovementType() == MovementGeneratorType.Idle ? 0.0f : getWanderDistance();
        data.currentwaypoint = 0;
        data.curhealth = (int) getHealth();
        data.curmana = (int) getPower(powerType.mana);

        // prevent add data integrity problems
        data.movementType = (byte) (getWanderDistance() == 0 && getDefaultMovementType() == MovementGeneratorType.Random ? MovementGeneratorType.Idle : getDefaultMovementType());

        data.spawnDifficulties = spawnDifficulties;
        data.npcflag = npcflag;
        data.unitFlags = unitFlags;
        data.unitFlags2 = unitFlags2;
        data.unitFlags3 = unitFlags3;
        data.dynamicflags = (int) dynamicflags.getValue();

        if (data.getSpawnGroupData() == null) {
            data.setSpawnGroupData(global.getObjectMgr().getDefaultSpawnGroup());
        }

        data.phaseId = getDBPhase() > 0 ? getDBPhase() : data.phaseId;
        data.phaseGroup = getDBPhase() < 0 ? -getDBPhase() : data.phaseGroup;

        // update in DB
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.World.GetPreparedStatement(WorldStatements.DEL_CREATURE);
        stmt.AddValue(0, getSpawnId());
        trans.append(stmt);

        byte index = 0;

        stmt = DB.World.GetPreparedStatement(WorldStatements.INS_CREATURE);
        stmt.AddValue(index++, getSpawnId());
        stmt.AddValue(index++, getEntry());
        stmt.AddValue(index++, mapid);
        stmt.AddValue(index++, data.spawnDifficulties.isEmpty() ? "" : tangible.StringHelper.join(',', data.spawnDifficulties));
        stmt.AddValue(index++, data.phaseId);
        stmt.AddValue(index++, data.phaseGroup);
        stmt.AddValue(index++, displayId);
        stmt.AddValue(index++, getCurrentEquipmentId());
        stmt.AddValue(index++, getLocation().getX());
        stmt.AddValue(index++, getLocation().getY());
        stmt.AddValue(index++, getLocation().getZ());
        stmt.AddValue(index++, getLocation().getO());
        stmt.AddValue(index++, getRespawnDelay());
        stmt.AddValue(index++, getWanderDistance());
        stmt.AddValue(index++, 0);
        stmt.AddValue(index++, getHealth());
        stmt.AddValue(index++, getPower(powerType.mana));
        stmt.AddValue(index++, (byte) getDefaultMovementType().getValue());
        stmt.AddValue(index++, npcflag);
        stmt.AddValue(index++, unitFlags);
        stmt.AddValue(index++, unitFlags2);
        stmt.AddValue(index++, unitFlags3);
        stmt.AddValue(index++, (int) dynamicflags.getValue());
        trans.append(stmt);

        DB.World.CommitTransaction(trans);
    }

    public final void selectLevel() {
        applyLevelScaling();
        int levelWithDelta = getUInt32Value(ObjectFields.UNIT_FIELD_SCALING_LEVEL_MAX) + getUInt32Value(ObjectFields.UNIT_FIELD_SCALING_LEVEL_DELTA);
        int level = MathUtil.roundToInterval(levelWithDelta, 1, SharedDefine.STRONG_MAX_LEVEL);
        setLevel(level);
        updateLevelDependantStats();
    }

    public final void updateLevelDependantStats() {
        var cInfo = getCreatureTemplate();
        var rank = isPet() ? 0 : cInfo.rank;
        var level = getLevel();
        var stats = getWorldContext().getObjectManager().getCreatureBaseStats(level, cInfo.unitClass);

        // health
        var healthmod = getHealthMod(rank);

        var basehp = (int) getMaxHealthByLevel(level);
        var health = (int) (basehp * healthmod);

        setCreateHealth(health);
        setMaxHealth(health);
        setHealth(health);
        resetPlayerDamageReq();

        // mana
        var mana = stats.generateMana(cInfo);
        setCreateMana(mana);

        switch (getClass()) {
            case Paladin:
            case Mage:
                setMaxPower(powerType.mana, (int) mana);
                setPower(powerType.mana, (int) mana);

                break;
            default: // We don't set max power here, 0 makes power bar hidden
                break;
        }

        setStatFlatModifier(UnitMod.HEALTH, UnitModifierFlatType.BASE_VALUE, health);

        //Damage
        var basedamage = getBaseDamageForLevel(level);
        var weaponBaseMinDamage = basedamage;
        var weaponBaseMaxDamage = basedamage * 1.5f;

        setBaseWeaponDamage(WeaponAttackType.BASE_ATTACK, WeaponDamageRange.MIN_DAMAGE, weaponBaseMinDamage);
        setBaseWeaponDamage(WeaponAttackType.BASE_ATTACK, WeaponDamageRange.MAX_DAMAGE, weaponBaseMaxDamage);

        setBaseWeaponDamage(WeaponAttackType.OFF_ATTACK, WeaponDamageRange.MIN_DAMAGE, weaponBaseMinDamage);
        setBaseWeaponDamage(WeaponAttackType.OFF_ATTACK, WeaponDamageRange.MAX_DAMAGE, weaponBaseMaxDamage);

        setBaseWeaponDamage(WeaponAttackType.RANGED_ATTACK, WeaponDamageRange.MIN_DAMAGE, weaponBaseMinDamage);
        setBaseWeaponDamage(WeaponAttackType.RANGED_ATTACK, WeaponDamageRange.MAX_DAMAGE, weaponBaseMaxDamage);

        setStatFlatModifier(UnitMod.ATTACK_POWER, UnitModifierFlatType.BASE_VALUE, stats.getAttackPower());
        setStatFlatModifier(UnitMod.ATTACK_POWER_RANGED, UnitModifierFlatType.BASE_VALUE, stats.getRangedAttackPower());

        /// @todo Why is this treated as uint32 when it's a float?
        var armor = getBaseArmorForLevel(level);
        setStatFlatModifier(UnitMod.ARMOR, UnitModifierFlatType.BASE_VALUE, armor);
    }

    public final float getHealthMod(CreatureClassification rank) {
        WorldSetting worldSettings = getWorldContext().getWorldSettings();
        // define rates for each elite rank
        return switch (rank) {
            case Normal -> worldSettings.rate.creatureHpNormal;
            case Elite -> worldSettings.rate.creatureHpElite;
            case RareElite -> worldSettings.rate.creatureHpRareElite;
            case Obsolete -> worldSettings.rate.creatureHpObsolete;
            case Rare -> worldSettings.rate.creatureHpRare;
            case Trivial -> worldSettings.rate.creatureHpTrivial;
            case MinusMob -> worldSettings.rate.creatureHpMinusMob;
        };
    }

    public final void lowerPlayerDamageReq(double unDamage) {
        lowerPlayerDamageReq((long) unDamage);
    }

    public final void lowerPlayerDamageReq(long unDamage) {
        if (getPlayerDamageReq() != 0) {
            if (getPlayerDamageReq() > unDamage) {
                setPlayerDamageReq(getPlayerDamageReq() - unDamage);
            } else {
                setPlayerDamageReq(0);
            }
        }
    }

    public final float getSpellDamageMod(CreatureClassification rank) {
        WorldSetting worldSettings = getWorldContext().getWorldSettings();
        switch (rank) // define rates for each elite rank
        {
            case Normal:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureNormalSpelldamage);
            case Elite:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureEliteEliteSpelldamage);
            case RareElite:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureEliteRareeliteSpelldamage);
            case WorldBoss:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureEliteWorldbossSpelldamage);
            case Rare:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureEliteRareSpelldamage);
            default:
                return WorldConfig.getFloatValue(WorldCfg.RateCreatureEliteEliteSpelldamage);
        }
    }

    @Override
    public void setCanDualWield(boolean value) {
        super.setCanDualWield(value);
        updateDamagePhysical(WeaponAttackType.OFF_ATTACK);
    }

    public final void loadEquipment(int id) {
        loadEquipment(id, true);
    }

    public final void loadEquipment() {
        loadEquipment(1, true);
    }

    public final void loadEquipment(int id, boolean force) {
        if (id == 0) {
            if (force) {
                for (byte i = 0; i < SharedConst.MaxEquipmentItems; ++i) {
                    setVirtualItem(i, 0);
                }

                setCurrentEquipmentId(0);
            }

            return;
        }

        var einfo = global.getObjectMgr().getEquipmentInfo(getEntry(), id);

        if (einfo == null) {
            return;
        }

        setCurrentEquipmentId((byte) id);

        for (byte i = 0; i < SharedConst.MaxEquipmentItems; ++i) {
            setVirtualItem(i, einfo.getItems()[i].itemId, einfo.getItems()[i].appearanceModId, einfo.getItems()[i].itemVisual);
        }
    }

    public final void setSpawnHealth() {
        if (getStaticFlags().hasFlag(CreatureStaticFlags5.NO_HEALTH_REGEN)) {
            return;
        }

        long curhealth;

        if (getCreatureData() != null && !regenerateHealth) {
            curhealth = getCreatureData().curhealth;

            if (curhealth != 0) {
                curhealth = (long) (curhealth * getHealthMod(getCreatureTemplate().rank));

                if (curhealth < 1) {
                    curhealth = 1;
                }
            }

            setPower(powerType.mana, (int) getCreatureData().curmana);
        } else {
            curhealth = getMaxHealth();
            setFullPower(powerType.mana);
        }

        setHealth((getDeathState() == deathState.Alive || getDeathState() == deathState.JustRespawned) ? curhealth : 0);
    }

    @Override
    public boolean hasQuest(int questId) {
        return global.getObjectMgr().getCreatureQuestRelations(getEntry()).hasQuest(questId);
    }

    @Override
    public boolean hasInvolvedQuest(int questId) {
        return global.getObjectMgr().getCreatureQuestInvolvedRelations(getEntry()).hasQuest(questId);
    }

    @Override
    public boolean isInvisibleDueToDespawn(WorldObject seer) {
        if (super.isInvisibleDueToDespawn(seer)) {
            return true;
        }

        return !isAlive() && getCorpseRemoveTime() <= gameTime.GetGameTime();
    }

    @Override
    public boolean canAlwaysSee(WorldObject obj) {
        return isAIEnabled() && this.getAI().canSeeAlways(obj);
    }

    public final boolean canStartAttack(Unit who, boolean force) {
        if (isCivilian()) {
            return false;
        }

        // This set of checks is should be done only for creatures
        if ((isImmuneToNPC() && !who.hasUnitFlag(UnitFlag.PlayerControlled)) || (isImmuneToPC() && who.hasUnitFlag(UnitFlag.PlayerControlled))) {
            return false;
        }

        // Do not attack non-combat pets
        if (who.isTypeId(TypeId.UNIT) && who.getCreatureType() == creatureType.NonCombatPet) {
            return false;
        }

        if (!canFly() && (getDistanceZ(who) > SharedConst.CreatureAttackRangeZ + getCombatDistance())) {
            return false;
        }

        if (!force) {
            if (!_IsTargetAcceptable(who)) {
                return false;
            }

            if (isNeutralToAll() || !isWithinDistInMap(who, (float) getAttackDistance(who) + getCombatDistance())) {
                return false;
            }
        }

        if (!canCreatureAttack(who, force)) {
            return false;
        }

        return isWithinLOSInMap(who);
    }

    public final double getAttackDistance(Unit player) {
        var aggroRate = WorldConfig.getFloatValue(WorldCfg.RateCreatureAggro);

        if (aggroRate == 0) {
            return 0.0f;
        }

        // WoW Wiki: the minimum radius seems to be 5 yards, while the maximum range is 45 yards
        var maxRadius = 45.0f * aggroRate;
        var minRadius = 5.0f * aggroRate;

        var expansionMaxLevel = (int) global.getObjectMgr().getMaxLevelForExpansion(expansion.forValue(getCreatureTemplate().requiredExpansion));
        var playerLevel = player.getLevelForTarget(this);
        var creatureLevel = getLevelForTarget(player);
        var levelDifference = creatureLevel - playerLevel;

        // The aggro radius for creatures with equal level as the player is 20 yards.
        // The combatreach should not get taken into account for the distance so we drop it from the range (see Supremus as expample)
        var baseAggroDistance = 20.0f - getCombatReach();

        // + - 1 yard for each level difference between player and creature
        double aggroRadius = baseAggroDistance + levelDifference;

        // detect range auras
        if ((creatureLevel + 5) <= WorldConfig.getIntValue(WorldCfg.MaxPlayerLevel)) {
            aggroRadius += getTotalAuraModifier(AuraType.ModDetectRange);
            aggroRadius += player.getTotalAuraModifier(AuraType.ModDetectedRange);
        }

        // The aggro range of creatures with higher levels than the total player level for the expansion should get the maxlevel treatment
        // This makes sure that creatures such as bosses wont have a bigger aggro range than the rest of the npc's
        // The following code is used for blizzlike behaviour such as skippable bosses
        if (creatureLevel > expansionMaxLevel) {
            aggroRadius = baseAggroDistance + (float) (expansionMaxLevel - playerLevel);
        }

        // Make sure that we wont go over the total range limits
        if (aggroRadius > maxRadius) {
            aggroRadius = maxRadius;
        } else if (aggroRadius < minRadius) {
            aggroRadius = minRadius;
        }

        return (aggroRadius * aggroRate);
    }

    @Override
    public void setDeathState(DeathState s) {
        super.setDeathState(s);

        if (s == deathState.JustDied) {
            setCorpseRemoveTime(gameTime.GetGameTime() + getCorpseDelay());
            var respawnDelay = getRespawnDelay();
            var scalingMode = WorldConfig.getUIntValue(WorldCfg.RespawnDynamicMode);

            if (scalingMode != 0) {
                tangible.RefObject<Integer> tempRef_respawnDelay = new tangible.RefObject<Integer>(respawnDelay);
                getMap().applyDynamicModeRespawnScaling(this, getSpawnId(), tempRef_respawnDelay, scalingMode);
                respawnDelay = tempRef_respawnDelay.refArgValue;
            }

            // @todo remove the boss respawn time hack in a dynspawn follow-up once we have creature groups in instances
            if (getRespawnCompatibilityMode()) {
                if (isDungeonBoss() && getRespawnDelay() == 0) {
                    setRespawnTime(Long.MAX_VALUE); // never respawn in this instance
                } else {
                    setRespawnTime(gameTime.GetGameTime() + respawnDelay + getCorpseDelay());
                }
            } else {
                if (isDungeonBoss() && getRespawnDelay() == 0) {
                    setRespawnTime(Long.MAX_VALUE); // never respawn in this instance
                } else {
                    setRespawnTime(gameTime.GetGameTime() + respawnDelay);
                }
            }

            saveRespawnTime();

            releaseSpellFocus(null, false); // remove spellcast focus
            doNotReacquireSpellFocusTarget(); // cancel delayed re-target
            setTarget(ObjectGuid.Empty); // drop target - dead mobs shouldn't ever target things

            replaceAllNpcFlags(NPCFlags.NONE);
            replaceAllNpcFlags2(NPCFlags2.NONE);

            setMountDisplayId(0); // if creature is mounted on a virtual mount, remove it at death

            setActive(false);
            setNoSearchAssistance(false);

            //Dismiss group if is leader
            if (getFormation() != null && getFormation().getLeader() == this) {
                getFormation().formationReset(true);
            }

            var needsFalling = (isFlying() || isHovering()) && !isUnderWater();
            setHover(false, false);
            setDisableGravity(false, false);

            if (needsFalling) {
                getMotionMaster().moveFall();
            }

            super.setDeathState(deathState.Corpse);
        } else if (s == deathState.JustRespawned) {
            if (isPet()) {
                setFullHealth();
            } else {
                setSpawnHealth();
            }

            setTappedBy(null);
            resetPlayerDamageReq();

            setCannotReachTarget(false);
            updateMovementFlags();

            clearUnitState(UnitState.AllErasable);

            if (!isPet()) {
                var creatureData = getCreatureData();
                var cInfo = getCreatureTemplate();

                long npcFlags;
                tangible.OutObject<Long> tempOut_npcFlags = new tangible.OutObject<Long>();
                int unitFlags;
                tangible.OutObject<Integer> tempOut_unitFlags = new tangible.OutObject<Integer>();
                int unitFlags2;
                tangible.OutObject<Integer> tempOut_unitFlags2 = new tangible.OutObject<Integer>();
                int unitFlags3;
                tangible.OutObject<Integer> tempOut_unitFlags3 = new tangible.OutObject<Integer>();
                int dynamicFlags;
                tangible.OutObject<Integer> tempOut_dynamicFlags = new tangible.OutObject<Integer>();
                ObjectManager.chooseCreatureFlags(cInfo, tempOut_npcFlags, tempOut_unitFlags, tempOut_unitFlags2, tempOut_unitFlags3, tempOut_dynamicFlags, creatureData);
                dynamicFlags = tempOut_dynamicFlags.outArgValue;
                unitFlags3 = tempOut_unitFlags3.outArgValue;
                unitFlags2 = tempOut_UnitFlag2.outArgValue;
                unitFlags = tempOut_UnitFlag.outArgValue;
                npcFlags = tempOut_npcFlags.outArgValue;

                if (cInfo.flagsExtra.hasFlag(CreatureFlagExtra.Worldevent)) {
                    npcFlags |= global.getGameEventMgr().getNPCFlag(this);
                }

                replaceAllNpcFlags(NPCFlags.forValue(npcFlags & 0xFFFFFFFF));
                replaceAllNpcFlags2(NPCFlags2.forValue(npcFlags >>> 32));

                replaceAllUnitFlags(UnitFlag.forValue(unitFlags));
                replaceAllUnitFlags2(UnitFlag2.forValue(unitFlags2));
                replaceAllUnitFlags3(unitFlags3.forValue(unitFlags3));
                replaceAllDynamicFlags(UnitDynFlags.forValue(dynamicFlags));

                removeUnitFlag(UnitFlag.IN_COMBAT);

                setMeleeDamageSchool(SpellSchool.forValue(cInfo.dmgSchool));
            }

            initializeMovementAI();
            super.setDeathState(deathState.Alive);
            loadCreaturesAddon();
        }
    }

    public final void respawn() {
        respawn(false);
    }

    public final void respawn(boolean force) {
        if (force) {
            if (isAlive()) {
                setDeathState(deathState.JustDied);
            } else if (getDeathState() != deathState.Corpse) {
                setDeathState(deathState.Corpse);
            }
        }

        if (getRespawnCompatibilityMode()) {
            updateObjectVisibilityOnDestroy();
            removeCorpse(false, false);

            if (getDeathState() == deathState.Dead) {
                Log.outDebug(LogFilter.unit, "Respawning creature {0} ({1})", getName(), getGUID().toString());
                setRespawnTime(0);
                resetPickPocketRefillTimer();
                setLoot(null);

                if (getOriginalEntry() != getEntry()) {
                    updateEntry(getOriginalEntry());
                }

                selectLevel();

                setDeathState(deathState.JustRespawned);

                CreatureModel display = new creatureModel(getNativeDisplayId(), getNativeDisplayScale(), 1.0f);

                tangible.RefObject<CreatureModel> tempRef_display = new tangible.RefObject<CreatureModel>(display);
                if (global.getObjectMgr().getCreatureModelRandomGender(tempRef_display, getCreatureTemplate()) != null) {
                    display = tempRef_display.refArgValue;
                    setDisplayId(display.creatureDisplayId, display.displayScale);
                    setNativeDisplayId(display.creatureDisplayId, display.displayScale);
                } else {
                    display = tempRef_display.refArgValue;
                }

                getMotionMaster().initializeDefault();

                //Re-initialize reactstate that could be altered by movementgenerators
                initializeReactState();

                IUnitAI ai = getAI();

                if (ai != null) // reset the AI to be sure no dirty or uninitialized values will be used till next tick
                {
                    ai.reset();
                }

                triggerJustAppeared = true;

                var poolid = getCreatureData() != null ? getCreatureData().poolId : 0;

                if (poolid != 0) {
                    global.getPoolMgr().<Creature>UpdatePool(getMap().getPoolData(), poolid, getSpawnId());
                }
            }

            updateObjectVisibility();
        } else {
            if (getSpawnId() != 0) {
                getMap().respawn(SpawnObjectType.CREATURE, getSpawnId());
            }
        }

        Log.outDebug(LogFilter.unit, String.format("Respawning creature %1$s (%2$s)", getName(), getGUID()));
    }

    public final void forcedDespawn(int timeMSToDespawn) {
        forcedDespawn(timeMSToDespawn, null);
    }

    public final void forcedDespawn() {
        forcedDespawn(0, null);
    }

    public final void forcedDespawn(int timeMSToDespawn, Duration forceRespawnTimer) {
        if (timeMSToDespawn != 0) {
            getEvents().addEvent(new ForcedDespawnDelayEvent(this, forceRespawnTimer), getEvents().CalculateTime(duration.ofSeconds(timeMSToDespawn)));

            return;
        }

        if (getRespawnCompatibilityMode()) {
            var corpseDelay = getCorpseDelay();
            var respawnDelay = getRespawnDelay();

            // do it before killing creature
            updateObjectVisibilityOnDestroy();

            var overrideRespawnTime = false;

            if (isAlive()) {
                if (forceRespawnTimer > duration.Zero) {
                    setCorpseDelay(0);
                    setRespawnDelay((int) forceRespawnTimer.TotalSeconds);
                    overrideRespawnTime = false;
                }

                setDeathState(deathState.JustDied);
            }

            // Skip corpse decay time
            removeCorpse(overrideRespawnTime, false);

            setCorpseDelay(corpseDelay);
            setRespawnDelay(respawnDelay);
        } else {
            if (forceRespawnTimer > duration.Zero) {
                saveRespawnTime((int) forceRespawnTimer.TotalSeconds);
            } else {
                var respawnDelay = getRespawnDelay();
                var scalingMode = WorldConfig.getUIntValue(WorldCfg.RespawnDynamicMode);

                if (scalingMode != 0) {
                    tangible.RefObject<Integer> tempRef_respawnDelay = new tangible.RefObject<Integer>(respawnDelay);
                    getMap().applyDynamicModeRespawnScaling(this, getSpawnId(), tempRef_respawnDelay, scalingMode);
                    respawnDelay = tempRef_respawnDelay.refArgValue;
                }

                setRespawnTime(gameTime.GetGameTime() + respawnDelay);
                saveRespawnTime();
            }

            addObjectToRemoveList();
        }
    }

    public final void despawnOrUnsummon(Duration msTimeToDespawn) {
        despawnOrUnsummon(msTimeToDespawn, null);
    }

    public final void despawnOrUnsummon() {
        despawnOrUnsummon(null, null);
    }

    public final void despawnOrUnsummon(Duration msTimeToDespawn, Duration forceRespawnTimer) {
        var summon = toTempSummon();

        if (summon != null) {
            summon.unSummon(duration.ofSeconds(msTimeToDespawn.TotalMilliseconds));
        } else {
            forcedDespawn((int) msTimeToDespawn.TotalMilliseconds, forceRespawnTimer);
        }
    }

    public final void loadTemplateImmunities() {
        // uint32 max used for "spell id", the immunity system will not perform SpellInfo checks against invalid spells
        // used so we know which immunities were loaded from template
        var placeholderSpellId = Integer.MAX_VALUE;

        // unapply template immunities (in case we're updating entry)
        for (int i = 0; i < mechanics.max.getValue(); ++i) {
            applySpellImmune(placeholderSpellId, SpellImmunity.mechanic, i, false);
        }

        for (var i = SpellSchool.NORMAL.getValue(); i < SpellSchool.max.getValue(); ++i) {
            applySpellImmune(placeholderSpellId, SpellImmunity.school, 1 << i, false);
        }

        // don't inherit immunities for hunter pets
        if (getOwnerGUID().isPlayer() && isHunterPet()) {
            return;
        }

        var mechanicMask = getCreatureTemplate().mechanicImmuneMask;

        if (mechanicMask != 0) {
            for (int i = 1; i < mechanics.max.getValue(); ++i) {
                if ((mechanicMask & (1L << (i - 1))) != 0) {
                    applySpellImmune(placeholderSpellId, SpellImmunity.mechanic, i, true);
                }
            }
        }

        var schoolMask = getCreatureTemplate().spellSchoolImmuneMask;

        if (schoolMask != 0) {
            for (var i = SpellSchool.NORMAL.getValue(); i <= SpellSchool.max.getValue(); ++i) {
                if ((schoolMask & (1 << i)) != 0) {
                    applySpellImmune(placeholderSpellId, SpellImmunity.school, 1 << i, true);
                }
            }
        }
    }

    @Override
    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster) {
        return isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, false);
    }

    @Override
    public boolean isImmunedToSpellEffect(SpellInfo spellInfo, SpellEffectInfo spellEffectInfo, WorldObject caster, boolean requireImmunityPurgesEffectAttribute) {
        if (getCreatureTemplate().type == creatureType.Mechanical && spellEffectInfo.isEffect(SpellEffectName.Heal)) {
            return true;
        }

        return super.isImmunedToSpellEffect(spellInfo, spellEffectInfo, caster, requireImmunityPurgesEffectAttribute);
    }

    public final Unit selectNearestTarget() {
        return selectNearestTarget(0);
    }

    public final Unit selectNearestTarget(float dist) {
        if (dist == 0.0f) {
            dist = SharedConst.MaxVisibilityDistance;
        }

        var u_check = new NearestHostileUnitCheck(this, dist);
        var searcher = new UnitLastSearcher(this, u_check, gridType.All);
        Cell.visitGrid(this, searcher, dist);

        return searcher.getTarget();
    }

    public final Unit selectNearestTargetInAttackDistance() {
        return selectNearestTargetInAttackDistance(0);
    }

    public final Unit selectNearestTargetInAttackDistance(float dist) {
        if (dist > SharedConst.MaxVisibilityDistance) {
            Log.outError(LogFilter.unit, "Creature ({0}) SelectNearestTargetInAttackDistance called with dist > MAX_VISIBILITY_DISTANCE. Distance set to ATTACK_DISTANCE.", getGUID().toString());
            dist = SharedConst.AttackDistance;
        }

        var u_check = new NearestHostileUnitInAttackDistanceCheck(this, dist);
        var searcher = new UnitLastSearcher(this, u_check, gridType.All);

        Cell.visitGrid(this, searcher, Math.max(dist, SharedConst.AttackDistance));

        return searcher.getTarget();
    }

    public final void sendAIReaction(AiReaction reactionType) {
        AIReaction packet = new AIReaction();

        packet.unitGUID = getGUID();
        packet.reaction = reactionType;

        sendMessageToSet(packet, true);
    }

    public final void callAssistance() {
        if (!alreadyCallAssistance && getVictim() != null && !isPet() && !isCharmed()) {
            setNoCallAssistance(true);

            var radius = WorldConfig.getFloatValue(WorldCfg.CreatureFamilyAssistanceRadius);

            if (radius > 0) {
                ArrayList<Creature> assistList = new ArrayList<>();

                var u_check = new AnyAssistCreatureInRangeCheck(this, getVictim(), radius);
                var searcher = new CreatureListSearcher(this, assistList, u_check, gridType.Grid);
                Cell.visitGrid(this, searcher, radius);

                if (!assistList.isEmpty()) {
                    AssistDelayEvent e = new AssistDelayEvent(getVictim().getGUID(), this);

                    while (!assistList.isEmpty()) {
                        // Pushing guids because in delay can happen some creature gets despawned
                        e.addAssistant(assistList.get(0).GUID);
                        assistList.remove(assistList.get(0));
                    }

                    getEvents().addEvent(e, getEvents().CalculateTime(duration.ofSeconds(WorldConfig.getUIntValue(WorldCfg.CreatureFamilyAssistanceDelay))));
                }
            }
        }
    }

    public final void callForHelp(float radius) {
        if (radius <= 0.0f || !isEngaged() || !isAlive() || isPet() || isCharmed()) {
            return;
        }

        var target = getThreatManager().getCurrentVictim();

        if (target == null) {
            target = getThreatManager().getAnyTarget();
        }

        if (target == null) {
            target = getCombatManager().getAnyTarget();
        }

        if (target == null) {
            Log.outError(LogFilter.unit, String.format("Creature %1$s (%2$s) trying to call for help without being in combat.", getEntry(), getName()));

            return;
        }

        var u_do = new CallOfHelpCreatureInRangeDo(this, target, radius);
        var worker = new CreatureWorker(this, u_do, gridType.Grid);
        Cell.visitGrid(this, worker, radius);
    }

    public final boolean canAssistTo(Unit u, Unit enemy) {
        return canAssistTo(u, enemy, true);
    }

    public final boolean canAssistTo(Unit u, Unit enemy, boolean checkfaction) {
        // is it true?
        if (!hasReactState(ReactState.Aggressive)) {
            return false;
        }

        // we don't need help from zombies :)
        if (!isAlive()) {
            return false;
        }

        // we cannot assist in evade mode
        if (isInEvadeMode()) {
            return false;
        }

        // or if enemy is in evade mode
        if (enemy.getObjectTypeId() == TypeId.UNIT && enemy.toCreature().isInEvadeMode()) {
            return false;
        }

        // we don't need help from non-combatant ;)
        if (isCivilian()) {
            return false;
        }

        if (hasUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue()) || isImmuneToNPC()) {
            return false;
        }

        // skip fighting creature
        if (isEngaged()) {
            return false;
        }

        // only free creature
        if (!getCharmerOrOwnerGUID().isEmpty()) {
            return false;
        }

        // only from same creature faction
        if (checkfaction) {
            if (getFaction() != u.getFaction()) {
                return false;
            }
        } else {
            if (!isFriendlyTo(u)) {
                return false;
            }
        }

        // skip non hostile to caster enemy creatures
        return isHostileTo(enemy);
    }

    public final boolean _IsTargetAcceptable(Unit target) {
        // if the target cannot be attacked, the target is not acceptable
        if (isFriendlyTo(target) || !target.isTargetableForAttack(false) || (getVehicle() != null && (isOnVehicle(target) || getVehicle().GetBase().isOnVehicle(target)))) {
            return false;
        }

        if (target.hasUnitState(UnitState.Died)) {
            // some creatures can detect fake death
            return getCanIgnoreFeignDeath() && target.hasUnitFlag2(UnitFlag2.FeignDeath);
        }

        // if I'm already fighting target, or I'm hostile towards the target, the target is acceptable
        return isEngagedBy(target) || isHostileTo(target);

        // if the target's victim is not friendly, or the target is friendly, the target is not acceptable
    }

    public final void saveRespawnTime() {
        saveRespawnTime(0);
    }

    public final void saveRespawnTime(int forceDelay) {
        if (isSummon() || getSpawnId() == 0 || (getCreatureData() != null && !getCreatureData().getDbData())) {
            return;
        }

        if (getRespawnCompatibilityMode()) {
            RespawnInfo ri = new RespawnInfo();
            ri.setObjectType(SpawnObjectType.CREATURE);
            ri.setSpawnId(getSpawnId());
            ri.setRespawnTime(getRespawnTime());
            getMap().saveRespawnInfoDB(ri);

            return;
        }

        var thisRespawnTime = forceDelay != 0 ? gameTime.GetGameTime() + forceDelay : getRespawnTime();
        getMap().saveRespawnTime(SpawnObjectType.CREATURE, getSpawnId(), getEntry(), thisRespawnTime, MapDefine.computeGridCoord(getHomePosition().getX(), getHomePosition().getY()).getId());
    }

    public final boolean canCreatureAttack(Unit victim) {
        return canCreatureAttack(victim, true);
    }

    public final boolean canCreatureAttack(Unit victim, boolean force) {
        if (!victim.isInMap(this)) {
            return false;
        }

        if (!isValidAttackTarget(victim)) {
            return false;
        }

        if (!victim.isInAccessiblePlaceFor(this)) {
            return false;
        }

        var ai = getAI();

        if (ai != null) {
            if (!ai.canAIAttack(victim)) {
                return false;
            }
        }

        // we cannot attack in evade mode
        if (isInEvadeMode()) {
            return false;
        }

        // or if enemy is in evade mode
        if (victim.getObjectTypeId() == TypeId.UNIT && victim.toCreature().isInEvadeMode()) {
            return false;
        }

        if (!getCharmerOrOwnerGUID().isPlayer()) {
            if (getMap().isDungeon()) {
                return true;
            }

            // don't check distance to home position if recently damaged, this should include taunt auras
            if (!isWorldBoss() && (getLastDamagedTime() > gameTime.GetGameTime() || hasAuraType(AuraType.ModTaunt))) {
                return true;
            }
        }

        // Map visibility range, but no more than 2*cell size
        var dist = Math.min(getMap().getVisibilityRange(), MapDefine.SizeofCells * 2);

        var unit = getCharmerOrOwner();

        if (unit != null) {
            return victim.isWithinDist(unit, dist);
        } else {
            // include sizes for huge npcs
            dist += getCombatReach() + victim.getCombatReach();

            // to prevent creatures in air ignore attacks because distance is already too high...
            if (getMovementTemplate().isFlightAllowed()) {
                return victim.getLocation().isInDist2D(homePosition, dist);
            } else {
                return victim.getLocation().isInDist(homePosition, dist);
            }
        }
    }

    public final boolean loadCreaturesAddon() {
        var creatureAddon = getCreatureAddon();

        if (creatureAddon == null) {
            return false;
        }

        if (creatureAddon.mount != 0) {
            mount(creatureAddon.mount);
        }

        setStandState(UnitStandStateType.forValue(creatureAddon.standState));
        replaceAllVisFlags(UnitVisFlags.forValue(creatureAddon.visFlags));
        setAnimTier(animTier.forValue(creatureAddon.animTier), false);

        //! Suspected correlation between UNIT_FIELD_BYTES_1, offset 3, value 0x2:
        //! If no inhabittype_fly (if no MovementFlag_DisableGravity or MovementFlag_CanFly flag found in sniffs)
        //! Check using InhabitType as movement flags are assigned dynamically
        //! basing on whether the creature is in air or not
        //! Set MovementFlag_Hover. Otherwise do nothing.
        if (getCanHover()) {
            addUnitMovementFlag(MovementFlag.Hover);
        }

        setSheath(sheathState.forValue(creatureAddon.sheathState));
        replaceAllPvpFlags(UnitPVPStateFlag.forValue(creatureAddon.pvpFlags));

        // These fields must only be handled by core internals and must not be modified via scripts/DB dat
        replaceAllPetFlags(UnitPetFlags.NONE);
        setShapeshiftForm(ShapeShiftForm.NONE);

        if (creatureAddon.emote != 0) {
            setEmoteState(emote.forValue(creatureAddon.emote));
        }

        setAIAnimKitId(creatureAddon.aiAnimKit);
        setMovementAnimKitId(creatureAddon.movementAnimKit);
        setMeleeAnimKitId(creatureAddon.meleeAnimKit);

        // Check if visibility distance different
        if (creatureAddon.visibilityDistanceType != visibilityDistanceType.NORMAL) {
            setVisibilityDistanceOverride(creatureAddon.visibilityDistanceType);
        }

        //Load Path
        if (creatureAddon.pathId != 0) {
            setWaypointPath(creatureAddon.pathId);
        }

        if (creatureAddon.auras != null) {
            for (var id : creatureAddon.auras) {
                var AdditionalSpellInfo = global.getSpellMgr().getSpellInfo(id, getMap().getDifficultyID());

                if (AdditionalSpellInfo == null) {
                    Logs.SQL.error("Creature ({0}) has wrong spell {1} defined in `auras` field.", getGUID().toString(), id);

                    continue;
                }

                // skip already applied aura
                if (hasAura(id)) {
                    continue;
                }

                addAura(id, this);
                Log.outDebug(LogFilter.unit, "Spell: {0} added to creature ({1})", id, getGUID().toString());
            }
        }

        return true;
    }

    // Send a message to LocalDefense channel for players opposition team in the zone
    public final void sendZoneUnderAttackMessage(Player attacker) {
        var enemy_team = attacker.getTeam();

        ZoneUnderAttack packet = new ZoneUnderAttack();
        packet.areaID = getAreaId();
        global.getWorldMgr().sendGlobalMessage(packet, null, (enemy_team == Team.ALLIANCE ? Team.Horde : Team.ALLIANCE));
    }

    @Override
    public boolean hasSpell(int spellId) {
        return getSpells().contains(spellId);
    }

    public final Position getRespawnPosition(tangible.OutObject<Float> dist) {
        if (getCreatureData() != null) {
            dist.outArgValue = getCreatureData().wanderDistance;

            return getCreatureData().spawnPoint.Copy();
        } else {
            dist.outArgValue = 0;

            return getHomePosition().Copy();
        }
    }

    public final void updateMovementFlags() {
        // Do not update movement flags if creature is controlled by a player (charm/vehicle)
        if (getPlayerMovingMe() != null) {
            return;
        }

        // Creatures with CREATURE_FLAG_EXTRA_NO_MOVE_FLAGS_UPDATE should control MovementFlags in your own scripts
        if (getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.NoMoveFlagsUpdate)) {
            return;
        }

        // Set the movement flags if the creature is in that mode. (Only fly if actually in air, only swim if in water, etc)
        var ground = getFloorZ();

        var canHover = getCanHover();
        var isInAir = (MathUtil.fuzzyGt(getLocation().getZ(), ground + (canHover ? getUnitData().HoverHeight : 0.0f) + MapDefine.GroundHeightTolerance) || MathUtil.fuzzyLt(getLocation().getZ(), ground - MapDefine.GroundHeightTolerance)); // Can be underground too, prevent the falling

        if (getMovementTemplate().isFlightAllowed() && (isInAir || !getMovementTemplate().isGroundAllowed()) && !isFalling()) {
            if (getMovementTemplate().flight == CreatureFlightMovementType.CanFly) {
                setCanFly(true);
            } else {
                setDisableGravity(true);
            }

            if (!hasAuraType(AuraType.Hover) && getMovementTemplate().ground != CreatureGroundMovementType.Hover) {
                setHover(false);
            }
        } else {
            setCanFly(false);
            setDisableGravity(false);

            if (isAlive() && (getCanHover() || hasAuraType(AuraType.Hover))) {
                setHover(true);
            }
        }

        if (!isInAir) {
            setFall(false);
        }

        setSwim(canSwim() && isInWater());
    }

    public final void refreshCanSwimFlag() {
        refreshCanSwimFlag(false);
    }

    public final void refreshCanSwimFlag(boolean recheck) {
        if (!isMissingCanSwimFlagOutOfCombat || recheck) {
            isMissingCanSwimFlagOutOfCombat = !hasUnitFlag(UnitFlag.CanSwim);
        }

        // Check if the creature has UNIT_FLAG_CAN_SWIM and add it if it's missing
        // Creatures must be able to chase a target in water if they can enter water
        if (isMissingCanSwimFlagOutOfCombat && canEnterWater()) {
            setUnitFlag(UnitFlag.CanSwim);
        }
    }

    public final void allLootRemovedFromCorpse() {
        var now = gameTime.GetGameTime();

        // Do not reset corpse remove time if corpse is already removed
        if (getCorpseRemoveTime() <= now) {
            return;
        }

        // Scripts can choose to ignore RATE_CORPSE_DECAY_LOOTED by calling setCorpseDelay(timer, true)
        var decayRate = _ignoreCorpseDecayRatio ? 1.0f : WorldConfig.getFloatValue(WorldCfg.RateCorpseDecayLooted);

        // corpse skinnable, but without skinning flag, and then skinned, corpse will despawn next update

//		bool isFullySkinned()
//			{
//				if (loot != null && loot.loot_type == LootType.SKINNING && loot.isLooted())
//					return true;
//
//				foreach (var(_, loot) in personalLoot)
//					if (loot != null && loot.loot_type != LootType.SKINNING || !loot.isLooted())
//						return false;
//
//				return true;
//			}

        if (isFullySkinned()) {
            setCorpseRemoveTime(now);
        } else {
            setCorpseRemoveTime(now + (int) (getCorpseDelay() * decayRate));
        }

        setRespawnTime(Math.max(getCorpseRemoveTime() + getRespawnDelay(), getRespawnTime()));
    }

    public final void applyLevelScaling() {
        CreatureDifficulty creatureDifficulty = getCreatureDifficulty();
        DbcObjectManager dbcObjectManager = getWorldContext().getDbcObjectManager();
        SandboxScaling sandbox = dbcObjectManager.sandboxScaling(creatureDifficulty.sandboxScalingId);
        if (sandbox != null)
        {
            setInt32Value(ObjectFields.UNIT_FIELD_SCALING_LEVEL_MIN, sandbox.getMinLevel());
            setInt32Value(ObjectFields.UNIT_FIELD_SCALING_LEVEL_MAX, sandbox.getMaxLevel());
        }

        int minDelta = Math.min(creatureDifficulty.deltaLevelMax, creatureDifficulty.deltaLevelMin);
        int maxDelta = Math.max(creatureDifficulty.deltaLevelMax, creatureDifficulty.deltaLevelMin);
        int delta = minDelta == maxDelta ? minDelta : RandomUtil.randomInt(minDelta, maxDelta);

        setInt32Value(ObjectFields.UNIT_FIELD_SCALING_LEVEL_DELTA, delta);
        setInt32Value(ObjectFields.UNIT_FIELD_SANDBOX_SCALING_ID, creatureDifficulty.sandboxScalingId);
    }

    @Override
    public float getHealthMultiplierForTarget(WorldObject target) {
        if (!getHasScalableLevels()) {
            return 1.0f;
        }

        var levelForTarget = getLevelForTarget(target);

        if (getLevel() < levelForTarget) {
            return 1.0f;
        }

        return (float) getMaxHealthByLevel(levelForTarget) / getCreateHealth();
    }

    public final float getBaseDamageForLevel(int level) {
        var cInfo = getCreatureTemplate();
        var scaling = cInfo.getLevelScaling(getMap().getDifficultyID());

        return global.getDB2Mgr().EvaluateExpectedStat(ExpectedStatType.CreatureAutoAttackDps, level, cInfo.getHealthScalingExpansion(), scaling.contentTuningId, playerClass.forValue(cInfo.unitClass));
    }

    @Override
    public float getDamageMultiplierForTarget(WorldObject target) {
        if (!getHasScalableLevels()) {
            return 1.0f;
        }

        var levelForTarget = getLevelForTarget(target);

        return getBaseDamageForLevel(levelForTarget) / getBaseDamageForLevel(getLevel());
    }

    @Override
    public float getArmorMultiplierForTarget(WorldObject target) {
        if (!getHasScalableLevels()) {
            return 1.0f;
        }

        var levelForTarget = getLevelForTarget(target);

        return getBaseArmorForLevel(levelForTarget) / getBaseArmorForLevel(getLevel());
    }

    @Override
    public int getLevelForTarget(WorldObject target) {
        var unitTarget = target.toUnit();

        if (unitTarget) {
            if (isWorldBoss()) {
                var level = unitTarget.getLevel() + WorldConfig.getIntValue(WorldCfg.WorldBossLevelDiff);

                tangible.RefObject<Integer> tempRef_level = new tangible.RefObject<Integer>(level);
                var tempVar = (int) MathUtil.RoundToInterval(tempRef_level, 1, 255);
                level = tempRef_level.refArgValue;
                return tempVar;
            }

            // If this creature should scale level, adapt level depending of target level
            // between UNIT_FIELD_SCALING_LEVEL_MIN and UNIT_FIELD_SCALING_LEVEL_MAX
            if (getHasScalableLevels()) {
                int scalingLevelMin = getUnitData().scalingLevelMin;
                int scalingLevelMax = getUnitData().scalingLevelMax;
                int scalingLevelDelta = getUnitData().scalingLevelDelta;
                int scalingFactionGroup = getUnitData().scalingFactionGroup;
                int targetLevel = unitTarget.getUnitData().effectiveLevel;

                if (targetLevel == 0) {
                    targetLevel = unitTarget.getLevel();
                }

                var targetLevelDelta = 0;

                var playerTarget = target.toPlayer();

                if (playerTarget != null) {
                    if (scalingFactionGroup != 0 && CliDB.FactionTemplateStorage.get(CliDB.ChrRacesStorage.get(playerTarget.getRace()).factionID).factionGroup != scalingFactionGroup) {
                        scalingLevelMin = scalingLevelMax;
                    }

                    int maxCreatureScalingLevel = playerTarget.getActivePlayerData().maxCreatureScalingLevel;
                    targetLevelDelta = Math.min(maxCreatureScalingLevel > 0 ? maxCreatureScalingLevel - targetLevel : 0, playerTarget.getActivePlayerData().scalingPlayerLevelDelta);
                }

                var levelWithDelta = targetLevel + targetLevelDelta;
                tangible.RefObject<Integer> tempRef_levelWithDelta = new tangible.RefObject<Integer>(levelWithDelta);
                var level = MathUtil.RoundToInterval(tempRef_levelWithDelta, scalingLevelMin, scalingLevelMax) + scalingLevelDelta;
                levelWithDelta = tempRef_levelWithDelta.refArgValue;

                tangible.RefObject<Integer> tempRef_level2 = new tangible.RefObject<Integer>(level);
                var tempVar2 = (int) MathUtil.RoundToInterval(tempRef_level2, 1, SharedConst.maxLevel + 3);
                level = tempRef_level2.refArgValue;
                return tempVar2;
            }
        }

        return super.getLevelForTarget(target);
    }

    public final String getAIName() {
        return global.getObjectMgr().getCreatureTemplate(getEntry()).AIName;
    }

    public final String getScriptName() {
        return global.getObjectMgr().getScriptName(getScriptId());
    }

    public final int getScriptId() {
        var creatureData = getCreatureData();

        if (creatureData != null) {
            var scriptId = creatureData.scriptId;

            if (scriptId != 0) {
                return scriptId;
            }
        }

        if (getCreatureTemplate().scriptID != 0) {
            return getCreatureTemplate().scriptID;
        }

        return global.getObjectMgr().getCreatureTemplate(getEntry()) != null ? global.getObjectMgr().getCreatureTemplate(getEntry()).ScriptID : 0;
    }

    public final boolean hasStringId(String id) {
        return getStringIds().contains(id);
    }

    public final int getVendorItemCurrentCount(VendorItem vItem) {
        if (vItem.getMaxcount() == 0) {
            return vItem.getMaxcount();
        }

        VendorItemCount vCount = null;

        for (var i = 0; i < vendorItemCounts.size(); i++) {
            vCount = vendorItemCounts.get(i);

            if (vCount.getItemId() == vItem.getItem()) {
                break;
            }
        }

        if (vCount == null) {
            return vItem.getMaxcount();
        }

        var ptime = gameTime.GetGameTime();

        if (vCount.getLastIncrementTime() + vItem.getIncrtime() <= ptime) {
            var pProto = global.getObjectMgr().getItemTemplate(vItem.getItem());

            var diff = (int) ((ptime - vCount.getLastIncrementTime()) / vItem.getIncrtime());

            if ((vCount.getCount() + diff * pProto.getBuyCount()) >= vItem.getMaxcount()) {
                vendorItemCounts.remove(vCount);

                return vItem.getMaxcount();
            }

            vCount.setCount(vCount.getCount() + diff * pProto.getBuyCount());
            vCount.setLastIncrementTime(ptime);
        }

        return vCount.getCount();
    }

    public final int updateVendorItemCurrentCount(VendorItem vItem, int used_count) {
        if (vItem.getMaxcount() == 0) {
            return 0;
        }

        VendorItemCount vCount = null;

        for (var i = 0; i < vendorItemCounts.size(); i++) {
            vCount = vendorItemCounts.get(i);

            if (vCount.getItemId() == vItem.getItem()) {
                break;
            }
        }

        if (vCount == null) {
            var new_count = vItem.getMaxcount() > used_count ? vItem.getMaxcount() - used_count : 0;
            vendorItemCounts.add(new VendorItemCount(vItem.getItem(), new_count));

            return new_count;
        }

        var ptime = gameTime.GetGameTime();

        if (vCount.getLastIncrementTime() + vItem.getIncrtime() <= ptime) {
            var pProto = global.getObjectMgr().getItemTemplate(vItem.getItem());

            var diff = (int) ((ptime - vCount.getLastIncrementTime()) / vItem.getIncrtime());

            if ((vCount.getCount() + diff * pProto.getBuyCount()) < vItem.getMaxcount()) {
                vCount.setCount(vCount.getCount() + diff * pProto.getBuyCount());
            } else {
                vCount.setCount(vItem.getMaxcount());
            }
        }

        vCount.setCount(vCount.getCount() > used_count ? vCount.getCount() - used_count : 0);
        vCount.setLastIncrementTime(ptime);

        return vCount.getCount();
    }

    @Override
    public String getName() {
        return getName(locale.enUS);
    }

    @Override
    public String getName(Locale locale) {
        if (locale != locale.enUS) {
            var cl = global.getObjectMgr().getCreatureLocale(getEntry());

            if (cl != null) {
                if (cl.name.length > locale.getValue() && !cl.name.charAt(new integer(locale.getValue())).isEmpty()) {
                    return cl.name.charAt(locale.getValue());
                }
            }
        }

        return super.getName(locale);
    }

    public int getPetAutoSpellOnPos(byte pos) {
        if (pos >= SharedConst.MaxSpellCharm || getCharmInfo() == null || getCharmInfo().getCharmSpell(pos).getActiveState() != ActiveStates.enabled) {
            return 0;
        } else {
            return getCharmInfo().getCharmSpell(pos).getAction();
        }
    }

    public final float getPetChaseDistance() {
        var range = 0f;

        for (byte i = 0; i < getPetAutoSpellSize(); ++i) {
            var spellID = getPetAutoSpellOnPos(i);

            if (spellID == 0) {
                continue;
            }

            var spellInfo = global.getSpellMgr().getSpellInfo(spellID, getMap().getDifficultyID());

            if (spellInfo != null) {
                if (spellInfo.getRecoveryTime1() == 0 && spellInfo.getRangeEntry().id != 1 && spellInfo.getRangeEntry().id != 2 && spellInfo.getMaxRange() > range) {
                    range = spellInfo.getMaxRange();
                }
            }
        }

        return range;
    }

    public final float getAggroRange(Unit target) {
        // Determines the aggro range for creatures (usually pets), used mainly for aggressive pet target selection.
        // Based on data from wowwiki due to lack of 3.3.5a data

        if (target != null && isPet()) {
            int targetLevel = 0;

            if (target.isTypeId(TypeId.PLAYER)) {
                targetLevel = target.getLevelForTarget(this);
            } else if (target.isTypeId(TypeId.UNIT)) {
                targetLevel = target.toCreature().getLevelForTarget(this);
            }

            var myLevel = getLevelForTarget(target);
            var levelDiff = targetLevel - myLevel;

            // The maximum Aggro Radius is capped at 45 yards (25 level difference)
            if (levelDiff < -25) {
                levelDiff = -25;
            }

            // The base aggro radius for mob of same level
            double aggroRadius = 20;

            // Aggro Radius varies with level difference at a rate of roughly 1 yard/level
            aggroRadius -= levelDiff;

            // detect range auras
            aggroRadius += getTotalAuraModifier(AuraType.ModDetectRange);

            // detected range auras
            aggroRadius += target.getTotalAuraModifier(AuraType.ModDetectedRange);

            // Just in case, we don't want pets running all over the map
            if (aggroRadius > SharedConst.MaxAggroRadius) {
                aggroRadius = SharedConst.MaxAggroRadius;
            }

            // Minimum Aggro Radius for a mob seems to be combat range (5 yards)
            //  hunter pets seem to ignore minimum aggro radius so we'll default it a little higher
            if (aggroRadius < 10) {
                aggroRadius = 10;
            }

            return (float) (aggroRadius);
        }

        // Default
        return 0.0f;
    }

    public final Unit selectNearestHostileUnitInAggroRange(boolean useLOS) {
        return selectNearestHostileUnitInAggroRange(useLOS, false);
    }

    public final Unit selectNearestHostileUnitInAggroRange() {
        return selectNearestHostileUnitInAggroRange(false, false);
    }

    public final Unit selectNearestHostileUnitInAggroRange(boolean useLOS, boolean ignoreCivilians) {
        // Selects nearest hostile target within creature's aggro range. Used primarily by
        //  pets set to aggressive. Will not return neutral or friendly targets
        var u_check = new NearestHostileUnitInAggroRangeCheck(this, useLOS, ignoreCivilians);
        var searcher = new UnitSearcher(this, u_check, gridType.Grid);
        Cell.visitGrid(this, searcher, SharedConst.MaxAggroRadius);

        return searcher.getTarget();
    }

    @Override
    public void setDisplayId(int modelId) {
        setDisplayId(modelId, 1f);
    }

    @Override
    public void setDisplayId(int modelId, float displayScale) {
        super.setDisplayId(modelId, displayScale);

        var minfo = global.getObjectMgr().getCreatureModelInfo(modelId);

        if (minfo != null) {
            setBoundingRadius((isPet() ? 1.0f : minfo.boundingRadius) * getObjectScale());
            setCombatReach((isPet() ? ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH : minfo.combatReach) * getObjectScale());
        }
    }

    public final void setDisplayFromModel(int modelIdx) {
        var model = getCreatureTemplate().getModelByIdx(modelIdx);

        if (model != null) {
            setDisplayId(model.creatureDisplayId, model.displayScale);
        }
    }

    @Override
    public void setTarget(ObjectGuid guid) {
        if (hasSpellFocus()) {
            spellFocusInfo.target = guid;
        } else {
            setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().target), guid);
        }
    }

    public final void setSpellFocus(Spell focusSpell, WorldObject target) {
        // Pointer validation and checking for a already existing focus
        if (spellFocusInfo.spell != null || focusSpell == null) {
            return;
        }

        // Prevent dead / feign death creatures from setting a focus target
        if (!isAlive() || hasUnitFlag2(UnitFlag2.FeignDeath) || hasAuraType(AuraType.FeignDeath)) {
            return;
        }

        // Don't allow stunned creatures to set a focus target
        if (hasUnitFlag(UnitFlag.Stunned)) {
            return;
        }

        // some spells shouldn't track targets
        if (focusSpell.isFocusDisabled()) {
            return;
        }

        var spellInfo = focusSpell.spellInfo;

        // don't use spell focus for vehicle spells
        if (spellInfo.hasAura(AuraType.ControlVehicle)) {
            return;
        }

        // instant non-channeled casts and non-target spells don't need facing updates
        if (target == null && (focusSpell.getCastTime() == 0 && !spellInfo.isChanneled())) {
            return;
        }

        // store pre-cast values for target and orientation (used to later restore)
        if (spellFocusInfo.delay == 0) {
            // only overwrite these fields if we aren't transitioning from one spell focus to another
            spellFocusInfo.target = getTarget();
            spellFocusInfo.orientation = getLocation().getO();
        } else // don't automatically reacquire target for the previous spellcast
        {
            spellFocusInfo.delay = 0;
        }

        spellFocusInfo.spell = focusSpell;

        var noTurnDuringCast = spellInfo.hasAttribute(SpellAttr5.AiDoesntFaceTarget);
        var turnDisabled = hasUnitFlag2(UnitFlag2.CannotTurn);
        // set target, then force send update packet to players if it changed to provide appropriate facing
        var newTarget = (target != null && !noTurnDuringCast && !turnDisabled) ? target.getGUID() : ObjectGuid.Empty;

        if (ObjectGuid.opNotEquals(getTarget(), newTarget)) {
            setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().target), newTarget);
        }

        // If we are not allowed to turn during cast but have a focus target, face the target
        if (!turnDisabled && noTurnDuringCast && target) {
            setFacingToObject(target, false);
        }

        if (!noTurnDuringCast) {
            addUnitState(UnitState.Focusing);
        }
    }

    @Override
    public boolean hasSpellFocus() {
        return hasSpellFocus(null);
    }

    @Override
    public boolean hasSpellFocus(Spell focusSpell) {
        if (isDead()) // dead creatures cannot focus
        {
            if (spellFocusInfo.spell != null || spellFocusInfo.delay != 0) {
                Log.outWarn(LogFilter.unit, String.format("Creature '%1$s' (entry %2$s) has spell focus (spell id %3$s, delay %4$sms) despite being dead.", getName(), getEntry(), (spellFocusInfo.spell != null ? spellFocusInfo.spell.spellInfo.getId() : 0), spellFocusInfo.delay));
            }

            return false;
        }

        if (focusSpell) {
            return focusSpell == spellFocusInfo.spell;
        } else {
            return spellFocusInfo.spell != null || spellFocusInfo.delay != 0;
        }
    }

    public final void releaseSpellFocus(Spell focusSpell) {
        releaseSpellFocus(focusSpell, true);
    }

    public final void releaseSpellFocus() {
        releaseSpellFocus(null, true);
    }

    public final void releaseSpellFocus(Spell focusSpell, boolean withDelay) {
        if (!spellFocusInfo.spell) {
            return;
        }

        // focused to something else
        if (focusSpell && focusSpell != spellFocusInfo.spell) {
            return;
        }

        if (spellFocusInfo.spell.spellInfo.hasAttribute(SpellAttr5.AiDoesntFaceTarget)) {
            clearUnitState(UnitState.Focusing);
        }

        if (isPet()) // player pets do not use delay system
        {
            if (!hasUnitFlag2(UnitFlag2.CannotTurn)) {
                reacquireSpellFocusTarget();
            }
        } else // don't allow re-target right away to prevent visual bugs
        {
            spellFocusInfo.delay = withDelay ? 1000 : 1;
        }

        spellFocusInfo.spell = null;
    }

    public final void doNotReacquireSpellFocusTarget() {
        spellFocusInfo.delay = 0;
        spellFocusInfo.spell = null;
    }

    public final void setCorpseDelay(int delay, boolean ignoreCorpseDecayRatio) {
        setCorpseDelay(delay);

        if (ignoreCorpseDecayRatio) {
            ignoreCorpseDecayRatio = true;
        }
    }

    public final boolean hasReactState(ReactState state) {
        return (getReactState() == state);
    }

    @Override
    public void setImmuneToAll(boolean apply) {
        setImmuneToAll(apply, hasReactState(ReactState.PASSIVE));
    }

@Override
    public void setImmuneToPC(boolean apply) {
        setImmuneToPC(apply, hasReactState(ReactState.PASSIVE));
    }

@Override
    public void setImmuneToNPC(boolean apply) {
        setImmuneToNPC(apply, hasReactState(ReactState.PASSIVE));
    }

    public final <T extends CreatureAI> T getAI() {
        return (T) getAi();
    }


        @Override
    public SpellSchoolMask getMeleeDamageSchoolMask() {
        return getMeleeDamageSchoolMask(WeaponAttackType.BASE_ATTACK);
    }

        @Override
    public SpellSchoolMask getMeleeDamageSchoolMask(WeaponAttackType attackType) {
        return meleeDamageSchoolMask;
    }

    public final void setMeleeDamageSchool(SpellSchool school) {
        meleeDamageSchoolMask = SpellSchoolMask.valueOf(school);
    }

    @Override
    public boolean loadFromDB(long spawnId, Map map, boolean addToMap, boolean allowDuplicate) {
        if (!allowDuplicate) {
            // If an alive instance of this spawnId is already found, skip creation
            // If only dead instance(s) exist, despawn them and spawn a new (maybe also dead) version
            var creatureBounds = map.getCreatureBySpawnIdStore().get(spawnId);
            ArrayList<Creature> despawnList = new ArrayList<>();

            for (var creature : creatureBounds) {
                if (creature.isAlive()) {
                    Logs.MAPS.debug("Would have spawned {0} but {1} already exists", spawnId, creature.getGUID().toString());

                    return false;
                } else {
                    despawnList.add(creature);
                    Logs.MAPS.debug("Despawned dead instance of spawn {0} ({1})", spawnId, creature.getGUID().toString());
                }
            }

            for (var despawnCreature : despawnList) {
                despawnCreature.addObjectToRemoveList();
            }
        }

        var data = global.getObjectMgr().getCreatureData(spawnId);

        if (data == null) {
            Logs.SQL.error(String.format("Creature (SpawnID: %1$s) not found in table `creature`, can't load.", spawnId));

            return false;
        }

        setSpawnId(spawnId);
        setRespawnCompatibilityMode(data.getSpawnGroupData().getFlags().hasFlag(SpawnGroupFlags.CompatibilityMode));
        setCreatureData(data);
        setWanderDistance(data.wanderDistance);
        setRespawnDelay((int) data.spawntimesecs);

        if (!create(map.generateLowGuid(HighGuid.Creature), map, data.id, data.spawnPoint, data, 0, !getRespawnCompatibilityMode())) {
            return false;
        }

        //We should set first home position, because then AI calls home movement
        setHomePosition(getLocation());

        setDeathState(deathState.Alive);

        setRespawnTime(getMap().getCreatureRespawnTime(getSpawnId()));

        if (getRespawnTime() == 0 && !map.isSpawnGroupActive(data.getSpawnGroupData().getGroupId())) {
            if (!getRespawnCompatibilityMode()) {
                // @todo pools need fixing! this is just a temporary thing, but they violate dynspawn principles
                if (data.poolId == 0) {
                    Log.outError(LogFilter.unit, String.format("Creature (SpawnID %1$s) trying to load in inactive spawn group '%2$s':\n%3$s", spawnId, data.getSpawnGroupData().getName(), getDebugInfo()));

                    return false;
                }
            }

            setRespawnTime(gameTime.GetGameTime() + RandomUtil.URand(4, 7));
        }

        if (getRespawnTime() != 0) {
            if (!getRespawnCompatibilityMode()) {
                // @todo same as above
                if (data.poolId == 0) {
                    Log.outError(LogFilter.unit, String.format("Creature (SpawnID %1$s) trying to load despite a respawn timer in progress:\n%2$s", spawnId, getDebugInfo()));

                    return false;
                }
            } else {
                // compatibility mode creatures will be respawned in ::Update()
                setDeathState(deathState.Dead);
            }

            if (canFly()) {
                var tz = map.getHeight(getPhaseShift(), data.spawnPoint, true, MapDefine.MaxFallDistance);

                if (data.spawnPoint.getZ() - tz > 0.1f && MapDefine.isValidMapCoordinatei(tz)) {
                    getLocation().relocate(data.spawnPoint.getX(), data.spawnPoint.getY(), tz);
                }
            }
        }

        setSpawnHealth();

        selectWildBattlePetLevel();

        // checked at creature_template loading
        setDefaultMovementType(MovementGeneratorType.forValue(data.movementType));

        getStringIds()[1] = data.stringId;

        return !addToMap || getMap().addToMap(this);
    }

    public final LootMode getLootMode() {
        return lootMode;
    }

    public final void setLootMode(LootMode lootMode) {
        lootMode = lootMode;
    }

    public final boolean hasLootMode(LootMode lootMode) {
        return (boolean) (lootMode.getValue() & lootMode.getValue());
    }

    public final void addLootMode(LootMode lootMode) {
        lootMode = LootMode.forValue(lootMode.getValue() | lootMode.getValue());
    }

    public final void removeLootMode(LootMode lootMode) {
        lootMode = LootMode.forValue(lootMode.getValue() & ~lootMode.getValue());
    }

    public final void resetLootMode() {
        lootMode = LootMode.Default;
    }

    public final void setNoCallAssistance(boolean val) {
        alreadyCallAssistance = val;
    }

    public final void setNoSearchAssistance(boolean val) {
        setHasSearchedAssistance(val);
    }

    @Override
    public MovementGeneratorType getDefaultMovementType() {
        return getDefaultMovementType();
    }

    public final void doImmediateBoundaryCheck() {
        boundaryCheckTime = 0;
    }

    public final void setRegenerateHealth(boolean value) {
        getStaticFlags().modifyFlag(CreatureStaticFlags5.NO_HEALTH_REGEN, !value);
    }

    public final void setHomePosition(float x, float y, float z, float o) {
        homePosition.relocate(x, y, z, o);
    }

    public final void setUnkillable(boolean value) {
        getStaticFlags().modifyFlag(CreatureStaticFlags.UNKILLABLE, value);
    }

    public final void setTransportHomePosition(float x, float y, float z, float o) {
        transportHomePosition.relocate(x, y, z, o);
    }

    public final void loadPath(int pathid) {
        setWaypointPath(pathid);
    }

    public final void updateCurrentWaypointInfo(int nodeId, int pathId) {
        _currentWaypointNodeInfo = (nodeId, pathId)
    }

    public final void resetPlayerDamageReq() {
        setPlayerDamageReq((int) (getHealth() / 2));
    }

    private void regenerateHealth() {
        if (!getCanRegenerateHealth()) {
            return;
        }

        var curValue = getHealth();
        var maxValue = getMaxHealth();

        if (curValue >= maxValue) {
            return;
        }

        double addvalue;

        // Not only pet, but any controlled creature (and not polymorphed)
        if (!getCharmerOrOwnerGUID().isEmpty() && !isPolymorphed()) {
            var HealthIncreaseRate = WorldConfig.getFloatValue(WorldCfg.RateHealth);
            addvalue = 0.015f * getMaxHealth() * HealthIncreaseRate;
        } else {
            addvalue = maxValue / 3;
        }

        // Apply modifiers (if any).
        addvalue *= getTotalAuraMultiplier(AuraType.ModHealthRegenPercent);
        addvalue += getTotalAuraModifier(AuraType.ModRegen) * SharedConst.CreatureRegenInterval / (5 * time.InMilliseconds);

        modifyHealth(addvalue);
    }

    private boolean destoryAI() {
        popAI();
        refreshAI();

        return true;
    }

    private void initializeMovementAI() {
        if (getFormation() != null) {
            if (getFormation().getLeader() == this) {
                getFormation().formationReset(false);
            } else if (getFormation().isFormed()) {
                getMotionMaster().moveIdle(); //wait the order of leader

                return;
            }
        }

        getMotionMaster().initialize();
    }

    private void selectWildBattlePetLevel() {
        if (isWildBattlePet()) {
            byte wildBattlePetLevel = 1;

            var areaTable = CliDB.AreaTableStorage.get(getZoneId());

            if (areaTable != null) {
                if (areaTable.WildBattlePetLevelMin > 0) {
                    wildBattlePetLevel = (byte) RandomUtil.URand(areaTable.WildBattlePetLevelMin, areaTable.WildBattlePetLevelMax);
                }
            }

            setWildBattlePetLevel(wildBattlePetLevel);
        }
    }

    private boolean createFromProto(long guidlow, int entry, CreatureData data) {
        return createFromProto(guidlow, entry, data, 0);
    }

    private boolean createFromProto(long guidlow, int entry) {
        return createFromProto(guidlow, entry, null, 0);
    }

    private boolean createFromProto(long guidlow, int entry, CreatureData data, int vehId) {
        setZoneScript();

        if (getZoneScript() != null && data != null) {
            entry = getZoneScript().getCreatureEntry(guidlow, data);

            if (entry == 0) {
                return false;
            }
        }

        var cinfo = global.getObjectMgr().getCreatureTemplate(entry);

        if (cinfo == null) {
            Logs.SQL.error("Creature.CreateFromProto: creature template (guidlow: {0}, entry: {1}) does not exist.", guidlow, entry);

            return false;
        }

        setOriginalEntry(entry);

        if (vehId != 0 || cinfo.vehicleId != 0) {
            create(ObjectGuid.create(HighGuid.Vehicle, getLocation().getMapId(), entry, guidlow));
        } else {
            create(ObjectGuid.create(HighGuid.Creature, getLocation().getMapId(), entry, guidlow));
        }

        if (!updateEntry(entry, data)) {
            return false;
        }

        if (vehId == 0) {
            if (getCreatureTemplate().vehicleId != 0) {
                vehId = getCreatureTemplate().vehicleId;
                entry = getCreatureTemplate().entry;
            } else {
                vehId = cinfo.vehicleId;
            }
        }

        if (vehId != 0) {
            if (createVehicleKit(vehId, entry, true)) {
                updateDisplayPower();
            }
        }

        return true;
    }

    private void loadTemplateRoot() {
        if (getMovementTemplate().isRooted()) {
            setControlled(true, UnitState.Root);
        }
    }

    private void initializeMovementFlags() {
        // It does the same, for now
        updateMovementFlags();
    }

    private long getMaxHealthByLevel(int level) {
        var cInfo = getCreatureTemplate();
        var scaling = cInfo.getLevelScaling(getMap().getDifficultyID());
        var baseHealth = global.getDB2Mgr().EvaluateExpectedStat(ExpectedStatType.CreatureHealth, level, cInfo.healthScalingExpansion, scaling.contentTuningId, playerClass.forValue(cInfo.unitClass));

        return (long) (baseHealth * cInfo.ModHealth * cInfo.modHealthExtra);
    }

    private float getBaseArmorForLevel(int level) {
        float baseHealth = getGameTableColumnForClass(sNpcTotalHpGameTable[creatureDifficulty->GetHealthScalingExpansion()].GetRow(level), cInfo->unit_class);
        return baseArmor * cInfo.modArmor;
    }

    private void setScriptStringId(String id) {
        if (!id.isEmpty()) {
            scriptStringId = id;
            getStringIds()[2] = scriptStringId;
        } else {
            scriptStringId = null;
            getStringIds()[2] = null;
        }
    }

    private void reacquireSpellFocusTarget() {
        if (!hasSpellFocus()) {
            Log.outError(LogFilter.unit, String.format("Creature::ReacquireSpellFocusTarget() being called with hasSpellFocus() returning false. %1$s", getDebugInfo()));

            return;
        }

        setUpdateFieldValue(getValues().modifyValue(getUnitData()).modifyValue(getUnitData().target), spellFocusInfo.target);

        if (!hasUnitFlag2(UnitFlag2.CannotTurn)) {
            if (!spellFocusInfo.target.isEmpty()) {
                var objTarget = global.getObjAccessor().GetWorldObject(this, spellFocusInfo.target);

                if (objTarget) {
                    setFacingToObject(objTarget, false);
                }
            } else {
                setFacingTo(spellFocusInfo.orientation, false);
            }
        }

        spellFocusInfo.delay = 0;
    }

    private void setDisableReputationGain(boolean disable) {
        setReputationGainDisabled(disable);
    }

    public final long getPlayerDamageReq() {
        return playerDamageReq;
    }

    public final void setPlayerDamageReq(long value) {
        playerDamageReq = value;
    }

    public final float getSightDistance() {
        return sightDistance;
    }

    public final void setSightDistance(float value) {
        sightDistance = value;
    }

    public final float getCombatDistance() {
        return combatDistance;
    }

    public final void setCombatDistance(float value) {
        combatDistance = value;
    }

    public final boolean isTempWorldObject() {
        return isTempWorldObject;
    }

    public final void setTempWorldObject(boolean value) {
        isTempWorldObject = value;
    }

    public final int getOriginalEntry() {
        return originalEntry;
    }

    public final void setOriginalEntry(int value) {
        originalEntry = value;
    }

    public final int getLootId() {
        if (lootid != null) {
            return lootid.intValue();
        }

        return getCreatureTemplate().lootId;
    }

    public final void setLootId(int value) {
        lootid = value;
    }

    public final HashMap<ObjectGuid, loot> getPersonalLoot() {
        return personalLoot;
    }

    public final void setPersonalLoot(HashMap<ObjectGuid, loot> value) {
        personalLoot = value;
    }

    public final MovementGeneratorType getDefaultMovementType() {
        return defaultMovementType;
    }

    public final void setDefaultMovementType(MovementGeneratorType mgt) {
        setDefaultMovementType(mgt);
    }

    public final void setDefaultMovementType(MovementGeneratorType value) {
        defaultMovementType = value;
    }


    public final StaticCreatureFlags getStaticFlags() {
        return staticFlags;
    }

    public final void setStaticFlags(StaticCreatureFlags value) {
        staticFlags = value;
    }

    public final int[] getSpells() {
        return spells;
    }

    public final void setSpells(int[] value) {
        spells = value;
    }

    public final long getCorpseRemoveTime() {
        return corpseRemoveTime;
    }

    public final void setCorpseRemoveTime(long value) {
        corpseRemoveTime = value;
    }

    public final Loot getLoot() {
        return loot;
    }

    public final void setLoot(Loot value) {
        loot = value;
    }

    public final boolean getCanHaveLoot() {
        return !getStaticFlags().hasFlag(CreatureStaticFlags.NO_LOOT);
    }

    public final void setCanHaveLoot(boolean value) {
        getStaticFlags().modifyFlag(CreatureStaticFlags.NO_LOOT, !value);
    }

    public final boolean getCanMelee() {
        return !getStaticFlags().hasFlag(CreatureStaticFlags.NO_MELEE);
    }

    public final void setCanMelee(boolean value) {
        getStaticFlags().modifyFlag(CreatureStaticFlags.NO_MELEE, !value);
    }

    public final int getGossipMenuId() {
        if (gossipMenuId != null) {
            return gossipMenuId;
        }
        return getCreatureTemplate().gossipMenuId;
    }

    public final void setGossipMenuId(int value) {
        gossipMenuId = value;
    }

    public final boolean isReturningHome() {
        return getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.home;
    }

    public final boolean isFormationLeader() {
        if (getFormation() == null) {
            return false;
        }

        return getFormation().isLeader(this);
    }

    public final boolean isFormationLeaderMoveAllowed() {
        if (getFormation() == null) {
            return false;
        }

        return getFormation().canLeaderStartMoving();
    }

    public final boolean getCanGiveExperience() {
        return !getStaticFlags().hasFlag(CreatureStaticFlags.NO_XP);
    }

    @Override
    public boolean isEngaged() {
        var ai = getAI();

        if (ai != null) {
            return ai.isEngaged();
        }

        return false;
    }

    public final boolean isEscorted() {
        var ai = getAI();

        if (ai != null) {
            return ai.isEscorted();
        }

        return false;
    }

    public final boolean getCanGeneratePickPocketLoot() {
        return pickpocketLootRestore <= gameTime.GetGameTime();
    }

    public final boolean isFullyLooted() {
        if (getLoot() != null && !getLoot().isLooted()) {
            return false;
        }


        for (var(_, loot) : getPersonalLoot()) {
            if (loot != null && !loot.isLooted()) {
                return false;
            }
        }

        return true;
    }

    public final HashSet<ObjectGuid> getTapList() {
        return tapList;
    }

    public final void setTapList(HashSet<ObjectGuid> tapList) {
        setTapList(tapList);
    }

    private void setTapList(HashSet<ObjectGuid> value) {
        tapList = value;
    }

    public final boolean getHasLootRecipient() {
        return !getTapList().isEmpty();
    }

    public final boolean isElite() {
        if (isPet()) {
            return false;
        }

        return getCreatureTemplate().rank != CreatureClassification.Elite && getCreatureTemplate().rank != CreatureClassification.RareElite;
    }

    public final boolean isWorldBoss() {
        if (isPet()) {
            return false;
        }

        return (boolean) (getCreatureTemplate().typeFlags.getValue() & CreatureTypeFlags.BossMob.getValue());
    }

    public final long getRespawnTimeEx() {
        var now = gameTime.GetGameTime();

        if (getRespawnTime() > now) {
            return getRespawnTime();
        } else {
            return now;
        }
    }

    public final Position getRespawnPosition() {
        tangible.OutObject<Float> tempOut__ = new tangible.OutObject<Float>();
        var tempVar = getRespawnPosition(tempOut__);
        _ = tempOut__.outArgValue;
        return tempVar;
    }

    public final CreatureMovementData getMovementTemplate() {
        CreatureMovementData movementOverride;
        tangible.OutObject<CreatureMovementData> tempOut_movementOverride = new tangible.OutObject<CreatureMovementData>();
        if (global.getObjectMgr().tryGetGetCreatureMovementOverride(getSpawnId(), tempOut_movementOverride)) {
            movementOverride = tempOut_movementOverride.outArgValue;
            return movementOverride;
        } else {
            movementOverride = tempOut_movementOverride.outArgValue;
        }

        return getCreatureTemplate().movement;
    }


    // Returns true if CREATURE_STATIC_FLAG_AQUATIC is set which strictly binds the creature to liquids
    public final boolean isAquatic() {
        return staticFlags.hasFlag(CreatureStaticFlag.AQUATIC);
    }

    // Returns true if CREATURE_STATIC_FLAG_AMPHIBIOUS is set which allows a creature to enter and leave liquids while sticking to the ocean floor. These creatures will become able to swim when engaged
    public final boolean isAmphibious() {
        return staticFlags.hasFlag(CreatureStaticFlag.AMPHIBIOUS);
    }

    // Returns true if CREATURE_STATIC_FLAG_FLOATING is set which is  disabling the gravity of the creature on spawn and reset
    public final boolean isFloating() {
        return staticFlags.hasFlag(CreatureStaticFlag.FLOATING);
    }

    public final void setFloating(boolean floating) {
        staticFlags.applyFlag(CreatureStaticFlag.FLOATING, floating);
        setDisableGravity(floating);
    }

    // Returns true if CREATURE_STATIC_FLAG_SESSILE is set which permanently roots the creature in place
    public final boolean isSessile() {
        return staticFlags.hasFlag(CreatureStaticFlag.SESSILE);
    }

    public final void setSessile(boolean sessile) {
        staticFlags.applyFlag(CreatureStaticFlag.SESSILE, sessile);
        setControlled(sessile, UnitState.ROOT);
    }

    // Returns true if CREATURE_STATIC_FLAG_3_CANNOT_PENETRATE_WATER is set which does not allow the creature to go below liquid surfaces
    public final boolean cannotPenetrateWater() {
        return staticFlags.hasFlag(CreatureStaticFlag3.CANNOT_PENETRATE_WATER);
    }

    public final void setCannotPenetrateWater(boolean cannotPenetrateWater) {
        staticFlags.applyFlag(CreatureStaticFlag3.CANNOT_PENETRATE_WATER, cannotPenetrateWater);
    }

    // Returns true if CREATURE_STATIC_FLAG_3_CANNOT_SWIM is set which prevents 'Amphibious' creatures from swimming when engaged
    public final boolean isSwimDisabled() {
        return staticFlags.hasFlag(CreatureStaticFlag3.CANNOT_SWIM);
    }

    // Returns true if CREATURE_STATIC_FLAG_4_PREVENT_SWIM is set which prevents 'Amphibious' creatures from swimming when engaged until the victim is no longer on the ocean floor
    public final boolean isSwimPrevented() {
        return staticFlags.hasFlag(CreatureStaticFlag4.PREVENT_SWIM);
    }


    @Override
    public boolean canSwim() {
        if (super.canSwim()) {
            return true;
        }

        return isPet();
    }

    @Override
    public boolean canEnterWater() {
        if (canSwim()) {
            return true;
        }

        return getMovementTemplate().isSwimAllowed();
    }

    public final boolean getHasCanSwimFlagOutOfCombat() {
        return !isMissingCanSwimFlagOutOfCombat;
    }

    public final boolean getHasScalableLevels() {
        return getUnitData().contentTuningID != 0;
    }

    public final String[] getStringIds() {
        return stringIds;
    }

    public final VendorItemData getVendorItems() {
        return global.getObjectMgr().getNpcVendorItemList(getEntry());
    }

    public byte getPetAutoSpellSize() {
        return 4;
    }

    @Override
    public float getNativeObjectScale() {
        return getCreatureTemplate().scale;
    }

    public final int getCorpseDelay() {
        return corpseDelay;
    }

    public final void setCorpseDelay(int delay) {
        setCorpseDelay(delay, false);
    }

    private void setCorpseDelay(int value) {
        corpseDelay = value;
    }

    public final boolean isRacialLeader() {
        return getCreatureTemplate().racialLeader;
    }

    public final boolean isCivilian() {
        return getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.Civilian);
    }

    public final boolean isTrigger() {
        return getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.trigger);
    }

    public final boolean isGuard() {
        return getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.Guard);
    }

    public final boolean canWalk() {
        return getMovementTemplate().isGroundAllowed();
    }

    @Override
    public boolean canFly() {
        return getMovementTemplate().isFlightAllowed() || isFlying();
    }

    public final boolean isDungeonBoss() {
        return (getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.DungeonBoss));
    }

    @Override
    public boolean isAffectedByDiminishingReturns() {
        return super.isAffectedByDiminishingReturns() || getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.AllDiminish);
    }

    public final ReactState getReactState() {
        return reactState;
    }

    public final void setReactState(ReactState value) {
        reactState = value;
    }

    public final boolean isInEvadeMode() {
        return hasUnitState(UnitState.Evade);
    }

    public final boolean isEvadingAttacks() {
        return isInEvadeMode() || getCannotReachTarget();
    }

    public final byte getOriginalEquipmentId() {
        return originalEquipmentId;
    }

    private void setOriginalEquipmentId(byte value) {
        originalEquipmentId = value;
    }

    public final byte getCurrentEquipmentId() {
        return currentEquipmentId;
    }

    public final void setCurrentEquipmentId(byte value) {
        currentEquipmentId = value;
    }

    public final CreatureTemplate getCreatureTemplate() {
        return creatureTemplate;
    }

    private void setCreatureTemplate(CreatureTemplate value) {
        creatureTemplate = value;
    }

    public final CreatureData getCreatureData() {
        return creatureData;
    }

    private void setCreatureData(CreatureData value) {
        creatureData = value;
    }

    public final boolean isReputationGainDisabled() {
        return isReputationGainDisabled;
    }

    private void setReputationGainDisabled(boolean value) {
        isReputationGainDisabled = value;
    }

    private CreatureAddon getCreatureAddon() {
        if (getSpawnId() != 0) {
            if (creatureData.creatureAddon != null) {
                return creatureData.creatureAddon;
            }
        }
        // dependent from difficulty mode entry
        return creatureTemplate.creatureTemplateAddon;
    }

    private boolean isSpawnedOnTransport() {
        return getCreatureData() != null && getCreatureData().getMapId() != getLocation().getMapId();
    }

    private boolean getCannotReachTarget() {
        return cannotReachTarget;
    }

    public final void setCannotReachTarget(boolean cannotReach) {
        if (cannotReach == getCannotReachTarget()) {
            return;
        }

        setCannotReachTarget(cannotReach);
        cannotReachTimer = 0;

        if (cannotReach) {
            Log.outDebug(LogFilter.unit, String.format("Creature::SetCannotReachTarget() called with true. Details: %1$s", getDebugInfo()));
        }
    }

    private void setCannotReachTarget(boolean value) {
        cannotReachTarget = value;
    }

    public final boolean getCanHover() {
        return getMovementTemplate().ground == CreatureGroundMovementType.Hover || isHovering();
    }

    // (secs) interval at which the creature pulses the entire zone into combat (only works in dungeons)
    public final int getCombatPulseDelay() {
        return combatPulseDelay;
    }

    public final void setCombatPulseDelay(int value) {
        combatPulseDelay = value;

        if (combatPulseTime == 0 || combatPulseTime > value) {
            combatPulseTime = value;
        }
    }

    public final long getLastDamagedTime() {
        return lastDamagedTime;
    }

    public final void setLastDamagedTime(long value) {
        lastDamagedTime = value;
    }

    public final boolean getHasSearchedAssistance() {
        return hasSearchedAssistance;
    }

    private void setHasSearchedAssistance(boolean value) {
        hasSearchedAssistance = value;
    }

    public final boolean getCanIgnoreFeignDeath() {
        return getCreatureTemplate().flagsExtra.hasFlag(CreatureFlagExtra.IgnoreFeighDeath);
    }

    public final long getRespawnTime() {
        return respawnTime;
    }

    public final void setRespawnTime(int respawn) {
        setRespawnTime(respawn != 0 ? gameTime.GetGameTime() + respawn : 0);
    }

    private void setRespawnTime(long value) {
        respawnTime = value;
    }


    public final int getRespawnDelay() {
        return respawnDelay;
    }


    public final void setRespawnDelay(int value) {
        respawnDelay = value;
    }

    public final float getWanderDistance() {
        return wanderDistance;
    }

    public final void setWanderDistance(float value) {
        wanderDistance = value;
    }

    public final boolean getCanRegenerateHealth() {
        return !getStaticFlags().hasFlag(CreatureStaticFlags5.NO_HEALTH_REGEN) && regenerateHealth;
    }

    public final Position getHomePosition() {
        return homePosition;
    }

    public final void setHomePosition(Position value) {
        homePosition.relocate(value);
    }

    public final Position getTransportHomePosition() {
        return transportHomePosition;
    }

    public final void setTransportHomePosition(Position value) {
        transportHomePosition.relocate(value);
    }


//	public (uint nodeId, uint pathId) CurrentWaypointInfo
//		{
//			get
//			{
//				return _currentWaypointNodeInfo;
//			}
//		}


    public final int getWaypointPath() {
        return waypointPath;
    }


    private void setWaypointPath(int value) {
        waypointPath = value;
    }

    public final CreatureGroup getFormation() {
        return formation;
    }

    public final void setFormation(CreatureGroup value) {
        formation = value;
    }

    public final boolean getRespawnCompatibilityMode() {
        return respawnCompatibilityMode;
    }

    private void setRespawnCompatibilityMode(boolean value) {
        respawnCompatibilityMode = value;
    }


    @Override
    public boolean updateStats(Stats stat) {
        return true;
    }

    @Override
    public boolean updateAllStats() {
        updateMaxHealth();
        updateAttackPowerAndDamage();
        updateAttackPowerAndDamage(true);

        for (var i = powerType.mana; i.getValue() < powerType.max.getValue(); ++i) {
            updateMaxPower(i);
        }

        updateAllResistances();

        return true;
    }

    @Override
    public void updateArmor() {
        var baseValue = getFlatModifierValue(UnitMod.ARMOR, UnitModifierFlatType.BASE_VALUE);
        var value = getTotalAuraModValue(UnitMod.ARMOR);
        setArmor((int) baseValue, (int) (value - baseValue));
    }

    @Override
    public void updateMaxHealth() {
        var value = getTotalAuraModValue(UnitMod.HEALTH);
        setMaxHealth(value);
    }


    @Override
    public int getPowerIndex(Power powerType) {
        if (powerType == getDisplayPowerType()) {
            return 0;
        }

        if (powerType == owerType.ALTERNATE_POWER) {
            return 1;
        }

        if (powerType == powerType.ComboPoints) {
            return 2;
        }

        return (int) powerType.max.getValue();
    }

    @Override
    public void updateMaxPower(Power power) {
        if (getPowerIndex(power) == (int) powerType.max.getValue()) {
            return;
        }

        var unitMod = UnitMod.PowerStart + power.getValue();

        var value = getFlatModifierValue(unitMod, UnitModifierFlatType.base) + getCreatePowerValue(power);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.base);
        value += getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        value *= getPctModifierValue(unitMod, UnitModifierPctType.Total);

        setMaxPower(power, (int) Math.rint(value));
    }


    @Override
    public void updateAttackPowerAndDamage() {
        updateAttackPowerAndDamage(false);
    }

    @Override
    public void updateAttackPowerAndDamage(boolean ranged) {
        var unitMod = ranged ? UnitMod.ATTACK_POWER_RANGED : UnitMod.ATTACK_POWER;

        var baseAttackPower = getFlatModifierValue(unitMod, UnitModifierFlatType.BASE_VALUE) * getPctModifierValue(unitMod, UnitModifierPctType.BASE_PCT);
        var attackPowerMultiplier = getPctModifierValue(unitMod, UnitModifierPctType.TOTAL_PCT) - 1.0f;

        if (ranged) {
            setRangedAttackPower((int) baseAttackPower);
            setRangedAttackPowerMultiplier((float) attackPowerMultiplier);
        } else {
            setAttackPower((int) baseAttackPower);
            setAttackPowerMultiplier((float) attackPowerMultiplier);
        }

        //automatically update weapon damage after attack power modification
        if (ranged) {
            updateDamagePhysical(WeaponAttackType.RANGED_ATTACK);
        } else {
            updateDamagePhysical(WeaponAttackType.BASE_ATTACK);
            updateDamagePhysical(WeaponAttackType.OFF_ATTACK);
        }
    }

    @Override
    public void calculateMinMaxDamage(WeaponAttackType attType, boolean normalized, boolean addTotalPct, tangible.OutObject<Double> minDamage, tangible.OutObject<Double> maxDamage) {
        float variance;
        UnitMod unitMod;

        switch (attType) {
            case BASE_ATTACK:
            default:
                variance = getCreatureTemplate().baseVariance;
                unitMod = UnitMod.DAMAGE_MAINHAND;

                break;
            case OFF_ATTACK:
                variance = getCreatureTemplate().baseVariance;
                unitMod = UnitMod.DAMAGE_OFFHAND;

                break;
            case RANGED_ATTACK:
                variance = getCreatureTemplate().rangeVariance;
                unitMod = UnitMod.DAMAGE_RANGED;

                break;
        }

        if (attType == WeaponAttackType.OFF_ATTACK && !haveOffhandWeapon()) {
            minDamage.outArgValue = 0.0f;
            maxDamage.outArgValue = 0.0f;

            return;
        }

        var weaponMinDamage = getWeaponDamageRange(attType, WeaponDamageRange.minDamage);
        var weaponMaxDamage = getWeaponDamageRange(attType, WeaponDamageRange.maxDamage);

        if (!canUseAttackType(attType)) // disarm case
        {
            weaponMinDamage = 0.0f;
            weaponMaxDamage = 0.0f;
        }

        var attackPower = getTotalAttackPowerValue(attType, false);
        var attackSpeedMulti = Math.max(getAPMultiplier(attType, normalized), 0.25f);

        var baseValue = getFlatModifierValue(unitMod, UnitModifierFlatType.base) + (attackPower / 3.5f) * variance;
        var basePct = getPctModifierValue(unitMod, UnitModifierPctType.base) * attackSpeedMulti;
        var totalValue = getFlatModifierValue(unitMod, UnitModifierFlatType.Total);
        var totalPct = addTotalPct ? getPctModifierValue(unitMod, UnitModifierPctType.Total) : 1.0f;
        var dmgMultiplier = getCreatureTemplate().modDamage; // = ModDamage * _GetDamageMod(rank);

        minDamage.outArgValue = ((weaponMinDamage + baseValue) * dmgMultiplier * basePct + totalValue) * totalPct;
        maxDamage.outArgValue = ((weaponMaxDamage + baseValue) * dmgMultiplier * basePct + totalValue) * totalPct;
    }

    @Override
    public final void setNewCellPosition(float x, float y, float z, float o) {
        moveState = CellMoveState.ACTIVE;
        newPosition.relocate(x, y, z, o);
    }
}
