package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class TurretAI extends CreatureAI {
    private final float minRange;

    public TurretAI(Creature creature) {
        super(creature);
        if (creature.getSpells()[0] == 0) {
            Log.outError(LogFilter.Server, String.format("TurretAI set for creature with spell1=0. AI will do nothing (%1$s)", creature.getGUID()));
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(creature.getSpells()[0], creature.getMap().getDifficultyID());
        minRange = spellInfo != null ? spellInfo.getMinRange(false) : 0;
        creature.setCombatDistance(spellInfo != null ? spellInfo.getMaxRange(false) : 0);
        creature.setSightDistance(creature.getCombatDistance());
    }

    @Override
    public boolean canAIAttack(Unit victim) {
        // todo use one function to replace it
        if (!me.isWithinCombatRange(victim, me.getCombatDistance()) || (minRange != 0 && me.isWithinCombatRange(victim, minRange))) {
            return false;
        }

        return true;
    }

    @Override
    public void attackStart(Unit victim) {
        if (victim != null) {
            me.attack(victim, false);
        }
    }

    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doSpellAttackIfReady(me.getSpells()[0]);
    }
}
