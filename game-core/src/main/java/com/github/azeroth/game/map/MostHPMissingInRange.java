package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.unit.Unit;

public class MostHPMissingInRange<T extends unit> implements ICheck<T> {
    private final Unit obj;
    private final float range;
    private long hp;

    public MostHPMissingInRange(Unit obj, float range, int hp) {
        obj = obj;
        range = range;
        hp = hp;
    }

    public final boolean invoke(T u) {
        if (u.isAlive() && u.isInCombat() && !obj.isHostileTo(u) && obj.isWithinDist(u, range) && u.getMaxHealth() - u.getHealth() > hp) {
            hp = (int) (u.getMaxHealth() - u.getHealth());

            return true;
        }

        return false;
    }
}
