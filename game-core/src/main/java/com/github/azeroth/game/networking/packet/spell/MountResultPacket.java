package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class MountResultPacket extends ServerPacket {
    public int result;

    public MountResultPacket() {
        super(ServerOpcode.MountResult);
    }

    @Override
    public void write() {
        this.writeInt32(result);
    }
}
