package com.github.azeroth.game.domain.creature;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor(staticName = "of")
public class NodeAndPathId {
    public final int nodeId;
    public final int pathId;
}
