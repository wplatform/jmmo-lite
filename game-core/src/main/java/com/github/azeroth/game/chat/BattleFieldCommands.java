package com.github.azeroth.game.chat;


class BattleFieldCommands {
    
    private static boolean handleBattlefieldEnable(CommandHandler handler, int battleId) {
        var bf = global.getBattleFieldMgr().getBattlefieldByBattleId(handler.getPlayer().getMap(), battleId);

        if (bf == null) {
            return false;
        }

        if (bf.isEnabled()) {
            bf.toggleBattlefield(false);

            if (battleId == 1) {
                handler.sendGlobalGMSysMessage("Wintergrasp is disabled");
            }
        } else {
            bf.toggleBattlefield(true);

            if (battleId == 1) {
                handler.sendGlobalGMSysMessage("Wintergrasp is enabled");
            }
        }

        return true;
    }

    
    private static boolean handleBattlefieldStart(CommandHandler handler, int battleId) {
        var bf = global.getBattleFieldMgr().getBattlefieldByBattleId(handler.getPlayer().getMap(), battleId);

        if (bf == null) {
            return false;
        }

        bf.startBattle();

        if (battleId == 1) {
            handler.sendGlobalGMSysMessage("Wintergrasp (Command start used)");
        }

        return true;
    }

    
    private static boolean handleBattlefieldEnd(CommandHandler handler, int battleId) {
        var bf = global.getBattleFieldMgr().getBattlefieldByBattleId(handler.getPlayer().getMap(), battleId);

        if (bf == null) {
            return false;
        }

        bf.endBattle(true);

        if (battleId == 1) {
            handler.sendGlobalGMSysMessage("Wintergrasp (Command stop used)");
        }

        return true;
    }

    
    private static boolean handleBattlefieldSwitch(CommandHandler handler, int battleId) {
        var bf = global.getBattleFieldMgr().getBattlefieldByBattleId(handler.getPlayer().getMap(), battleId);

        if (bf == null) {
            return false;
        }

        bf.endBattle(false);

        if (battleId == 1) {
            handler.sendGlobalGMSysMessage("Wintergrasp (Command switch used)");
        }

        return true;
    }

    
    private static boolean handleBattlefieldTimer(CommandHandler handler, int battleId, int time) {
        var bf = global.getBattleFieldMgr().getBattlefieldByBattleId(handler.getPlayer().getMap(), battleId);

        if (bf == null) {
            return false;
        }

        bf.setTimer(time * time.InMilliseconds);

        if (battleId == 1) {
            handler.sendGlobalGMSysMessage("Wintergrasp (Command timer used)");
        }

        return true;
    }
}
