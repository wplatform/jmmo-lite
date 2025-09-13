package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;
import game.*;

class BanCommands {
    
    private static boolean handleBanAccountCommand(CommandHandler handler, String playerName, int duration, String reason) {
        return handleBanHelper(BanMode.Account, playerName, duration, reason, handler);
    }

    
    private static boolean handleBanCharacterCommand(CommandHandler handler, String playerName, int duration, String reason) {
        if (playerName.isEmpty()) {
            return false;
        }

        if (duration == 0) {
            return false;
        }

        if (reason.isEmpty()) {
            return false;
        }

        tangible.RefObject<String> tempRef_playerName = new tangible.RefObject<String>(playerName);
        if (!ObjectManager.normalizePlayerName(tempRef_playerName)) {
            playerName = tempRef_playerName.refArgValue;
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        } else {
            playerName = tempRef_playerName.refArgValue;
        }

        var author = handler.getSession() != null ? handler.getSession().getPlayerName() : "Server";

        switch (global.getWorldMgr().banCharacter(playerName, duration, reason, author)) {
            case Success: {
                if (duration > 0) {
                    if (WorldConfig.getBoolValue(WorldCfg.ShowBanInWorld)) {
                        global.getWorldMgr().sendWorldText(SysMessage.BanCharacterYoubannedmessageWorld, author, playerName, time.secsToTimeString(duration, TimeFormat.ShortText, false), reason);
                    } else {
                        handler.sendSysMessage(SysMessage.BanYoubanned, playerName, time.secsToTimeString(duration, TimeFormat.ShortText, false), reason);
                    }
                } else {
                    if (WorldConfig.getBoolValue(WorldCfg.ShowBanInWorld)) {
                        global.getWorldMgr().sendWorldText(SysMessage.BanCharacterYoupermbannedmessageWorld, author, playerName, reason);
                    } else {
                        handler.sendSysMessage(SysMessage.BanYoupermbanned, playerName, reason);
                    }
                }

                break;
            }
            case Notfound: {
                handler.sendSysMessage(SysMessage.BanNotfound, "character", playerName);

                return false;
            }
            default:
                break;
        }

        return true;
    }

    
    private static boolean handleBanAccountByCharCommand(CommandHandler handler, String playerName, int duration, String reason) {
        return handleBanHelper(BanMode.Character, playerName, duration, reason, handler);
    }

    
    private static boolean handleBanIPCommand(CommandHandler handler, String ipAddress, int duration, String reason) {
        return handleBanHelper(BanMode.IP, ipAddress, duration, reason, handler);
    }


    private static boolean handleBanHelper(BanMode mode, String nameOrIP, int duration, String reason, CommandHandler handler) {
        if (nameOrIP.isEmpty()) {
            return false;
        }

        if (reason.isEmpty()) {
            return false;
        }

        switch (mode) {
            case Character:
                tangible.RefObject<String> tempRef_nameOrIP = new tangible.RefObject<String>(nameOrIP);
                if (!ObjectManager.normalizePlayerName(tempRef_nameOrIP)) {
                    nameOrIP = tempRef_nameOrIP.refArgValue;
                    handler.sendSysMessage(SysMessage.PlayerNotFound);

                    return false;
                } else {
                    nameOrIP = tempRef_nameOrIP.refArgValue;
                }

                break;
            case IP:
                tangible.OutObject<system.Net.IPAddress> tempOut__ = new tangible.OutObject<system.Net.IPAddress>();
                if (!IPAddress.tryParse(nameOrIP, tempOut__)) {
                    _ = tempOut__.outArgValue;
                    return false;
                } else {
                    _ = tempOut__.outArgValue;
                }

                break;
        }

        var author = handler.getSession() ? handler.getSession().getPlayerName() : "Server";

        switch (global.getWorldMgr().banAccount(mode, nameOrIP, duration, reason, author)) {
            case Success:
                if (duration > 0) {
                    if (WorldConfig.getBoolValue(WorldCfg.ShowBanInWorld)) {
                        global.getWorldMgr().sendWorldText(SysMessage.BanAccountYoubannedmessageWorld, author, nameOrIP, time.secsToTimeString(duration, 0, false), reason);
                    } else {
                        handler.sendSysMessage(SysMessage.BanYoubanned, nameOrIP, time.secsToTimeString(duration, TimeFormat.ShortText, false), reason);
                    }
                } else {
                    if (WorldConfig.getBoolValue(WorldCfg.ShowBanInWorld)) {
                        global.getWorldMgr().sendWorldText(SysMessage.BanAccountYoupermbannedmessageWorld, author, nameOrIP, reason);
                    } else {
                        handler.sendSysMessage(SysMessage.BanYoupermbanned, nameOrIP, reason);
                    }
                }

                break;
            case SyntaxError:
                return false;
            case Notfound:
                switch (mode) {
                    default:
                        handler.sendSysMessage(SysMessage.BanNotfound, "account", nameOrIP);

                        break;
                    case Character:
                        handler.sendSysMessage(SysMessage.BanNotfound, "character", nameOrIP);

                        break;
                    case IP:
                        handler.sendSysMessage(SysMessage.BanNotfound, "ip", nameOrIP);

                        break;
                }

                return false;
            case Exists:
                handler.sendSysMessage(SysMessage.BanExists);

                break;
        }

        return true;
    }
}
