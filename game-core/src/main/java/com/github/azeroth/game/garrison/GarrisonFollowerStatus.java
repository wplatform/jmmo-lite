package com.github.azeroth.game.garrison;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum GarrisonFollowerStatus {
    FOLLOWER_STATUS_FAVORITE    (0x01),
    FOLLOWER_STATUS_EXHAUSTED   (0x02),
    FOLLOWER_STATUS_INACTIVE    (0x04),
    FOLLOWER_STATUS_TROOP       (0x08),
    FOLLOWER_STATUS_NO_XP_GAIN  (0x10);

    public final int value;
}
