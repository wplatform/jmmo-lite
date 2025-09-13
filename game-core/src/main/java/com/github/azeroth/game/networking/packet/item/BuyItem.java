package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.networking.WorldPacket;

public class BuyItem extends ClientPacket {
    public ObjectGuid vendorGUID = ObjectGuid.EMPTY;
    public itemInstance item;

    public int muid;

    public int slot;
    public ItemVendorType itemType = ItemVendorType.values()[0];
    public int quantity;
    public ObjectGuid containerGUID = ObjectGuid.EMPTY;

    public BuyItem(WorldPacket packet) {
        super(packet);
        item = new itemInstance();
    }

    @Override
    public void read() {
        vendorGUID = this.readPackedGuid();
        containerGUID = this.readPackedGuid();
        quantity = this.readInt32();
        muid = this.readUInt();
        slot = this.readUInt();
        item.read(this);
        itemType = ItemVendorType.forValue(this.<Integer>readBit(3));
    }
}
