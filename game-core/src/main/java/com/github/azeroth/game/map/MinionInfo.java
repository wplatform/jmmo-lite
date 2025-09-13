package com.github.azeroth.game.map;

class MinionInfo {
    private bossInfo bossInfo;

    public MinionInfo(BossInfo _bossInfo) {
        setBossInfo(_bossInfo);
    }

    public final BossInfo getBossInfo() {
        return bossInfo;
    }

    public final void setBossInfo(BossInfo value) {
        bossInfo = value;
    }
}
