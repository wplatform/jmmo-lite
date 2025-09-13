package com.github.azeroth.game.map;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class NearestHostileUnitCheck implements ICheck<unit> {
    private final Creature me;
    private final boolean playerOnly;
    private float range;


    public NearestHostileUnitCheck(Creature creature, float dist) {
        this(creature, dist, false);
    }

    public NearestHostileUnitCheck(Creature creature) {
        this(creature, 0, false);
    }

    public NearestHostileUnitCheck(Creature creature, float dist, boolean playerOnly) {
        me = creature;
        playerOnly = playerOnly;

        range = (dist == 0 ? 9999 : dist);
    }

    public final boolean invoke(Unit u) {
        if (!me.isWithinDist(u, range)) {
            return false;
        }

        if (!me.isValidAttackTarget(u)) {
            return false;
        }

        if (playerOnly && !u.isTypeId(TypeId.PLAYER)) {
            return false;
        }

        range = me.getDistance(u); // use found unit range as new range limit for next check

        return true;
    }
}
