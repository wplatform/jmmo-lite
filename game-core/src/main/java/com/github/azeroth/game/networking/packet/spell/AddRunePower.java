package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ServerPacket;

public class AddRunePower extends ServerPacket {
    public int addedRunesMask;

    public AddRunePower() {
        super(ServerOpcode.AddRunePower);
    }

    @Override
    public void write() {
        this.writeInt32(addedRunesMask);
    }
}
