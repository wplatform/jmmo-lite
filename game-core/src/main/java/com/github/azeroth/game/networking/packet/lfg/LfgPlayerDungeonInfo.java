package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class LfgPlayerDungeonInfo {
    public int slot;
    public int completionQuantity;
    public int completionLimit;
    public int completionCurrencyID;
    public int specificQuantity;
    public int specificLimit;
    public int overallQuantity;
    public int overallLimit;
    public int purseWeeklyQuantity;
    public int purseWeeklyLimit;
    public int purseQuantity;
    public int purseLimit;
    public int quantity;
    public int completedMask;
    public int encounterMask;
    public boolean firstReward;
    public boolean shortageEligible;
    public lfgPlayerQuestReward rewards = new lfgPlayerQuestReward();
    public ArrayList<LfgPlayerQuestReward> shortageReward = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(slot);
        data.writeInt32(completionQuantity);
        data.writeInt32(completionLimit);
        data.writeInt32(completionCurrencyID);
        data.writeInt32(specificQuantity);
        data.writeInt32(specificLimit);
        data.writeInt32(overallQuantity);
        data.writeInt32(overallLimit);
        data.writeInt32(purseWeeklyQuantity);
        data.writeInt32(purseWeeklyLimit);
        data.writeInt32(purseQuantity);
        data.writeInt32(purseLimit);
        data.writeInt32(quantity);
        data.writeInt32(completedMask);
        data.writeInt32(encounterMask);
        data.writeInt32(shortageReward.size());
        data.writeBit(firstReward);
        data.writeBit(shortageEligible);
        data.flushBits();

        rewards.write(data);

        for (var shortageReward : shortageReward) {
            shortageReward.write(data);
        }
    }
}
