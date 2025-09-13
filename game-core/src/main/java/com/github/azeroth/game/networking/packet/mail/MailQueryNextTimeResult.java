package com.github.azeroth.game.networking.packet.mail;


import java.util.ArrayList;


public class MailQueryNextTimeResult extends ServerPacket {
    public float nextMailTime;
    public ArrayList<MailnextTimeEntry> next;

    public MailQueryNextTimeResult() {
        super(ServerOpcode.MailQueryNextTimeResult);
        next = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeFloat(nextMailTime);
        this.writeInt32(next.size());

        for (var entry : next) {
            this.writeGuid(entry.senderGuid);
            this.writeFloat(entry.timeLeft);
            this.writeInt32(entry.altSenderID);
            this.writeInt8(entry.altSenderType);
            this.writeInt32(entry.stationeryID);
        }
    }

    public static class MailNextTimeEntry {
        public ObjectGuid senderGuid = ObjectGuid.EMPTY;
        public float timeLeft;
        public int altSenderID;
        public byte altSenderType;
        public int stationeryID;

        public MailNextTimeEntry(Mail mail) {
            switch (mail.messageType) {
                case Normal:
                    senderGuid = ObjectGuid.create(HighGuid.Player, mail.sender);

                    break;
                case Auction:
                case Creature:
                case Gameobject:
                case Calendar:
                    altSenderID = (int) mail.sender;

                    break;
            }

            timeLeft = mail.deliver_time - gameTime.GetGameTime();
            altSenderType = (byte) mail.messageType.getValue();
            stationeryID = mail.stationery.getValue();
        }
    }
}
