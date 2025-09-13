package com.github.azeroth.game.networking.packet.perksporgram;

import com.github.azeroth.game.networking.WorldPacket;

public final class PerksVendorItem {
    public int vendorItemID;
    public int mountID;
    public int battlePetSpeciesID;
    public int transmogSetID;
    public int itemModifiedAppearanceID;
    public int field_14;
    public int field_18;
    public int price;
    public long availableUntil;
    public boolean disabled;

    public void write(WorldPacket data) {
        data.writeInt32(vendorItemID);
        data.writeInt32(mountID);
        data.writeInt32(battlePetSpeciesID);
        data.writeInt32(transmogSetID);
        data.writeInt32(itemModifiedAppearanceID);
        data.writeInt32(field_14);
        data.writeInt32(field_18);
        data.writeInt32(price);
        data.writeInt64(availableUntil);
        data.writeBit(disabled);
        data.flushBits();
    }

    public PerksVendorItem clone() {
        PerksVendorItem varCopy = new PerksVendorItem();

        varCopy.vendorItemID = this.vendorItemID;
        varCopy.mountID = this.mountID;
        varCopy.battlePetSpeciesID = this.battlePetSpeciesID;
        varCopy.transmogSetID = this.transmogSetID;
        varCopy.itemModifiedAppearanceID = this.itemModifiedAppearanceID;
        varCopy.field_14 = this.field_14;
        varCopy.field_18 = this.field_18;
        varCopy.price = this.price;
        varCopy.availableUntil = this.availableUntil;
        varCopy.disabled = this.disabled;

        return varCopy;
    }
}
