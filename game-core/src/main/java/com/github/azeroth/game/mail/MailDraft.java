package com.github.azeroth.game.mail;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.loot.Loot;
import com.github.azeroth.game.loot.LootStorage;

import java.util.HashMap;


public class MailDraft {
    private final int m_mailTemplateId;
    private final String m_subject;
    private final String m_body;
    private final HashMap<Long, item> m_items = new HashMap<Long, item>();
    private boolean m_mailTemplateItemsNeed;

    private long m_money;
    private long m_COD;


    public MailDraft(int mailTemplateId) {
        this(mailTemplateId, true);
    }

    public MailDraft(int mailTemplateId, boolean need_items) {
        m_mailTemplateId = mailTemplateId;
        m_mailTemplateItemsNeed = need_items;
        m_money = 0;
        m_COD = 0;
    }

    public MailDraft(String subject, String body) {
        m_mailTemplateId = 0;
        m_mailTemplateItemsNeed = false;
        m_subject = subject;
        m_body = body;
        m_money = 0;
        m_COD = 0;
    }

    public final MailDraft addItem(Item item) {
        m_items.put(item.getGUID().getCounter(), item);

        return this;
    }

    public final void sendReturnToSender(int senderAcc, long senderGuid, long receiver_guid, SQLTransaction trans) {
        var receiverGuid = ObjectGuid.create(HighGuid.Player, receiver_guid);
        var receiver = global.getObjAccessor().findPlayer(receiverGuid);

        int rc_account = 0;

        if (receiver == null) {
            rc_account = global.getCharacterCacheStorage().getCharacterAccountIdByGuid(receiverGuid);
        }

        if (receiver == null && rc_account == 0) // sender not exist
        {
            deleteIncludedItems(trans, true);

            return;
        }

        // prepare mail and send in other case
        var needItemDelay = false;

        if (!m_items.isEmpty()) {
            // if item send to character at another account, then apply item delivery delay
            needItemDelay = senderAcc != rc_account;

            // set owner to new receiver (to prevent delete item with sender char deleting)
            for (var item : m_items.values()) {
                item.saveToDB(trans); // item not in inventory and can be save standalone
                // owner in data will set at mail receive and item extracting
                var stmt = DB.characters.GetPreparedStatement(CharStatements.UPD_ITEM_OWNER);
                stmt.AddValue(0, receiver_guid);
                stmt.AddValue(1, item.GUID.counter);
                trans.append(stmt);
            }
        }

        // If theres is an item, there is a one hour delivery delay.
        var deliver_delay = needItemDelay ? WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay) : 0;

        // will delete item or place to receiver mail list
        sendMailTo(trans, new MailReceiver(receiver, receiver_guid), new MailSender(MailMessageType.NORMAL, senderGuid), MailCheckMask.Returned, deliver_delay);
    }


    public final void sendMailTo(SQLTransaction trans, Player receiver, MailSender sender, MailCheckMask checkMask) {
        sendMailTo(trans, receiver, sender, checkMask, 0);
    }

    public final void sendMailTo(SQLTransaction trans, Player receiver, MailSender sender) {
        sendMailTo(trans, receiver, sender, MailCheckMask.NONE, 0);
    }

    public final void sendMailTo(SQLTransaction trans, Player receiver, MailSender sender, MailCheckMask checkMask, int deliver_delay) {
        sendMailTo(trans, new MailReceiver(receiver), sender, checkMask, deliver_delay);
    }


    public final void sendMailTo(SQLTransaction trans, MailReceiver receiver, MailSender sender, MailCheckMask checkMask) {
        sendMailTo(trans, receiver, sender, checkMask, 0);
    }

    public final void sendMailTo(SQLTransaction trans, MailReceiver receiver, MailSender sender) {
        sendMailTo(trans, receiver, sender, MailCheckMask.NONE, 0);
    }

    public final void sendMailTo(SQLTransaction trans, MailReceiver receiver, MailSender sender, MailCheckMask checkMask, int deliver_delay) {
        var pReceiver = receiver.getPlayer(); // can be NULL
        var pSender = sender.getMailMessageType() == MailMessageType.Normal ? global.getObjAccessor().findPlayer(ObjectGuid.create(HighGuid.Player, sender.getSenderId())) : null;

        if (pReceiver != null) {
            prepareItems(pReceiver, trans); // generate mail template items
        }

        var mailId = global.getObjectMgr().generateMailID();

        var deliver_time = gameTime.GetGameTime() + deliver_delay;

        //expire time if COD 3 days, if no COD 30 days, if auction sale pending 1 hour
        int expire_delay;

        // auction mail without any items and money
        if (sender.getMailMessageType() == MailMessageType.Auction && m_items.isEmpty() && m_money == 0) {
            expire_delay = WorldConfig.getUIntValue(WorldCfg.MailDeliveryDelay);
        }
        // default case: expire time if COD 3 days, if no COD 30 days (or 90 days if sender is a game master)
        else if (m_COD != 0) {
            expire_delay = 3 * time.Day;
        } else {
            expire_delay = (int) (pSender != null && pSender.isGameMaster() ? 90 * time.Day : 30 * time.Day);
        }

        var expire_time = deliver_time + expire_delay;

        // Add to DB
        byte index = 0;
        var stmt = DB.characters.GetPreparedStatement(CharStatements.INS_MAIL);
        stmt.AddValue(index, mailId);
        stmt.AddValue(++index, (byte) sender.getMailMessageType().getValue());
        stmt.AddValue(++index, (byte) sender.getStationery().getValue());
        stmt.AddValue(++index, getMailTemplateId());
        stmt.AddValue(++index, sender.getSenderId());
        stmt.AddValue(++index, receiver.getPlayerGUIDLow());
        stmt.AddValue(++index, getSubject());
        stmt.AddValue(++index, getBody());
        stmt.AddValue(++index, !m_items.isEmpty());
        stmt.AddValue(++index, expire_time);
        stmt.AddValue(++index, deliver_time);
        stmt.AddValue(++index, m_money);
        stmt.AddValue(++index, m_COD);
        stmt.AddValue(++index, (byte) checkMask.getValue());
        trans.append(stmt);

        for (var item : m_items.values()) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_MAIL_ITEM);
            stmt.AddValue(0, mailId);
            stmt.AddValue(1, item.GUID.counter);
            stmt.AddValue(2, receiver.getPlayerGUIDLow());
            trans.append(stmt);
        }

        // For online receiver update in game mail status and data
        if (pReceiver != null) {
            pReceiver.addNewMailDeliverTime(deliver_time);


            Mail m = new MAIL();
            m.messageID = mailId;
            m.mailTemplateId = getMailTemplateId();
            m.subject = getSubject();
            m.body = getBody();
            m.money = getMoney();
            m.COD = getCOD();

            for (var item : m_items.values()) {
                m.addItem(item.GUID.counter, item.entry);
            }

            m.messageType = sender.getMailMessageType();
            m.stationery = sender.getStationery();
            m.sender = sender.getSenderId();
            m.receiver = receiver.getPlayerGUIDLow();
            m.expire_time = expire_time;
            m.deliver_time = deliver_time;
            m.checkMask = checkMask;
            m.state = MailState.Unchanged;

            pReceiver.addMail(m); // to insert new mail to beginning of maillist

            if (!m_items.isEmpty()) {
                for (var item : m_items.values()) {
                    pReceiver.addMItem(item);
                }
            }
        } else if (!m_items.isEmpty()) {
            deleteIncludedItems(null);
        }
    }

    public final MailDraft addMoney(long money) {
        m_money = money;

        return this;
    }

    public final MailDraft addCOD(int COD) {
        m_COD = COD;

        return this;
    }

    private void prepareItems(Player receiver, SQLTransaction trans) {
        if (m_mailTemplateId == 0 || !m_mailTemplateItemsNeed) {
            return;
        }

        m_mailTemplateItemsNeed = false;

        // The mail sent after turning in the quest The Good News and The Bad News contains 100g
        if (m_mailTemplateId == 123) {
            m_money = 1000000;
        }

        Loot mailLoot = new loot(null, ObjectGuid.Empty, LootType.NONE, null);

        // can be empty
        mailLoot.fillLoot(m_mailTemplateId, LootStorage.MAIL, receiver, true, true, LootModes.Default, itemContext.NONE);

        for (int i = 0; m_items.size() < SharedConst.MaxMailItems && i < mailLoot.items.size(); ++i) {
            var lootitem = mailLoot.lootItemInSlot(i, receiver);

            if (lootitem != null) {
                var item = item.createItem(lootitem.itemid, lootitem.count, lootitem.context, receiver);

                if (item != null) {
                    item.saveToDB(trans); // save for prevent lost at next mail load, if send fail then item will deleted
                    addItem(item);
                }
            }
        }
    }


    private void deleteIncludedItems(SQLTransaction trans) {
        deleteIncludedItems(trans, false);
    }

    private void deleteIncludedItems(SQLTransaction trans, boolean inDB) {
        for (var item : m_items.values()) {
            if (inDB) {
                item.deleteFromDB(trans);
            }
        }

        m_items.clear();
    }

    private int getMailTemplateId() {
        return m_mailTemplateId;
    }

    private String getSubject() {
        return m_subject;
    }

    private long getMoney() {
        return m_money;
    }

    private long getCOD() {
        return m_COD;
    }

    private String getBody() {
        return m_body;
    }
}
