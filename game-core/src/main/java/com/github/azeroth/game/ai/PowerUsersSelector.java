package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.unit.Unit;

class PowerUsersSelector implements ICheck<unit> {
    private final Unit me;
    private final Power power;
    private final float dist;
    private final boolean playerOnly;

    public PowerUsersSelector(Unit unit, Power power, float dist, boolean playerOnly) {
        me = unit;
        power = power;
        dist = dist;
        playerOnly = playerOnly;
    }

    public final boolean invoke(Unit target) {
        if (me == null || target == null) {
            return false;
        }

        if (target.getDisplayPowerType() != power) {
            return false;
        }

        if (playerOnly && target.getObjectTypeId() != TypeId.PLAYER) {
            return false;
        }

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist)) {
            return false;
        }

        if (dist < 0.0f && me.isWithinCombatRange(target, -_dist)) {
            return false;
        }

        return true;
    }
}
