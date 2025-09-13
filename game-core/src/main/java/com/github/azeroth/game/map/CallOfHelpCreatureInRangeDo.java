package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class CallOfHelpCreatureInRangeDo implements IDoWork<Creature> {
    private final Unit funit;
    private final Unit enemy;
    private final float range;

    public CallOfHelpCreatureInRangeDo(Unit funit, Unit enemy, float range) {
        funit = funit;
        enemy = enemy;
        range = range;
    }

    public final void invoke(Creature u) {
        if (u == funit) {
            return;
        }

        if (!u.canAssistTo(funit, enemy, false)) {
            return;
        }

        // too far
        // Don't use combat reach distance, range must be an absolute value, otherwise the chain aggro range will be too big
        if (!u.isWithinDist(funit, range, true, false, false)) {
            return;
        }

        // only if see assisted creature's enemy
        if (!u.isWithinLOSInMap(enemy)) {
            return;
        }

        u.engageWithTarget(enemy);
    }
}
