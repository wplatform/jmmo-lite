package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.WorldPacket;

final class BracketInfo {
    public int personalRating;
    public int ranking;
    public int seasonPlayed;
    public int seasonWon;
    public int unused1;
    public int unused2;
    public int weeklyPlayed;
    public int weeklyWon;
    public int roundsSeasonPlayed;
    public int roundsSeasonWon;
    public int roundsWeeklyPlayed;
    public int roundsWeeklyWon;
    public int bestWeeklyRating;
    public int lastWeeksBestRating;
    public int bestSeasonRating;
    public int pvpTierID;
    public int unused3;
    public int unused4;
    public int rank;
    public boolean disqualified;

    public void write(WorldPacket data) {
        data.writeInt32(personalRating);
        data.writeInt32(ranking);
        data.writeInt32(seasonPlayed);
        data.writeInt32(seasonWon);
        data.writeInt32(unused1);
        data.writeInt32(unused2);
        data.writeInt32(weeklyPlayed);
        data.writeInt32(weeklyWon);
        data.writeInt32(roundsSeasonPlayed);
        data.writeInt32(roundsSeasonWon);
        data.writeInt32(roundsWeeklyPlayed);
        data.writeInt32(roundsWeeklyWon);
        data.writeInt32(bestWeeklyRating);
        data.writeInt32(lastWeeksBestRating);
        data.writeInt32(bestSeasonRating);
        data.writeInt32(pvpTierID);
        data.writeInt32(unused3);
        data.writeInt32(unused4);
        data.writeInt32(rank);
        data.writeBit(disqualified);
        data.flushBits();
    }

    public BracketInfo clone() {
        BracketInfo varCopy = new BracketInfo();

        varCopy.personalRating = this.personalRating;
        varCopy.ranking = this.ranking;
        varCopy.seasonPlayed = this.seasonPlayed;
        varCopy.seasonWon = this.seasonWon;
        varCopy.unused1 = this.unused1;
        varCopy.unused2 = this.unused2;
        varCopy.weeklyPlayed = this.weeklyPlayed;
        varCopy.weeklyWon = this.weeklyWon;
        varCopy.roundsSeasonPlayed = this.roundsSeasonPlayed;
        varCopy.roundsSeasonWon = this.roundsSeasonWon;
        varCopy.roundsWeeklyPlayed = this.roundsWeeklyPlayed;
        varCopy.roundsWeeklyWon = this.roundsWeeklyWon;
        varCopy.bestWeeklyRating = this.bestWeeklyRating;
        varCopy.lastWeeksBestRating = this.lastWeeksBestRating;
        varCopy.bestSeasonRating = this.bestSeasonRating;
        varCopy.pvpTierID = this.pvpTierID;
        varCopy.unused3 = this.unused3;
        varCopy.unused4 = this.unused4;
        varCopy.rank = this.rank;
        varCopy.disqualified = this.disqualified;

        return varCopy;
    }
}
