package com.github.azeroth.time;

import java.time.*;

public class GameTime {


    private static final Clock SYSTEM_CLOCK_AT_DEFAULT_ZONE = Clock.systemDefaultZone();
    private static final Instant START_TIME = SYSTEM_CLOCK_AT_DEFAULT_ZONE.instant();

    private static Instant NOW = START_TIME;
    private final static WowTime UTC_WOW_TIME = new WowTime();
    private final static WowTime WOW_TIME = new WowTime();

    public static ZonedDateTime getDateAndTime() {
        return NOW.atZone(ZoneId.systemDefault());
    }

    public static Instant getTime() {
        return now();
    }

    public static Instant now() {
        return NOW;
    }

    public static long getGameTime() {
        return now().getEpochSecond();
    }

    public static long getSystemTime() {
        return getGameTime();
    }

    public static int getGameTimeMS() {
        Duration duration = Duration.between(START_TIME, SYSTEM_CLOCK_AT_DEFAULT_ZONE.instant());
        return (int) duration.toMillis();
    }

    public static long getUptime() {
        return now().getEpochSecond() - START_TIME.getEpochSecond();
    }


    public static WowTime getUtcWowTime() {
        return UTC_WOW_TIME;
    }

    public static WowTime getWowTime() {
        return WOW_TIME;
    }

    public static void updateGameTimers() {
        NOW = SYSTEM_CLOCK_AT_DEFAULT_ZONE.instant();
        UTC_WOW_TIME.setDateTime(NOW, ZoneId.of("UTC"));
        WOW_TIME.setDateTime(NOW, ZoneId.systemDefault());
    }

}

