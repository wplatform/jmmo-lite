package com.github.azeroth.defines;

public enum RollMask {
    PASS(0x01),
    NEED(0x02),
    GREED(0x04),
    DISENCHANT(0x08),
    TRANS_MOG(0x10),

    ROLL_ALL_TYPE_NO_DISENCHANT(0x07),
    ROLL_ALL_TYPE_MASK(0x0F);

    public final byte value;

    RollMask(int value) {
        this.value = (byte) value;
    }
}
