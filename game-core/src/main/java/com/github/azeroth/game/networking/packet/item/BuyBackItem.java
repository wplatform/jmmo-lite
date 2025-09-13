package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

public class BuyBackItem extends ClientPacket {
    public ObjectGuid vendorGUID = ObjectGuid.EMPTY;
    public int slot;

    public BuyBackItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        vendorGUID = this.readPackedGuid();
        slot = this.readUInt();
    }
}

//Structs

