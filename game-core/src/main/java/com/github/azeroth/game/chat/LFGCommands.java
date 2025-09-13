package com.github.azeroth.game.chat;


import com.github.azeroth.game.dungeonfinding.LFGQueue;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;


class LFGCommands {
    
    private static boolean handleLfgPlayerInfoCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            printPlayerInfo(handler, target);

            return true;
        }

        return false;
    }

    
    private static boolean handleLfgGroupInfoCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        PlayerGroup groupTarget = null;
        var target = player.getConnectedPlayer();

        if (target != null) {
            groupTarget = target.getGroup();
        } else {
            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GROUP_MEMBER);
            stmt.AddValue(0, player.getGUID().getCounter());
            var resultGroup = DB.characters.query(stmt);

            if (!resultGroup.isEmpty()) {
                groupTarget = global.getGroupMgr().getGroupByDbStoreId(resultGroup.<Integer>Read(0));
            }
        }

        if (!groupTarget) {
            handler.sendSysMessage(SysMessage.LfgNotInGroup, player.getName());

            return false;
        }

        var guid = groupTarget.getGUID();
        handler.sendSysMessage(SysMessage.LfgGroupInfo, groupTarget.isLFGGroup(), global.getLFGMgr().getState(guid), global.getLFGMgr().getDungeon(guid));

        for (var slot : groupTarget.getMemberSlots()) {
            var p = global.getObjAccessor().findPlayer(slot.guid);

            if (p) {
                printPlayerInfo(handler, p);
            } else {
                handler.sendSysMessage("{0} is offline.", slot.name);
            }
        }

        return true;
    }

    
    private static boolean handleLfgOptionsCommand(CommandHandler handler, Integer optionsArg) {
        if (optionsArg != null) {
            global.getLFGMgr().setOptions(LfgOptions.forValue(optionsArg.intValue()));
            handler.sendSysMessage(SysMessage.LfgOptionsChanged);
        }

        handler.sendSysMessage(SysMessage.LfgOptions, global.getLFGMgr().getOptions());

        return true;
    }

    
    private static boolean handleLfgQueueInfoCommand(CommandHandler handler, String full) {
        handler.sendSysMessage(global.getLFGMgr().dumpQueueInfo(!full.isEmpty()));

        return true;
    }

    
    private static boolean handleLfgCleanCommand(CommandHandler handler) {
        handler.sendSysMessage(SysMessage.LfgClean);
        global.getLFGMgr().clean();

        return true;
    }

    private static void printPlayerInfo(CommandHandler handler, Player player) {
        if (!player) {
            return;
        }

        var guid = player.getGUID();
        var dungeons = global.getLFGMgr().getSelectedDungeons(guid);

        handler.sendSysMessage(SysMessage.LfgPlayerInfo, player.getName(), global.getLFGMgr().getState(guid), dungeons.size(), LFGQueue.concatenateDungeons(dungeons), LFGQueue.getRolesString(global.getLFGMgr().getRoles(guid)));
    }
}
