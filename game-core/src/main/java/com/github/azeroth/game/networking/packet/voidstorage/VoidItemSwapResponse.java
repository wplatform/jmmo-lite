package com.github.azeroth.game.networking.packet.voidstorage;

import com.github.azeroth.game.networking.ServerPacket;

public class VoidItemSwapResponse extends ServerPacket {
    public ObjectGuid voidItemA = ObjectGuid.EMPTY;
    public ObjectGuid voidItemB = ObjectGuid.EMPTY;
    public int voidItemSlotA;
    public int voidItemSlotB;

    public VoidItemSwapResponse() {
        super(ServerOpcode.VoidItemSwapResponse);
    }

    @Override
    public void write() {
        this.writeGuid(voidItemA);
        this.writeInt32(voidItemSlotA);
        this.writeGuid(voidItemB);
        this.writeInt32(voidItemSlotB);
    }
}
