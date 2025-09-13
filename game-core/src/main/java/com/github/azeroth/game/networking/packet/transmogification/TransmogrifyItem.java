package com.github.azeroth.game.networking.packet.transmogification;

import com.github.azeroth.game.networking.WorldPacket;

final class TransmogrifyItem {
    public int itemModifiedAppearanceID;
    public int slot;
    public int spellItemEnchantmentID;
    public int secondaryItemModifiedAppearanceID;

    public void read(WorldPacket data) {
        itemModifiedAppearanceID = data.readInt32();
        slot = data.readUInt32();
        spellItemEnchantmentID = data.readInt32();
        secondaryItemModifiedAppearanceID = data.readInt32();
    }

    public TransmogrifyItem clone() {
        TransmogrifyItem varCopy = new TransmogrifyItem();

        varCopy.itemModifiedAppearanceID = this.itemModifiedAppearanceID;
        varCopy.slot = this.slot;
        varCopy.spellItemEnchantmentID = this.spellItemEnchantmentID;
        varCopy.secondaryItemModifiedAppearanceID = this.secondaryItemModifiedAppearanceID;

        return varCopy;
    }
}
