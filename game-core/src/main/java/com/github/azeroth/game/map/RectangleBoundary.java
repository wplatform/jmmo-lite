package com.github.azeroth.game.map;

public class RectangleBoundary extends AreaBoundary {
    private final float minX;
    private final float maxX;
    private final float minY;

    private final float maxY;

    // X axis is north/south, Y axis is east/west, larger values are northwest

    public RectangleBoundary(float southX, float northX, float eastY, float westY) {
        this(southX, northX, eastY, westY, false);
    }

    public RectangleBoundary(float southX, float northX, float eastY, float westY, boolean isInverted) {
        super(isInverted);
        minX = southX;
        maxX = northX;
        minY = eastY;
        maxY = westY;
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        return !(pos.getX() < minX || pos.getX() > maxX || pos.getY() < minY || pos.getY() > maxY);
    }
}
