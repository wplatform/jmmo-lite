package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ClassTalentsRequestNewConfig extends ClientPacket {
    public TraitconfigPacket config = new traitConfigPacket();

    public ClassTalentsRequestNewConfig(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        config.read(this);
    }
}
