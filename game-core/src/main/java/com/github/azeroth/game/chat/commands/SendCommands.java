package com.github.azeroth.game.chat.commands;


import com.github.azeroth.game.chat.PlayerIdentifier;
import com.github.azeroth.game.chat.QuotedString;
import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailReceiver;
import com.github.azeroth.game.mail.MailSender;

import java.util.ArrayList;
import java.util.Map;



class SendCommands {
    
    private static boolean handleSendMailCommand(CommandHandler handler, PlayerIdentifier playerIdentifier, QuotedString subject, QuotedString text) {
        // format: name "subject text" "mail text"
        if (playerIdentifier == null) {
            playerIdentifier = PlayerIdentifier.fromTarget(handler);
        }

        if (playerIdentifier == null) {
            return false;
        }

        if (subject.isEmpty() || text.isEmpty()) {
            return false;
        }

        // from console show not existed sender
        MailSender sender = new MailSender(MailMessageType.NORMAL, handler.getSession() ? handler.getSession().getPlayer().getGUID().getCounter() : 0, MailStationery.gm);

        // @todo Fix poor design
        SQLTransaction trans = new SQLTransaction();

        (new MailDraft(subject, text)).sendMailTo(trans, new MailReceiver(playerIdentifier.getGUID().getCounter()), sender);

        DB.characters.CommitTransaction(trans);

        var nameLink = handler.playerLink(playerIdentifier.getName());
        handler.sendSysMessage(SysMessage.MailSent, nameLink);

        return true;
    }

    
    private static boolean handleSendItemsCommand(CommandHandler handler, PlayerIdentifier playerIdentifier, QuotedString subject, QuotedString text, String itemsStr) {
        // format: name "subject text" "mail text" item1[:count1] item2[:count2] ... item12[:count12]
        if (playerIdentifier == null) {
            playerIdentifier = PlayerIdentifier.fromTarget(handler);
        }

        if (playerIdentifier == null) {
            return false;
        }

        if (subject.isEmpty() || text.isEmpty()) {
            return false;
        }

        // extract items
        ArrayList<Map.entry<Integer, Integer>> items = new ArrayList<Map.entry<Integer, Integer>>();

        var tokens = new LocalizedString();

        for (var i = 0; i < tokens.length; ++i) {
            // parse item str
            var itemIdAndCountStr = tokens.get(i).split("[:]", -1);

            int itemId;
            tangible.OutObject<Integer> tempOut_itemId = new tangible.OutObject<Integer>();
            if (!tangible.TryParseHelper.tryParseInt(itemIdAndCountStr[0], tempOut_itemId) || itemId == 0) {
                itemId = tempOut_itemId.outArgValue;
                return false;
            } else {
                itemId = tempOut_itemId.outArgValue;
            }

            var itemProto = global.getObjectMgr().getItemTemplate(itemId);

            if (itemProto == null) {
                handler.sendSysMessage(SysMessage.CommandItemidinvalid, itemId);

                return false;
            }

            int itemCount;
            tangible.OutObject<Integer> tempOut_itemCount = new tangible.OutObject<Integer>();
            if (itemIdAndCountStr[1].isEmpty() || !tangible.TryParseHelper.tryParseInt(itemIdAndCountStr[1], tempOut_itemCount)) {
                itemCount = tempOut_itemCount.outArgValue;
                itemCount = 1;
            } else {
                itemCount = tempOut_itemCount.outArgValue;
            }

            if (itemCount < 1 || (itemProto.getMaxCount() > 0 && itemCount > itemProto.getMaxCount())) {
                handler.sendSysMessage(SysMessage.CommandInvalidItemCount, itemCount, itemId);

                return false;
            }

            while (itemCount > itemProto.getMaxStackSize()) {
                items.add(new KeyValuePair<Integer, Integer>(itemId, itemProto.getMaxStackSize()));
                itemCount -= itemProto.getMaxStackSize();
            }

            items.add(new KeyValuePair<Integer, Integer>(itemId, itemCount));

            if (items.size() > SharedConst.MaxMailItems) {
                handler.sendSysMessage(SysMessage.CommandMailItemsLimit, SharedConst.MaxMailItems);

                return false;
            }
        }

        // from console show not existed sender
        MailSender sender = new MailSender(MailMessageType.NORMAL, handler.getSession() ? handler.getSession().getPlayer().getGUID().getCounter() : 0, MailStationery.gm);

        // fill mail
        MailDraft draft = new MailDraft(subject, text);

        SQLTransaction trans = new SQLTransaction();

        for (var pair : items) {
            var item = item.createItem(pair.getKey(), pair.getValue(), itemContext.NONE, handler.getSession() ? handler.getSession().getPlayer() : null);

            if (item) {
                item.saveToDB(trans); // save for prevent lost at next mail load, if send fail then item will deleted
                draft.addItem(item);
            }
        }

        draft.sendMailTo(trans, new MailReceiver(playerIdentifier.getGUID().getCounter()), sender);
        DB.characters.CommitTransaction(trans);

        var nameLink = handler.playerLink(playerIdentifier.getName());
        handler.sendSysMessage(SysMessage.MailSent, nameLink);

        return true;
    }

    
    private static boolean handleSendMoneyCommand(CommandHandler handler, PlayerIdentifier playerIdentifier, QuotedString subject, QuotedString text, long money) {
        // format: name "subject text" "mail text" money
        if (playerIdentifier == null) {
            playerIdentifier = PlayerIdentifier.fromTarget(handler);
        }

        if (playerIdentifier == null) {
            return false;
        }

        if (subject.isEmpty() || text.isEmpty()) {
            return false;
        }

        if (money <= 0) {
            return false;
        }

        // from console show not existed sender
        MailSender sender = new MailSender(MailMessageType.NORMAL, handler.getSession() ? handler.getSession().getPlayer().getGUID().getCounter() : 0, MailStationery.gm);

        SQLTransaction trans = new SQLTransaction();

        (new MailDraft(subject, text)).addMoney(new integer(money)).sendMailTo(trans, new MailReceiver(playerIdentifier.getGUID().getCounter()), sender);

        DB.characters.CommitTransaction(trans);

        var nameLink = handler.playerLink(playerIdentifier.getName());
        handler.sendSysMessage(SysMessage.MailSent, nameLink);

        return true;
    }

    
    private static boolean handleSendMessageCommand(CommandHandler handler, PlayerIdentifier playerIdentifier, QuotedString msgStr) {
        // - Find the player
        if (playerIdentifier == null) {
            playerIdentifier = PlayerIdentifier.fromTarget(handler);
        }

        if (playerIdentifier == null || !playerIdentifier.isConnected()) {
            return false;
        }

        if (!msgStr.isEmpty()) {
            return false;
        }

        // Check that he is not logging out.
        if (playerIdentifier.getConnectedPlayer().getSession().isLogingOut()) {
            handler.sendSysMessage(SysMessage.PlayerNotFound);

            return false;
        }

        // - Send the message
        playerIdentifier.getConnectedPlayer().getSession().sendNotification("{0}", msgStr);

        playerIdentifier.getConnectedPlayer().getSession().sendNotification("|cffff0000[Message from administrator]:|r");

        // Confirmation message
        var nameLink = handler.getNameLink(playerIdentifier.getConnectedPlayer());
        handler.sendSysMessage(SysMessage.Sendmessage, nameLink, msgStr);

        return true;
    }
}
