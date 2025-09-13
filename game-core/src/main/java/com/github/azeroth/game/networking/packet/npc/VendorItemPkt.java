package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.WorldPacket;

public class VendorItemPkt {
    public int muID;
    public int type;
    public itemInstance item = new itemInstance();
    public int quantity = -1;
    public long price;
    public int durability;
    public int stackCount;
    public int extendedCostID;
    public int playerConditionFailed;
    public boolean locked;
    public boolean doNotFilterOnVendor;
    public boolean refundable;

    public final void write(WorldPacket data) {
        data.writeInt64(price);
        data.writeInt32(muID);
        data.writeInt32(durability);
        data.writeInt32(stackCount);
        data.writeInt32(quantity);
        data.writeInt32(extendedCostID);
        data.writeInt32(playerConditionFailed);
        data.writeBits(type, 3);
        data.writeBit(locked);
        data.writeBit(doNotFilterOnVendor);
        data.writeBit(refundable);
        data.flushBits();

        item.write(data);
    }
}
