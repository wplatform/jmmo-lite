package com.github.azeroth.game.entity.creature;

import Time.GameTime;

public class VendorItemCount {
    private int itemId;
    private int count;
    private long lastIncrementTime;

    public VendorItemCount(int item, int count) {
        setItemId(item);
        setCount(count);
        setLastIncrementTime(GameTime.getGameTime());
    }

    public final int getItemId() {
        return itemId;
    }

    public final void setItemId(int value) {
        itemId = value;
    }

    public final int getCount() {
        return count;
    }

    public final void setCount(int value) {
        count = value;
    }

    public final long getLastIncrementTime() {
        return lastIncrementTime;
    }

    public final void setLastIncrementTime(long value) {
        lastIncrementTime = value;
    }
}
