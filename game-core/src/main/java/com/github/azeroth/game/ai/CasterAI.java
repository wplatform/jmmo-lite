package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class CasterAI extends CombatAI {
    private float attackDistance;

    public CasterAI(Creature creature) {
        super(creature);
        attackDistance = SharedConst.MeleeRange;
    }

    @Override
    public void initializeAI() {
        super.initializeAI();

        attackDistance = 30.0f;

        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null && info.condition == AICondition.Combat && attackDistance > info.maxRange) {
                attackDistance = info.maxRange;
            }
        }

        if (attackDistance == 30.0f) {
            attackDistance = SharedConst.MeleeRange;
        }
    }

    @Override
    public void attackStart(Unit victim) {
        attackStartCaster(victim, attackDistance);
    }

    @Override
    public void justEngagedWith(Unit victim) {
        if (spells.isEmpty()) {
            return;
        }

        var spell = (int) (RandomUtil.Rand32() % spells.size());
        int count = 0;

        for (var id : spells) {
            var info = getAISpellInfo(id, me.getMap().getDifficultyID());

            if (info != null) {
                if (info.condition == AICondition.Aggro) {
                    me.castSpell(victim, id, false);
                } else if (info.condition == AICondition.Combat) {
                    var cooldown = info.realCooldown;

                    if (count == spell) {
                        doCast(spells.get(spell));
                        cooldown += duration.ofSeconds(me.getCurrentSpellCastTime(id));
                    }

                    events.ScheduleEvent(id, cooldown);
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

        if (me.getVictim() != null) {
            if (me.getVictim().hasBreakableByDamageCrowdControlAura(me)) {
                me.interruptNonMeleeSpells(false);

                return;
            }
        }

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        var spellId = events.executeEvent();

        if (spellId != 0) {
            doCast(spellId);
            var casttime = (int) me.getCurrentSpellCastTime(spellId);
            var info = getAISpellInfo(spellId, me.getMap().getDifficultyID());

            if (info != null) {
                events.ScheduleEvent(spellId, duration.ofSeconds(casttime != 0 ? casttime : 500) + info.realCooldown);
            }
        }
    }
}
