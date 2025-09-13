package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class QuestConfirmAccept extends ClientPacket {
    public int questID;

    public QuestConfirmAccept(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questID = this.readUInt32();
    }
}
