package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class ViolenceLevel extends ClientPacket {
    public byte violenceLevel; // 0 - no combat effects, 1 - display some combat effects, 2 - blood, 3 - bloody, 4 - bloodier, 5 - bloodiest

    public ViolenceLevel(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        violenceLevel = this.readByte();
    }
}
