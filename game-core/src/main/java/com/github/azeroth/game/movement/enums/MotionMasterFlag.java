package com.github.azeroth.game.movement.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;

@Getter
public enum MotionMasterFlag implements EnumFlag.FlagValue {
    NONE(0x0),
    UPDATE(0x1), // Update in progress
    STATIC_INITIALIZATION_PENDING(0x2), // Static movement (MOTION_SLOT_DEFAULT) hasn't been initialized
    INITIALIZATION_PENDING(0x4), // MotionMaster is stalled until signaled
    INITIALIZING(0x8), // MotionMaster is initializing
    DELAYED(UPDATE.value | INITIALIZATION_PENDING.value);

    private final byte value;

    MotionMasterFlag(int value) {
        this.value = (byte) value;
    }
}
