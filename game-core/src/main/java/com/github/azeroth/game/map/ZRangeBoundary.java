package com.github.azeroth.game.map;

public class ZRangeBoundary extends AreaBoundary {
    private final float minZ;
    private final float maxZ;


    public ZRangeBoundary(float minZ, float maxZ) {
        this(minZ, maxZ, false);
    }

    public ZRangeBoundary(float minZ, float maxZ, boolean isInverted) {
        super(isInverted);
        minZ = minZ;
        maxZ = maxZ;
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        return (minZ <= pos.getZ() && pos.getZ() <= maxZ);
    }
}
