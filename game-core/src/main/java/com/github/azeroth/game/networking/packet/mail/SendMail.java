package com.github.azeroth.game.networking.packet.mail;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class SendMail extends ClientPacket {
    public StructSendMail info;

    public SendMail(WorldPacket packet) {
        super(packet);
        info = new StructSendMail();
    }

    @Override
    public void read() {
        info.mailbox = this.readPackedGuid();
        info.stationeryID = this.readInt32();
        info.sendMoney = this.readInt64();
        info.cod = this.readInt64();

        var targetLength = this.<Integer>readBit(9);
        var subjectLength = this.<Integer>readBit(9);
        var bodyLength = this.<Integer>readBit(11);

        var count = this.<Integer>readBit(5);

        info.target = this.readString(targetLength);
        info.subject = this.readString(subjectLength);
        info.body = this.readString(bodyLength);

        for (var i = 0; i < count; ++i) {
            var att = new StructSendMail.MailAttachment();
            att.attachPosition = this.readUInt8();
            att.itemGUID = this.readPackedGuid();

            info.attachments.add(att);
        }
    }

    public static class StructSendMail {
        public ObjectGuid mailbox = ObjectGuid.EMPTY;
        public int stationeryID;
        public long sendMoney;
        public long cod;
        public String target;
        public String subject;
        public String body;
        public ArrayList<MailAttachment> attachments = new ArrayList<>();

        public final static class MailAttachment {
            public byte attachPosition;
            public ObjectGuid itemGUID = ObjectGuid.EMPTY;

            public MailAttachment clone() {
                MailAttachment varCopy = new MailAttachment();

                varCopy.attachPosition = this.attachPosition;
                varCopy.itemGUID = this.itemGUID;

                return varCopy;
            }
        }
    }
}
