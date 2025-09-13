package com.github.azeroth.game.entity.player;

public enum FriendsResult {
    DbError(0x00),
    ListFull(0x01),
    online(0x02),
    Offline(0x03),
    NotFound(0x04),
    removed(0x05),
    AddedOnline(0x06),
    AddedOffline(0x07),
    Already(0x08),
    Self(0x09),
    Enemy(0x0a),
    IgnoreFull(0x0b),
    IgnoreSelf(0x0c),
    IgnoreNotFound(0x0d),
    IgnoreAlready(0x0e),
    IgnoreAdded(0x0f),
    IgnoreRemoved(0x10),
    IgnoreAmbiguous(0x11), // That Name Is Ambiguous, Type More Of The Player'S Server Name
    MuteFull(0x12),
    MuteSelf(0x13),
    MuteNotFound(0x14),
    MuteAlready(0x15),
    MuteAdded(0x16),
    MuteRemoved(0x17),
    MuteAmbiguous(0x18), // That Name Is Ambiguous, Type More Of The Player'S Server Name
    unk1(0x19), // no message at client
    unk2(0x1A),
    unk3(0x1B),
    unknown(0x1C); // Unknown friend response from server

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, FriendsResult> mappings;
    private int intValue;

    private FriendsResult(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, FriendsResult> getMappings() {
        if (mappings == null) {
            synchronized (FriendsResult.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, FriendsResult>();
                }
            }
        }
        return mappings;
    }

    public static FriendsResult forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
