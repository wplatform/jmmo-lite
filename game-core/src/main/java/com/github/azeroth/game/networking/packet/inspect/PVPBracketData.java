package com.github.azeroth.game.networking.packet.inspect;

import com.github.azeroth.game.networking.WorldPacket;

public final class PVPBracketData {
    public int rating;
    public int rank;
    public int weeklyPlayed;
    public int weeklyWon;
    public int seasonPlayed;
    public int seasonWon;
    public int weeklyBestRating;
    public int seasonBestRating;
    public int pvpTierID;
    public int weeklyBestWinPvpTierID;
    public int unused1;
    public int unused2;
    public int unused3;
    public int roundsSeasonPlayed;
    public int roundsSeasonWon;
    public int roundsWeeklyPlayed;
    public int roundsWeeklyWon;
    public byte bracket;
    public boolean disqualified;

    public void write(WorldPacket data) {
        data.writeInt8(bracket);
        data.writeInt32(unused3);
        data.writeInt32(rating);
        data.writeInt32(rank);
        data.writeInt32(weeklyPlayed);
        data.writeInt32(weeklyWon);
        data.writeInt32(seasonPlayed);
        data.writeInt32(seasonWon);
        data.writeInt32(weeklyBestRating);
        data.writeInt32(seasonBestRating);
        data.writeInt32(pvpTierID);
        data.writeInt32(weeklyBestWinPvpTierID);
        data.writeInt32(unused1);
        data.writeInt32(unused2);
        data.writeInt32(roundsSeasonPlayed);
        data.writeInt32(roundsSeasonWon);
        data.writeInt32(roundsWeeklyPlayed);
        data.writeInt32(roundsWeeklyWon);
        data.writeBit(disqualified);
        data.flushBits();
    }

    public PVPBracketData clone() {
        PVPBracketData varCopy = new PVPBracketData();

        varCopy.rating = this.rating;
        varCopy.rank = this.rank;
        varCopy.weeklyPlayed = this.weeklyPlayed;
        varCopy.weeklyWon = this.weeklyWon;
        varCopy.seasonPlayed = this.seasonPlayed;
        varCopy.seasonWon = this.seasonWon;
        varCopy.weeklyBestRating = this.weeklyBestRating;
        varCopy.seasonBestRating = this.seasonBestRating;
        varCopy.pvpTierID = this.pvpTierID;
        varCopy.weeklyBestWinPvpTierID = this.weeklyBestWinPvpTierID;
        varCopy.unused1 = this.unused1;
        varCopy.unused2 = this.unused2;
        varCopy.unused3 = this.unused3;
        varCopy.roundsSeasonPlayed = this.roundsSeasonPlayed;
        varCopy.roundsSeasonWon = this.roundsSeasonWon;
        varCopy.roundsWeeklyPlayed = this.roundsWeeklyPlayed;
        varCopy.roundsWeeklyWon = this.roundsWeeklyWon;
        varCopy.bracket = this.bracket;
        varCopy.disqualified = this.disqualified;

        return varCopy;
    }
}
