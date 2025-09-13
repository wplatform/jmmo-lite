package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

class LFGPlayerReward extends ServerPacket {

    public int queuedSlot;

    public int actualSlot;

    public int rewardMoney;

    public int addedXP;
    public ArrayList<LFGPlayerrewards> rewards = new ArrayList<>();

    public LFGPlayerReward() {
        super(ServerOpcode.LfgPlayerReward);
    }

    @Override
    public void write() {
        this.writeInt32(queuedSlot);
        this.writeInt32(actualSlot);
        this.writeInt32(rewardMoney);
        this.writeInt32(addedXP);
        this.writeInt32(rewards.size());

        for (var reward : rewards) {
            reward.write(this);
        }
    }
}
