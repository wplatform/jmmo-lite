package com.github.azeroth.game.networking.packet.voidstorage;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;

public class VoidStorageContents extends ServerPacket {
    public ArrayList<VoidItem> items = new ArrayList<>();

    public VoidStorageContents() {
        super(ServerOpCode.SMSG_VOID_STORAGE_CONTENTS);
    }

    @Override
    public void write() {
        this.writeBits(items.size(), 8);
        this.flushBits();

        for (var voidItem : items) {
            voidItem.write(this);
        }
    }
}
