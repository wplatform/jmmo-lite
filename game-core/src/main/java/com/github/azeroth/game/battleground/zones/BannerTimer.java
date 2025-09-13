package com.github.azeroth.game.battleground.zones;


final class BannerTimer {
    public int timer;
    public byte type;
    public byte teamIndex;

    public BannerTimer clone() {
        BannerTimer varCopy = new BannerTimer();

        varCopy.timer = this.timer;
        varCopy.type = this.type;
        varCopy.teamIndex = this.teamIndex;

        return varCopy;
    }
}
