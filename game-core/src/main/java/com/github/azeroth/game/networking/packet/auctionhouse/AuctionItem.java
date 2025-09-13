package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class AuctionItem {
    public itemInstance item;
    public int count;
    public int charges;
    public ArrayList<ItemEnchantData> enchantments = new ArrayList<>();
    public int flags;
    public int auctionID;
    public ObjectGuid owner = ObjectGuid.EMPTY;
    public Long minBid = null;
    public Long minIncrement = null;
    public Long buyoutPrice = null;
    public Long unitPrice = null;
    public int durationLeft;
    public byte deleteReason;
    public boolean censorServerSideInfo;
    public boolean censorBidInfo;
    public ObjectGuid itemGuid = ObjectGuid.EMPTY;
    public ObjectGuid ownerAccountID = ObjectGuid.EMPTY;
    public int endTime;
    public ObjectGuid bidder = null;
    public Long bidAmount = null;
    public ArrayList<ItemGemData> gems = new ArrayList<>();
    public auctionBucketKey auctionBucketKey;
    public ObjectGuid creator = null;

    public final void write(WorldPacket data) {
        data.writeBit(item != null);
        data.writeBits(enchantments.size(), 4);
        data.writeBits(gems.size(), 2);
        data.writeBit(minBid != null);
        data.writeBit(minIncrement != null);
        data.writeBit(buyoutPrice != null);
        data.writeBit(unitPrice != null);
        data.writeBit(censorServerSideInfo);
        data.writeBit(censorBidInfo);
        data.writeBit(auctionBucketKey != null);
        data.writeBit(creator != null);

        if (!censorBidInfo) {
            data.writeBit(bidder != null);
            data.writeBit(bidAmount != null);
        }

        data.flushBits();

        if (item != null) {
            item.write(data);
        }

        data.writeInt32(count);
        data.writeInt32(charges);
        data.writeInt32(flags);
        data.writeInt32(auctionID);
        data.writeGuid(owner);
        data.writeInt32(durationLeft);
        data.writeInt8(deleteReason);

        for (var enchant : enchantments) {
            enchant.write(data);
        }

        if (minBid != null) {
            data.writeInt64(minBid.longValue());
        }

        if (minIncrement != null) {
            data.writeInt64(minIncrement.longValue());
        }

        if (buyoutPrice != null) {
            data.writeInt64(buyoutPrice.longValue());
        }

        if (unitPrice != null) {
            data.writeInt64(unitPrice.longValue());
        }

        if (!censorServerSideInfo) {
            data.writeGuid(itemGuid);
            data.writeGuid(ownerAccountID);
            data.writeInt32(endTime);
        }

        if (creator != null) {
            data.writeGuid(creator.getValue());
        }

        if (!censorBidInfo) {
            if (bidder != null) {
                data.writeGuid(bidder.getValue());
            }

            if (bidAmount != null) {
                data.writeInt64(bidAmount.longValue());
            }
        }

        for (var gem : gems) {
            gem.write(data);
        }

        if (auctionBucketKey != null) {
            auctionBucketKey.write(data);
        }
    }
}
