package com.github.azeroth.game.misc;

import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

/**
 * Only returns true for the given attacker's current victim, if any
 */
public class IsVictimOf implements ICheck<WorldObject> {
    private final WorldObject victim;

    public IsVictimOf(Unit attacker) {
        victim = attacker == null ? null : attacker.getVictim();
    }

    public final boolean invoke(WorldObject obj) {
        return obj != null && (victim == obj);
    }
}
