package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverCloseQuest extends ClientPacket {
    public int questID;

    public QuestGiverCloseQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questID = this.readUInt();
    }
}
