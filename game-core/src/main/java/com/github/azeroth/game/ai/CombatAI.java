package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.ArrayList;

public class CombatAI extends CreatureAI {

    protected ArrayList<Integer> spells = new ArrayList<>();

    public CombatAI(Creature c) {
        super(c);
    }

    @Override
    public void initializeAI() {
        for (var i = 0; i < SharedConst.MaxCreatureSpells; ++i) {
            if (me.getSpells()[i] != 0 && global.getSpellMgr().hasSpellInfo(me.getSpells()[i], me.getMap().getDifficultyID())) {
                spells.add(me.getSpells()[i]);
            }
        }

        super.initializeAI();
    }

    @Override
    public void reset() {
        events.reset();
    }

    @Override
    public void justDied(Unit killer) {
        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null && info.condition == AICondition.Die) {
                me.castSpell(killer, id, true);
            }
        }
    }

    @Override
    public void justEngagedWith(Unit victim) {
        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null) {
                if (info.condition == AICondition.Aggro) {
                    me.castSpell(victim, id, false);
                } else if (info.condition == AICondition.Combat) {
                    events.ScheduleEvent(id, info.cooldown, info.Cooldown * 2);
                }
            }
        }
    }


    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        events.update(diff);

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        var spellId = events.executeEvent();

        if (spellId != 0) {
            doCast(spellId);
            var info = getAISpellInfo(spellId, me.getMap().getDifficultyID());

            if (info != null) {
                events.ScheduleEvent(spellId, info.cooldown, info.Cooldown * 2);
            }
        } else {
            doMeleeAttackIfReady();
        }
    }


    @Override
    public void spellInterrupted(int spellId, int unTimeMs) {
        events.RescheduleEvent(spellId, duration.ofSeconds(unTimeMs));
    }
}
