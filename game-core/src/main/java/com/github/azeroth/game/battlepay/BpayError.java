package com.github.azeroth.game.battlepay;

/**
 * Client error enum See Blizzard_StoreUISecure.lua Last update : 9.0.2 36474
 */
public enum BpayError {
    Ok(0),
    PurchaseDenied(1),
    PaymentFailed(2),
    other(3),
    WrongCurrency(12),
    BattlepayDisabled(13),
    InvalidPaymentMethod(25),
    InsufficientBalance(28),
    ParentalControlsNoPurchase(34),
    ConsumableTokenOwned(46),
    TooManyTokens(47);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, BpayError> mappings;
    private int intValue;

    private BpayError(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, BpayError> getMappings() {
        if (mappings == null) {
            synchronized (BpayError.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, BpayError>();
                }
            }
        }
        return mappings;
    }

    public static BpayError forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
