package com.github.azeroth.game.chat;


import game.*;

class AccountIdentifier {
    private int id;
    private String name;
    private WorldSession session;

    public AccountIdentifier() {
    }

    public AccountIdentifier(WorldSession session) {
        id = session.getAccountId();
        name = session.getAccountName();
        session = session;
    }

    public static AccountIdentifier fromTarget(CommandHandler handler) {
        var player = handler.getPlayer();

        if (player != null) {
            var target = player.getSelectedPlayer();

            if (target != null) {
                var session = target.getSession();

                if (session != null) {
                    return new AccountIdentifier(session);
                }
            }
        }

        return null;
    }

    public final int getID() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final boolean isConnected() {
        return session != null;
    }

    public final WorldSession getConnectedSession() {
        return session;
    }

    public final ChatCommandResult tryConsume(CommandHandler handler, String args) {
        dynamic text;
        tangible.OutObject<dynamic> tempOut_text = new tangible.OutObject<dynamic>();

        var next = CommandArgs.tryConsume(tempOut_text, String.class, handler, args);
        text = tempOut_text.outArgValue;

        if (!next.isSuccessful()) {
            return next;
        }

        // first try parsing as account name
        name = text;
        id = global.getAccountMgr().getId(name);
        session = global.getWorldMgr().findSession(id);

        if (id != 0) // account with name exists, we are done
        {
            return next;
        }

        // try parsing as account id instead
        int id;
        tangible.OutObject<Integer> tempOut_id = new tangible.OutObject<Integer>();
        if (tangible.TryParseHelper.tryParseInt(text, tempOut_id)) {
            id = tempOut_id.outArgValue;
            return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserAccountNameNoExist, name));
        } else {
            id = tempOut_id.outArgValue;
        }

        id = id;
        session = global.getWorldMgr().findSession(id);

        tangible.OutObject<String> tempOut__name = new tangible.OutObject<String>();
        if (global.getAccountMgr().getName(id, tempOut__name)) {
            name = tempOut__name.outArgValue;
            return next;
        } else {
            name = tempOut__name.outArgValue;
            return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserAccountIdNoExist, id));
        }
    }
}
