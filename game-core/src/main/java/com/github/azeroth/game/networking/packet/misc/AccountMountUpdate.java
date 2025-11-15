package com.github.azeroth.game.networking.packet.misc;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.HashMap;

public class AccountMountUpdate extends ServerPacket {
    public boolean isFullUpdate = false;
    public HashMap<Integer, Integer> mounts = new HashMap<>();

    public AccountMountUpdate() {
        super(ServerOpCode.SMSG_ACCOUNT_MOUNT_UPDATE);
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
