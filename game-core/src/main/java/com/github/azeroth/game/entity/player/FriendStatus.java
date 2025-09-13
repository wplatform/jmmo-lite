package com.github.azeroth.game.entity.player;

public enum FriendStatus {
    Offline(0x00),
    online(0x01),
    AFK(0x02),
    DND(0x04),
    RAF(0x08);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, FriendStatus> mappings;
    private int intValue;

    private FriendStatus(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, FriendStatus> getMappings() {
        if (mappings == null) {
            synchronized (FriendStatus.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, FriendStatus>();
                }
            }
        }
        return mappings;
    }

    public static FriendStatus forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
