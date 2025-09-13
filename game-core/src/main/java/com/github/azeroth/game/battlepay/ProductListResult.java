package com.github.azeroth.game.battlepay;

public enum ProductListResult {
    Available(0),
    LockUnk1(1),
    LockUnk2(2),
    RegionLocked(3);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, ProductListResult> mappings;
    private int intValue;

    private ProductListResult(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, ProductListResult> getMappings() {
        if (mappings == null) {
            synchronized (ProductListResult.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, ProductListResult>();
                }
            }
        }
        return mappings;
    }

    public static ProductListResult forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
