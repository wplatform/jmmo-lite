package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UnlearnSkill extends ClientPacket {
    public int skillLine;

    public UnlearnSkill(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        skillLine = this.readUInt32();
    }
}
