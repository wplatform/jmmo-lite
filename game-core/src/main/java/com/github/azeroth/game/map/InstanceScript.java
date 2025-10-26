package com.github.azeroth.game.map;


import com.github.azeroth.dbc.domain.DungeonEncounter;
import com.github.azeroth.game.ai.BossAI;
import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.instance.EncounterState;
import com.github.azeroth.game.domain.map.MapDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import game.GameEvents;
import game.PhasingHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;


public class InstanceScript extends ZoneScript {
    private final HashMap<Integer, BossInfo> bosses = new HashMap<>();
    private final ArrayList<PersistentInstanceScriptValueBase> persistentScriptValues = new ArrayList<>();
    private final MultiMap<Integer, DoorInfo> doors = new MultiMap<Integer, DoorInfo>();
    private final HashMap<Integer, MinionInfo> minions = new HashMap<Integer, MinionInfo>();
    private final HashMap<Integer, Integer> creatureInfo = new HashMap<Integer, Integer>();
    private final HashMap<Integer, Integer> gameObjectInfo = new HashMap<Integer, Integer>();
    private final HashMap<Integer, ObjectGuid> objectGuids = new HashMap<Integer, ObjectGuid>();
    private final ArrayList<InstanceSpawnGroupInfo> instanceSpawnGroups = new ArrayList<>();
    private final ArrayList<Integer> activatedAreaTriggers = new ArrayList<>();
    private String headers;
    private int completedEncounters; // DEPRECATED, REMOVE
    private int entranceId;
    private int temporaryEntranceId;
    private int combatResurrectionTimer;
    private byte combatResurrectionCharges; // the counter for available battle resurrections
    private boolean combatResurrectionTimerStarted;

    private InstanceMap instance;

    public InstanceScript(InstanceMap map) {
        setInstance(map);
        instanceSpawnGroups = global.getObjectMgr().getInstanceSpawnGroupsForMap(map.getId());
    }

    public final InstanceMap getInstance() {
        return instance;
    }

    public final void setInstance(InstanceMap value) {
        instance = value;
    }

    public boolean isEncounterInProgress() {
        for (var boss : bosses.values()) {
            if (boss.state == EncounterState.inProgress) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void onCreatureCreate(Creature creature) {
        addObject(creature, true);
        addMinion(creature, true);
    }

    @Override
    public void onCreatureRemove(Creature creature) {
        addObject(creature, false);
        addMinion(creature, false);
    }

    @Override
    public void onGameObjectCreate(GameObject go) {
        addObject(go, true);
        addDoor(go, true);
    }

    @Override
    public void onGameObjectRemove(GameObject go) {
        addObject(go, false);
        addDoor(go, false);
    }

    public final ObjectGuid getObjectGuid(int type) {
        return objectGuids.get(type);
    }

    @Override
    public ObjectGuid getGuidData(int type) {
        return getObjectGuid(type);
    }

    public final void setHeaders(String dataHeaders) {
        headers = dataHeaders;
    }

    public final void loadBossBoundaries(BossBoundaryEntry[] data) {
        for (var entry : data) {
            if (entry.getBossId() < bosses.size()) {
                bosses.get(entry.getBossId()).getBoundary().add(entry.getBoundary());
            }
        }
    }

    public final void loadMinionData(MinionData... data) {
        for (var minion : data) {
            if (minion.getEntry() == 0) {
                continue;
            }

            if (minion.getBossId() < bosses.size()) {
                minions.put(minion.getEntry(), new MinionInfo(bosses.get(minion.getBossId())));
            }
        }

        Log.outDebug(LogFilter.Scripts, "InstanceScript.LoadMinionData: {0} minions loaded.", minions.size());
    }

    public final void loadDoorData(DoorData... data) {
        for (var door : data) {
            if (door.getEntry() == 0) {
                continue;
            }

            if (door.getBossId() < bosses.size()) {
                doors.add(door.getEntry(), new DoorInfo(bosses.get(door.getBossId()), door.getType()));
            }
        }

        Log.outDebug(LogFilter.Scripts, "InstanceScript.LoadDoorData: {0} doors loaded.", doors.count);
    }

    public final void loadObjectData(ObjectData[] creatureData, ObjectData[] gameObjectData) {
        if (creatureData != null) {
            loadObjectData(creatureData, creatureInfo);
        }

        if (gameObjectData != null) {
            loadObjectData(gameObjectData, gameObjectInfo);
        }

        Log.outDebug(LogFilter.Scripts, "InstanceScript.LoadObjectData: {0} objects loaded.", creatureInfo.size() + gameObjectInfo.size());
    }

    public final void loadDungeonEncounterData(DungeonEncounterData[] encounters) {
        for (var encounter : encounters) {
            loadDungeonEncounterData(encounter.getBossId(), encounter.getDungeonEncounterId());
        }
    }

    public void updateDoorState(GameObject door) {
        var range = doors.get(door.getEntry());

        if (range.isEmpty()) {
            return;
        }

        var open = true;

        for (var info : range) {
            if (!open) {
                break;
            }

            switch (info.getType()) {
                case Room:
                    open = (info.getBossInfo().getState() != EncounterState.inProgress);

                    break;
                case Passage:
                    open = (info.getBossInfo().getState() == EncounterState.Done);

                    break;
                case SpawnHole:
                    open = (info.getBossInfo().getState() == EncounterState.inProgress);

                    break;
                default:
                    break;
            }
        }

        door.setGoState(open ? GOState.Active : GOState.Ready);
    }

    public final BossInfo getBossInfo(int id) {
        return bosses.get(id);
    }

    public void addDoor(GameObject door, boolean add) {
        var range = doors.get(door.getEntry());

        if (range.isEmpty()) {
            return;
        }

        for (var data : range) {
            if (add) {
                data.getBossInfo().getDoor()[data.getType().getValue()].add(door.getGUID());
            } else {
                data.getBossInfo().getDoor()[data.getType().getValue()].remove(door.getGUID());
            }
        }

        if (add) {
            updateDoorState(door);
        }
    }

    public final void addMinion(Creature minion, boolean add) {
        var minionInfo = minions.get(minion.getEntry());

        if (minionInfo == null) {
            return;
        }

        if (add) {
            minionInfo.bossInfo.minion.add(minion.getGUID());
        } else {
            minionInfo.bossInfo.minion.remove(minion.getGUID());
        }
    }

    // Triggers a GameEvent
    // * If source is null then event is triggered for each player in the instance as "source"

    @Override
    public void triggerGameEvent(int gameEventId, WorldObject source) {
        triggerGameEvent(gameEventId, source, null);
    }

    @Override
    public void triggerGameEvent(int gameEventId) {
        triggerGameEvent(gameEventId, null, null);
    }

    @Override
    public void triggerGameEvent(int gameEventId, WorldObject source, WorldObject target) {
        if (source != null) {
            super.triggerGameEvent(gameEventId, source, target);

            return;
        }

        processEvent(target, gameEventId, source);
        getInstance().doOnPlayers(player -> GameEvents.triggerForPlayer(gameEventId, player));

        GameEvents.triggerForMap(gameEventId, getInstance());
    }

    public final Creature getCreature(int type) {
        return getInstance().getCreature(getObjectGuid(type));
    }

    public final GameObject getGameObject(int type) {
        return getInstance().getGameObject(getObjectGuid(type));
    }

    public boolean setBossState(int id, EncounterState state) {
        if (id < bosses.size()) {
            var bossInfo = bosses.get(id);

            if (bossInfo.getState() == EncounterState.ToBeDecided) // loading
            {
                bossInfo.setState(state);
                Log.outDebug(LogFilter.Scripts, String.format("InstanceScript: Initialize boss %1$s state as %2$s (map %3$s, %4$s).", id, state, getInstance().getId(), getInstance().getInstanceId()));

                return false;
            } else {
                if (bossInfo.getState() == state) {
                    return false;
                }

                if (bossInfo.getState() == EncounterState.Done) {
                    Logs.MAPS.error(String.format("InstanceScript: Tried to set instance boss %1$s state from %2$s back to %3$s for map %4$s, instance id %5$s. blocked!", id, bossInfo.getState(), state, getInstance().getId(), getInstance().getInstanceId()));

                    return false;
                }

                if (state == EncounterState.Done) {
                    for (var guid : bossInfo.getMinion()) {
                        var minion = getInstance().getCreature(guid);

                        if (minion) {
                            if (minion.isWorldBoss() && minion.isAlive()) {
                                return false;
                            }
                        }
                    }
                }

                DungeonEncounter dungeonEncounter = null;

                switch (state) {
                    case InProgress: {
                        var resInterval = getCombatResurrectionChargeInterval();
                        initializeCombatResurrections((byte) 1, resInterval);
                        sendEncounterStart(1, 9, resInterval, resInterval);

                        getInstance().doOnPlayers(player ->
                        {
                            if (player.isAlive) {
                                unit.procSkillsAndAuras(player, null, new ProcFlagsInit(procFlags.EncounterStart), new ProcFlagsInit(), ProcFlagsSpellType.MaskAll, ProcFlagsSpellPhase.NONE, ProcFlagsHit.NONE, null, null, null);
                            }
                        });

                        break;
                    }
                    case Fail:
                        resetCombatResurrections();
                        sendEncounterEnd();

                        break;
                    case Done:
                        resetCombatResurrections();
                        sendEncounterEnd();
                        dungeonEncounter = bossInfo.getDungeonEncounterForDifficulty(getInstance().getDifficultyID());

                        if (dungeonEncounter != null) {
                            doUpdateCriteria(CriteriaType.DefeatDungeonEncounter, dungeonEncounter.id);
                            sendBossKillCredit(dungeonEncounter.id);
                        }

                        break;
                    default:
                        break;
                }

                bossInfo.setState(state);

                if (dungeonEncounter != null) {
                    getInstance().updateInstanceLock(new UpdateBossStateSaveDataEvent(dungeonEncounter, id, state));
                }
            }

            for (int type = 0; type < DoorType.max.getValue(); ++type) {
                for (var guid : bossInfo.getDoor()[type]) {
                    var door = getInstance().getGameObject(guid);

                    if (door) {
                        updateDoorState(door);
                    }
                }
            }

            for (var guid : bossInfo.getMinion()) {
                var minion = getInstance().getCreature(guid);

                if (minion) {
                    updateMinionState(minion, state);
                }
            }

            updateSpawnGroups();

            return true;
        }

        return false;
    }


    public final boolean _SkipCheckRequiredBosses() {
        return _SkipCheckRequiredBosses(null);
    }

    public final boolean _SkipCheckRequiredBosses(Player player) {
        return player && player.getSession().hasPermission(RBACPermissions.SkipCheckInstanceRequiredBosses);
    }

    public void create() {
        for (int i = 0; i < bosses.size(); ++i) {
            setBossState(i, EncounterState.NotStarted);
        }

        updateSpawnGroups();
    }

    public final void load(String data) {
        if (StringUtil.isEmpty(data)) {
            outLoadInstDataFail();

            return;
        }

        outLoadInstData(data);

        InstanceScriptDataReader reader = new InstanceScriptDataReader(this);

        if (reader.load(data) == InstanceScriptDataReader.result.Ok) {
            // in loot-based lockouts instance can be loaded with later boss marked as killed without preceding bosses
            // but we still need to have them alive
            for (int i = 0; i < bosses.size(); ++i) {
                if (bosses.get(i).getState() == EncounterState.Done && !checkRequiredBosses(i)) {
                    bosses.get(i).setState(EncounterState.NotStarted);
                }
            }

            updateSpawnGroups();
            afterDataLoad();
        } else {
            outLoadInstDataFail();
        }

        outLoadInstDataComplete();
    }

    public final String getSaveData() {
        outSaveInstData();

        InstanceScriptDataWriter writer = new InstanceScriptDataWriter(this);

        writer.fillData();

        outSaveInstDataComplete();

        return writer.getString();
    }

    public final String updateBossStateSaveData(String oldData, UpdateBossStateSaveDataEvent saveEvent) {
        if (!getInstance().getMapDifficulty().isUsingEncounterLocks()) {
            return getSaveData();
        }

        InstanceScriptDataWriter writer = new InstanceScriptDataWriter(this);
        writer.fillDataFrom(oldData);
        writer.setBossState(saveEvent);

        return writer.getString();
    }

    public final String updateAdditionalSaveData(String oldData, UpdateAdditionalSaveDataEvent saveEvent) {
        if (!getInstance().getMapDifficulty().isUsingEncounterLocks()) {
            return getSaveData();
        }

        InstanceScriptDataWriter writer = new InstanceScriptDataWriter(this);
        writer.fillDataFrom(oldData);
        writer.setAdditionalData(saveEvent);

        return writer.getString();
    }

    public final Integer getEntranceLocationForCompletedEncounters(int completedEncountersMask) {
        if (!getInstance().getMapDifficulty().isUsingEncounterLocks()) {
            return entranceId;
        }

        return computeEntranceLocationForCompletedEncounters(completedEncountersMask);
    }

    public Integer computeEntranceLocationForCompletedEncounters(int completedEncountersMask) {
        return null;
    }


    public final void handleGameObject(ObjectGuid guid, boolean open) {
        handleGameObject(guid, open, null);
    }

    public final void handleGameObject(ObjectGuid guid, boolean open, GameObject go) {
        if (!go) {
            go = getInstance().getGameObject(guid);
        }

        if (go) {
            go.setGoState(open ? GOState.Active : GOState.Ready);
        } else {
            Log.outDebug(LogFilter.Scripts, "InstanceScript: HandleGameObject failed");
        }
    }


    public final void doUseDoorOrButton(ObjectGuid uiGuid, int withRestoreTime) {
        doUseDoorOrButton(uiGuid, withRestoreTime, false);
    }

    public final void doUseDoorOrButton(ObjectGuid uiGuid) {
        doUseDoorOrButton(uiGuid, 0, false);
    }

    public final void doUseDoorOrButton(ObjectGuid uiGuid, int withRestoreTime, boolean useAlternativeState) {
        if (uiGuid.isEmpty()) {
            return;
        }

        var go = getInstance().getGameObject(uiGuid);

        if (go) {
            if (go.getGoType() == GameObjectTypes.door || go.getGoType() == GameObjectTypes.button) {
                if (go.getLootState() == LootState.Ready) {
                    go.useDoorOrButton(withRestoreTime, useAlternativeState);
                } else if (go.getLootState() == LootState.Activated) {
                    go.resetDoorOrButton();
                }
            } else {
                Log.outError(LogFilter.Scripts, "InstanceScript: DoUseDoorOrButton can't use gameobject entry {0}, because type is {1}.", go.getEntry(), go.getGoType());
            }
        } else {
            Log.outDebug(LogFilter.Scripts, "InstanceScript: DoUseDoorOrButton failed");
        }
    }

    public final void doRespawnGameObject(ObjectGuid guid, Duration timeToDespawn) {
        var go = getInstance().getGameObject(guid);

        if (go) {
            switch (go.getGoType()) {
                case Door:
                case Button:
                case Trap:
                case FishingNode:
                    // not expect any of these should ever be handled
                    Log.outError(LogFilter.Scripts, "InstanceScript: DoRespawnGameObject can't respawn gameobject entry {0}, because type is {1}.", go.getEntry(), go.getGoType());

                    return;
                default:
                    break;
            }

            if (go.isSpawned()) {
                return;
            }

            go.setRespawnTime((int) timeToDespawn.TotalSeconds);
        } else {
            Log.outDebug(LogFilter.Scripts, "InstanceScript: DoRespawnGameObject failed");
        }
    }

    public final void doUpdateWorldState(int worldStateId, int value) {
        global.getWorldStateMgr().setValue(worldStateId, value, false, getInstance());
    }

    // Update Achievement Criteria for all players in instance

    public final void doUpdateCriteria(CriteriaType type, int miscValue1, int miscValue2) {
        doUpdateCriteria(type, miscValue1, miscValue2, null);
    }

    public final void doUpdateCriteria(CriteriaType type, int miscValue1) {
        doUpdateCriteria(type, miscValue1, 0, null);
    }

    public final void doUpdateCriteria(CriteriaType type) {
        doUpdateCriteria(type, 0, 0, null);
    }

    public final void doUpdateCriteria(CriteriaType type, int miscValue1, int miscValue2, Unit unit) {
        getInstance().doOnPlayers(player -> player.updateCriteria(type, miscValue1, miscValue2, 0, unit));
    }

    // Remove Auras due to Spell on all players in instance

    public final void doRemoveAurasDueToSpellOnPlayers(int spell, boolean includePets) {
        doRemoveAurasDueToSpellOnPlayers(spell, includePets, false);
    }

    public final void doRemoveAurasDueToSpellOnPlayers(int spell) {
        doRemoveAurasDueToSpellOnPlayers(spell, false, false);
    }

    public final void doRemoveAurasDueToSpellOnPlayers(int spell, boolean includePets, boolean includeControlled) {
        getInstance().doOnPlayers(player -> doRemoveAurasDueToSpellOnPlayer(player, spell, includePets, includeControlled));
    }


    public final void doRemoveAurasDueToSpellOnPlayer(Player player, int spell, boolean includePets) {
        doRemoveAurasDueToSpellOnPlayer(player, spell, includePets, false);
    }

    public final void doRemoveAurasDueToSpellOnPlayer(Player player, int spell) {
        doRemoveAurasDueToSpellOnPlayer(player, spell, false, false);
    }

    public final void doRemoveAurasDueToSpellOnPlayer(Player player, int spell, boolean includePets, boolean includeControlled) {
        if (!player) {
            return;
        }

        player.removeAura(spell);

        if (!includePets) {
            return;
        }

        for (var i = 0; i < SharedConst.MaxSummonSlot; ++i) {
            var summonGUID = player.getSummonSlot()[i];

            if (!summonGUID.isEmpty()) {
                var summon = getInstance().getCreature(summonGUID);

                if (summon != null) {
                    summon.removeAura(spell);
                }
            }
        }

        if (!includeControlled) {
            return;
        }

        for (var i = 0; i < player.getControlled().size(); ++i) {
            var controlled = player.getControlled().get(i);

            if (controlled != null) {
                if (controlled.isInWorld() && controlled.isCreature()) {
                    controlled.removeAura(spell);
                }
            }
        }
    }

    // Cast spell on all players in instance

    public final void doCastSpellOnPlayers(int spell, boolean includePets) {
        doCastSpellOnPlayers(spell, includePets, false);
    }

    public final void doCastSpellOnPlayers(int spell) {
        doCastSpellOnPlayers(spell, false, false);
    }

    public final void doCastSpellOnPlayers(int spell, boolean includePets, boolean includeControlled) {
        getInstance().doOnPlayers(player -> doCastSpellOnPlayer(player, spell, includePets, includeControlled));
    }


    public final void doCastSpellOnPlayer(Player player, int spell, boolean includePets) {
        doCastSpellOnPlayer(player, spell, includePets, false);
    }

    public final void doCastSpellOnPlayer(Player player, int spell) {
        doCastSpellOnPlayer(player, spell, false, false);
    }

    public final void doCastSpellOnPlayer(Player player, int spell, boolean includePets, boolean includeControlled) {
        if (!player) {
            return;
        }

        player.castSpell(player, spell, true);

        if (!includePets) {
            return;
        }

        for (var i = 0; i < SharedConst.MaxSummonSlot; ++i) {
            var summonGUID = player.getSummonSlot()[i];

            if (!summonGUID.isEmpty()) {
                var summon = getInstance().getCreature(summonGUID);

                if (summon != null) {
                    summon.castSpell(player, spell, true);
                }
            }
        }

        if (!includeControlled) {
            return;
        }

        for (var i = 0; i < player.getControlled().size(); ++i) {
            var controlled = player.getControlled().get(i);

            if (controlled != null) {
                if (controlled.isInWorld() && controlled.isCreature()) {
                    controlled.castSpell(player, spell, true);
                }
            }
        }
    }

    public final DungeonEncounter getBossDungeonEncounter(int id) {
        return id < bosses.size() ? bosses.get(id).getDungeonEncounterForDifficulty(getInstance().getDifficultyID()) : null;
    }

    public final DungeonEncounter getBossDungeonEncounter(Creature creature) {
        CreatureAI tempVar = creature.getAI();
        var bossAi = tempVar instanceof BossAI ? (BossAI) tempVar : null;

        if (bossAi != null) {
            return getBossDungeonEncounter(bossAi.getBossId());
        }

        return null;
    }


    public boolean checkAchievementCriteriaMeet(int criteria_id, Player source, Unit target) {
        return checkAchievementCriteriaMeet(criteria_id, source, target, 0);
    }

    public boolean checkAchievementCriteriaMeet(int criteria_id, Player source) {
        return checkAchievementCriteriaMeet(criteria_id, source, null, 0);
    }

    public boolean checkAchievementCriteriaMeet(int criteria_id, Player source, Unit target, int miscvalue1) {
        Log.outError(LogFilter.Server, "Achievement system call CheckAchievementCriteriaMeet but instance script for map {0} not have implementation for achievement criteria {1}", getInstance().getId(), criteria_id);

        return false;
    }

    public final boolean isEncounterCompleted(int dungeonEncounterId) {
        for (int i = 0; i < bosses.size(); ++i) {
            for (var j = 0; j < bosses.get(i).getDungeonEncounters().length; ++j) {
                if (bosses.get(i).getDungeonEncounters()[j] != null && bosses.get(i).getDungeonEncounters()[j].id == dungeonEncounterId) {
                    return bosses.get(i).getState() == EncounterState.Done;
                }
            }
        }

        return false;
    }

    public final boolean isEncounterCompletedInMaskByBossId(int completedEncountersMask, int bossId) {
        var dungeonEncounter = getBossDungeonEncounter(bossId);

        if (dungeonEncounter != null) {
            if ((completedEncountersMask & (1 << dungeonEncounter.bit)) != 0) {
                return bosses.get(bossId).getState() == EncounterState.Done;
            }
        }

        return false;
    }

    public final void sendEncounterUnit(EncounterFrameType type, Unit unit) {
        sendEncounterUnit(type, unit, 0);
    }

    public final void sendEncounterUnit(EncounterFrameType type) {
        sendEncounterUnit(type, null, 0);
    }

    public final void sendEncounterUnit(EncounterFrameType type, Unit unit, byte priority) {
        switch (type) {
            case Engage:
                if (unit == null) {
                    return;
                }

                InstanceEncounterEngageUnit encounterEngageMessage = new InstanceEncounterEngageUnit();
                encounterEngageMessage.unit = unit.getGUID();
                encounterEngageMessage.targetFramePriority = priority;
                getInstance().sendToPlayers(encounterEngageMessage);

                break;
            case Disengage:
                if (!unit) {
                    return;
                }

                InstanceEncounterDisengageUnit encounterDisengageMessage = new InstanceEncounterDisengageUnit();
                encounterDisengageMessage.unit = unit.getGUID();
                getInstance().sendToPlayers(encounterDisengageMessage);

                break;
            case UpdatePriority:
                if (!unit) {
                    return;
                }

                InstanceEncounterChangePriority encounterChangePriorityMessage = new InstanceEncounterChangePriority();
                encounterChangePriorityMessage.unit = unit.getGUID();
                encounterChangePriorityMessage.targetFramePriority = priority;
                getInstance().sendToPlayers(encounterChangePriorityMessage);

                break;
            default:
                break;
        }
    }

    public final void sendBossKillCredit(int encounterId) {
        BossKill bossKillCreditMessage = new BossKill();
        bossKillCreditMessage.dungeonEncounterID = encounterId;

        getInstance().sendToPlayers(bossKillCreditMessage);
    }

    public final void updateEncounterStateForKilledCreature(int creatureId, Unit source) {
        updateEncounterState(EncounterCreditType.KillCreature, creatureId, source);
    }

    public final void updateEncounterStateForSpellCast(int spellId, Unit source) {
        updateEncounterState(EncounterCreditType.castSpell, spellId, source);
    }

    public final void setCompletedEncountersMask(int newMask) {
        completedEncounters = newMask;

        var encounters = global.getObjectMgr().getDungeonEncounterList(getInstance().getId(), getInstance().getDifficultyID());

        if (encounters != null) {
            for (var encounter : encounters) {
                if ((completedEncounters & (1 << encounter.dbcEntry.bit)) != 0 && encounter.dbcEntry.CompleteWorldStateID != 0) {
                    doUpdateWorldState((int) encounter.dbcEntry.CompleteWorldStateID, 1);
                }
            }
        }
    }

    public final void updateCombatResurrection(int diff) {
        if (!combatResurrectionTimerStarted) {
            return;
        }

        if (combatResurrectionTimer <= diff) {
            addCombatResurrectionCharge();
        } else {
            _combatResurrectionTimer -= diff;
        }
    }

    public final void addCombatResurrectionCharge() {
        ++combatResurrectionCharges;
        combatResurrectionTimer = getCombatResurrectionChargeInterval();
        combatResurrectionTimerStarted = true;

        var gainCombatResurrectionCharge = new InstanceEncounterGainCombatResurrectionCharge();
        gainCombatResurrectionCharge.inCombatResCount = combatResurrectionCharges;
        gainCombatResurrectionCharge.combatResChargeRecovery = combatResurrectionTimer;
        getInstance().sendToPlayers(gainCombatResurrectionCharge);
    }

    public final void useCombatResurrection() {
        --_combatResurrectionCharges;

        getInstance().sendToPlayers(new InstanceEncounterInCombatResurrection());
    }

    public final void resetCombatResurrections() {
        combatResurrectionCharges = 0;
        combatResurrectionTimer = 0;
        combatResurrectionTimerStarted = false;
    }

    public final int getCombatResurrectionChargeInterval() {
        int interval = 0;
        var playerCount = getInstance().getPlayers().size();

        if (playerCount != 0) {
            interval = (int) (90 * time.Minute * time.InMilliseconds / playerCount);
        }

        return interval;
    }

    public final boolean instanceHasScript(WorldObject obj, String scriptName) {
        var instance = obj.getMap().toInstanceMap();

        if (instance != null) {
            return Objects.equals(instance.getScriptName(), scriptName);
        }

        return false;
    }

    public void update(int diff) {
    }

    // Called when a player successfully enters the instance.
    public void onPlayerEnter(Player player) {
    }

    // Called when a player successfully leaves the instance.
    public void onPlayerLeave(Player player) {
    }

    // Return wether server allow two side groups or not
    public final boolean serverAllowsTwoSideGroups() {
        return WorldConfig.getBoolValue(WorldCfg.AllowTwoSideInteractionGroup);
    }

    public final EncounterState getBossState(int id) {
        return id < bosses.size() ? bosses.get(id).getState() : EncounterState.TO_BE_DECIDED;
    }

    public final ArrayList<AreaBoundary> getBossBoundary(int id) {
        return id < bosses.size() ? bosses.get(id).getBoundary() : null;
    }

    public boolean checkRequiredBosses(int bossId) {
        return checkRequiredBosses(bossId, null);
    }

    public boolean checkRequiredBosses(int bossId, Player player) {
        return true;
    }

    public final int getCompletedEncounterMask() {
        return completedEncounters;
    }

    // Sets a temporary entrance that does not get saved to db
    public final void setTemporaryEntranceLocation(int worldSafeLocationId) {
        temporaryEntranceId = worldSafeLocationId;
    }

    // Get's the current entrance id
    public final int getEntranceLocation() {
        return temporaryEntranceId != 0 ? _temporaryEntranceId : entranceId;
    }

    public final void setEntranceLocation(int worldSafeLocationId) {
        entranceId = worldSafeLocationId;
        temporaryEntranceId = 0;
    }

    // Only used by areatriggers that inherit from OnlyOnceAreaTriggerScript
    public final void markAreaTriggerDone(int id) {
        activatedAreaTriggers.add(id);
    }

    public final void resetAreaTriggerDone(int id) {
        activatedAreaTriggers.remove((Integer) id);
    }

    public final boolean isAreaTriggerDone(int id) {
        return activatedAreaTriggers.contains(id);
    }

    public final int getEncounterCount() {
        return bosses.size();
    }

    public final byte getCombatResurrectionCharges() {
        return combatResurrectionCharges;
    }

    public final void registerPersistentScriptValue(PersistentInstanceScriptValueBase value) {
        persistentScriptValues.add(value);
    }

    public final String getHeader() {
        return headers;
    }

    public final ArrayList<PersistentInstanceScriptValueBase> getPersistentScriptValues() {
        return persistentScriptValues;
    }

    public final void setBossNumber(int number) {
        for (int i = 0; i < number; ++i) {
            bosses.put(i, new bossInfo());
        }
    }

    public final void outSaveInstData() {
        Log.outDebug(LogFilter.Scripts, "Saving Instance Data for Instance {0} (Map {1}, Instance Id {2})", getInstance().getMapName(), getInstance().getId(), getInstance().getInstanceId());
    }

    public final void outSaveInstDataComplete() {
        Log.outDebug(LogFilter.Scripts, "Saving Instance Data for Instance {0} (Map {1}, Instance Id {2}) completed.", getInstance().getMapName(), getInstance().getId(), getInstance().getInstanceId());
    }

    public final void outLoadInstData(String input) {
        Log.outDebug(LogFilter.Scripts, "Loading Instance Data for Instance {0} (Map {1}, Instance Id {2}). Input is '{3}'", getInstance().getMapName(), getInstance().getId(), getInstance().getInstanceId(), input);
    }

    public final void outLoadInstDataComplete() {
        Log.outDebug(LogFilter.Scripts, "Instance Data Load for Instance {0} (Map {1}, Instance Id: {2}) is complete.", getInstance().getMapName(), getInstance().getId(), getInstance().getInstanceId());
    }

    public final void outLoadInstDataFail() {
        Log.outDebug(LogFilter.Scripts, "Unable to load Instance Data for Instance {0} (Map {1}, Instance Id: {2}).", getInstance().getMapName(), getInstance().getId(), getInstance().getInstanceId());
    }

    public final ArrayList<InstanceSpawnGroupInfo> getInstanceSpawnGroups() {
        return instanceSpawnGroups;
    }

    // Override this function to validate all additional data loads
    public void afterDataLoad() {
    }

    private void loadObjectData(ObjectData[] objectData, HashMap<Integer, Integer> objectInfo) {
        for (var data : objectData) {
            objectInfo.put(data.entry, data.type);
        }
    }

    private void loadDungeonEncounterData(int bossId, int[] dungeonEncounterIds) {
        if (bossId < bosses.size()) {
            for (var i = 0; i < dungeonEncounterIds.length && i < MapDefine.MaxDungeonEncountersPerBoss; ++i) {
                bosses.get(bossId).getDungeonEncounters()[i] = CliDB.DungeonEncounterStorage.get(dungeonEncounterIds[i]);
            }
        }
    }

    private void updateMinionState(Creature minion, EncounterState state) {
        switch (state) {
            case NotStarted:
                if (!minion.isAlive()) {
                    minion.respawn();
                } else if (minion.isInCombat()) {
                    minion.getAI().enterEvadeMode();
                }

                break;
            case InProgress:
                if (!minion.isAlive()) {
                    minion.respawn();
                } else if (minion.getVictim() == null) {
                    minion.getAI().doZoneInCombat();
                }

                break;
            default:
                break;
        }
    }

    private void updateSpawnGroups() {
        if (instanceSpawnGroups.isEmpty()) {
            return;
        }

        HashMap<Integer, InstanceState> newStates = new HashMap<Integer, InstanceState>();

        for (var info : instanceSpawnGroups) {
            if (!newStates.containsKey(info.spawnGroupId)) {
                newStates.put(info.spawnGroupId, 0); // makes sure there's a BLOCK second in the map
            }

            if (newStates.get(info.spawnGroupId).equals(InstanceState.ForceBlock)) // nothing will change this
            {
                continue;
            }

            if (((1 << getBossState(info.bossStateId).getValue()) & info.bossStates) == 0) {
                continue;
            }

            if (((getInstance().getTeamIdInInstance() == TeamId.ALLIANCE) && info.flags.hasFlag(InstanceSpawnGroupFlags.HordeOnly)) || ((getInstance().getTeamIdInInstance() == TeamId.HORDE) && info.flags.hasFlag(InstanceSpawnGroupFlags.AllianceOnly))) {
                continue;
            }

            if (info.flags.hasFlag(InstanceSpawnGroupFlags.BlockSpawn)) {
                newStates.put(info.spawnGroupId, InstanceState.ForceBlock);
            } else if (info.flags.hasFlag(InstanceSpawnGroupFlags.ActivateSpawn)) {
                newStates.put(info.spawnGroupId, InstanceState.Spawn);
            }
        }

        for (var pair : newStates.entrySet()) {
            var groupId = pair.getKey();
            var doSpawn = pair.getValue() == InstanceState.Spawn;

            if (getInstance().isSpawnGroupActive(groupId) == doSpawn) {
                continue; // nothing to do here
            }

            // if we should spawn group, then spawn it...
            if (doSpawn) {
                getInstance().spawnGroupSpawn(groupId, getInstance());
            } else // otherwise, set it as inactive so it no longer respawns (but don't despawn it)
            {
                getInstance().setSpawnGroupInactive(groupId);
            }
        }
    }

    private void addObject(Creature obj, boolean add) {
        if (creatureInfo.containsKey(obj.getEntry())) {
            addObject(obj, creatureInfo.get(obj.getEntry()), add);
        }
    }

    private void addObject(GameObject obj, boolean add) {
        if (gameObjectInfo.containsKey(obj.getEntry())) {
            addObject(obj, gameObjectInfo.get(obj.getEntry()), add);
        }
    }

    private void addObject(WorldObject obj, int type, boolean add) {
        if (add) {
            objectGuids.put(type, obj.getGUID());
        } else {
            var guid = objectGuids.get(type);

            if (!guid.IsEmpty && guid == obj.getGUID()) {
                objectGuids.remove(type);
            }
        }
    }

    private void doCloseDoorOrButton(ObjectGuid guid) {
        if (guid.isEmpty()) {
            return;
        }

        var go = getInstance().getGameObject(guid);

        if (go) {
            if (go.getGoType() == GameObjectTypes.door || go.getGoType() == GameObjectTypes.button) {
                if (go.getLootState() == LootState.Activated) {
                    go.resetDoorOrButton();
                }
            } else {
                Log.outError(LogFilter.Scripts, "InstanceScript: DoCloseDoorOrButton can't use gameobject entry {0}, because type is {1}.", go.getEntry(), go.getGoType());
            }
        } else {
            Log.outDebug(LogFilter.Scripts, "InstanceScript: DoCloseDoorOrButton failed");
        }
    }

    // Send Notify to all players in instance
    private void doSendNotifyToInstance(String format, object... args) {
        getInstance().doOnPlayers(player ->
        {
            if (player.session != null) {
                player.session.sendNotification(format, args);
            }
        });
    }


    private void sendEncounterStart(int inCombatResCount, int maxInCombatResCount, int inCombatResChargeRecovery) {
        sendEncounterStart(inCombatResCount, maxInCombatResCount, inCombatResChargeRecovery, 0);
    }

    private void sendEncounterStart(int inCombatResCount, int maxInCombatResCount) {
        sendEncounterStart(inCombatResCount, maxInCombatResCount, 0, 0);
    }

    private void sendEncounterStart(int inCombatResCount) {
        sendEncounterStart(inCombatResCount, 0, 0, 0);
    }

    private void sendEncounterStart() {
        sendEncounterStart(0, 0, 0, 0);
    }

    private void sendEncounterStart(int inCombatResCount, int maxInCombatResCount, int inCombatResChargeRecovery, int nextCombatResChargeTime) {
        InstanceEncounterStart encounterStartMessage = new InstanceEncounterStart();
        encounterStartMessage.inCombatResCount = inCombatResCount;
        encounterStartMessage.maxInCombatResCount = maxInCombatResCount;
        encounterStartMessage.combatResChargeRecovery = inCombatResChargeRecovery;
        encounterStartMessage.nextCombatResChargeTime = nextCombatResChargeTime;

        getInstance().sendToPlayers(encounterStartMessage);
    }

    private void sendEncounterEnd() {
        getInstance().sendToPlayers(new InstanceEncounterEnd());
    }

    private void updateEncounterState(EncounterCreditType type, int creditEntry, Unit source) {
        var encounters = global.getObjectMgr().getDungeonEncounterList(getInstance().getId(), getInstance().getDifficultyID());

        if (encounters.isEmpty()) {
            return;
        }

        int dungeonId = 0;

        for (var encounter : encounters) {
            if (encounter.creditType == type && encounter.creditEntry == creditEntry) {
                completedEncounters |= (1 << encounter.dbcEntry.bit);

                if (encounter.dbcEntry.CompleteWorldStateID != 0) {
                    doUpdateWorldState((int) encounter.dbcEntry.CompleteWorldStateID, 1);
                }

                if (encounter.lastEncounterDungeon != 0) {
                    dungeonId = encounter.lastEncounterDungeon;

                    Log.outDebug(LogFilter.Lfg, "UpdateEncounterState: Instance {0} (instanceId {1}) completed encounter {2}. Credit Dungeon: {3}", getInstance().getMapName(), getInstance().getInstanceId(), encounter.dbcEntry.name.charAt(global.getWorldMgr().getDefaultDbcLocale()), dungeonId);

                    break;
                }
            }
        }

        if (dungeonId != 0) {
            var players = getInstance().getPlayers();

            for (var player : players) {
                var grp = player.getGroup();

                if (grp != null) {
                    if (grp.isLFGGroup()) {
                        global.getLFGMgr().finishDungeon(grp.getGUID(), dungeonId, getInstance());

                        return;
                    }
                }
            }
        }
    }

    private void updatePhasing() {
        getInstance().doOnPlayers(player -> PhasingHandler.sendToPlayer(player));
    }


    private void initializeCombatResurrections(byte charges) {
        initializeCombatResurrections(charges, 0);
    }

    private void initializeCombatResurrections() {
        initializeCombatResurrections(1, 0);
    }

    private void initializeCombatResurrections(byte charges, int interval) {
        combatResurrectionCharges = charges;

        if (interval == 0) {
            return;
        }

        combatResurrectionTimer = interval;
        combatResurrectionTimerStarted = true;
    }

    private enum InstanceState {
        Block,
        Spawn,
        ForceBlock;

        public static final int SIZE = Integer.SIZE;

        public static InstanceState forValue(int value) {
            return values()[value];
        }

        public int getValue() {
            return this.ordinal();
        }
    }
}
