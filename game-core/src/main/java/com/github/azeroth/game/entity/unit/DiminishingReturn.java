package com.github.azeroth.game.entity.unit;


public final class DiminishingReturn {
    public int stack;
    public int hitTime;
    public DiminishingLevels hitCount = DiminishingLevels.values()[0];

    public DiminishingReturn() {
    }
    public DiminishingReturn(int hitTime, DiminishingLevels hitCount) {
        stack = 0;
        hitTime = hitTime;
        hitCount = hitCount;
    }

    public void clear() {
        stack = 0;
        hitTime = 0;
        hitCount = DiminishingLevels.Level1;
    }

    public DiminishingReturn clone() {
        DiminishingReturn varCopy = new DiminishingReturn();

        varCopy.stack = this.stack;
        varCopy.hitTime = this.hitTime;
        varCopy.hitCount = this.hitCount;

        return varCopy;
    }
}
