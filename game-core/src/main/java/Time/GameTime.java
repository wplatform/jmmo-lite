package Time;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class GameTime {
    private static final long StartTime = System.currentTimeMillis() / 1000;

    private static long GameTime = System.currentTimeMillis() / 1000;
    private static int GameMSTime = 0;

    private static ZonedDateTime GameTimeSystemPoint = ZonedDateTime.now(ZoneId.of("UTC"));
    private static ZonedDateTime GameTimeSteadyPoint = ZonedDateTime.now(ZoneId.of("UTC"));

    private static java.time.LocalDateTime DateTime;

    private static WowTime UtcWow = new WowTime();
    private static WowTime Wow = new WowTime();

    public static int getGameTimeMS() {
        return GameMSTime;
    }

    public static ZonedDateTime getSystemTime() {
        return GameTimeSystemPoint;
    }

    public static ZonedDateTime now() {
        return GameTimeSteadyPoint;
    }

    public static long getUptime() {
        return GameTime - StartTime;
    }

    public static java.time.LocalDateTime getDateAndTime() {
        return DateTime;
    }

    public static WowTime getUtcWowTime() {
        return UtcWow;
    }

    public static WowTime getWowTime() {
        return Wow;
    }

    public static void updateGameTimers() {
        GameTime = System.currentTimeMillis() / 1000;
        GameMSTime = getMSTime();
        GameTimeSystemPoint = ZonedDateTime.now(ZoneId.of("UTC"));
        GameTimeSteadyPoint = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTime = java.time.LocalDateTime.now();
    }

    private static int getMSTime() {
        // Implement the method to return milliseconds since the epoch
        return (int) (System.currentTimeMillis() % 1000);
    }
}

