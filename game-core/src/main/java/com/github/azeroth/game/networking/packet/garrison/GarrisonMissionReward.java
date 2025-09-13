package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class GarrisonMissionReward {
    public int itemID;
    public int itemQuantity;
    public int currencyID;
    public int currencyQuantity;
    public int followerXP;
    public int garrMssnBonusAbilityID;
    public int itemFileDataID;
    public itemInstance itemInstance;

    public void write(WorldPacket data) {
        data.writeInt32(itemID);
        data.writeInt32(itemQuantity);
        data.writeInt32(currencyID);
        data.writeInt32(currencyQuantity);
        data.writeInt32(followerXP);
        data.writeInt32(garrMssnBonusAbilityID);
        data.writeInt32(itemFileDataID);
        data.writeBit(itemInstance != null);
        data.flushBits();

        if (itemInstance != null) {
            itemInstance.write(data);
        }
    }

    public GarrisonMissionReward clone() {
        GarrisonMissionReward varCopy = new GarrisonMissionReward();

        varCopy.itemID = this.itemID;
        varCopy.itemQuantity = this.itemQuantity;
        varCopy.currencyID = this.currencyID;
        varCopy.currencyQuantity = this.currencyQuantity;
        varCopy.followerXP = this.followerXP;
        varCopy.garrMssnBonusAbilityID = this.garrMssnBonusAbilityID;
        varCopy.itemFileDataID = this.itemFileDataID;
        varCopy.itemInstance = this.itemInstance;

        return varCopy;
    }
}
