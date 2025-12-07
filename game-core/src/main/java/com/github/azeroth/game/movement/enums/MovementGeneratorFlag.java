package com.github.azeroth.game.movement.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MovementGeneratorFlag implements EnumFlag.FlagValue {
    NONE(0x000),
    INITIALIZATION_PENDING(0x001),
    INITIALIZED(0x002),
    SPEED_UPDATE_PENDING(0x004),
    INTERRUPTED(0x008),
    PAUSED(0x010),
    TIMED_PAUSED(0x020),
    DEACTIVATED(0x040),
    INFORM_ENABLED(0x080),
    FINALIZED(0x100),
    PERSIST_ON_DEATH(0x200),

    TRANSITORY(SPEED_UPDATE_PENDING.value | INTERRUPTED.value);

    public final int value;
}
