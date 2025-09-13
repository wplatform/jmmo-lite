package com.github.azeroth.game.chat;


import com.github.azeroth.game.networking.packet.PrintNotification;
import game.ServerMessageType;

class MessageCommands {
    
    private static boolean handleNameAnnounceCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        var name = "Console";
        var session = handler.getSession();

        if (session) {
            name = session.getPlayer().getName();
        }

        global.getWorldMgr().sendWorldText(SysMessage.AnnounceColor, name, message);

        return true;
    }

    
    private static boolean handleGMNameAnnounceCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        var name = "Console";
        var session = handler.getSession();

        if (session) {
            name = session.getPlayer().getName();
        }

        global.getWorldMgr().sendGMText(SysMessage.AnnounceColor, name, message);

        return true;
    }

    
    private static boolean handleAnnounceCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        global.getWorldMgr().sendServerMessage(ServerMessageType.String, handler.getParsedString(SysMessage.Systemmessage, message));

        return true;
    }

    
    private static boolean handleGMAnnounceCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        global.getWorldMgr().sendGMText(SysMessage.GmBroadcast, message);

        return true;
    }

    
    private static boolean handleNotifyCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        var str = handler.getSysMessage(SysMessage.GlobalNotify);
        str += message;

        global.getWorldMgr().sendGlobalMessage(new PrintNotification(str));

        return true;
    }

    
    private static boolean handleGMNotifyCommand(CommandHandler handler, Tail message) {
        if (message.isEmpty()) {
            return false;
        }

        var str = handler.getSysMessage(SysMessage.GmNotify);
        str += message;

        global.getWorldMgr().sendGlobalGMMessage(new PrintNotification(str));

        return true;
    }

    

    private static boolean handleWhispersCommand(CommandHandler handler, Boolean operationArg, String playerNameArg) {
        if (operationArg == null) {
            handler.sendSysMessage(SysMessage.CommandWhisperaccepting, handler.getSession().getPlayer().isAcceptWhispers() ? handler.getSysMessage(SysMessage.on) : handler.getSysMessage(SysMessage.Off));

            return true;
        }

        if (operationArg != null) {
            handler.getSession().getPlayer().setAcceptWhispers(true);
            handler.sendSysMessage(SysMessage.CommandWhisperon);

            return true;
        } else {
            // Remove all players from the Gamemaster's whisper whitelist
            handler.getSession().getPlayer().clearWhisperWhiteList();

            handler.getSession().getPlayer().setAcceptWhispers(false);
            handler.sendSysMessage(SysMessage.CommandWhisperoff);

            return true;
        }

        //todo fix me
		/*if (operationArg->holds_alternative < EXACT_SEQUENCE("remove") > ())
		{
			if (!playerNameArg)
				return false;

			if (normalizePlayerName(*playerNameArg))
			{
				if (Player * player = ObjectAccessor::FindPlayerByName(*playerNameArg))
				{
					handler->GetSession()->GetPlayer()->RemoveFromWhisperWhiteList(player->GetGUID());
					handler->PSendSysMessage(LANG_COMMAND_WHISPEROFFPLAYER, playerNameArg->c_str());
					return true;
				}
				else
				{
					handler->PSendSysMessage(LANG_PLAYER_NOT_FOUND, playerNameArg->c_str());
					handler->SetSentErrorMessage(true);
					return false;
				}
			}
		}
		handler.sendSysMessage(SysMessage.UseBol);
		return false;*/
    }
}
