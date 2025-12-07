package game.ai;

import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class TurretAI extends CreatureAI {
    private final float minRange;

    public TurretAI(Creature creature) {
        super(creature);
        if (creature.spells[0] == 0) {
            Log.outError(LogFilter.Server, String.format("TurretAI set for creature with spell1=0. AI will do nothing (%1$s)", creature.getGUID().clone()));
        }

        var spellInfo = Global.getSpellMgr().getSpellInfo(creature.spells[0], creature.getMap().getDifficultyID());
        minRange = spellInfo != null ? spellInfo.getMinRange(false) : 0;
        creature.combatDistance = spellInfo != null ? spellInfo.getMaxRange(false) : 0;
        creature.sightDistance = creature.combatDistance;
    }

    @Override
    public boolean canAIAttack(Unit victim) {
        // todo use one function to replace it
        if (!me.isWithinCombatRange(victim, me.combatDistance) || (minRange != 0 && me.isWithinCombatRange(victim, minRange))) {
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doSpellAttackIfReady(me.spells[0]);
    }
}