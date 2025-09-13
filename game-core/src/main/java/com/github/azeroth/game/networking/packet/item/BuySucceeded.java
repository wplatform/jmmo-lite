package com.github.azeroth.game.networking.packet.item;


public class BuySucceeded extends ServerPacket {
    public ObjectGuid vendorGUID = ObjectGuid.EMPTY;
    public int muid;
    public int quantityBought;
    public int newQuantity;

    public BuySucceeded() {
        super(ServerOpcode.BuySucceeded);
    }

    @Override
    public void write() {
        this.writeGuid(vendorGUID);
        this.writeInt32(muid);
        this.writeInt32(newQuantity);
        this.writeInt32(quantityBought);
    }
}
