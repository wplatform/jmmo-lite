package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SetTitle extends ClientPacket {
    public int titleID;

    public setTitle(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        titleID = this.readInt32();
    }
}
