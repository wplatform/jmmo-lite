package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;

public class QuestConfirmAcceptResponse extends ServerPacket {
    public ObjectGuid initiatedBy = ObjectGuid.EMPTY;
    public int questID;
    public String questTitle;

    public QuestConfirmAcceptResponse() {
        super(ServerOpcode.QuestConfirmAccept);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
        this.writeGuid(initiatedBy);

        this.writeBits(questTitle.getBytes().length, 10);
        this.writeString(questTitle);
    }
}
