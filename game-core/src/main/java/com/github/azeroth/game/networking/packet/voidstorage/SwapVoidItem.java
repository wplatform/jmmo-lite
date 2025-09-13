package com.github.azeroth.game.networking.packet.voidstorage;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SwapVoidItem extends ClientPacket {
    public ObjectGuid npc = ObjectGuid.EMPTY;
    public ObjectGuid voidItemGuid = ObjectGuid.EMPTY;
    public int dstSlot;

    public SwapVoidItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npc = this.readPackedGuid();
        voidItemGuid = this.readPackedGuid();
        dstSlot = this.readUInt32();
    }
}
