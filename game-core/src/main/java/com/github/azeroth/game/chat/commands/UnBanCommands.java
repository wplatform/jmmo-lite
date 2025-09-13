package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.CommandHandler;
import game.*;


class UnBanCommands {
    
    private static boolean handleUnBanAccountCommand(CommandHandler handler, String name) {
        return handleUnBanHelper(BanMode.Account, name, handler);
    }

    
    private static boolean handleUnBanCharacterCommand(CommandHandler handler, String name) {
        tangible.RefObject<String> tempRef_name = new tangible.RefObject<String>(name);
        if (!ObjectManager.normalizePlayerName(tempRef_name)) {
            name = tempRef_name.refArgValue;
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        } else {
            name = tempRef_name.refArgValue;
        }

        if (!global.getWorldMgr().removeBanCharacter(name)) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        handler.sendSysMessage(SysMessage.UnbanUnbanned, name);

        return true;
    }

    
    private static boolean handleUnBanAccountByCharCommand(CommandHandler handler, String name) {
        return handleUnBanHelper(BanMode.Character, name, handler);
    }

    
    private static boolean handleUnBanIPCommand(CommandHandler handler, String ip) {
        return handleUnBanHelper(BanMode.IP, ip, handler);
    }

    private static boolean handleUnBanHelper(BanMode mode, String nameOrIp, CommandHandler handler) {
        if (nameOrIp.isEmpty()) {
            return false;
        }

        switch (mode) {
            case Character:
                tangible.RefObject<String> tempRef_nameOrIp = new tangible.RefObject<String>(nameOrIp);
                if (!ObjectManager.normalizePlayerName(tempRef_nameOrIp)) {
                    nameOrIp = tempRef_nameOrIp.refArgValue;
                    handler.sendSysMessage(SysMessage.PlayerNotFound);

                    return false;
                } else {
                    nameOrIp = tempRef_nameOrIp.refArgValue;
                }

                break;
            case IP:
                tangible.OutObject<system.Net.IPAddress> tempOut__ = new tangible.OutObject<system.Net.IPAddress>();
                if (!IPAddress.tryParse(nameOrIp, tempOut__)) {
                    _ = tempOut__.outArgValue;
                    return false;
                } else {
                    _ = tempOut__.outArgValue;
                }

                break;
        }

        if (global.getWorldMgr().removeBanAccount(mode, nameOrIp)) {
            handler.sendSysMessage(SysMessage.UnbanUnbanned, nameOrIp);
        } else {
            handler.sendSysMessage(SysMessage.UnbanError, nameOrIp);
        }

        return true;
    }
}
