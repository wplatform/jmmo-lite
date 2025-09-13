package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class QuestUpdateComplete extends ServerPacket {
    public int questID;

    public QuestUpdateComplete() {
        super(ServerOpCode.SMSG_QUEST_UPDATE_COMPLETE);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
    }
}
