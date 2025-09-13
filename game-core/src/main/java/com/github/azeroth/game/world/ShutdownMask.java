package com.github.azeroth.game.world;

public class ShutdownMask {
    public static final shutdownMask RESTART = new shutdownMask(1);
    public static final ShutdownMask IDLE = new shutdownMask(2);
    public static final ShutdownMask FORCE = new shutdownMask(4);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ShutdownMask> mappings;
    private int intValue;

    private shutdownMask(int value) {
        intValue = value;
        synchronized (ShutdownMask.class) {
            getMappings().put(value, this);
        }
    }

    private static java.util.HashMap<Integer, ShutdownMask> getMappings() {
        if (mappings == null) {
            synchronized (ShutdownMask.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ShutdownMask>();
                }
            }
        }
        return mappings;
    }

    public static ShutdownMask forValue(int value) {
        synchronized (ShutdownMask.class) {
            ShutdownMask enumObj = getMappings().get(value);
            if (enumObj == null) {
                return new shutdownMask(value);
            } else {
                return enumObj;
            }
        }
    }

    public int getValue() {
        return intValue;
    }
}
