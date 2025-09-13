package com.github.azeroth.game.battlepay;

public enum BpayDistributionStatus {
    NONE(0),
    AVAILABLE(1),
    ADD_TO_PROCESS(2),
    PROCESS_COMPLETE(3),
    FINISHED(4);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, BpayDistributionStatus> mappings;
    private int intValue;

    private BpayDistributionStatus(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, BpayDistributionStatus> getMappings() {
        if (mappings == null) {
            synchronized (BpayDistributionStatus.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, BpayDistributionStatus>();
                }
            }
        }
        return mappings;
    }

    public static BpayDistributionStatus forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
