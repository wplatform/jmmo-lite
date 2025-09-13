package com.github.azeroth.game.entity.player;

public class HeirloomData {
    private HeirloomPlayerFlags flags = HeirloomPlayerFlags.values()[0];

    private int bonusId;

    public HeirloomData(HeirloomPlayerFlag flags) {
        this(flags, 0);
    }

    public HeirloomData() {
        this(0, 0);
    }

    public HeirloomData(HeirloomPlayerFlag flags, int bonusId) {
        setFlags(flags);
        setBonusId(bonusId);
    }

    public final HeirloomPlayerFlag getFlags() {
        return flags;
    }

    public final void setFlags(HeirloomPlayerFlag value) {
        flags = value;
    }


    public final int getBonusId() {
        return bonusId;
    }


    public final void setBonusId(int value) {
        bonusId = value;
    }
}
