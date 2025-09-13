package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;


class DeserterCommands {
    private static boolean handleDeserterAdd(CommandHandler handler, int time, boolean isInstance) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        var aura = player.addAura(isInstance ? spells.LFGDundeonDeserter : spells.BGDeserter, player);

        if (aura == null) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        aura.setDuration((int) (time * time.InMilliseconds));

        return true;
    }

    private static boolean handleDeserterRemove(CommandHandler handler, boolean isInstance) {
        var player = handler.getSelectedPlayer();

        if (!player) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        player.removeAura(isInstance ? spells.LFGDundeonDeserter : spells.BGDeserter);

        return true;
    }

    
    private static class DeserterInstanceCommands {
        
        private static boolean handleDeserterInstanceAdd(CommandHandler handler, int time) {
            return handleDeserterAdd(handler, time, true);
        }

        
        private static boolean handleDeserterInstanceRemove(CommandHandler handler) {
            return handleDeserterRemove(handler, true);
        }
    }

    
    private static class DeserterBGCommands {
        
        private static boolean handleDeserterBGAdd(CommandHandler handler, int time) {
            return handleDeserterAdd(handler, time, false);
        }

        
        private static boolean handleDeserterBGRemove(CommandHandler handler) {
            return handleDeserterRemove(handler, false);
        }
    }
}
