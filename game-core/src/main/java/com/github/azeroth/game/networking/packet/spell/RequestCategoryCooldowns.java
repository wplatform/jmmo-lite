package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RequestCategoryCooldowns extends ClientPacket {
    public RequestCategoryCooldowns(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
