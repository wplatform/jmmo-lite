package com.github.azeroth.game.ai;




import com.github.azeroth.game.entity.creature.Creature;

import java.util.*;






public class CombatAI extends CreatureAI {

//ORIGINAL LINE: protected List<uint> _spells = new();
    protected ArrayList<Integer> spells = new ArrayList<Integer>();

    public CombatAI(Creature c) {
        super(c);
    }

    @Override
    public void initializeAI() {
        for (var i = 0; i < SharedConst.MaxCreatureSpells; ++i) {
            if (me.spells[i] != 0 && Global.getSpellMgr().hasSpellInfo(me.spells[i], me.getMap().getDifficultyID())) {
                spells.add(me.spells[i]);
            }
        }

        super.initializeAI();
    }

    @Override
    public void reset() {
        events.Reset();
    }

    @Override
    public void justDied(Unit killer) {
        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null && info.condition == AICondition.DIE) {
                me.castSpell(killer, id, true);
            }
        }
    }

    @Override
    public void justEngagedWith(Unit victim) {
        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null) {
                if (info.condition == AICondition.AGGRO) {
                    me.castSpell(victim, id, false);
                } else if (info.condition == AICondition.COMBAT) {
                    events.ScheduleEvent(id, info.cooldown, info.cooldown * 2);
                }
            }
        }
    }

//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        events.Update(diff);

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        var spellId = events.ExecuteEvent();

        if (spellId != 0) {
            doCast(spellId);
            var info = getAISpellInfo(spellId, me.getMap().getDifficultyID());

            if (info != null) {
                events.ScheduleEvent(spellId, info.cooldown, info.cooldown * 2);
            }
        } else {
            doMeleeAttackIfReady();
        }
    }


//ORIGINAL LINE: public override void SpellInterrupted(uint spellId, uint unTimeMs)
    @Override
    public void spellInterrupted(int spellId, int unTimeMs) {
        events.RescheduleEvent(spellId, TimeSpan.FromMilliseconds(unTimeMs));
    }
}