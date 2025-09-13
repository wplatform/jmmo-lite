package com.github.azeroth.game.movement;


public final class ChaseAngle {
    public float relativeAngle; // we want to be at this angle relative to the target (0 = front, M_PI = back)
    public float tolerance; // but we'll tolerate anything within +- this much


    public chaseAngle(float angle) {
        this(angle, MathUtil.PiOver4);
    }

    public chaseAngle() {
    }

    public chaseAngle(float angle, float tol) {
        relativeAngle = position.normalizeOrientation(angle);
        tolerance = tol;
    }

    public float upperBound() {
        return position.normalizeOrientation(relativeAngle + tolerance);
    }

    public float lowerBound() {
        return position.normalizeOrientation(RelativeAngle - tolerance);
    }

    public boolean isAngleOkay(float relAngle) {
        var diff = Math.abs(relAngle - relativeAngle);

        return (Math.min(diff, (2 * (float) Math.PI) - diff) <= tolerance);
    }

    public ChaseAngle clone() {
        ChaseAngle varCopy = new chaseAngle();

        varCopy.relativeAngle = this.relativeAngle;
        varCopy.tolerance = this.tolerance;

        return varCopy;
    }
}
