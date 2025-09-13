package com.github.azeroth.game.networking.packet.combat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetSheathed extends ClientPacket {
    public int currentSheathState;
    public boolean animate = true;

    public SetSheathed(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        currentSheathState = this.readInt32();
        animate = this.readBit();
    }
}
