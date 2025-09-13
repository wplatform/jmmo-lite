package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class FriendlyCCedInRange implements ICheck<Creature> {
    private final Unit obj;
    private final float range;

    public FriendlyCCedInRange(Unit obj, float range) {
        obj = obj;
        range = range;
    }

    public final boolean invoke(Creature u) {
        if (u.isAlive() && u.isInCombat() && !obj.isHostileTo(u) && obj.isWithinDist(u, range) && (u.isFeared() || u.isCharmed() || u.getHasRootAura() || u.hasUnitState(UnitState.Stunned) || u.hasUnitState(UnitState.Confused))) {
            return true;
        }

        return false;
    }
}
