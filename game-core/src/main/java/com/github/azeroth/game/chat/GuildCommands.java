package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.guild.guild;
import game.ObjectManager;


class GuildCommands {
    
    private static boolean handleGuildCreateCommand(CommandHandler handler, StringArguments args) {
        if (args.isEmpty()) {
            return false;
        }

        Player target;
        tangible.OutObject<Player> tempOut_target = new tangible.OutObject<Player>();
        if (!handler.extractPlayerTarget(args.get(0) != '"' ? args : null, tempOut_target)) {
            target = tempOut_target.outArgValue;
            return false;
        } else {
            target = tempOut_target.outArgValue;
        }

        var guildName = handler.extractQuotedArg(args.NextString(" "));

        if (StringUtil.isEmpty(guildName)) {
            return false;
        }

        if (target.getGuildId() != 0) {
            handler.sendSysMessage(SysMessage.PlayerInGuild);

            return false;
        }

        if (global.getGuildMgr().getGuildByName(guildName)) {
            handler.sendSysMessage(SysMessage.GuildRenameAlreadyExists);

            return false;
        }

        if (global.getObjectMgr().isReservedName(guildName) || !ObjectManager.isValidCharterName(guildName)) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        Guild guild = new guild();

        if (!guild.create(target, guildName)) {
            handler.sendSysMessage(SysMessage.GuildNotCreated);

            return false;
        }

        global.getGuildMgr().addGuild(guild);

        return true;
    }

    
    private static boolean handleGuildDeleteCommand(CommandHandler handler, QuotedString guildName) {
        if (guildName.isEmpty()) {
            return false;
        }

        var guild = global.getGuildMgr().getGuildByName(guildName);

        if (guild == null) {
            return false;
        }

        guild.disband();

        return true;
    }

    
    private static boolean handleGuildInviteCommand(CommandHandler handler, PlayerIdentifier targetIdentifier, QuotedString guildName) {
        if (targetIdentifier == null) {
            targetIdentifier = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (targetIdentifier == null) {
            return false;
        }

        if (guildName.isEmpty()) {
            return false;
        }

        var targetGuild = global.getGuildMgr().getGuildByName(guildName);

        if (targetGuild == null) {
            return false;
        }

        targetGuild.addMember(null, targetIdentifier.getGUID());

        return true;
    }

    
    private static boolean handleGuildUninviteCommand(CommandHandler handler, PlayerIdentifier targetIdentifier, QuotedString guildName) {
        if (targetIdentifier == null) {
            targetIdentifier = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (targetIdentifier == null) {
            return false;
        }

        var guildId = targetIdentifier.isConnected() ? targetIdentifier.getConnectedPlayer().getGuildId() : global.getCharacterCacheStorage().getCharacterGuildIdByGuid(targetIdentifier.getGUID());

        if (guildId == 0) {
            return false;
        }

        var targetGuild = global.getGuildMgr().getGuildById(guildId);

        if (targetGuild == null) {
            return false;
        }

        targetGuild.deleteMember(null, targetIdentifier.getGUID(), false, true, true);

        return true;
    }

    
    private static boolean handleGuildRankCommand(CommandHandler handler, PlayerIdentifier player, byte rank) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var guildId = player.isConnected() ? player.getConnectedPlayer().getGuildId() : global.getCharacterCacheStorage().getCharacterGuildIdByGuid(player.getGUID());

        if (guildId == 0) {
            return false;
        }

        var targetGuild = global.getGuildMgr().getGuildById(guildId);

        if (!targetGuild) {
            return false;
        }

        return targetGuild.changeMemberRank(null, player.getGUID(), GuildRankId.forValue(rank));
    }

    
    private static boolean handleGuildRenameCommand(CommandHandler handler, QuotedString oldGuildName, QuotedString newGuildName) {
        if (oldGuildName.isEmpty()) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        if (newGuildName.isEmpty()) {
            handler.sendSysMessage(SysMessage.InsertGuildName);

            return false;
        }

        var guild = global.getGuildMgr().getGuildByName(oldGuildName);

        if (!guild) {
            handler.sendSysMessage(SysMessage.CommandCouldnotfind, oldGuildName);

            return false;
        }

        if (global.getGuildMgr().getGuildByName(newGuildName)) {
            handler.sendSysMessage(SysMessage.GuildRenameAlreadyExists, newGuildName);

            return false;
        }

        if (!guild.setName(newGuildName)) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        handler.sendSysMessage(SysMessage.GuildRenameDone, oldGuildName, newGuildName);

        return true;
    }

    
    private static boolean handleGuildInfoCommand(CommandHandler handler, StringArguments args) {
        Guild guild = null;
        var target = handler.getSelectedPlayerOrSelf();

        if (!args.isEmpty() && args.get(0) != '\0') {
            if (Character.isDigit(args.get(0))) {
                guild = global.getGuildMgr().getGuildById(args.NextUInt64(" "));
            } else {
                guild = global.getGuildMgr().getGuildByName(args.NextString(" "));
            }
        } else if (target) {
            guild = target.getGuild();
        }

        if (!guild) {
            return false;
        }

        // Display Guild Information
        handler.sendSysMessage(SysMessage.GuildInfoName, guild.getName(), guild.getId()); // Guild id + Name

        String guildMasterName;
        tangible.OutObject<String> tempOut_guildMasterName = new tangible.OutObject<String>();
        if (global.getCharacterCacheStorage().getCharacterNameByGuid(guild.getLeaderGUID(), tempOut_guildMasterName)) {
            guildMasterName = tempOut_guildMasterName.outArgValue;
            handler.sendSysMessage(SysMessage.GuildInfoGuildMaster, guildMasterName, guild.getLeaderGUID().toString()); // Guild Master
        } else {
            guildMasterName = tempOut_guildMasterName.outArgValue;
        }

        // Format creation date

        var createdDateTime = time.UnixTimeToDateTime(guild.getCreatedDate());
        handler.sendSysMessage(SysMessage.GuildInfoCreationDate, createdDateTime.ToLongDateString()); // Creation Date
        handler.sendSysMessage(SysMessage.GuildInfoMemberCount, guild.getMembersCount()); // Number of Members
        handler.sendSysMessage(SysMessage.GuildInfoBankGold, guild.getBankMoney() / 100 / 100); // Bank gold (in gold coins)
        handler.sendSysMessage(SysMessage.GuildInfoLevel, guild.getLevel()); // Level
        handler.sendSysMessage(SysMessage.GuildInfoMotd, guild.getMOTD()); // Message of the Day
        handler.sendSysMessage(SysMessage.GuildInfoExtraInfo, guild.getInfo()); // Extra Information

        return true;
    }
}
