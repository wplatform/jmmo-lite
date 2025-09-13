package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class FriendlyMissingBuffInRange implements ICheck<Creature> {
    private final Unit obj;
    private final float range;
    private final int spell;

    public FriendlyMissingBuffInRange(Unit obj, float range, int spellid) {
        obj = obj;
        range = range;
        spell = spellid;
    }

    public final boolean invoke(Creature u) {
        if (u.isAlive() && u.isInCombat() && !obj.isHostileTo(u) && obj.isWithinDist(u, range) && !(u.hasAura(spell))) {
            return true;
        }

        return false;
    }
}
