package com.github.azeroth.game.networking.packet.mail;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MailListEntry {
    public long mailID;
    public byte senderType;
    public ObjectGuid senderCharacter = null;
    public Integer altSenderID = null;
    public long cod;
    public int stationeryID;
    public long sentMoney;
    public int flags;
    public float daysLeft;
    public int mailTemplateID;
    public String subject = "";
    public String body = "";
    public ArrayList<MailAttachedItem> attachments = new ArrayList<>();

    public MailListEntry(Mail mail, Player player) {
        mailID = mail.messageID;
        senderType = (byte) mail.messageType.getValue();

        switch (mail.messageType) {
            case Normal:
                senderCharacter = ObjectGuid.create(HighGuid.Player, mail.sender);

                break;
            case Creature:
            case Gameobject:
            case Auction:
            case Calendar:
                altSenderID = (int) mail.sender;

                break;
        }

        cod = mail.COD;
        stationeryID = mail.stationery.getValue();
        sentMoney = mail.money;
        flags = mail.checkMask.getValue();
        daysLeft = (float) (mail.expire_time - gameTime.GetGameTime()) / time.Day;
        mailTemplateID = (int) mail.mailTemplateId;
        subject = mail.subject;
        body = mail.body;

        for (byte i = 0; i < mail.items.size(); i++) {
            var item = player.getMItem(mail.items.get(i).item_guid);

            if (item) {
                attachments.add(new MailAttachedItem(item, i));
            }
        }
    }

    public final void write(WorldPacket data) {
        data.writeInt64(mailID);
        data.writeInt8(senderType);
        data.writeInt64(cod);
        data.writeInt32(stationeryID);
        data.writeInt64(sentMoney);
        data.writeInt32(flags);
        data.writeFloat(daysLeft);
        data.writeInt32(mailTemplateID);
        data.writeInt32(attachments.size());

        data.writeBit(senderCharacter != null);
        data.writeBit(altSenderID != null);
        data.writeBits(subject.getBytes().length, 8);
        data.writeBits(body.getBytes().length, 13);
        data.flushBits();

        attachments.forEach(p -> p.write(data));

        if (senderCharacter != null) {
            data.writeGuid(senderCharacter.getValue());
        }

        if (altSenderID != null) {
            data.writeInt32(altSenderID.intValue());
        }

        data.writeString(subject);
        data.writeString(body);
    }
}
