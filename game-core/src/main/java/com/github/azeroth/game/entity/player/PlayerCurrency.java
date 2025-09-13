package com.github.azeroth.game.entity.player;


public class PlayerCurrency {
    private PlayerCurrencystate state = PlayerCurrencyState.values()[0];
    private int quantity;
    private int weeklyQuantity;
    private int trackedQuantity;
    private int increasedCapQuantity;
    private int earnedQuantity;
    private CurrencyDbflags flags = CurrencyDbFlags.values()[0];

    public final PlayerCurrencyState getState() {
        return state;
    }

    public final void setState(PlayerCurrencyState value) {
        state = value;
    }

    public final int getQuantity() {
        return quantity;
    }

    public final void setQuantity(int value) {
        quantity = value;
    }

    public final int getWeeklyQuantity() {
        return weeklyQuantity;
    }

    public final void setWeeklyQuantity(int value) {
        weeklyQuantity = value;
    }

    public final int getTrackedQuantity() {
        return trackedQuantity;
    }

    public final void setTrackedQuantity(int value) {
        trackedQuantity = value;
    }

    public final int getIncreasedCapQuantity() {
        return increasedCapQuantity;
    }

    public final void setIncreasedCapQuantity(int value) {
        increasedCapQuantity = value;
    }

    public final int getEarnedQuantity() {
        return earnedQuantity;
    }

    public final void setEarnedQuantity(int value) {
        earnedQuantity = value;
    }

    public final CurrencyDbFlags getFlags() {
        return flags;
    }

    public final void setFlags(CurrencyDbFlags value) {
        flags = value;
    }
}
