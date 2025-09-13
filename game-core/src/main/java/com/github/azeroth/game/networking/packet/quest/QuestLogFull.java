package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class QuestLogFull extends ServerPacket {
    public QuestLogFull() {
        super(ServerOpCode.SMSG_QUEST_LOG_FULL);
    }

    @Override
    public void write() {
    }
}
