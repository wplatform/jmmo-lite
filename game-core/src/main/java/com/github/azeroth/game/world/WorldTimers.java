package com.github.azeroth.game.world;

/**
 * Timers for different object refresh rates
 */
public enum WorldTimers {
    auctions,
    AuctionsPending,
    UpTime,
    Corpses,
    events,
    CleanDB,
    AutoBroadcast,
    mailBox,
    DeleteChars,
    AhBot,
    PingDB,
    GuildSave,
    Blackmarket,
    WhoList,
    ChannelSave,
    max;

    public static final int SIZE = Integer.SIZE;

    public static WorldTimers forValue(int value) {
        return values()[value];
    }

    public int getValue() {
        return this.ordinal();
    }
}
