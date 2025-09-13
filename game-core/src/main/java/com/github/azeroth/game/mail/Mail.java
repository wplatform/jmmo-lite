package com.github.azeroth.game.mail;


import java.util.ArrayList;


public class Mail {
    public long messageID;
    public MailMessageType messageType = MailMessageType.values()[0];
    public MailStationery stationery = MailStationery.values()[0];
    public int mailTemplateId;
    public long sender;
    public long receiver;
    public String subject;
    public String body;
    public ArrayList<MailItemInfo> items = new ArrayList<>();
    public ArrayList<Long> removedItems = new ArrayList<>();
    public long expire_time;
    public long deliver_time;
    public long money;
    public long COD;
    public MailCheckMask checkMask = MailCheckMask.values()[0];
    public MailState state = MailState.values()[0];

    public final void addItem(long itemGuidLow, int item_template) {
        MailItemInfo mii = new MailItemInfo();
        mii.item_guid = itemGuidLow;
        mii.item_template = item_template;
        items.add(mii);
    }

    public final boolean removeItem(long itemGuid) {
        for (var item : items) {
            if (item.item_guid == itemGuid) {
                items.remove(item);

                return true;
            }
        }

        return false;
    }

    public final boolean hasItems() {
        return !items.isEmpty();
    }
}
