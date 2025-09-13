package com.github.azeroth.game.networking.packet.auctionhouse;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class BucketInfo {
    public AuctionBucketkey key;
    public int totalQuantity;
    public long minPrice;
    public int requiredLevel;
    public ArrayList<Integer> itemModifiedAppearanceIDs = new ArrayList<>();
    public Byte maxBattlePetQuality = null;
    public Byte maxBattlePetLevel = null;
    public Byte battlePetBreedID = null;
    public Integer unk901_1 = null;
    public boolean containsOwnerItem;
    public boolean containsOnlyCollectedAppearances;

    public final void write(WorldPacket data) {
        key.write(data);
        data.writeInt32(totalQuantity);
        data.writeInt32(requiredLevel);
        data.writeInt64(minPrice);
        data.writeInt32(itemModifiedAppearanceIDs.size());

        if (!itemModifiedAppearanceIDs.isEmpty()) {
            for (int id : itemModifiedAppearanceIDs) {
                data.writeInt32(id);
            }
        }

        data.writeBit(maxBattlePetQuality != null);
        data.writeBit(maxBattlePetLevel != null);
        data.writeBit(battlePetBreedID != null);
        data.writeBit(unk901_1 != null);
        data.writeBit(containsOwnerItem);
        data.writeBit(containsOnlyCollectedAppearances);
        data.flushBits();

        if (maxBattlePetQuality != null) {
            data.writeInt8(maxBattlePetQuality.byteValue());
        }

        if (maxBattlePetLevel != null) {
            data.writeInt8(maxBattlePetLevel.byteValue());
        }

        if (battlePetBreedID != null) {
            data.writeInt8(battlePetBreedID.byteValue());
        }

        if (unk901_1 != null) {
            data.writeInt32(unk901_1.intValue());
        }
    }
}
