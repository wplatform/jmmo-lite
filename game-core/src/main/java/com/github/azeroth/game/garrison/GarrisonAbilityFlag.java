package com.github.azeroth.game.garrison;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GarrisonAbilityFlag {

    FLAG_TRAIT                         (0x0001),
    CANNOT_ROLL                        (0x0002),
    HORDE_ONLY                         (0x0004),
    ALLIANCE_ONLY                      (0x0008),
    FLAG_CANNOT_REMOVE                 (0x0010),
    FLAG_EXCLUSIVE                     (0x0020),
    FLAG_SINGLE_MISSION_DURATION       (0x0040),
    FLAG_ACTIVE_ONLY_ON_ZONE_SUPPORT   (0x0080),
    FLAG_APPLY_TO_FIRST_MISSION        (0x0100),
    FLAG_IS_SPECIALIZATION             (0x0200),
    FLAG_IS_EMPTY_SLOT                 (0x0400);

    public final int value;
}
