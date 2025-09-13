package com.github.azeroth.game.pvp;


enum OutdoorPvPHPTowerType {
    BrokenHill(0),
    Overlook(1),
    Stadium(2),
    Num(3);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, OutdoorPvPHPTowerType> mappings;
    private int intValue;

    private OutdoorPvPHPTowerType(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, OutdoorPvPHPTowerType> getMappings() {
        if (mappings == null) {
            synchronized (OutdoorPvPHPTowerType.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, OutdoorPvPHPTowerType>();
                }
            }
        }
        return mappings;
    }

    public static OutdoorPvPHPTowerType forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
