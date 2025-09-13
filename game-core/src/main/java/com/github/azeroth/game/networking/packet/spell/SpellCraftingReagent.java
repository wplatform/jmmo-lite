package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellCraftingReagent {
    public int itemID;
    public int dataSlotIndex;
    public int quantity;
    public Byte unknown_1000 = null;

    public void read(WorldPacket data) {
        itemID = data.readInt32();
        dataSlotIndex = data.readInt32();
        quantity = data.readInt32();

        if (data.readBit()) {
            unknown_1000 = data.readUInt8();
        }
    }

    public SpellCraftingReagent clone() {
        SpellCraftingReagent varCopy = new SpellCraftingReagent();

        varCopy.itemID = this.itemID;
        varCopy.dataSlotIndex = this.dataSlotIndex;
        varCopy.quantity = this.quantity;
        varCopy.unknown_1000 = this.unknown_1000;

        return varCopy;
    }
}
