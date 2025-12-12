package com.github.azeroth.game.domain.misc;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WaypointPathFlag implements EnumFlag.FlagValue {
    None(0x00),
    FollowPathBackwardsFromEndToStart(0x01),
    ExactSplinePath                     (0x02); // Points are going to be merged into single packets and pathfinding is disabled
    public static final WaypointPathFlag FlyingPath = ExactSplinePath;   // flying paths are always exact splines
    private final int value;
}
