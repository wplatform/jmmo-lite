package com.github.azeroth.game.scenario;

public final class ScenarioPOIPoint {
    public int X;
    public int Y;
    public int Z;

    public ScenarioPOIPoint() {
    }

    public ScenarioPOIPoint(int x, int y, int z) {
        X = x;
        Y = y;
        Z = z;
    }

    public ScenarioPOIPoint clone() {
        ScenarioPOIPoint varCopy = new ScenarioPOIPoint();

        varCopy.X = this.X;
        varCopy.Y = this.Y;
        varCopy.Z = this.Z;

        return varCopy;
    }
}
