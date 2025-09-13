package com.github.azeroth.game.networking.packet.loot;


import com.github.azeroth.game.networking.WorldPacket;

public class LootItemData {
    public byte type;
    public LootSlotType UIType = LootSlotType.values()[0];
    public int quantity;
    public byte lootItemType;
    public byte lootListID;
    public boolean canTradeToTapList;
    public ItemInstance loot;

    public final void write(WorldPacket data) {
        data.writeBits(type, 2);
        data.writeBits(UIType, 3);
        data.writeBit(canTradeToTapList);
        data.flushBits();
        loot.write(data); // WorldPackets::Item::ItemInstance
        data.writeInt32(quantity);
        data.writeInt8(lootItemType);
        data.writeInt8(lootListID);
    }
}
