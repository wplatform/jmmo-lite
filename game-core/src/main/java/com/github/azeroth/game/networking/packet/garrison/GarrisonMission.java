package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonMission {
    public long dbID;
    public int missionRecID;
    public long offerTime;
    public int offerDuration;
    public long startTime = 2288912640;
    public int travelDuration;
    public int missionDuration;
    public int missionState = 0;
    public int successChance = 0;
    public int flags = 0;
    public float missionScalar = 1.0f;
    public int contentTuningID = 0;
    public ArrayList<GarrisonEncounter> encounters = new ArrayList<>();
    public ArrayList<GarrisonMissionReward> rewards = new ArrayList<>();
    public ArrayList<GarrisonMissionReward> overmaxRewards = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt64(dbID);
        data.writeInt32(missionRecID);
        data.writeInt64(offerTime);
        data.writeInt32(offerDuration);
        data.writeInt64(startTime);
        data.writeInt32(travelDuration);
        data.writeInt32(missionDuration);
        data.writeInt32(missionState);
        data.writeInt32(successChance);
        data.writeInt32(flags);
        data.writeFloat(missionScalar);
        data.writeInt32(contentTuningID);
        data.writeInt32(encounters.size());
        data.writeInt32(rewards.size());
        data.writeInt32(overmaxRewards.size());

        for (var encounter : encounters) {
            encounter.write(data);
        }

        for (var missionRewardItem : rewards) {
            missionRewardItem.write(data);
        }

        for (var missionRewardItem : overmaxRewards) {
            missionRewardItem.write(data);
        }
    }
}
