package com.github.azeroth.game.movement.enums;


public enum NavArea {
    empty(0),
    MagmaSlime(8), // don't need to differentiate between them
    Water(9),
    GroundSteep(10),
    ground(11),
    maxValue(11),
    MinValue(8),

    AllMask(0x3F); // max allowed second
    // areas 1-60 will be used for destructible areas (currently skipped in vmaps, WMO with flag 1)
    // ground is the highest second to make recast choose ground over water when merging surfaces very close to each other (shallow water would be walkable)

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, NavArea> mappings;
    private int intValue;

    private NavArea(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, NavArea> getMappings() {
        if (mappings == null) {
            synchronized (NavArea.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, NavArea>();
                }
            }
        }
        return mappings;
    }

    public static NavArea forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
