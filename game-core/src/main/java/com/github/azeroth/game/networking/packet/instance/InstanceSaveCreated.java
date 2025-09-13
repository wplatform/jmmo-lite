package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceSaveCreated extends ServerPacket {
    public boolean gm;

    public InstanceSaveCreated() {
        super(ServerOpcode.InstanceSaveCreated);
    }

    @Override
    public void write() {
        this.writeBit(gm);
        this.flushBits();
    }
}
