package com.github.azeroth.game.movement;


public final class ChaseRange {
    // this contains info that informs how we should path!
    public float minRange; // we have to move if we are within this range...    (min. attack range)
    public float minTolerance; // ...and if we are, we will move this far away
    public float maxRange; // we have to move if we are outside this range...   (max. attack range)
    public float maxTolerance; // ...and if we are, we will move into this range

    public ChaseRange() {
    }

    public ChaseRange(float range) {
        minRange = range > ObjectDefine.CONTACT_DISTANCE ? 0 : range - SharedConst.contactDistance;
        minTolerance = range;
        maxRange = range + SharedConst.contactDistance;
        maxTolerance = range;
    }

    public ChaseRange(float min, float max) {
        minRange = min;
        minTolerance = Math.min(min + SharedConst.contactDistance, (min + max) / 2);
        maxRange = max;
        maxTolerance = Math.max(max - SharedConst.contactDistance, minTolerance);
    }

    public ChaseRange(float min, float tMin, float tMax, float max) {
        minRange = min;
        minTolerance = tMin;
        maxRange = max;
        maxTolerance = tMax;
    }

    public ChaseRange clone() {
        ChaseRange varCopy = new ChaseRange();

        varCopy.minRange = this.minRange;
        varCopy.minTolerance = this.minTolerance;
        varCopy.maxRange = this.maxRange;
        varCopy.maxTolerance = this.maxTolerance;

        return varCopy;
    }
}
