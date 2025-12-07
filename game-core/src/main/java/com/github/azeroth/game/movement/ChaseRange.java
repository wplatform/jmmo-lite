package com.github.azeroth.game.movement;



import static com.github.azeroth.game.domain.object.ObjectDefine.CONTACT_DISTANCE;

public final class ChaseRange {
    // this contains info that informs how we should path!
    public float minRange; // we have to move if we are within this range...    (min. attack range)
    public float minTolerance; // ...and if we are, we will move this far away
    public float maxRange; // we have to move if we are outside this range...   (max. attack range)
    public float maxTolerance; // ...and if we are, we will move into this range

    public ChaseRange() {
    }

    public ChaseRange(float range) {
        minRange = range > CONTACT_DISTANCE ? 0 : range - CONTACT_DISTANCE;
        minTolerance = range;
        maxRange = range + CONTACT_DISTANCE;
        maxTolerance = range;
    }

    public ChaseRange(float min, float max) {
        minRange = min;
        minTolerance = Math.min(min + CONTACT_DISTANCE, (min + max) / 2);
        maxRange = max;
        maxTolerance = Math.max(max - CONTACT_DISTANCE, minTolerance);
    }

    public ChaseRange(float min, float tMin, float tMax, float max) {
        minRange = min;
        minTolerance = tMin;
        maxRange = max;
        maxTolerance = tMax;
    }
}
