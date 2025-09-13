package com.github.azeroth.game.dungeonfinding;


public final class LfgWaitTime {
    public int time;
    public int number;

    public LfgWaitTime clone() {
        LfgWaitTime varCopy = new LfgWaitTime();

        varCopy.time = this.time;
        varCopy.number = this.number;

        return varCopy;
    }
}
