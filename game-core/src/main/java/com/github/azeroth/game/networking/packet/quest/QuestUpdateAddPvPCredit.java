package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;

public class QuestUpdateAddPvPCredit extends ServerPacket {
    public int questID;
    public short count;

    public QuestUpdateAddPvPCredit() {
        super(ServerOpcode.QuestUpdateAddPvpCredit);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
        this.writeInt16(count);
    }
}
