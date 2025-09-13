package com.github.azeroth.game.networking.packet.item;


public class SellResponse extends ServerPacket {
    public ObjectGuid vendorGUID = ObjectGuid.EMPTY;
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public SellResult reason = SellResult.unk;

    public SellResponse() {
        super(ServerOpcode.SellResponse);
    }

    @Override
    public void write() {
        this.writeGuid(vendorGUID);
        this.writeGuid(itemGUID);
        this.writeInt8((byte) reason.getValue());
    }
}
