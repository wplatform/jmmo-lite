package com.github.azeroth.game.combat;

import com.github.azeroth.game.entity.unit.Unit;

public class CombatReference {
    public Unit first;
    public Unit second;
    public boolean isPvP;

    private boolean suppressFirst;
    private boolean suppressSecond;


    public CombatReference(Unit a, Unit b) {
        this(a, b, false);
    }

    public CombatReference(Unit a, Unit b, boolean pvp) {
        first = a;
        second = b;
        isPvP = pvp;
    }

    public final void endCombat() {
        // sequencing matters here - AI might do nasty stuff, so make sure refs are in a consistent state before you hand off!

        // first, get rid of any threat that still exists...
        first.getThreatManager().clearThreat(second);
        second.getThreatManager().clearThreat(first);

        // ...then, remove the references from both managers...
        first.getCombatManager().purgeReference(second.getGUID(), isPvP);
        second.getCombatManager().purgeReference(first.getGUID(), isPvP);

        // ...update the combat state, which will potentially remove IN_COMBAT...
        var needFirstAI = first.getCombatManager().updateOwnerCombatState();
        var needSecondAI = second.getCombatManager().updateOwnerCombatState();

        // ...and if that happened, also notify the AI of it...
        if (needFirstAI) {
            var firstAI = first.getAI();

            if (firstAI != null) {
                firstAI.justExitedCombat();
            }
        }

        if (needSecondAI) {
            var secondAI = second.getAI();

            if (secondAI != null) {
                secondAI.justExitedCombat();
            }
        }
    }

    public final void refresh() {
        boolean needFirstAI = false, needSecondAI = false;

        if (suppressFirst) {
            suppressFirst = false;
            needFirstAI = first.getCombatManager().updateOwnerCombatState();
        }

        if (suppressSecond) {
            suppressSecond = false;
            needSecondAI = second.getCombatManager().updateOwnerCombatState();
        }

        if (needFirstAI) {
            CombatManager.notifyAICombat(first, second);
        }

        if (needSecondAI) {
            CombatManager.notifyAICombat(second, first);
        }
    }

    public final void suppressFor(Unit who) {
        suppress(who);

        if (who.getCombatManager().updateOwnerCombatState()) {
            var ai = who.getAI();

            if (ai != null) {
                ai.justExitedCombat();
            }
        }
    }

    // suppressed combat refs do not generate a combat state for one side of the relation
    // (used by: vanish, feign death)
    public final boolean isSuppressedFor(Unit who) {
        return (who == first) ? _suppressFirst : suppressSecond;
    }

    public final void suppress(Unit who) {
        if (who == first) {
            suppressFirst = true;
        } else {
            suppressSecond = true;
        }
    }

    public final Unit getOther(Unit me) {
        return (first == me) ? Second : first;
    }
}
