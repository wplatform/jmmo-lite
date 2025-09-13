package com.github.azeroth.game.networking.packet.social;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SocialContractRequest extends ClientPacket {
    public SocialContractRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
