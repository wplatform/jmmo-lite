package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.HashMap;

public class AccountMountUpdate extends ServerPacket {
    public boolean isFullUpdate = false;
    public HashMap<Integer, MountStatusFlags> mounts = new HashMap<Integer, MountStatusFlags>();

    public AccountMountUpdate() {
        super(ServerOpcode.AccountMountUpdate);
    }

    @Override
    public void write() {
        this.writeBit(isFullUpdate);
        this.writeInt32(mounts.size());

        for (var spell : mounts.entrySet()) {
            this.writeInt32(spell.getKey());
            this.writeBits(spell.getValue(), 2);
        }

        this.flushBits();
    }
}
