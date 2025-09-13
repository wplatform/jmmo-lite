package com.github.azeroth.game.chat;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import game.ObjectManager;

class PlayerIdentifier {
    private String name;
    private ObjectGuid guid = ObjectGuid.EMPTY;
    private Player player;

    public PlayerIdentifier() {
    }

    public PlayerIdentifier(Player player) {
        name = player.getName();
        guid = player.getGUID();
        player = player;
    }

    public static PlayerIdentifier fromTarget(CommandHandler handler) {
        var player = handler.getPlayer();

        if (player != null) {
            var target = player.getSelectedPlayer();

            if (target != null) {
                return new PlayerIdentifier(target);
            }
        }

        return null;
    }

    public static PlayerIdentifier fromSelf(CommandHandler handler) {
        var player = handler.getPlayer();

        if (player != null) {
            return new PlayerIdentifier(player);
        }

        return null;
    }

    public static PlayerIdentifier fromTargetOrSelf(CommandHandler handler) {
        var fromTarget = fromTarget(handler);

        if (fromTarget != null) {
            return fromTarget;
        } else {
            return fromSelf(handler);
        }
    }

    public final String getName() {
        return name;
    }

    public final ObjectGuid getGUID() {
        return guid;
    }

    public final boolean isConnected() {
        return player != null;
    }

    public final Player getConnectedPlayer() {
        return player;
    }

    public final ChatCommandResult tryConsume(CommandHandler handler, String args) {
        dynamic tempVal;
        tangible.OutObject<dynamic> tempOut_tempVal = new tangible.OutObject<dynamic>();

        var next = CommandArgs.tryConsume(tempOut_tempVal, Long.class, handler, args);
        tempVal = tempOut_tempVal.outArgValue;

        if (!next.isSuccessful()) {
            tangible.OutObject<dynamic> tempOut_tempVal2 = new tangible.OutObject<dynamic>();
            next = CommandArgs.tryConsume(tempOut_tempVal2, String.class, handler, args);
            tempVal = tempOut_tempVal2.outArgValue;
        }

        if (!next.isSuccessful()) {
            return next;
        }

        if (tempVal instanceof Long) {
            guid = ObjectGuid.create(HighGuid.Player, tempVal);

            if ((player = global.getObjAccessor().FindPlayerByLowGUID(guid.getCounter())) != null) {
                name = player.getName();
            } else {
                tangible.OutObject<String> tempOut__name = new tangible.OutObject<String>();
                if (!global.getCharacterCacheStorage().getCharacterNameByGuid(guid, tempOut__name)) {
                    name = tempOut__name.outArgValue;
                    return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserCharGuidNoExist, guid.toString()));
                } else {
                    name = tempOut__name.outArgValue;
                }
            }

            return next;
        } else {
            name = tempVal;

            tangible.RefObject<String> tempRef__name = new tangible.RefObject<String>(name);
            if (!ObjectManager.normalizePlayerName(tempRef__name)) {
                name = tempRef__name.refArgValue;
                return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserCharNameInvalid, name));
            } else {
                name = tempRef__name.refArgValue;
            }

            if ((player = global.getObjAccessor().FindPlayerByName(name)) != null) {
                guid = player.getGUID();
            } else if ((guid = global.getCharacterCacheStorage().getCharacterGuidByName(name)).IsEmpty) {
                return ChatCommandResult.fromErrorMessage(handler.getParsedString(SysMessage.CmdparserCharNameNoExist, name));
            }

            return next;
        }
    }
}
