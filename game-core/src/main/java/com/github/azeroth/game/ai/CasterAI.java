package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




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
        if (spells.Empty()) {
            return;
        }

        var spell = (int)(RandomHelper.Rand32() % spells.size());
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint count = 0;
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
                        cooldown += TimeSpan.FromMilliseconds(me.getCurrentSpellCastTime(id));
                    }

                    events.ScheduleEvent(id, cooldown);
                }
            }
        }
    }
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        events.Update(diff);

        if (me.getVictim() != null) {
            if (me.getVictim().hasBreakableByDamageCrowdControlAura(me)) {
                me.interruptNonMeleeSpells(false);

                return;
            }
        }

        if (me.hasUnitState(UnitState.Casting)) {
            return;
        }

        var spellId = events.ExecuteEvent();

        if (spellId != 0) {
            doCast(spellId);
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var casttime = (uint)Me.GetCurrentSpellCastTime(spellId);
            var casttime = (int)me.getCurrentSpellCastTime(spellId);
            var info = getAISpellInfo(spellId, me.getMap().getDifficultyID());

            if (info != null) {
                events.ScheduleEvent(spellId, TimeSpan.FromMilliseconds(casttime != 0 ? casttime : 500) + info.realCooldown);
            }
        }
    }
}