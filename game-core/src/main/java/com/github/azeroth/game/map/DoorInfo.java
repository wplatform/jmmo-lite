package com.github.azeroth.game.map;


class DoorInfo {
    private bossInfo bossInfo;
    private Doortype type = DoorType.values()[0];

    public DoorInfo(BossInfo bossInfo, DoorType doorType) {
        setBossInfo(bossInfo);
        setType(doorType);
    }

    public final BossInfo getBossInfo() {
        return bossInfo;
    }

    public final void setBossInfo(BossInfo value) {
        bossInfo = value;
    }

    public final DoorType getType() {
        return type;
    }

    public final void setType(DoorType value) {
        type = value;
    }
}
