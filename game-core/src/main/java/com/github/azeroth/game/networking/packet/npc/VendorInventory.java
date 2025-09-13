package com.github.azeroth.game.networking.packet.npc;


import java.util.ArrayList;


public class VendorInventory extends ServerPacket {
    public byte reason = 0;
    public ArrayList<VendorItemPkt> items = new ArrayList<>();
    public ObjectGuid vendor = ObjectGuid.EMPTY;

    public VendorInventory() {
        super(ServerOpcode.VendorInventory);
    }

    @Override
    public void write() {
        this.writeGuid(vendor);
        this.writeInt8(reason);
        this.writeInt32(items.size());

        for (var item : items) {
            item.write(this);
        }
    }
}
