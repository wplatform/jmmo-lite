package com.github.azeroth.game.battlepay;

public enum BpayUpdateStatus {
    Loading(9),
    Ready(6),
    finish(3);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, BpayUpdateStatus> mappings;
    private int intValue;

    private BpayUpdateStatus(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, BpayUpdateStatus> getMappings() {
        if (mappings == null) {
            synchronized (BpayUpdateStatus.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, BpayUpdateStatus>();
                }
            }
        }
        return mappings;
    }

    public static BpayUpdateStatus forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
