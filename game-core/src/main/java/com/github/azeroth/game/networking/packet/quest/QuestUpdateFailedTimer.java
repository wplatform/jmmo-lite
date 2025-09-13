package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class QuestUpdateFailedTimer extends ServerPacket {
    public int questID;

    public QuestUpdateFailedTimer() {
        super(ServerOpCode.SMSG_QUEST_UPDATE_FAILED_TIMER);
    }

    @Override
    public void write() {
        this.writeInt32(questID);
    }
}
