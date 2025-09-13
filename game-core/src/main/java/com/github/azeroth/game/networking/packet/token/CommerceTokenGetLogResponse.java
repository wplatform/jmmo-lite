package com.github.azeroth.game.networking.packet.token;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

class CommerceTokenGetLogResponse extends ServerPacket {
    private final ArrayList<AuctionableTokenInfo> auctionableTokenAuctionableList = new ArrayList<>();

    public int unkInt; // send CMSG_UPDATE_WOW_TOKEN_AUCTIONABLE_LIST
    public Tokenresult result = TokenResult.values()[0];

    public CommerceTokenGetLogResponse() {
        super(ServerOpcode.CommerceTokenGetLogResponse);
    }

    @Override
    public void write() {
        this.writeInt32(unkInt);
        this.writeInt32((int) result.getValue());
        this.writeInt32(auctionableTokenAuctionableList.size());

        for (var auctionableTokenAuctionable : auctionableTokenAuctionableList) {
            this.writeInt64(auctionableTokenAuctionable.unkInt1);
            this.writeInt64(auctionableTokenAuctionable.unkInt2);
            this.writeInt64(auctionableTokenAuctionable.buyoutPrice);
            this.writeInt32(auctionableTokenAuctionable.owner);
            this.writeInt32(auctionableTokenAuctionable.durationLeft);
        }
    }


    private final static class AuctionableTokenInfo {

        public long unkInt1;
        public long unkInt2;

        public int owner;

        public long buyoutPrice;

        public int durationLeft;

        public AuctionableTokenInfo clone() {
            AuctionableTokenInfo varCopy = new AuctionableTokenInfo();

            varCopy.unkInt1 = this.unkInt1;
            varCopy.unkInt2 = this.unkInt2;
            varCopy.owner = this.owner;
            varCopy.buyoutPrice = this.buyoutPrice;
            varCopy.durationLeft = this.durationLeft;

            return varCopy;
        }
    }
}
