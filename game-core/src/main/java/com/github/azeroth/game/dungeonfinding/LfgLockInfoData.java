package com.github.azeroth.game.dungeonfinding;


public class LfgLockInfoData {
    public LfgLockStatusType lockStatus = LfgLockStatusType.values()[0];
    public short requiredItemLevel;
    public float currentItemLevel;


    public LfgLockInfoData(LfgLockStatusType lockStatus, short requiredItemLevel) {
        this(lockStatus, requiredItemLevel, 0);
    }

    public LfgLockInfoData(LfgLockStatusType lockStatus) {
        this(lockStatus, 0, 0);
    }

    public LfgLockInfoData() {
        this(0, 0, 0);
    }

    public LfgLockInfoData(LfgLockStatusType lockStatus, short requiredItemLevel, float currentItemLevel) {
        lockStatus = lockStatus;
        requiredItemLevel = requiredItemLevel;
        currentItemLevel = currentItemLevel;
    }
}
