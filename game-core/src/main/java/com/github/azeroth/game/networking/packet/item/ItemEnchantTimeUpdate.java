package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.networking.ServerPacket;

public class ItemEnchantTimeUpdate extends ServerPacket {
    public ObjectGuid ownerGuid = ObjectGuid.EMPTY;
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;

    public int durationLeft;

    public int slot;

    public ItemEnchantTimeUpdate() {
        super(ServerOpcode.ItemEnchantTimeUpdate);
    }

    @Override
    public void write() {
        this.writeGuid(itemGuid);
        this.writeInt32(durationLeft);
        this.writeInt32(slot);
        this.writeGuid(ownerGuid);
    }
}
