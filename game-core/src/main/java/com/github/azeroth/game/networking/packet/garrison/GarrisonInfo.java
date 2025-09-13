package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonInfo {
    public GarrisonType garrTypeID = GarrisonType.values()[0];
    public int garrSiteID;
    public int garrSiteLevelID;
    public int numFollowerActivationsRemaining;
    public int numMissionsStartedToday; // might mean something else, but sending 0 here enables follower abilities "Increase success chance of the first mission of the day by %."
    public int minAutoTroopLevel;
    public ArrayList<GarrisonPlotInfo> plots = new ArrayList<>();
    public ArrayList<GarrisonBuildingInfo> buildings = new ArrayList<>();
    public ArrayList<GarrisonFollower> followers = new ArrayList<>();
    public ArrayList<GarrisonFollower> autoTroops = new ArrayList<>();
    public ArrayList<GarrisonMission> missions = new ArrayList<>();
    public ArrayList<ArrayList<GarrisonMissionReward>> missionRewards = new ArrayList<ArrayList<GarrisonMissionReward>>();
    public ArrayList<ArrayList<GarrisonMissionReward>> missionOvermaxRewards = new ArrayList<ArrayList<GarrisonMissionReward>>();
    public ArrayList<GarrisonMissionBonusAbility> missionAreaBonuses = new ArrayList<>();
    public ArrayList<GarrisonTalent> talents = new ArrayList<>();
    public ArrayList<GarrisonCollection> collections = new ArrayList<>();
    public ArrayList<GarrisonEventList> eventLists = new ArrayList<>();
    public ArrayList<GarrisonSpecGroup> specGroups = new ArrayList<>();
    public ArrayList<Boolean> canStartMission = new ArrayList<>();
    public ArrayList<Integer> archivedMissions = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32((int) garrTypeID.getValue());
        data.writeInt32(garrSiteID);
        data.writeInt32(garrSiteLevelID);
        data.writeInt32(buildings.size());
        data.writeInt32(plots.size());
        data.writeInt32(followers.size());
        data.writeInt32(autoTroops.size());
        data.writeInt32(missions.size());
        data.writeInt32(missionRewards.size());
        data.writeInt32(missionOvermaxRewards.size());
        data.writeInt32(missionAreaBonuses.size());
        data.writeInt32(talents.size());
        data.writeInt32(collections.size());
        data.writeInt32(eventLists.size());
        data.writeInt32(specGroups.size());
        data.writeInt32(canStartMission.size());
        data.writeInt32(archivedMissions.size());
        data.writeInt32(numFollowerActivationsRemaining);
        data.writeInt32(numMissionsStartedToday);
        data.writeInt32(minAutoTroopLevel);

        for (var plot : plots) {
            plot.write(data);
        }

        for (var mission : missions) {
            mission.write(data);
        }

        for (var missionReward : missionRewards) {
            data.writeInt32(missionReward.size());
        }

        for (var missionReward : missionOvermaxRewards) {
            data.writeInt32(missionReward.size());
        }

        for (var areaBonus : missionAreaBonuses) {
            areaBonus.write(data);
        }

        for (var collection : collections) {
            collection.write(data);
        }

        for (var eventList : eventLists) {
            eventList.write(data);
        }

        for (var specGroup : specGroups) {
            specGroup.write(data);
        }

        for (var id : archivedMissions) {
            data.writeInt32(id);
        }

        for (var building : buildings) {
            building.write(data);
        }

        for (var canStartMission : canStartMission) {
            data.writeBit(canStartMission);
        }

        data.flushBits();

        for (var follower : followers) {
            follower.write(data);
        }

        for (var follower : autoTroops) {
            follower.write(data);
        }

        for (var talent : talents) {
            talent.write(data);
        }

        for (var missionReward : missionRewards) {
            for (var missionRewardItem : missionReward) {
                missionRewardItem.write(data);
            }
        }

        for (var missionReward : missionOvermaxRewards) {
            for (var missionRewardItem : missionReward) {
                missionRewardItem.write(data);
            }
        }
    }
}
