package com.github.azeroth.game.map;


import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.map.MapDb2Entries;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.GroupInstanceReference;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.networking.packet.InstanceSaveCreated;
import com.github.azeroth.game.networking.packet.PendingRaidLock;
import com.github.azeroth.game.networking.packet.RaidInstanceMessage;
import com.github.azeroth.game.scenario.InstanceScenario;
import com.github.azeroth.game.listener.interfaces.imap.IInstanceMapGetInstanceScript;

import java.time.LocalDateTime;


public class InstanceMap extends Map {
    private final InstanceLock instanceLock;
    private final GroupInstanceReference owningGroupRef = new GroupInstanceReference();
    private InstanceScript data;
    private int scriptId;
    private InstanceScenario scenario;
    private LocalDateTime instanceExpireEvent = null;

    public InstanceMap(int id, long expiry, int instanceId, Difficulty spawnMode, int instanceTeam, InstanceLock instanceLock) {
        super(id, expiry, instanceId, spawnMode);
        instanceLock = instanceLock;

        //lets initialize visibility distance for dungeons
        initVisibilityDistance();

        // the timer is started by default, and stopped when the first player joins
        // this make sure it gets unloaded if for some reason no player joins
        setUnloadTimer((int) Math.max(WorldConfig.getIntValue(WorldCfg.InstanceUnloadDelay), 1));

        global.getWorldStateMgr().setValue(WorldStates.TeamInInstanceAlliance, instanceTeam == TeamId.ALLIANCE ? 1 : 0, false, this);
        global.getWorldStateMgr().setValue(WorldStates.TeamInInstanceHorde, instanceTeam == TeamId.HORDE ? 1 : 0, false, this);

        if (instanceLock != null) {
            instanceLock.setInUse(true);
            instanceExpireEvent = instanceLock.getExpiryTime(); // ignore extension state for reset event (will ask players to accept extended save on expiration)
        }
    }

    public final int getMaxPlayers() {
        var mapDiff = getMapDifficulty();

        if (mapDiff != null && mapDiff.MaxPlayers != 0) {
            return mapDiff.MaxPlayers;
        }

        return getEntry().MaxPlayers;
    }

    public final int getTeamIdInInstance() {
        if (global.getWorldStateMgr().getValue(WorldStates.TeamInInstanceAlliance, this) != 0) {
            return TeamId.ALLIANCE;
        }

        if (global.getWorldStateMgr().getValue(WorldStates.TeamInInstanceHorde, this) != 0) {
            return TeamId.HORDE;
        }

        return TeamIds.Neutral;
    }

    public final Team getTeamInInstance() {
        return getTeamIdInInstance() == TeamId.ALLIANCE ? Team.ALLIANCE : Team.Horde;
    }

    public final int getScriptId() {
        return scriptId;
    }

    public final InstanceScript getInstanceScript() {
        return data;
    }

    public final InstanceScenario getInstanceScenario() {
        return scenario;
    }

    public final void setInstanceScenario(InstanceScenario scenario) {
        scenario = scenario;
    }

    public final InstanceLock getInstanceLock() {
        return instanceLock;
    }

    @Override
    public void initVisibilityDistance() {
        //init visibility distance for instances
        setVisibleDistance(global.getWorldMgr().getMaxVisibleDistanceInInstances());
        setVisibilityNotifyPeriod(global.getWorldMgr().getVisibilityNotifyPeriodInInstances());
    }

    @Override
    public TransferAbortParams cannotEnter(Player player) {
        if (player.getMap() == this) {
            Logs.MAPS.error("InstanceMap:CannotEnter - player {0} ({1}) already in map {2}, {3}, {4}!", player.getName(), player.getGUID().toString(), getId(), getInstanceId(), getDifficultyID());
            return new TransferAbortParams(TransferAbortReason.error);
        }

        // allow GM's to enter
        if (player.isGameMaster()) {
            return super.cannotEnter(player);
        }

        // cannot enter if the instance is full (player cap), GMs don't count
        var maxPlayers = getMaxPlayers();

        if (getPlayersCountExceptGMs() >= maxPlayers) {
            Log.outInfo(LogFilter.Maps, "MAP: Instance '{0}' of map '{1}' cannot have more than '{2}' players. Player '{3}' rejected", getInstanceId(), getMapName(), maxPlayers, player.getName());

            return new TransferAbortParams(TransferAbortReason.MaxPlayers);
        }

        // cannot enter while an encounter is in progress (unless this is a relog, in which case it is permitted)
        if (!player.isLoading() && isRaid() && getInstanceScript() != null && getInstanceScript().isEncounterInProgress()) {
            return new TransferAbortParams(TransferAbortReason.ZoneInCombat);
        }

        if (instanceLock != null) {
            // cannot enter if player is permanent saved to a different instance id
            var lockError = global.getInstanceLockMgr().canJoinInstanceLock(player.getGUID(), new MapDb2Entries(getEntry(), getMapDifficulty()), instanceLock);

            if (lockError != TransferAbortReason.NONE) {
                return new TransferAbortParams(lockError);
            }
        }

        return super.cannotEnter(player);
    }

    @Override
    public boolean addPlayerToMap(Player player) {
        return addPlayerToMap(player, true);
    }

    @Override
    public boolean addPlayerToMap(Player player, boolean initPlayer) {
        // increase current instances (hourly limit)
        player.addInstanceEnterTime(getInstanceId(), gameTime.GetGameTime());

        MapDb2Entries entries = new MapDb2Entries(getEntry(), getMapDifficulty());

        if (entries.MapDifficulty.HasResetSchedule() && instanceLock != null && instanceLock.getData().getCompletedEncountersMask() != 0) {
            if (!entries.MapDifficulty.IsUsingEncounterLocks()) {
                var playerLock = global.getInstanceLockMgr().findActiveInstanceLock(player.getGUID(), entries);

                if (playerLock == null || (playerLock.isExpired() && playerLock.isExtended()) || playerLock.getData().getCompletedEncountersMask() != instanceLock.getData().getCompletedEncountersMask()) {
                    PendingRaidLock pendingRaidLock = new PendingRaidLock();
                    pendingRaidLock.timeUntilLock = 60000;
                    pendingRaidLock.completedMask = instanceLock.getData().getCompletedEncountersMask();
                    pendingRaidLock.extending = playerLock != null && playerLock.isExtended();
                    pendingRaidLock.warningOnly = entries.Map.IsFlexLocking(); // events it triggers:  1 : INSTANCE_LOCK_WARNING   0 : INSTANCE_LOCK_STOP / INSTANCE_LOCK_START
                    player.getSession().sendPacket(pendingRaidLock);

                    if (!entries.Map.IsFlexLocking()) {
                        player.setPendingBind(getInstanceId(), 60000);
                    }
                }
            }
        }

        Log.outInfo(LogFilter.Maps, "MAP: Player '{0}' entered instance '{1}' of map '{2}'", player.getName(), getInstanceId(), getMapName());

        // initialize unload state
        setUnloadTimer(0);

        // this will acquire the same mutex so it cannot be in the previous block
        super.addPlayerToMap(player, initPlayer);

        if (data != null) {
            data.onPlayerEnter(player);
        }

        if (scenario != null) {
            scenario.onPlayerEnter(player);
        }

        return true;
    }

    @Override
    public void update(int delta) {
        super.update(delta);

        if (data != null) {
            data.update(delta);
            data.updateCombatResurrection(delta);
        }

        if (scenario != null) {
            scenario.update(delta);
        }

        if (instanceExpireEvent != null && instanceExpireEvent.getValue().compareTo(gameTime.GetSystemTime()) < 0) {
            reset(InstanceResetMethod.Expire);
            instanceExpireEvent = global.getInstanceLockMgr().getNextResetTime(new MapDb2Entries(getEntry(), getMapDifficulty()));
        }
    }

    @Override
    public void removePlayerFromMap(Player player, boolean remove) {
        Logs.MAPS.debug("MAP: Removing player '{}' from instance '{}' of map '{}' before relocating to another map", player.getName(), getInstanceId(), getMapName());

        if (data != null) {
            data.onPlayerLeave(player);
        }

        // if last player set unload timer
        if (getUnloadTimer() == 0 && getPlayers().size() == 1) {
            setUnloadTimer((instanceLock != null && instanceLock.isExpired()) ? 1 : (int) Math.max(WorldConfig.getIntValue(WorldCfg.InstanceUnloadDelay), 1));
        }

        if (scenario != null) {
            scenario.onPlayerExit(player);
        }

        super.removePlayerFromMap(player, remove);
    }

    public final void createInstanceData() {
        if (data != null) {
            return;
        }

        var mInstance = global.getObjectMgr().getInstanceTemplate(getId());

        if (mInstance != null) {
            scriptId = mInstance.scriptId;
            data = global.getScriptMgr().<IInstanceMapGetInstanceScript, InstanceScript>RunScriptRet(p -> p.GetInstanceScript(this), getScriptId(), null);
        }

        if (data == null) {
            return;
        }

        if (instanceLock == null || instanceLock.getInstanceId() == 0) {
            data.create();

            return;
        }

        MapDb2Entries entries = new MapDb2Entries(getEntry(), getMapDifficulty());

        if (!entries.IsInstanceIdBound() || !isRaid() || !entries.MapDifficulty.IsRestoringDungeonState() || owningGroupRef.isValid()) {
            data.create();

            return;
        }

        var lockData = instanceLock.getInstanceInitializationData();
        data.setCompletedEncountersMask(lockData.getCompletedEncountersMask());
        data.setEntranceLocation(lockData.getEntranceWorldSafeLocId());

        if (!lockData.getData().isEmpty()) {
            Logs.MAPS.debug(String.format("Loading instance data for `%1$s` with id %2$s", global.getObjectMgr().getScriptName(scriptId), getInstanceIdInternal()));
            data.load(lockData.getData());
        } else {
            data.create();
        }
    }

    public final PlayerGroup getOwningGroup() {
        return owningGroupRef.getTarget();
    }

    public final void trySetOwningGroup(PlayerGroup group) {
        if (!owningGroupRef.isValid()) {
            owningGroupRef.link(group, this);
        }
    }

    public final InstanceresetResult reset(InstanceResetMethod method) {
        // raids can be reset if no boss was killed
        if (method != InstanceResetMethod.Expire && instanceLock != null && instanceLock.getData().getCompletedEncountersMask() != 0) {
            return InstanceResetResult.CannotReset;
        }

        if (havePlayers()) {
            switch (method) {
                case Manual:
                    // notify the players to leave the instance so it can be reset
                    for (var player : getPlayers()) {
                        player.sendResetFailedNotify(getId());
                    }

                    break;
                case OnChangeDifficulty:
                    // no client notification
                    break;
                case Expire: {
                    RaidInstanceMessage raidInstanceMessage = new RaidInstanceMessage();
                    raidInstanceMessage.type = InstanceResetWarningType.Expired;
                    raidInstanceMessage.mapID = getId();
                    raidInstanceMessage.difficultyID = getDifficultyID();
                    raidInstanceMessage.write();

                    PendingRaidLock pendingRaidLock = new PendingRaidLock();
                    pendingRaidLock.timeUntilLock = 60000;
                    pendingRaidLock.completedMask = instanceLock.getData().getCompletedEncountersMask();
                    pendingRaidLock.extending = true;
                    pendingRaidLock.warningOnly = getEntry().IsFlexLocking();
                    pendingRaidLock.write();

                    for (var player : getPlayers()) {
                        player.sendPacket(raidInstanceMessage);
                        player.sendPacket(pendingRaidLock);

                        if (!pendingRaidLock.warningOnly) {
                            player.setPendingBind(getInstanceId(), 60000);
                        }
                    }

                    break;
                }
                default:
                    break;
            }

            return InstanceResetResult.NotEmpty;
        } else {
            // unloaded at next update
            setUnloadTimer(1);
        }

        return InstanceResetResult.success;
    }

    public final String getScriptName() {
        return global.getObjectMgr().getScriptName(scriptId);
    }

    public final void updateInstanceLock(UpdateBossStateSaveDataEvent updateSaveDataEvent) {
        if (instanceLock != null) {
            var instanceCompletedEncounters = instanceLock.getData().getCompletedEncountersMask() | (1 << updateSaveDataEvent.dungeonEncounter.bit);

            MapDb2Entries entries = new MapDb2Entries(getEntry(), getMapDifficulty());

            SQLTransaction trans = new SQLTransaction();

            if (entries.IsInstanceIdBound()) {
                global.getInstanceLockMgr().updateSharedInstanceLock(trans, new InstanceLockUpdateEvent(getInstanceId(), data.getSaveData(), instanceCompletedEncounters, updateSaveDataEvent.dungeonEncounter, data.getEntranceLocationForCompletedEncounters(instanceCompletedEncounters)));
            }

            for (var player : getPlayers()) {
                // never instance bind GMs with GM mode enabled
                if (player.isGameMaster()) {
                    continue;
                }

                var playerLock = global.getInstanceLockMgr().findActiveInstanceLock(player.getGUID(), entries);
                var oldData = "";
                int playerCompletedEncounters = 0;

                if (playerLock != null) {
                    oldData = playerLock.getData().getData();
                    playerCompletedEncounters = playerLock.getData().getCompletedEncountersMask() | (1 << updateSaveDataEvent.dungeonEncounter.bit);
                }

                var isNewLock = playerLock == null || playerLock.getData().getCompletedEncountersMask() == 0 || playerLock.isExpired();

                var newLock = global.getInstanceLockMgr().updateInstanceLockForPlayer(trans, player.getGUID(), entries, new InstanceLockUpdateEvent(getInstanceId(), data.updateBossStateSaveData(oldData, updateSaveDataEvent), instanceCompletedEncounters, updateSaveDataEvent.dungeonEncounter, data.getEntranceLocationForCompletedEncounters(playerCompletedEncounters)));

                if (isNewLock) {
                    InstanceSaveCreated data = new InstanceSaveCreated();
                    data.gm = player.isGameMaster();
                    player.sendPacket(data);

                    player.getSession().sendCalendarRaidLockoutAdded(newLock);
                }
            }

            DB.characters.CommitTransaction(trans);
        }
    }

    public final void updateInstanceLock(UpdateAdditionalSaveDataEvent updateSaveDataEvent) {
        if (instanceLock != null) {
            var instanceCompletedEncounters = instanceLock.getData().getCompletedEncountersMask();

            MapDb2Entries entries = new MapDb2Entries(getEntry(), getMapDifficulty());

            SQLTransaction trans = new SQLTransaction();

            if (entries.IsInstanceIdBound()) {
                global.getInstanceLockMgr().updateSharedInstanceLock(trans, new InstanceLockUpdateEvent(getInstanceId(), data.getSaveData(), instanceCompletedEncounters, null, null));
            }

            for (var player : getPlayers()) {
                // never instance bind GMs with GM mode enabled
                if (player.isGameMaster()) {
                    continue;
                }

                var playerLock = global.getInstanceLockMgr().findActiveInstanceLock(player.getGUID(), entries);
                var oldData = "";

                if (playerLock != null) {
                    oldData = playerLock.getData().getData();
                }

                var isNewLock = playerLock == null || playerLock.getData().getCompletedEncountersMask() == 0 || playerLock.isExpired();

                var newLock = global.getInstanceLockMgr().updateInstanceLockForPlayer(trans, player.getGUID(), entries, new InstanceLockUpdateEvent(getInstanceId(), data.updateAdditionalSaveData(oldData, updateSaveDataEvent), instanceCompletedEncounters, null, null));

                if (isNewLock) {
                    InstanceSaveCreated data = new InstanceSaveCreated();
                    data.gm = player.isGameMaster();
                    player.sendPacket(data);

                    player.getSession().sendCalendarRaidLockoutAdded(newLock);
                }
            }

            DB.characters.CommitTransaction(trans);
        }
    }

    public final void createInstanceLockForPlayer(Player player) {
        MapDb2Entries entries = new MapDb2Entries(getEntry(), getMapDifficulty());
        var playerLock = global.getInstanceLockMgr().findActiveInstanceLock(player.getGUID(), entries);

        var isNewLock = playerLock == null || playerLock.getData().getCompletedEncountersMask() == 0 || playerLock.isExpired();

        SQLTransaction trans = new SQLTransaction();

        var newLock = global.getInstanceLockMgr().updateInstanceLockForPlayer(trans, player.getGUID(), entries, new InstanceLockUpdateEvent(getInstanceId(), data.getSaveData(), instanceLock.getData().getCompletedEncountersMask(), null, null));

        DB.characters.CommitTransaction(trans);

        if (isNewLock) {
            InstanceSaveCreated data = new InstanceSaveCreated();
            data.gm = player.isGameMaster();
            player.sendPacket(data);

            player.getSession().sendCalendarRaidLockoutAdded(newLock);
        }
    }

    @Override
    public String getDebugInfo() {
        return String.format("%1$s\nScriptId: %2$s ScriptName: %3$s", super.getDebugInfo(), getScriptId(), getScriptName());
    }

    protected void finalize() throws Throwable {
        if (instanceLock != null) {
            instanceLock.setInUse(false);
        }
    }
}
