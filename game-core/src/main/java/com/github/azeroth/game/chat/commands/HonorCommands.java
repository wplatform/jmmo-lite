package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;


class HonorCommands {

    private static boolean handleHonorUpdateCommand(CommandHandler handler) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        target.updateHonorFields();

        return true;
    }


    private static class HonorAddCommands {

        private static boolean handleHonorAddCommand(CommandHandler handler, int amount) {
            var target = handler.getSelectedPlayer();

            if (!target) {
                handler.sendSysMessage(SysMessage.PlayerNotFound);

                return false;
            }

            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            target.rewardHonor(null, 1, amount);

            return true;
        }


        private static boolean handleHonorAddKillCommand(CommandHandler handler) {
            var target = handler.getSelectedUnit();

            if (!target) {
                handler.sendSysMessage(SysMessage.PlayerNotFound);

                return false;
            }

            // check online security
            var player = target.toPlayer();

            if (player) {
                if (handler.hasLowerSecurity(player, ObjectGuid.Empty)) {
                    return false;
                }
            }

            handler.getPlayer().rewardHonor(target, 1);

            return true;
        }
    }
}
