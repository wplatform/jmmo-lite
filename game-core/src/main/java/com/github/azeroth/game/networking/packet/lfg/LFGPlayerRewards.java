package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

public final class LFGPlayerRewards {
    public ItemInstance rewardItem;
    public Integer rewardCurrency = null;
    public int quantity;
    public int bonusQuantity;
    public LFGPlayerRewards() {
    }
    public LFGPlayerRewards(int id, int quantity, int bonusQuantity, boolean isCurrency) {
        quantity = quantity;
        bonusQuantity = bonusQuantity;
        rewardItem = null;
        rewardCurrency = null;

        if (!isCurrency) {
            rewardItem = new itemInstance();
            rewardItem.itemID = id;
        } else {
            rewardCurrency = id;
        }
    }

    public void write(WorldPacket data) {
        data.writeBit(rewardItem != null);
        data.writeBit(rewardCurrency != null);

        if (rewardItem != null) {
            rewardItem.write(data);
        }

        data.writeInt32(quantity);
        data.writeInt32(bonusQuantity);

        if (rewardCurrency != null) {
            data.writeInt32(rewardCurrency.intValue());
        }
    }

    public LFGPlayerRewards clone() {
        LFGPlayerRewards varCopy = new LFGPlayerRewards();

        varCopy.rewardItem = this.rewardItem;
        varCopy.rewardCurrency = this.rewardCurrency;
        varCopy.quantity = this.quantity;
        varCopy.bonusQuantity = this.bonusQuantity;

        return varCopy;
    }
}
