package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;

class TitleCommands {
    
    private static boolean handleTitlesCurrentCommand(CommandHandler handler, int titleId) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var titleInfo = CliDB.CharTitlesStorage.get(titleId);

        if (titleInfo == null) {
            handler.sendSysMessage(SysMessage.InvalidTitleId, titleId);

            return false;
        }

        var tNameLink = handler.getNameLink(target);
        var titleNameStr = String.format(target.getNativeGender() == gender.Male ? titleInfo.name.charAt(handler.getSessionDbcLocale()) : titleInfo.Name1[handler.getSessionDbcLocale()].ConvertFormatSyntax(), target.getName());

        target.setTitle(titleInfo);
        target.setChosenTitle(titleInfo.MaskID);

        handler.sendSysMessage(SysMessage.TitleCurrentRes, titleId, titleNameStr, tNameLink);

        return true;
    }

    
    private static boolean handleTitlesAddCommand(CommandHandler handler, int titleId) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var titleInfo = CliDB.CharTitlesStorage.get(titleId);

        if (titleInfo == null) {
            handler.sendSysMessage(SysMessage.InvalidTitleId, titleId);

            return false;
        }

        var tNameLink = handler.getNameLink(target);

        var titleNameStr = String.format((target.getNativeGender() == gender.Male ? titleInfo.Name : titleInfo.name1)[handler.getSessionDbcLocale()].ConvertFormatSyntax(), target.getName());

        target.setTitle(titleInfo);
        handler.sendSysMessage(SysMessage.TitleAddRes, titleId, titleNameStr, tNameLink);

        return true;
    }

    
    private static boolean handleTitlesRemoveCommand(CommandHandler handler, int titleId) {
        var target = handler.getSelectedPlayer();

        if (!target) {
            handler.sendSysMessage(SysMessage.NoCharSelected);

            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
            return false;
        }

        var titleInfo = CliDB.CharTitlesStorage.get(titleId);

        if (titleInfo == null) {
            handler.sendSysMessage(SysMessage.InvalidTitleId, titleId);

            return false;
        }

        target.setTitle(titleInfo, true);

        var tNameLink = handler.getNameLink(target);
        var titleNameStr = String.format((target.getNativeGender() == gender.Male ? titleInfo.Name : titleInfo.name1)[handler.getSessionDbcLocale()].ConvertFormatSyntax(), target.getName());

        handler.sendSysMessage(SysMessage.TitleRemoveRes, titleId, titleNameStr, tNameLink);

        if (!target.hasTitle(target.getPlayerData().playerTitle)) {
            target.setChosenTitle(0);
            handler.sendSysMessage(SysMessage.CurrentTitleReset, tNameLink);
        }

        return true;
    }

    
    private static class TitleSetCommands {
        //Edit Player KnownTitles

        private static boolean handleTitlesSetMaskCommand(CommandHandler handler, long mask) {
            var target = handler.getSelectedPlayer();

            if (!target) {
                handler.sendSysMessage(SysMessage.NoCharSelected);

                return false;
            }

            // check online security
            if (handler.hasLowerSecurity(target, ObjectGuid.Empty)) {
                return false;
            }

            var titles2 = mask;

            for (var tEntry : CliDB.CharTitlesStorage.values()) {
                titles2 &= ~(1 << tEntry.MaskID);
            }

            mask &= ~titles2; // remove not existed titles

            target.setKnownTitles(0, mask);
            handler.sendSysMessage(SysMessage.Done);

            if (!target.hasTitle(target.getPlayerData().playerTitle)) {
                target.setChosenTitle(0);
                handler.sendSysMessage(SysMessage.CurrentTitleReset, handler.getNameLink(target));
            }

            return true;
        }
    }
}
