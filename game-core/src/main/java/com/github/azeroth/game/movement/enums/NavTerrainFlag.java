package com.github.azeroth.game.movement.enums;


public enum NavTerrainFlag {
    empty(0x00),
    ground(1 << (NavArea.MaxValue - NavArea.ground)),
    GroundSteep(1 << (NavArea.MaxValue - NavArea.GroundSteep)),
    Water(1 << (NavArea.MaxValue - NavArea.Water)),
    MagmaSlime(1 << (NavArea.MaxValue - NavArea.MagmaSlime));

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, NavTerrainFlag> mappings;
    private int intValue;

    private NavTerrainFlag(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, NavTerrainFlag> getMappings() {
        if (mappings == null) {
            synchronized (NavTerrainFlag.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, NavTerrainFlag>();
                }
            }
        }
        return mappings;
    }

    public static NavTerrainFlag forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
