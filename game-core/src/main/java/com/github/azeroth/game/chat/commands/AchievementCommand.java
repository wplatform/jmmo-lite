package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;

class AchievementCommand {

    private static boolean handleAchievementAddCommand(CommandHandler handler, AchievementRecord achievementEntry) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        target.completedAchievement(achievementEntry);

        return true;
    }
}
