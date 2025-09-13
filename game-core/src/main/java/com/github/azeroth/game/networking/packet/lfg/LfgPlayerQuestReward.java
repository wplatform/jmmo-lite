package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class LfgPlayerQuestReward {
    public int mask;
    public int rewardMoney;
    public int rewardXP;
    public ArrayList<LfgPlayerQuestRewarditem> item = new ArrayList<>();
    public ArrayList<LfgPlayerQuestRewardcurrency> currency = new ArrayList<>();
    public ArrayList<LfgPlayerQuestRewardCurrency> bonusCurrency = new ArrayList<>();
    public Integer rewardSpellID = null; // Only used by SMSG_LFG_PLAYER_INFO
    public Integer unused1 = null;
    public Long unused2 = null;
    public Integer honor = null; // Only used by SMSG_REQUEST_PVP_REWARDS_RESPONSE

    public final void write(WorldPacket data) {
        data.writeInt32(mask);
        data.writeInt32(rewardMoney);
        data.writeInt32(rewardXP);
        data.writeInt32(item.size());
        data.writeInt32(currency.size());
        data.writeInt32(bonusCurrency.size());

        // Item
        for (var item : item) {
            data.writeInt32(item.itemID);
            data.writeInt32(item.quantity);
        }

        // Currency
        for (var currency : currency) {
            data.writeInt32(currency.currencyID);
            data.writeInt32(currency.quantity);
        }

        // BonusCurrency
        for (var bonusCurrency : bonusCurrency) {
            data.writeInt32(bonusCurrency.currencyID);
            data.writeInt32(bonusCurrency.quantity);
        }

        data.writeBit(rewardSpellID != null);
        data.writeBit(unused1 != null);
        data.writeBit(unused2 != null);
        data.writeBit(honor != null);
        data.flushBits();

        if (rewardSpellID != null) {
            data.writeInt32(rewardSpellID.intValue());
        }

        if (unused1 != null) {
            data.writeInt32(unused1.intValue());
        }

        if (unused2 != null) {
            data.writeInt64(unused2.longValue());
        }

        if (honor != null) {
            data.writeInt32(honor.intValue());
        }
    }
}
