package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestLogRemoveQuest extends ClientPacket {
    public byte entry;

    public QuestLogRemoveQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        entry = this.readUInt8();
    }
}
