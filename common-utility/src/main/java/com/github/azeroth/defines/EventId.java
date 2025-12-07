package com.github.azeroth.defines;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EventId {
    CHARGE(1003),
    JUMP(1004),

    /// Special charge event which is used for charge spells that have explicit targets
    /// and had a path already generated - using it in PointMovementGenerator will not
    /// create a new spline and launch it
    CHARGE_PREPATH(1005),

    FACE(1006),
    VEHICLE_BOARD(1007),
    VEHICLE_EXIT(1008),
    ASSIST_MOVE(1009);

    public final int value;
}
