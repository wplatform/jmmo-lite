package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class AttackStop extends ClientPacket {
    public attackStop(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
