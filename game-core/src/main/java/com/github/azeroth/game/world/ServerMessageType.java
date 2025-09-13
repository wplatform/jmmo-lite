package com.github.azeroth.game.world;

public enum ServerMessageType {
    ShutdownTime(1),
    RestartTime(2),
    String(3),
    ShutdownCancelled(4),
    RestartCancelled(5),
    BgShutdownTime(6),
    BgRestartTime(7),
    InstanceShutdownTime(8),
    InstanceRestartTime(9),
    ContentReady(10),
    TicketServicedSoon(11),
    WaitTimeUnavailable(12),
    TicketWaitTime(13);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ServerMessageType> mappings;
    private int intValue;

    private ServerMessageType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ServerMessageType> getMappings() {
        if (mappings == null) {
            synchronized (ServerMessageType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ServerMessageType>();
                }
            }
        }
        return mappings;
    }

    public static ServerMessageType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
