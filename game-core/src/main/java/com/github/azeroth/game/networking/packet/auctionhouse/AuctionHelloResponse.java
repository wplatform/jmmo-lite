package com.github.azeroth.game.networking.packet.auctionhouse;


import com.github.azeroth.game.networking.ServerPacket;

class AuctionHelloResponse extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public int deliveryDelay;
    public boolean openForBusiness = true;

    public AuctionHelloResponse() {
        super(ServerOpcode.AuctionHelloResponse);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeInt32(deliveryDelay);
        this.writeBit(openForBusiness);
        this.flushBits();
    }
}
