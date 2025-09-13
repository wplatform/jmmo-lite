package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.ServerPacket;

public class SeasonInfo extends ServerPacket {
    public int mythicPlusDisplaySeasonID;
    public int mythicPlusMilestoneSeasonID;
    public int previousArenaSeason;
    public int currentArenaSeason;
    public int pvpSeasonID;
    public int conquestWeeklyProgressCurrencyID;
    public boolean weeklyRewardChestsEnabled;

    public SeasonInfo() {
        super(ServerOpcode.SeasonInfo);
    }

    @Override
    public void write() {
        this.writeInt32(mythicPlusDisplaySeasonID);
        this.writeInt32(mythicPlusMilestoneSeasonID);
        this.writeInt32(currentArenaSeason);
        this.writeInt32(previousArenaSeason);
        this.writeInt32(conquestWeeklyProgressCurrencyID);
        this.writeInt32(pvpSeasonID);
        this.writeBit(weeklyRewardChestsEnabled);
        this.flushBits();
    }
}

//Structs

