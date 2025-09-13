package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;
import game.*;

public class AuctionBucketKey {
    public int itemID;
    public short itemLevel;
    public Short battlePetSpeciesID = null;
    public Short suffixItemNameDescriptionID = null;

    public auctionBucketKey() {
    }

    public auctionBucketKey(AuctionsBucketKey key) {
        itemID = key.getItemId();
        itemLevel = key.getItemLevel();

        if (key.getBattlePetSpeciesId() != 0) {
            battlePetSpeciesID = key.getBattlePetSpeciesId();
        }

        if (key.getSuffixItemNameDescriptionId() != 0) {
            suffixItemNameDescriptionID = key.getSuffixItemNameDescriptionId();
        }
    }

    public auctionBucketKey(WorldPacket data) {
        data.resetBitPos();
        itemID = data.<Integer>readBit(20);
        var hasBattlePetSpeciesId = data.readBit();
        itemLevel = data.<SHORT>readBit(11);
        var hasSuffixItemNameDescriptionId = data.readBit();

        if (hasBattlePetSpeciesId) {
            battlePetSpeciesID = data.readUInt16();
        }

        if (hasSuffixItemNameDescriptionId) {
            suffixItemNameDescriptionID = data.readUInt16();
        }
    }

    public final void write(WorldPacket data) {
        data.writeBits(itemID, 20);
        data.writeBit(battlePetSpeciesID != null);
        data.writeBits(itemLevel, 11);
        data.writeBit(suffixItemNameDescriptionID != null);
        data.flushBits();

        if (battlePetSpeciesID != null) {
            data.writeInt16(battlePetSpeciesID.shortValue());
        }

        if (suffixItemNameDescriptionID != null) {
            data.writeInt16(suffixItemNameDescriptionID.shortValue());
        }
    }
}
