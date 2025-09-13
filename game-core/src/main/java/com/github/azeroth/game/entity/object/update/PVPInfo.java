package com.github.azeroth.game.entity.object.update;

public final class PVPInfo extends UpdateMaskObject {

    private boolean disqualified;
    private byte bracket;
    private int pvpRatingID;
    private int weeklyPlayed;
    private int weeklyWon;
    private int seasonPlayed;
    private int seasonWon;
    private int rating;
    private int weeklyBestRating;
    private int seasonBestRating;
    private int pvpTierID;
    private int weeklyBestWinPvpTierID;
    private int field_28;
    private int field_2C;
    private int weeklyRoundsPlayed;
    private int weeklyRoundsWon;
    private int seasonRoundsPlayed;
    private int seasonRoundsWon;

    public PVPInfo() {
        super(19);
    }

    @Override
    public void clearChangesMask() {

    }
}
