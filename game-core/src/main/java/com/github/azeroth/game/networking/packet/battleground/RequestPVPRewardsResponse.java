package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.ServerPacket;

public class RequestPVPRewardsResponse extends ServerPacket {

    public int ratedRewardPointsThisWeek;

    public int arenaRewardPointsThisWeek;

    public int ratedMaxRewardPointsThisWeek;

    public int arenaRewardPoints;

    public int randomRewardPointsThisWeek;

    public int arenaMaxRewardPointsThisWeek;

    public int ratedRewardPoints;

    public int maxRewardPointsThisWeek;

    public int rewardPointsThisWeek;

    public int randomMaxRewardPointsThisWeek;

    public RequestPVPRewardsResponse() {
        super(ServerOpcode.RequestPvpRewardsResponse);
    }

    @Override
    public void write() {
        throw new UnsupportedOperationException();
    }
}
