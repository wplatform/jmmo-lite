package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

public class ItemEnchantData {
    public int ID;
    public int expiration;
    public int charges;
    public byte slot;

    public ItemEnchantData() {
    }

    public ItemEnchantData(int id, int expiration, int charges, byte slot) {
        ID = id;
        expiration = expiration;
        charges = charges;
        slot = slot;
    }

    public final void write(WorldPacket data) {
        data.writeInt32(ID);
        data.writeInt32(expiration);
        data.writeInt32(charges);
        data.writeInt8(slot);
    }
}
