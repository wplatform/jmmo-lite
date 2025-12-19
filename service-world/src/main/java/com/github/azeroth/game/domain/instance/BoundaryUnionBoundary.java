package com.github.azeroth.game.domain.instance;

import com.github.azeroth.game.domain.object.Position;
import lombok.Getter;

@Getter
public class BoundaryUnionBoundary extends AreaBoundary {
    private final AreaBoundary b1;
    private final AreaBoundary b2;


    public BoundaryUnionBoundary(AreaBoundary b1, AreaBoundary b2, boolean isInverted) {
        super(isInverted);
        this.b1 = b1;
        this.b2 = b2;
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        return b1.isWithinBoundary(pos) || b2.isWithinBoundary(pos);
    }
}
