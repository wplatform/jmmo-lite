package com.github.azeroth.game.map;

class BoundaryUnionBoundary extends AreaBoundary {
    private final AreaBoundary b1;
    private final AreaBoundary b2;


    public BoundaryUnionBoundary(AreaBoundary b1, AreaBoundary b2) {
        this(b1, b2, false);
    }

    public BoundaryUnionBoundary(AreaBoundary b1, AreaBoundary b2, boolean isInverted) {
        super(isInverted);
        b1 = b1;
        b2 = b2;
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        return b1.isWithinBoundary(pos) || b2.isWithinBoundary(pos);
    }
}
