package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetActionBarToggles extends ClientPacket {
    public byte mask;

    public SetActionBarToggles(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        mask = this.readUInt8();
    }
}
