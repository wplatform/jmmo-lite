package com.github.azeroth.game.networking.packet.ticket;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class Complaint extends ClientPacket {
    public SupportSpamType complaintType = SupportSpamType.values()[0];
    public Complaintoffender offender = new complaintOffender();
    public long mailID;
    public Complaintchat chat = new complaintChat();

    public long eventGuid;
    public long inviteGuid;

    public Complaint(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        complaintType = SupportSpamType.forValue(this.readUInt8());
        offender.read(this);

        switch (complaintType) {
            case Mail:
                mailID = this.readUInt64();

                break;
            case Chat:
                chat.read(this);

                break;
            case Calendar:
                eventGuid = this.readUInt64();
                inviteGuid = this.readUInt64();

                break;
        }
    }

    public final static class ComplaintOffender {
        public ObjectGuid playerGuid = ObjectGuid.EMPTY;
        public int realmAddress;
        public int timeSinceOffence;

        public void read(WorldPacket data) {
            playerGuid = data.readPackedGuid();
            realmAddress = data.readUInt32();
            timeSinceOffence = data.readUInt32();
        }

        public ComplaintOffender clone() {
            ComplaintOffender varCopy = new complaintOffender();

            varCopy.playerGuid = this.playerGuid;
            varCopy.realmAddress = this.realmAddress;
            varCopy.timeSinceOffence = this.timeSinceOffence;

            return varCopy;
        }
    }

    public final static class ComplaintChat {
        public int command;
        public int channelID;
        public String messageLog;

        public void read(WorldPacket data) {
            command = data.readUInt32();
            channelID = data.readUInt32();
            messageLog = data.readString(data.<Integer>readBit(12));
        }

        public ComplaintChat clone() {
            ComplaintChat varCopy = new complaintChat();

            varCopy.command = this.command;
            varCopy.channelID = this.channelID;
            varCopy.messageLog = this.messageLog;

            return varCopy;
        }
    }
}
