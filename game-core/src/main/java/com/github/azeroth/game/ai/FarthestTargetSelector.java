package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.unit.Unit;

class FarthestTargetSelector implements ICheck<unit> {
    private final Unit me;
    private final float dist;
    private final boolean playerOnly;
    private final boolean inLos;

    public FarthestTargetSelector(Unit unit, float dist, boolean playerOnly, boolean inLos) {
        me = unit;
        dist = dist;
        playerOnly = playerOnly;
        inLos = inLos;
    }

    public final boolean invoke(Unit target) {
        if (me == null || target == null) {
            return false;
        }

        if (playerOnly && target.getObjectTypeId() != TypeId.PLAYER) {
            return false;
        }

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist)) {
            return false;
        }

        if (inLos && !me.isWithinLOSInMap(target)) {
            return false;
        }

        return true;
    }
}
