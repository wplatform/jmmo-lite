package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestWorldQuestUpdate extends ClientPacket {
    public RequestWorldQuestUpdate(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
