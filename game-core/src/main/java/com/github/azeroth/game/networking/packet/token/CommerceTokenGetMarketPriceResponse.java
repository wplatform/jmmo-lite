package com.github.azeroth.game.networking.packet.token;

import com.github.azeroth.game.networking.ServerPacket;

public class CommerceTokenGetMarketPriceResponse extends ServerPacket {
    public long currentMarketPrice;
    public int unkInt; // send CMSG_REQUEST_WOW_TOKEN_MARKET_PRICE
    public Tokenresult result = TokenResult.values()[0];
    public int auctionDuration; // preset auction duration enum

    public CommerceTokenGetMarketPriceResponse() {
        super(ServerOpcode.CommerceTokenGetMarketPriceResponse);
    }

    @Override
    public void write() {
        this.writeInt64(currentMarketPrice);
        this.writeInt32(unkInt);
        this.writeInt32((int) result.getValue());
        this.writeInt32(auctionDuration);
    }
}
