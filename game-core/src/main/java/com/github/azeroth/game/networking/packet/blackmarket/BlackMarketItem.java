package com.github.azeroth.game.networking.packet.blackmarket;

import com.github.azeroth.game.networking.WorldPacket;

public final class BlackMarketItem {

    public int marketID;

    public int sellerNPC;
    public itemInstance item;

    public int quantity;

    public long minBid;

    public long minIncrement;

    public long currentBid;

    public int secondsRemaining;

    public int numBids;
    public boolean highBid;

    public void read(WorldPacket data) {
        marketID = data.readUInt32();
        sellerNPC = data.readUInt32();
        item.read(data);
        quantity = data.readUInt32();
        minBid = data.readUInt64();
        minIncrement = data.readUInt64();
        currentBid = data.readUInt64();
        secondsRemaining = data.readUInt32();
        numBids = data.readUInt32();
        highBid = data.readBit();
    }

    public void write(WorldPacket data) {
        data.writeInt32(marketID);
        data.writeInt32(sellerNPC);
        data.writeInt32(quantity);
        data.writeInt64(minBid);
        data.writeInt64(minIncrement);
        data.writeInt64(currentBid);
        data.writeInt32(secondsRemaining);
        data.writeInt32(numBids);
        item.write(data);
        data.writeBit(highBid);
        data.flushBits();
    }

    public BlackMarketItem clone() {
        BlackMarketItem varCopy = new BlackMarketItem();

        varCopy.marketID = this.marketID;
        varCopy.sellerNPC = this.sellerNPC;
        varCopy.item = this.item;
        varCopy.quantity = this.quantity;
        varCopy.minBid = this.minBid;
        varCopy.minIncrement = this.minIncrement;
        varCopy.currentBid = this.currentBid;
        varCopy.secondsRemaining = this.secondsRemaining;
        varCopy.numBids = this.numBids;
        varCopy.highBid = this.highBid;

        return varCopy;
    }
}
