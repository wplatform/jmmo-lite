package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class DurabilityDamageDeath extends ServerPacket {
    public int percent;

    public DurabilityDamageDeath() {
        super(ServerOpcode.DurabilityDamageDeath);
    }

    @Override
    public void write() {
        this.writeInt32(percent);
    }
}
