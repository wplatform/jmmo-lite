package com.github.azeroth.game.map;

public class ParallelogramBoundary extends AreaBoundary {
    private final DoublePosition a;
    private final DoublePosition b;
    private final DoublePosition d;
    private final DoublePosition c;
    private final double abx;
    private final double dax;
    private final double aby;

    private final double day;

    // Note: AB must be orthogonal to AD

    public ParallelogramBoundary(Position cornerA, Position cornerB, Position cornerD) {
        this(cornerA, cornerB, cornerD, false);
    }

    public ParallelogramBoundary(Position cornerA, Position cornerB, Position cornerD, boolean isInverted) {
        super(isInverted);
        a = new DoublePosition(cornerA);
        b = new DoublePosition(cornerB);
        d = new DoublePosition(cornerD);
        c = new DoublePosition(d.getDoublePositionX() + (b.getDoublePositionX() - a.getDoublePositionX()), d.getDoublePositionY() + (b.getDoublePositionY() - a.getDoublePositionY()));
        abx = b.getDoublePositionX() - a.getDoublePositionX();
        dax = a.getDoublePositionX() - d.getDoublePositionX();
        aby = b.getDoublePositionY() - a.getDoublePositionY();
        day = a.getDoublePositionY() - d.getDoublePositionY();
    }

    @Override
    public boolean isWithinBoundaryArea(Position pos) {
        // half-plane signs
        var sign1 = ((-_b.getDoublePositionX() + pos.getX()) * _aby - (-_b.getDoublePositionY() + pos.getY()) * abx) < 0;
        var sign2 = ((-_a.getDoublePositionX() + pos.getX()) * _day - (-_a.getDoublePositionY() + pos.getY()) * dax) < 0;
        var sign3 = ((-_d.getDoublePositionY() + pos.getY()) * _abx - (-_d.getDoublePositionX() + pos.getX()) * aby) < 0; // AB = -CD
        var sign4 = ((-_c.getDoublePositionY() + pos.getY()) * _dax - (-_c.getDoublePositionX() + pos.getX()) * day) < 0; // DA = -BC

        // if all signs are equal, the point is inside
        return ((sign1 == sign2) && (sign2 == sign3) && (sign3 == sign4));
    }
}
