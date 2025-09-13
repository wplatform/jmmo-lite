package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

public class NearestAttackableUnitInObjectRangeCheck implements ICheck<unit> {
    private final WorldObject obj;
    private final Unit funit;
    private float range;

    public NearestAttackableUnitInObjectRangeCheck(WorldObject obj, Unit funit, float range) {
        obj = obj;
        funit = funit;
        range = range;
    }

    public final boolean invoke(Unit u) {
        if (u.isTargetableForAttack() && obj.isWithinDist(u, range) && (funit.isInCombatWith(u) || funit.isHostileTo(u)) && obj.canSeeOrDetect(u)) {
            range = obj.getDistance(u); // use found unit range as new range limit for next check

            return true;
        }

        return false;
    }
}
