package com.github.azeroth.game.chat;


import com.github.azeroth.game.map.InstanceLock;

import java.util.ArrayList;



class InstanceCommands {
    
    private static boolean handleInstanceGetBossStateCommand(CommandHandler handler, int encounterId, PlayerIdentifier player) {
        // Character name must be provided when using this from console.
        if (player == null || handler.getSession() == null) {
            handler.sendSysMessage(SysMessage.CmdSyntax);

            return false;
        }

        if (player == null) {
            player = PlayerIdentifier.fromSelf(handler);
        }

        if (player.isConnected()) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var map = player.getConnectedPlayer().getMap().toInstanceMap();

        if (map == null) {
            handler.sendSysMessage(SysMessage.NotDungeon);

            return false;
        }

        if (map.getInstanceScript() == null) {
            handler.sendSysMessage(SysMessage.NoInstanceData);

            return false;
        }

        if (encounterId > map.getInstanceScript().getEncounterCount()) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        var state = map.getInstanceScript().getBossState(encounterId);
        handler.sendSysMessage(SysMessage.CommandInstGetBossState, encounterId, state);

        return true;
    }

    
    private static boolean handleInstanceListBindsCommand(CommandHandler handler) {
        var player = handler.getSelectedPlayer();

        if (player == null) {
            player = handler.getSession().getPlayer();
        }

        var now = gameTime.GetDateAndTime();
        var instanceLocks = global.getInstanceLockMgr().getInstanceLocksForPlayer(player.getGUID());

        for (var instanceLock : instanceLocks) {
            MapDb2Entries entries = new MapDb2Entries(instanceLock.getMapId(), instanceLock.getDifficultyId());
            var timeleft = !instanceLock.isExpired() ? time.secsToTimeString((long) (instanceLock.getEffectiveExpiryTime() - now).TotalSeconds, 0, false) : "-";

            handler.sendSysMessage(SysMessage.CommandListBindInfo, entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale()), entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, instanceLock.getInstanceId(), handler.getSysMessage(instanceLock.isExpired() ? SysMessage.Yes : SysMessage.No), handler.getSysMessage(instanceLock.isExtended() ? SysMessage.Yes : SysMessage.No), timeleft);
        }

        handler.sendSysMessage(SysMessage.CommandListBindPlayerBinds, instanceLocks.size());

        return true;
    }

    
    private static boolean handleInstanceSetBossStateCommand(CommandHandler handler, int encounterId, EncounterState state, PlayerIdentifier player) {
        // Character name must be provided when using this from console.
        if (player == null || handler.getSession() == null) {
            handler.sendSysMessage(SysMessage.CmdSyntax);

            return false;
        }

        if (player == null) {
            player = PlayerIdentifier.fromSelf(handler);
        }

        if (!player.isConnected()) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var map = player.getConnectedPlayer().getMap().toInstanceMap();

        if (map == null) {
            handler.sendSysMessage(SysMessage.NotDungeon);

            return false;
        }

        if (map.getInstanceScript() == null) {
            handler.sendSysMessage(SysMessage.NoInstanceData);

            return false;
        }

        // Reject improper values.
        if (encounterId > map.getInstanceScript().getEncounterCount()) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        map.getInstanceScript().setBossState(encounterId, state);
        handler.sendSysMessage(SysMessage.CommandInstSetBossState, encounterId, state);

        return true;
    }

    
    private static boolean handleInstanceStatsCommand(CommandHandler handler) {
        handler.sendSysMessage("instances loaded: {0}", global.getMapMgr().GetNumInstances());
        handler.sendSysMessage("players in instances: {0}", global.getMapMgr().GetNumPlayersInInstances());

        var statistics = global.getInstanceLockMgr().getStatistics();

        handler.sendSysMessage(SysMessage.CommandInstStatSaves, statistics.instanceCount);
        handler.sendSysMessage(SysMessage.CommandInstStatPlayersbound, statistics.playerCount);

        return true;
    }

    

    private static boolean handleInstanceUnbindCommand(CommandHandler handler, Object mapArg, Integer difficultyArg) {
        var player = handler.getSelectedPlayer();

        if (player == null) {
            player = handler.getSession().getPlayer();
        }

        Integer mapId = null;
        Difficulty difficulty = null;

        if (mapArg instanceof Integer) {
            mapId = (int) mapArg;
        }

        if (difficultyArg != null && CliDB.DifficultyStorage.containsKey(difficultyArg.intValue())) {
            difficulty = Difficulty.forValue(difficultyArg);
        }

        ArrayList<InstanceLock> locksReset = new ArrayList<>();
        ArrayList<InstanceLock> locksNotReset = new ArrayList<>();

        global.getInstanceLockMgr().resetInstanceLocksForPlayer(player.getGUID(), mapId, difficulty, locksReset, locksNotReset);

        var now = gameTime.GetDateAndTime();

        for (var instanceLock : locksReset) {
            MapDb2Entries entries = new MapDb2Entries(instanceLock.getMapId(), instanceLock.getDifficultyId());
            var timeleft = !instanceLock.isExpired() ? time.secsToTimeString((long) (instanceLock.getEffectiveExpiryTime() - now).TotalSeconds, 0, false) : "-";

            handler.sendSysMessage(SysMessage.CommandInstUnbindUnbinding, entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale()), entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, instanceLock.getInstanceId(), handler.getSysMessage(instanceLock.isExpired() ? SysMessage.Yes : SysMessage.No), handler.getSysMessage(instanceLock.isExtended() ? SysMessage.Yes : SysMessage.No), timeleft);
        }

        handler.sendSysMessage(SysMessage.CommandInstUnbindUnbound, locksReset.size());

        for (var instanceLock : locksNotReset) {
            MapDb2Entries entries = new MapDb2Entries(instanceLock.getMapId(), instanceLock.getDifficultyId());
            var timeleft = !instanceLock.isExpired() ? time.secsToTimeString((long) (instanceLock.getEffectiveExpiryTime() - now).TotalSeconds, 0, false) : "-";

            handler.sendSysMessage(SysMessage.CommandInstUnbindFailed, entries.Map.id, entries.Map.MapName.get(global.getWorldMgr().getDefaultDbcLocale()), entries.MapDifficulty.difficultyID, CliDB.DifficultyStorage.get(entries.MapDifficulty.difficultyID).name, instanceLock.getInstanceId(), handler.getSysMessage(instanceLock.isExpired() ? SysMessage.Yes : SysMessage.No), handler.getSysMessage(instanceLock.isExtended() ? SysMessage.Yes : SysMessage.No), timeleft);
        }

        player.sendRaidInfo();

        return true;
    }
}
