package com.github.azeroth.game.networking.packet.lfg;

public enum RideType {
    NONE(0),
    Battlegrounds(1),
    Lfg(2);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, RideType> mappings;
    private int intValue;

    private RideType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, RideType> getMappings() {
        if (mappings == null) {
            synchronized (RideType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, RideType>();
                }
            }
        }
        return mappings;
    }

    public static RideType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
