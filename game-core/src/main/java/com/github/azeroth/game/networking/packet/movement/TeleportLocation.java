package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

public class TeleportLocation {
    public position pos;
    public int unused901_1 = -1;
    public int unused901_2 = -1;

    public final void write(WorldPacket data) {
        data.writeXYZO(pos);
        data.writeInt32(unused901_1);
        data.writeInt32(unused901_2);
    }
}
