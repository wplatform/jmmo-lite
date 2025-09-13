package com.github.azeroth.game.chat;


import com.github.azeroth.game.arena.ArenaTeam;


class ArenaCommands {
    
    private static boolean handleArenaCreateCommand(CommandHandler handler, PlayerIdentifier captain, String name, ArenaTypes type) {
        if (global.getArenaTeamMgr().getArenaTeamByName(name) != null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNameExists, name);

            return false;
        }

        if (captain == null) {
            captain = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (captain == null) {
            return false;
        }

        if (global.getCharacterCacheStorage().getCharacterArenaTeamIdByGuid(captain.getGUID(), (byte) type.getValue()) != 0) {
            handler.sendSysMessage(SysMessage.ArenaErrorSize, captain.getName());

            return false;
        }

        ArenaTeam arena = new ArenaTeam();

        if (!arena.create(captain.getGUID(), (byte) type.getValue(), name, (int) 4293102085, (byte) 101, (int) 4293253939, (byte) 4, (int) 4284049911)) {
            handler.sendSysMessage(SysMessage.BadValue);

            return false;
        }

        global.getArenaTeamMgr().addArenaTeam(arena);
        handler.sendSysMessage(SysMessage.ArenaCreate, arena.getName(), arena.getId(), arena.getArenaType(), arena.getCaptain());

        return true;
    }

    
    private static boolean handleArenaDisbandCommand(CommandHandler handler, int teamId) {
        var arena = global.getArenaTeamMgr().getArenaTeamById(teamId);

        if (arena == null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNotFound, teamId);

            return false;
        }

        if (arena.isFighting()) {
            handler.sendSysMessage(SysMessage.ArenaErrorCombat);

            return false;
        }

        var name = arena.getName();
        arena.disband();

        handler.sendSysMessage(SysMessage.ArenaDisband, name, teamId);

        return true;
    }

    
    private static boolean handleArenaRenameCommand(CommandHandler handler, String oldName, String newName) {
        var arena = global.getArenaTeamMgr().getArenaTeamByName(oldName);

        if (arena == null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNameNotFound, oldName);

            return false;
        }

        if (global.getArenaTeamMgr().getArenaTeamByName(newName) != null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNameExists, oldName);

            return false;
        }

        if (arena.isFighting()) {
            handler.sendSysMessage(SysMessage.ArenaErrorCombat);

            return false;
        }

        if (!arena.setName(newName)) {
            handler.sendSysMessage(SysMessage.ArenaRename, arena.getId(), oldName, newName);

            return true;
        }

        handler.sendSysMessage(SysMessage.BadValue);

        return false;
    }

    
    private static boolean handleArenaCaptainCommand(CommandHandler handler, int teamId, PlayerIdentifier target) {
        var arena = global.getArenaTeamMgr().getArenaTeamById(teamId);

        if (arena == null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNotFound, teamId);

            return false;
        }

        if (arena.isFighting()) {
            handler.sendSysMessage(SysMessage.ArenaErrorCombat);

            return false;
        }

        if (target == null) {
            target = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (target == null) {
            return false;
        }

        if (!arena.isMember(target.getGUID())) {
            handler.sendSysMessage(SysMessage.ArenaErrorNotMember, target.getName(), arena.getName());

            return false;
        }

        if (com.github.azeroth.game.entity.Objects.equals(arena.getCaptain(), target.getGUID())) {
            handler.sendSysMessage(SysMessage.ArenaErrorCaptain, target.getName(), arena.getName());

            return false;
        }

        String oldCaptainName;
        tangible.OutObject<String> tempOut_oldCaptainName = new tangible.OutObject<String>();
        if (!global.getCharacterCacheStorage().getCharacterNameByGuid(arena.getCaptain(), tempOut_oldCaptainName)) {
            oldCaptainName = tempOut_oldCaptainName.outArgValue;
            return false;
        } else {
            oldCaptainName = tempOut_oldCaptainName.outArgValue;
        }

        arena.setCaptain(target.getGUID());
        handler.sendSysMessage(SysMessage.ArenaCaptain, arena.getName(), arena.getId(), oldCaptainName, target.getName());

        return true;
    }

    
    private static boolean handleArenaInfoCommand(CommandHandler handler, int teamId) {
        var arena = global.getArenaTeamMgr().getArenaTeamById(teamId);

        if (arena == null) {
            handler.sendSysMessage(SysMessage.ArenaErrorNotFound, teamId);

            return false;
        }

        handler.sendSysMessage(SysMessage.ArenaInfoHeader, arena.getName(), arena.getId(), arena.getRating(), arena.getArenaType(), arena.getArenaType());

        for (var member : arena.getMembers()) {
            handler.sendSysMessage(SysMessage.ArenaInfoMembers, member.name, member.guid, member.personalRating, (com.github.azeroth.game.entity.Objects.equals(arena.getCaptain(), member.guid) ? "- Captain" : ""));
        }

        return true;
    }

    
    private static boolean handleArenaLookupCommand(CommandHandler handler, String needle) {
        if (needle.isEmpty()) {
            return false;
        }

        var found = false;


        for (var(_, team) : global.getArenaTeamMgr().getArenaTeamMap()) {
            if (team.getName().Equals(needle, StringComparison.OrdinalIgnoreCase)) {
                if (handler.getSession() != null) {
                    handler.sendSysMessage(SysMessage.ArenaLookup, team.getName(), team.getId(), team.getArenaType(), team.getArenaType());
                    found = true;

                    continue;
                }
            }
        }

        if (!found) {
            handler.sendSysMessage(SysMessage.ArenaErrorNameNotFound, needle);
        }

        return true;
    }
}
