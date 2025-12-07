package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;
import java.util.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class CombatAI extends CreatureAI {
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
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
                    events.ScheduleEvent(id, info.cooldown, info.cooldown * 2);
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void SpellInterrupted(uint spellId, uint unTimeMs)
    @Override
    public void spellInterrupted(int spellId, int unTimeMs) {
        events.RescheduleEvent(spellId, TimeSpan.FromMilliseconds(unTimeMs));
    }
}