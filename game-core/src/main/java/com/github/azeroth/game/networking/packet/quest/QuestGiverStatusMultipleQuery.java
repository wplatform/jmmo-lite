package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverStatusMultipleQuery extends ClientPacket {
    public QuestGiverStatusMultipleQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
