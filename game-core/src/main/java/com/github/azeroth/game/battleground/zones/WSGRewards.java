package com.github.azeroth.game.battleground.zones;



///#region Constants

enum WSGRewards {
    Win(0),
    FlapCap(1),
    MapComplete(2),
    RewardNum(3);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, WSGRewards> mappings;
    private int intValue;

    private WSGRewards(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, WSGRewards> getMappings() {
        if (mappings == null) {
            synchronized (WSGRewards.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, WSGRewards>();
                }
            }
        }
        return mappings;
    }

    public static WSGRewards forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
