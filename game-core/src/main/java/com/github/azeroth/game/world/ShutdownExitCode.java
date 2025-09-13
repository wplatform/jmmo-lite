package com.github.azeroth.game.world;

public enum ShutdownExitCode {
    Shutdown(0),
    error(1),
    RESTART(2);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ShutdownExitCode> mappings;
    private int intValue;

    private ShutdownExitCode(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ShutdownExitCode> getMappings() {
        if (mappings == null) {
            synchronized (ShutdownExitCode.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ShutdownExitCode>();
                }
            }
        }
        return mappings;
    }

    public static ShutdownExitCode forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
