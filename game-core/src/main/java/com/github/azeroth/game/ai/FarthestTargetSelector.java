package com.github.azeroth.game.ai;










public class FarthestTargetSelector implements ICheck<Unit> {
    private final Unit me;
    private final float dist;
    private final boolean playerOnly;
    private final boolean inLos;

    public FarthestTargetSelector(Unit unit, float dist, boolean playerOnly, boolean inLos) {
        me = unit;
        this.dist = dist;
        this.playerOnly = playerOnly;
        this.inLos = inLos;
    }

    public final boolean invoke(Unit target) {
        if (me == null || target == null) {
            return false;
        }

        if (playerOnly && target.getTypeId() != TypeId.Player) {
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