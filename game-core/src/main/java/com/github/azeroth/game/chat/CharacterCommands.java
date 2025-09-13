package com.github.azeroth.game.chat;


import game.*;

import java.util.ArrayList;



class CharacterCommands {
    
    private static boolean handleCharacterTitlesCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null || !player.isConnected()) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var target = player.getConnectedPlayer();

        var loc = handler.getSessionDbcLocale();
        var targetName = player.getName();
        var knownStr = handler.getSysMessage(SysMessage.Known);

        // Search in CharTitles.dbc
        for (var titleInfo : CliDB.CharTitlesStorage.values()) {
            if (target.hasTitle(titleInfo)) {
                var name = (target.getNativeGender() == gender.Male ? titleInfo.Name : titleInfo.name1)[loc];

                if (name.isEmpty()) {
                    name = (target.getNativeGender() == gender.Male ? titleInfo.Name : titleInfo.name1)[Global.getWorldMgr().getDefaultDbcLocale()];
                }

                if (name.isEmpty()) {
                    continue;
                }

                var activeStr = "";

                if (target.getPlayerData().playerTitle == titleInfo.MaskID) {
                    activeStr = handler.getSysMessage(SysMessage.active);
                }

                var titleName = String.format(name.ConvertFormatSyntax(), targetName);

                // send title in "id (idx:idx) - [namedlink locale]" format
                if (handler.getSession() != null) {
                    handler.sendSysMessage(SysMessage.TitleListChat, titleInfo.id, titleInfo.MaskID, titleInfo.id, titleName, loc, knownStr, activeStr);
                } else {
                    handler.sendSysMessage(SysMessage.TitleListConsole, titleInfo.id, titleInfo.MaskID, name, loc, knownStr, activeStr);
                }
            }
        }

        return true;
    }

    //rename character


    private static boolean handleCharacterRenameCommand(CommandHandler handler, PlayerIdentifier player, String newName) {
        if (player == null && !newName.isEmpty()) {
            return false;
        }

        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            return false;
        }

        // check online security
        if (handler.hasLowerSecurity(null, player.getGUID())) {
            return false;
        }

        if (!newName.isEmpty()) {
            tangible.RefObject<String> tempRef_newName = new tangible.RefObject<String>(newName);
            if (!ObjectManager.normalizePlayerName(tempRef_newName)) {
                newName = tempRef_newName.refArgValue;
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            } else {
                newName = tempRef_newName.refArgValue;
            }

            if (ObjectManager.checkPlayerName(newName, player.isConnected() ? player.getConnectedPlayer().getSession().getSessionDbcLocale() : global.getWorldMgr().getDefaultDbcLocale(), true) != ResponseCodes.CharNameSuccess) {
                handler.sendSysMessage(SysMessage.BadValue);

                return false;
            }

            var session = handler.getSession();

            if (session != null) {
                if (!session.hasPermission(RBACPermissions.SkipCheckCharacterCreationReservedname) && global.getObjectMgr().isReservedName(newName)) {
                    handler.sendSysMessage(SysMessage.ReservedName);

                    return false;
                }
            }

            var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHECK_NAME);
            stmt.AddValue(0, newName);
            var result = DB.characters.query(stmt);

            if (!result.isEmpty()) {
                handler.sendSysMessage(SysMessage.RenamePlayerAlreadyExists, newName);

                return false;
            }

            // Remove declined name from db
            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_DECLINED_NAME);
            stmt.AddValue(0, player.getGUID().getCounter());
            DB.characters.execute(stmt);

            var target = player.getConnectedPlayer();

            if (target != null) {
                target.setName(newName);
                session = target.getSession();

                if (session != null) {
                    session.kickPlayer("HandleCharacterRenameCommand GM Command renaming character");
                }
            } else {
                stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_NAME_BY_GUID);
                stmt.AddValue(0, newName);
                stmt.AddValue(1, player.getGUID().getCounter());
                DB.characters.execute(stmt);
            }

            global.getCharacterCacheStorage().updateCharacterData(player.getGUID(), newName);

            handler.sendSysMessage(SysMessage.RenamePlayerWithNewName, player.getName(), newName);

            if (session != null) {
                var sessionPlayer = session.getPlayer();

                if (sessionPlayer) {
                    Log.outCommand(session.getAccountId(), "GM {0} (Account: {1}) forced rename {2} to player {3} (Account: {4})", sessionPlayer.getName(), session.getAccountId(), newName, sessionPlayer.getName(), global.getCharacterCacheStorage().getCharacterAccountIdByGuid(sessionPlayer.getGUID()));
                }
            } else {
                Log.outCommand(0, "CONSOLE forced rename '{0}' to '{1}' ({2})", player.getName(), newName, player.getGUID().toString());
            }
        } else {
            var target = player.getConnectedPlayer();

            if (target != null) {
                handler.sendSysMessage(SysMessage.RenamePlayer, handler.getNameLink(target));
                target.setAtLoginFlag(AtLoginFlags.Rename);
            } else {
                // check offline security
                if (handler.hasLowerSecurity(null, player.getGUID())) {
                    return false;
                }

                handler.sendSysMessage(SysMessage.RenamePlayerGuid, handler.playerLink(player.getName()), player.getGUID().toString());

                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ADD_AT_LOGIN_FLAG);
                stmt.AddValue(0, (short) AtLoginFlags.Rename.getValue());
                stmt.AddValue(1, player.getGUID().getCounter());
                DB.characters.execute(stmt);
            }
        }

        return true;
    }

    
    private static boolean handleCharacterLevelCommand(CommandHandler handler, PlayerIdentifier player, short newlevel) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var oldlevel = player.isConnected() ? player.getConnectedPlayer().getLevel() : global.getCharacterCacheStorage().getCharacterLevelByGuid(player.getGUID());

        if (newlevel < 1) {
            newlevel = 1;
        }

        if (newlevel > SharedConst.StrongMaxLevel) {
            newlevel = SharedConst.StrongMaxLevel;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            target.giveLevel((int) newlevel);
            target.initTalentForLevel();
            target.setXP(0);

            if (handler.needReportToTarget(target)) {
                if (oldlevel == newlevel) {
                    target.sendSysMessage(SysMessage.YoursLevelProgressReset, handler.getNameLink());
                } else if (oldlevel < newlevel) {
                    target.sendSysMessage(SysMessage.YoursLevelUp, handler.getNameLink(), newlevel);
                } else // if (oldlevel > newlevel)
                {
                    target.sendSysMessage(SysMessage.YoursLevelDown, handler.getNameLink(), newlevel);
                }
            }
        } else {
            // Update level and reset XP, everything else will be updated at login
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_LEVEL);
            stmt.AddValue(0, (byte) newlevel);
            stmt.AddValue(1, player.getGUID().getCounter());
            DB.characters.execute(stmt);
        }

        if (!handler.getSession() || (handler.getSession().getPlayer() != player.getConnectedPlayer())) // including chr == NULL
        {
            handler.sendSysMessage(SysMessage.YouChangeLvl, handler.playerLink(player.getName()), newlevel);
        }

        return true;
    }

    
    private static boolean handleCharacterCustomizeCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            return false;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            handler.sendSysMessage(SysMessage.CustomizePlayer, handler.getNameLink(target));
            target.setAtLoginFlag(AtLoginFlags.Customize);
        } else {
            handler.sendSysMessage(SysMessage.CustomizePlayerGuid, handler.playerLink(player.getName()), player.getGUID().getCounter());
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ADD_AT_LOGIN_FLAG);
            stmt.AddValue(0, (short) AtLoginFlags.Customize.getValue());
            stmt.AddValue(1, player.getGUID().getCounter());
            DB.characters.execute(stmt);
        }

        return true;
    }

    
    private static boolean handleCharacterChangeAccountCommand(CommandHandler handler, PlayerIdentifier player, AccountIdentifier newAccount) {
        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            return false;
        }

        var characterInfo = global.getCharacterCacheStorage().getCharacterCacheByGuid(player.getGUID());

        if (characterInfo == null) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var oldAccountId = characterInfo.accountId;

        // nothing to do :)
        if (newAccount.getID() == oldAccountId) {
            return true;
        }

        var charCount = global.getAccountMgr().getCharactersCount(newAccount.getID());

        if (charCount != 0) {
            if (charCount >= WorldConfig.getIntValue(WorldCfg.CharactersPerRealm)) {
                handler.sendSysMessage(SysMessage.AccountCharacterListFull, newAccount.getName(), newAccount.getID());

                return false;
            }
        }

        var onlinePlayer = player.getConnectedPlayer();

        if (onlinePlayer != null) {
            onlinePlayer.getSession().kickPlayer("HandleCharacterChangeAccountCommand GM Command transferring character to another account");
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ACCOUNT_BY_GUID);
        stmt.AddValue(0, newAccount.getID());
        stmt.AddValue(1, player.getGUID().getCounter());
        DB.characters.DirectExecute(stmt);

        global.getWorldMgr().updateRealmCharCount(oldAccountId);
        global.getWorldMgr().updateRealmCharCount(newAccount.getID());

        global.getCharacterCacheStorage().updateCharacterAccountId(player.getGUID(), newAccount.getID());

        handler.sendSysMessage(SysMessage.ChangeAccountSuccess, player.getName(), newAccount.getName());

        var logString = String.format("changed ownership of player %1$s (%2$s) from account %3$s to account %4$s", player.getName(), player.getGUID(), oldAccountId, newAccount.getID());
        var session = handler.getSession();

        if (session != null) {
            var sessionPlayer = session.getPlayer();

            if (sessionPlayer != null) {
                Log.outCommand(session.getAccountId(), String.format("GM %1$s (Account: %2$s) %3$s", sessionPlayer.getName(), session.getAccountId(), logString));
            }
        } else {
            Log.outCommand(0, String.format("%1$s %2$s", handler.getSysMessage(SysMessage.Console), logString));
        }

        return true;
    }

    
    private static boolean handleCharacterChangeFactionCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            return false;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            handler.sendSysMessage(SysMessage.CustomizePlayer, handler.getNameLink(target));
            target.setAtLoginFlag(AtLoginFlags.ChangeFaction);
        } else {
            handler.sendSysMessage(SysMessage.CustomizePlayerGuid, handler.playerLink(player.getName()), player.getGUID().getCounter());
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ADD_AT_LOGIN_FLAG);
            stmt.AddValue(0, (short) AtLoginFlags.ChangeFaction.getValue());
            stmt.AddValue(1, player.getGUID().getCounter());
            DB.characters.execute(stmt);
        }

        return true;
    }

    
    private static boolean handleCharacterChangeRaceCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTarget(handler);
        }

        if (player == null) {
            return false;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            handler.sendSysMessage(SysMessage.CustomizePlayer, handler.getNameLink(target));
            target.setAtLoginFlag(AtLoginFlags.ChangeRace);
        } else {
            handler.sendSysMessage(SysMessage.CustomizePlayerGuid, handler.playerLink(player.getName()), player.getGUID().getCounter());
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ADD_AT_LOGIN_FLAG);
            stmt.AddValue(0, (short) AtLoginFlags.ChangeRace.getValue());
            stmt.AddValue(1, player.getGUID().getCounter());
            DB.characters.execute(stmt);
        }

        return true;
    }

    
    private static boolean handleCharacterReputationCommand(CommandHandler handler, PlayerIdentifier player) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null || !player.isConnected()) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        var target = player.getConnectedPlayer();
        var loc = handler.getSessionDbcLocale();

        var targetFSL = target.getReputationMgr().getStateList();

        for (var pair : targetFSL.entrySet()) {
            var faction = pair.getValue();
            var factionEntry = CliDB.FactionStorage.get(faction.id);
            var factionName = factionEntry != null ? factionEntry.name.charAt(loc) : "#Not found#";
            var rank = target.getReputationMgr().getRank(factionEntry);
            var rankName = handler.getSysMessage(ReputationMgr.ReputationRankStrIndex[rank.getValue()]);
            StringBuilder ss = new StringBuilder();

            if (handler.getSession() != null) {
                ss.append(String.format("%1$s - |cffffffff|Hfaction:%1$s|h[%2$s %3$s]|h|r", faction.id, factionName, loc));
            } else {
                ss.append(String.format("%1$s - %2$s %3$s", faction.id, factionName, loc));
            }

            ss.append(String.format(" %1$s (%2$s)", rankName, target.getReputationMgr().getReputation(factionEntry)));

            if (faction.flags.hasFlag(ReputationFlags.Visible)) {
                ss.append(handler.getSysMessage(SysMessage.FactionVisible));
            }

            if (faction.flags.hasFlag(ReputationFlags.AtWar)) {
                ss.append(handler.getSysMessage(SysMessage.FactionAtwar));
            }

            if (faction.flags.hasFlag(ReputationFlags.Peaceful)) {
                ss.append(handler.getSysMessage(SysMessage.FactionPeaceForced));
            }

            if (faction.flags.hasFlag(ReputationFlags.hidden)) {
                ss.append(handler.getSysMessage(SysMessage.FactionHidden));
            }

            if (faction.flags.hasFlag(ReputationFlags.header)) {
                ss.append(handler.getSysMessage(SysMessage.FactionInvisibleForced));
            }

            if (faction.flags.hasFlag(ReputationFlags.inactive)) {
                ss.append(handler.getSysMessage(SysMessage.FactionInactive));
            }

            handler.sendSysMessage(ss.toString());
        }

        return true;
    }

    
    private static boolean handleCharacterEraseCommand(CommandHandler handler, PlayerIdentifier player) {
        int accountId;

        var target = player == null ? null : player.getConnectedPlayer();

        if (target != null) {
            accountId = target.getSession().getAccountId();
            target.getSession().kickPlayer("HandleCharacterEraseCommand GM Command deleting character");
        } else {
            accountId = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(player.getGUID());
        }

        String accountName;
        tangible.OutObject<String> tempOut_accountName = new tangible.OutObject<String>();
        global.getAccountMgr().getName(accountId, tempOut_accountName);
        accountName = tempOut_accountName.outArgValue;

        player.deleteFromDB(player.getGUID(), accountId, true, true);
        handler.sendSysMessage(SysMessage.CharacterDeleted, player.getName(), player.getGUID().toString(), accountName, accountId);

        return true;
    }

    

    private static boolean handleLevelUpCommand(CommandHandler handler, PlayerIdentifier player, short level) {
        if (player == null) {
            player = PlayerIdentifier.fromTargetOrSelf(handler);
        }

        if (player == null) {
            return false;
        }

        var oldlevel = player.isConnected() ? player.getConnectedPlayer().getLevel() : global.getCharacterCacheStorage().getCharacterLevelByGuid(player.getGUID());
        var newlevel = oldlevel + level;

        if (newlevel < 1) {
            newlevel = 1;
        }

        if (newlevel > SharedConst.StrongMaxLevel) // hardcoded maximum level
        {
            newlevel = SharedConst.StrongMaxLevel;
        }

        var target = player.getConnectedPlayer();

        if (target != null) {
            target.giveLevel((int) newlevel);
            target.initTalentForLevel();
            target.setXP(0);

            if (handler.needReportToTarget(player.getConnectedPlayer())) {
                if (oldlevel == newlevel) {
                    player.getConnectedPlayer().sendSysMessage(SysMessage.YoursLevelProgressReset, handler.getNameLink());
                } else if (oldlevel < newlevel) {
                    player.getConnectedPlayer().sendSysMessage(SysMessage.YoursLevelUp, handler.getNameLink(), newlevel);
                } else // if (oldlevel > newlevel)
                {
                    player.getConnectedPlayer().sendSysMessage(SysMessage.YoursLevelDown, handler.getNameLink(), newlevel);
                }
            }
        } else {
            // Update level and reset XP, everything else will be updated at login
            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_LEVEL);
            stmt.AddValue(0, newlevel);
            stmt.AddValue(1, player.getGUID().getCounter());
            DB.characters.execute(stmt);
        }

        if (handler.getSession() == null || handler.getSession().getPlayer() != target) // including chr == NULL
        {
            handler.sendSysMessage(SysMessage.YouChangeLvl, handler.playerLink(player.getName()), newlevel);
        }

        return true;
    }

    
    private static class DeletedCommands {
        
        private static boolean handleCharacterDeletedDeleteCommand(CommandHandler handler, String needle) {
            ArrayList<DeletedInfo> foundList = new ArrayList<>();

            if (!getDeletedCharacterInfoList(foundList, needle)) {
                return false;
            }

            if (foundList.isEmpty()) {
                handler.sendSysMessage(SysMessage.CharacterDeletedListEmpty);

                return false;
            }

            handler.sendSysMessage(SysMessage.CharacterDeletedDelete);
            handleCharacterDeletedListHelper(foundList, handler);

            // Call the appropriate function to delete them (current account for deleted character is 0)
            for (var info : foundList) {
                player.deleteFromDB(info.guid, 0, false, true);
            }

            return true;
        }

        

        private static boolean handleCharacterDeletedListCommand(CommandHandler handler, String needle) {
            ArrayList<DeletedInfo> foundList = new ArrayList<>();

            if (!getDeletedCharacterInfoList(foundList, needle)) {
                return false;
            }

            // if no character have been found, output a warning
            if (foundList.isEmpty()) {
                handler.sendSysMessage(SysMessage.CharacterDeletedListEmpty);

                return false;
            }

            handleCharacterDeletedListHelper(foundList, handler);

            return true;
        }

        

        private static boolean handleCharacterDeletedRestoreCommand(CommandHandler handler, String needle, String newCharName, AccountIdentifier newAccount) {
            ArrayList<DeletedInfo> foundList = new ArrayList<>();

            if (!getDeletedCharacterInfoList(foundList, needle)) {
                return false;
            }

            if (foundList.isEmpty()) {
                handler.sendSysMessage(SysMessage.CharacterDeletedListEmpty);

                return false;
            }

            handler.sendSysMessage(SysMessage.CharacterDeletedRestore);
            handleCharacterDeletedListHelper(foundList, handler);

            if (newCharName.isEmpty()) {
                // Drop not existed account cases
                for (var info : foundList) {
                    handleCharacterDeletedRestoreHelper(info, handler);
                }

                return true;
            }

            if (foundList.size() == 1) {
                var delInfo = foundList.get(0);

                // update name
                delInfo.name = newCharName;

                // if new account provided update deleted info
                if (newAccount != null) {
                    delInfo.accountId = newAccount.getID();
                    delInfo.accountName = newAccount.getName();
                }

                handleCharacterDeletedRestoreHelper(delInfo, handler);

                return true;
            }

            handler.sendSysMessage(SysMessage.CharacterDeletedErrRename);

            return false;
        }

        
        private static boolean handleCharacterDeletedOldCommand(CommandHandler handler, Short days) {
            var keepDays = WorldConfig.getIntValue(WorldCfg.ChardeleteKeepDays);

            if (days != null) {
                keepDays = days.shortValue();
            } else if (keepDays <= 0) // config option value 0 -> disabled and can't be used
            {
                return false;
            }

            player.deleteOldCharacters(keepDays);

            return true;
        }

        private static boolean getDeletedCharacterInfoList(ArrayList<DeletedInfo> foundList, String searchString) {
            SQLResult result;
            PreparedStatement stmt;

            if (!searchString.isEmpty()) {
                // search by GUID
                if (searchString.IsNumber()) {
                    long guid;
                    tangible.OutObject<Long> tempOut_guid = new tangible.OutObject<Long>();
                    if (!tangible.TryParseHelper.tryParseLong(searchString, tempOut_guid)) {
                        guid = tempOut_guid.outArgValue;
                        return false;
                    } else {
                        guid = tempOut_guid.outArgValue;
                    }

                    stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_DEL_INFO_BY_GUID);
                    stmt.AddValue(0, guid);
                    result = DB.characters.query(stmt);
                }
                // search by name
                else {
                    tangible.RefObject<String> tempRef_searchString = new tangible.RefObject<String>(searchString);
                    if (!ObjectManager.normalizePlayerName(tempRef_searchString)) {
                        searchString = tempRef_searchString.refArgValue;
                        return false;
                    } else {
                        searchString = tempRef_searchString.refArgValue;
                    }

                    stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_DEL_INFO_BY_NAME);
                    stmt.AddValue(0, searchString);
                    result = DB.characters.query(stmt);
                }
            } else {
                stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_CHAR_DEL_INFO);
                result = DB.characters.query(stmt);
            }

            if (!result.isEmpty()) {
                do {
                    DeletedInfo info = new DeletedInfo();

                    info.guid = ObjectGuid.create(HighGuid.Player, result.<Long>Read(0));
                    info.name = result.<String>Read(1);
                    info.accountId = result.<Integer>Read(2);

                    // account name will be empty for not existed account
                    tangible.OutObject<String> tempOut_accountName = new tangible.OutObject<String>();
                    global.getAccountMgr().getName(info.accountId, tempOut_accountName);
                    info.accountName = tempOut_accountName.outArgValue;
                    info.deleteDate = result.<Long>Read(3);
                    foundList.add(info);
                } while (result.NextRow());
            }

            return true;
        }

        private static void handleCharacterDeletedListHelper(ArrayList<DeletedInfo> foundList, CommandHandler handler) {
            if (handler.getSession() == null) {
                handler.sendSysMessage(SysMessage.CharacterDeletedListBar);
                handler.sendSysMessage(SysMessage.CharacterDeletedListHeader);
                handler.sendSysMessage(SysMessage.CharacterDeletedListBar);
            }

            for (var info : foundList) {
                var dateStr = time.UnixTimeToDateTime(info.deleteDate).ToShortDateString();

                if (!handler.getSession()) {
                    handler.sendSysMessage(SysMessage.CharacterDeletedListLineConsole, info.guid.toString(), info.name, info.accountName.isEmpty() ? "<Not existed>" : info.accountName, info.accountId, dateStr);
                } else {
                    handler.sendSysMessage(SysMessage.CharacterDeletedListLineChat, info.guid.toString(), info.name, info.accountName.isEmpty() ? "<Not existed>" : info.accountName, info.accountId, dateStr);
                }
            }

            if (!handler.getSession()) {
                handler.sendSysMessage(SysMessage.CharacterDeletedListBar);
            }
        }

        private static void handleCharacterDeletedRestoreHelper(DeletedInfo delInfo, CommandHandler handler) {
            if (delInfo.accountName.isEmpty()) // account not exist
            {
                handler.sendSysMessage(SysMessage.CharacterDeletedSkipAccount, delInfo.name, delInfo.guid.toString(), delInfo.accountId);

                return;
            }

            // check character count
            var charcount = global.getAccountMgr().getCharactersCount(delInfo.accountId);

            if (charcount >= WorldConfig.getIntValue(WorldCfg.CharactersPerRealm)) {
                handler.sendSysMessage(SysMessage.CharacterDeletedSkipFull, delInfo.name, delInfo.guid.toString(), delInfo.accountId);

                return;
            }

            if (!global.getCharacterCacheStorage().getCharacterGuidByName(delInfo.name).isEmpty()) {
                handler.sendSysMessage(SysMessage.CharacterDeletedSkipName, delInfo.name, delInfo.guid.toString(), delInfo.accountId);

                return;
            }

            var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_RESTORE_DELETE_INFO);
            stmt.AddValue(0, delInfo.name);
            stmt.AddValue(1, delInfo.accountId);
            stmt.AddValue(2, delInfo.guid.getCounter());
            DB.characters.execute(stmt);

            global.getCharacterCacheStorage().updateCharacterInfoDeleted(delInfo.guid, false, delInfo.name);
        }

        private final static class DeletedInfo {
            public ObjectGuid guid = ObjectGuid.EMPTY; // the GUID from the character
            public String name; // the character name
            public int accountId; // the account id
            public String accountName; // the account name
            public long deleteDate; // the date at which the character has been deleted

            public DeletedInfo clone() {
                DeletedInfo varCopy = new DeletedInfo();

                varCopy.guid = this.guid;
                varCopy.name = this.name;
                varCopy.accountId = this.accountId;
                varCopy.accountName = this.accountName;
                varCopy.deleteDate = this.deleteDate;

                return varCopy;
            }
        }
    }
}
