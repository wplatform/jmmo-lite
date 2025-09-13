package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;

import java.time.LocalDateTime;



class BanListCommands {


    private static boolean handleBanListAccountCommand(CommandHandler handler, String filter) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.DelExpiredIpBans);
        DB.Login.execute(stmt);

        SQLResult result;

        if (filter.isEmpty()) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_BANNED_ALL);
            result = DB.Login.query(stmt);
        } else {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_ACCOUNT_BANNED_BY_FILTER);
            stmt.AddValue(0, filter);
            result = DB.Login.query(stmt);
        }

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.BanlistNoaccount);

            return true;
        }

        return handleBanListHelper(result, handler);
    }


    private static boolean handleBanListCharacterCommand(CommandHandler handler, String filter) {
        if (filter.isEmpty()) {
            return false;
        }

        var stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_GUID_BY_NAME_FILTER);
        stmt.AddValue(0, filter);
        var result = DB.characters.query(stmt);

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.BanlistNocharacter);

            return true;
        }

        handler.sendSysMessage(SysMessage.BanlistMatchingcharacter);

        // Chat short output
        if (handler.getSession()) {
            do {
                var stmt2 = DB.characters.GetPreparedStatement(CharStatements.SEL_BANNED_NAME);
                stmt2.AddValue(0, result.<Long>Read(0));
                var banResult = DB.characters.query(stmt2);

                if (!banResult.isEmpty()) {
                    handler.sendSysMessage(banResult.<String>Read(0));
                }
            } while (result.NextRow());
        }
        // Console wide output
        else {
            handler.sendSysMessage(SysMessage.BanlistCharacters);
            handler.sendSysMessage(" =============================================================================== ");
            handler.sendSysMessage(SysMessage.BanlistCharactersHeader);

            do {
                handler.sendSysMessage("-------------------------------------------------------------------------------");

                var char_name = result.<String>Read(1);

                var stmt2 = DB.characters.GetPreparedStatement(CharStatements.SEL_BANINFO_LIST);
                stmt2.AddValue(0, result.<Long>Read(0));
                var banInfo = DB.characters.query(stmt2);

                if (!banInfo.isEmpty()) {
                    do {
                        var timeBan = banInfo.<Long>Read(0);
                        var tmBan = time.UnixTimeToDateTime(timeBan);
                        var bannedby = banInfo.<String>Read(2).substring(0, 15);
                        var banreason = banInfo.<String>Read(3).substring(0, 15);

                        if (banInfo.<Long>Read(0) == banInfo.<Long>Read(1)) {
                            handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|   permanent  |{6}|{7}|", char_name, tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), bannedby, banreason);
                        } else {
                            var timeUnban = banInfo.<Long>Read(1);
                            var tmUnban = time.UnixTimeToDateTime(timeUnban);

                            handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|{6:D2}-{7:D2}-{8:D2} {9:D2}:{10:D2}|{11}|{12}|", char_name, tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), tmUnban.getYear() % 100, tmUnban.getMonthValue() + 1, tmUnban.getDayOfMonth(), tmUnban.getHour(), tmUnban.getMinute(), bannedby, banreason);
                        }
                    } while (banInfo.NextRow());
                }
            } while (result.NextRow());

            handler.sendSysMessage(" =============================================================================== ");
        }

        return true;
    }



    private static boolean handleBanListIPCommand(CommandHandler handler, String filter) {
        var stmt = DB.Login.GetPreparedStatement(LoginStatements.DelExpiredIpBans);
        DB.Login.execute(stmt);

        SQLResult result;

        if (filter.isEmpty()) {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_IP_BANNED_ALL);
            result = DB.Login.query(stmt);
        } else {
            stmt = DB.Login.GetPreparedStatement(LoginStatements.SEL_IP_BANNED_BY_IP);
            stmt.AddValue(0, filter);
            result = DB.Login.query(stmt);
        }

        if (result.isEmpty()) {
            handler.sendSysMessage(SysMessage.BanlistNoip);

            return true;
        }

        handler.sendSysMessage(SysMessage.BanlistMatchingip);

        // Chat short output
        if (handler.getSession()) {
            do {
                handler.sendSysMessage("{0}", result.<String>Read(0));
            } while (result.NextRow());
        }
        // Console wide output
        else {
            handler.sendSysMessage(SysMessage.BanlistIps);
            handler.sendSysMessage(" ===============================================================================");
            handler.sendSysMessage(SysMessage.BanlistIpsHeader);

            do {
                handler.sendSysMessage("-------------------------------------------------------------------------------");

                long timeBan = result.<Integer>Read(1);
                var tmBan = time.UnixTimeToDateTime(timeBan);
                var bannedby = result.<String>Read(3).substring(0, 15);
                var banreason = result.<String>Read(4).substring(0, 15);

                if (result.<Integer>Read(1) == result.<Integer>Read(2)) {
                    handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|   permanent  |{6}|{7}|", result.<String>Read(0), tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), bannedby, banreason);
                } else {
                    long timeUnban = result.<Integer>Read(2);
                    LocalDateTime tmUnban = LocalDateTime.MIN;
                    tmUnban = time.UnixTimeToDateTime(timeUnban);

                    handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|{6:D2}-{7:D2}-{8:D2} {9:D2}:{10:D2}|{11}|{12}|", result.<String>Read(0), tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), tmUnban.getYear() % 100, tmUnban.getMonthValue() + 1, tmUnban.getDayOfMonth(), tmUnban.getHour(), tmUnban.getMinute(), bannedby, banreason);
                }
            } while (result.NextRow());

            handler.sendSysMessage(" ===============================================================================");
        }

        return true;
    }

    private static boolean handleBanListHelper(SQLResult result, CommandHandler handler) {
        handler.sendSysMessage(SysMessage.BanlistMatchingaccount);

        // Chat short output
        if (handler.getSession()) {
            do {
                var accountid = result.<Integer>Read(0);

                var banResult = DB.Login.query("SELECT account.username FROM account, account_banned WHERE account_banned.id='{0}' AND account_banned.id=account.id", accountid);

                if (!banResult.isEmpty()) {
                    handler.sendSysMessage(banResult.<String>Read(0));
                }
            } while (result.NextRow());
        }
        // Console wide output
        else {
            handler.sendSysMessage(SysMessage.BanlistAccounts);
            handler.sendSysMessage(" ===============================================================================");
            handler.sendSysMessage(SysMessage.BanlistAccountsHeader);

            do {
                handler.sendSysMessage("-------------------------------------------------------------------------------");

                var accountId = result.<Integer>Read(0);

                String accountName;

                // "account" case, name can be get in same query
                if (result.GetFieldCount() > 1) {
                    accountName = result.<String>Read(1);
                }
                // "character" case, name need extract from another DB
                else {
                    tangible.OutObject<String> tempOut_accountName = new tangible.OutObject<String>();
                    global.getAccountMgr().getName(accountId, tempOut_accountName);
                    accountName = tempOut_accountName.outArgValue;
                }

                // No SQL injection. id is uint32.
                var banInfo = DB.Login.query("SELECT bandate, unbandate, bannedby, banreason FROM account_banned WHERE id = {0} ORDER BY unbandate", accountId);

                if (!banInfo.isEmpty()) {
                    do {
                        long timeBan = banInfo.<Integer>Read(0);
                        LocalDateTime tmBan = LocalDateTime.MIN;
                        tmBan = time.UnixTimeToDateTime(timeBan);
                        var bannedby = banInfo.<String>Read(2).substring(0, 15);
                        var banreason = banInfo.<String>Read(3).substring(0, 15);

                        if (banInfo.<Integer>Read(0) == banInfo.<Integer>Read(1)) {
                            handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|   permanent  |{6}|{7}|", accountName.substring(0, 15), tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), bannedby, banreason);
                        } else {
                            long timeUnban = banInfo.<Integer>Read(1);
                            LocalDateTime tmUnban = LocalDateTime.MIN;
                            tmUnban = time.UnixTimeToDateTime(timeUnban);

                            handler.sendSysMessage("|{0}|{1:D2}-{2:D2}-{3:D2} {4:D2}:{5:D2}|{6:D2}-{7:D2}-{8:D2} {9:D2}:{10:D2}|{11}|{12}|", accountName.substring(0, 15), tmBan.getYear() % 100, tmBan.getMonthValue() + 1, tmBan.getDayOfMonth(), tmBan.getHour(), tmBan.getMinute(), tmUnban.getYear() % 100, tmUnban.getMonthValue() + 1, tmUnban.getDayOfMonth(), tmUnban.getHour(), tmUnban.getMinute(), bannedby, banreason);
                        }
                    } while (banInfo.NextRow());
                }
            } while (result.NextRow());

            handler.sendSysMessage(" ===============================================================================");
        }

        return true;
    }
}
