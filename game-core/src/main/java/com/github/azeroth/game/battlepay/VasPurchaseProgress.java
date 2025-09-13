package com.github.azeroth.game.battlepay;

public enum VasPurchaseProgress {
    Invalid(0),
    PrePurchase(1),
    PaymentPending(2),
    ApplyingLicense(3),
    WaitingOnQueue(4),
    Ready(5),
    ProcessingFactionChange(6),
    Complete(7);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, VasPurchaseProgress> mappings;
    private int intValue;

    private VasPurchaseProgress(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, VasPurchaseProgress> getMappings() {
        if (mappings == null) {
            synchronized (VasPurchaseProgress.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, VasPurchaseProgress>();
                }
            }
        }
        return mappings;
    }

    public static VasPurchaseProgress forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
