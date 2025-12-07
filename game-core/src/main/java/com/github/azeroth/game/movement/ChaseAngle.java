package com.github.azeroth.game.movement;


import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.utils.MathUtil;

public final class ChaseAngle {
    public float relativeAngle; // we want to be at this angle relative to the target (0 = front, M_PI = back)
    public float tolerance; // but we'll tolerate anything within +- this much


    public ChaseAngle(float angle) {
        this(angle, MathUtil.PI_OVER_4);
    }

    public ChaseAngle() {
    }

    public ChaseAngle(float angle, float tol) {
        relativeAngle = Position.normalizeOrientation(angle);
        tolerance = tol;
    }

    public float upperBound() {
        return Position.normalizeOrientation(relativeAngle + tolerance);
    }

    public float lowerBound() {
        return Position.normalizeOrientation(relativeAngle - tolerance);
    }

    public boolean isAngleOkay(float relAngle) {
        var diff = Math.abs(relAngle - relativeAngle);
        return (Math.min(diff, (2 * (float) Math.PI) - diff) <= tolerance);
    }
}
