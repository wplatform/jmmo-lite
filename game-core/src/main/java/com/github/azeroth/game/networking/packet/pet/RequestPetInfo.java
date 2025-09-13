package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RequestPetInfo extends ClientPacket {
    public RequestPetInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
