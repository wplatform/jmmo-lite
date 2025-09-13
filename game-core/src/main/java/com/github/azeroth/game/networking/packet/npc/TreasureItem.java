package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.networking.WorldPacket;

public final class TreasureItem {
    public GossipOptionRewardtype type = GossipOptionRewardType.values()[0];
    public int ID;
    public int quantity;

    public void write(WorldPacket data) {
        data.writeBits((byte) type.getValue(), 1);
        data.writeInt32(ID);
        data.writeInt32(quantity);
    }

    public TreasureItem clone() {
        TreasureItem varCopy = new TreasureItem();

        varCopy.type = this.type;
        varCopy.ID = this.ID;
        varCopy.quantity = this.quantity;

        return varCopy;
    }
}
