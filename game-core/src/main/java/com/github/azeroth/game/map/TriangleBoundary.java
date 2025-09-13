package com.github.azeroth.game.map;

public class TriangleBoundary extends AreaBoundary {
    private final DoublePosition a;
    private final DoublePosition b;
    private final DoublePosition c;
    private final double abx;
    private final double bcx;
    private final double cax;
    private final double aby;
    private final double bcy;
    private final double cay;


    public TriangleBoundary(Position pointA, Position pointB, Position pointC) {
        this(pointA, pointB, pointC, false);
    }

    public TriangleBoundary(Position pointA, Position pointB, Position pointC, boolean isInverted) {
        super(isInverted);
        a = new DoublePosition(pointA);
        b = new DoublePosition(pointB);
        c = new DoublePosition(pointC);

        abx = b.getDoublePositionX() - a.getDoublePositionX();
        bcx = c.getDoublePositionX() - b.getDoublePositionX();
        cax = a.getDoublePositionX() - c.getDoublePositionX();
        aby = b.getDoublePositionY() - a.getDoublePositionY();
        bcy = c.getDoublePositionY() - b.getDoublePositionY();
        cay = a.getDoublePositionY() - c.getDoublePositionY();
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        // half-plane signs
        var sign1 = ((-_b.getDoublePositionX() + pos.getX()) * _aby - (-_b.getDoublePositionY() + pos.getY()) * abx) < 0;
        var sign2 = ((-_c.getDoublePositionX() + pos.getX()) * _bcy - (-_c.getDoublePositionY() + pos.getY()) * bcx) < 0;
        var sign3 = ((-_a.getDoublePositionX() + pos.getX()) * _cay - (-_a.getDoublePositionY() + pos.getY()) * cax) < 0;

        // if all signs are the same, the point is inside the triangle
        return ((sign1 == sign2) && (sign2 == sign3));
    }
}
