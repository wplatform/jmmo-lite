package com.github.azeroth.game.map;

import com.github.azeroth.game.domain.object.Position;

public class AreaBoundary {
    private final boolean isInvertedBoundary;

    public AreaBoundary(boolean isInverted) {
        isInvertedBoundary = isInverted;
    }

    public final boolean isWithinBoundary(Position pos) {
        return pos != null && (isWithinBoundaryArea(pos) != isInvertedBoundary);
    }

    public boolean isWithinBoundaryArea(Position pos) {
        return false;
    }

    public static class DoublePosition extends Position {
        private final double doublePosX;
        private final double doublePosY;
        private final double doublePosZ;


        public DoublePosition(double x, double y, double z) {
            this(x, y, z, 0f);
        }

        public DoublePosition(double x, double y) {
            this(x, y, 0.0, 0f);
        }

        public DoublePosition(double x) {
            this(x, 0.0, 0.0, 0f);
        }

        public DoublePosition() {
            this(0.0, 0.0, 0.0, 0f);
        }

        public DoublePosition(double x, double y, double z, float o) {
            super((float) x, (float) y, (float) z, o);
            doublePosX = x;
            doublePosY = y;
            doublePosZ = z;
        }


        public DoublePosition(float x, float y, float z) {
            this(x, y, z, 0f);
        }

        public DoublePosition(float x, float y) {
            this(x, y, 0f, 0f);
        }

        public DoublePosition(float x) {
            this(x, 0f, 0f, 0f);
        }

        public DoublePosition(float x, float y, float z, float o) {
            super(x, y, z, o);
            doublePosX = x;
            doublePosY = y;
            doublePosZ = z;
        }

        public DoublePosition(Position pos) {
            this(pos.getX(), pos.getY(), pos.getZ(), pos.getO());
        }

        public final double getDoublePositionX() {
            return doublePosX;
        }

        public final double getDoublePositionY() {
            return doublePosY;
        }

        public final double getDoubleExactDist2dSq(DoublePosition pos) {
            var offX = getDoublePositionX() - pos.getDoublePositionX();
            var offY = getDoublePositionY() - pos.getDoublePositionY();

            return (offX * offX) + (offY * offY);
        }
    }
}
