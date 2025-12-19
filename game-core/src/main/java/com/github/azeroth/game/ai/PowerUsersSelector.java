package com.github.azeroth.game.ai;










public class PowerUsersSelector implements ICheck<Unit> {
    private final Unit me;
    private final PowerType power = PowerType.values()[0];
    private final float dist;
    private final boolean playerOnly;

    public PowerUsersSelector(Unit unit, PowerType power, float dist, boolean playerOnly) {
        me = unit;
        this.power = power;
        this.dist = dist;
        this.playerOnly = playerOnly;
    }

    public final boolean invoke(Unit target) {
        if (me == null || target == null) {
            return false;
        }

        if (target.getDisplayPowerType() != power) {
            return false;
        }

        if (playerOnly && target.getTypeId() != TypeId.Player) {
            return false;
        }

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist)) {
            return false;
        }

        if (dist < 0.0f && me.isWithinCombatRange(target, -dist)) {
            return false;
        }

        return true;
    }
}