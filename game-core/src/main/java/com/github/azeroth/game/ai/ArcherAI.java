package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class ArcherAI extends CreatureAI {
    private final float minRange;

    public ArcherAI(Creature creature) {
        super(creature);
        if (creature.spells[0] == 0) {
            Log.outError(LogFilter.ScriptsAi, String.format("ArcherAI set for creature with spell1=0. AI will do nothing (%1$s)", me.getGUID().clone()));
        }

        var spellInfo = Global.getSpellMgr().getSpellInfo(creature.spells[0], creature.getMap().getDifficultyID());
        minRange = spellInfo != null ? spellInfo.getMinRange(false) : 0;

        if (minRange == 0) {
            minRange = SharedConst.MeleeRange;
        }

        creature.combatDistance = spellInfo != null ? spellInfo.getMaxRange(false) : 0;
        creature.sightDistance = creature.combatDistance;
    }

    @Override
    public void attackStart(Unit who) {
        if (who == null) {
            return;
        }

        if (me.isWithinCombatRange(who, minRange)) {
            if (me.attack(who, true) && !who.isFlying()) {
                me.getMotionMaster().moveChase(who);
            }
        } else {
            if (me.attack(who, false) && !who.isFlying()) {
                me.getMotionMaster().moveChase(who, me.combatDistance);
            }
        }

        if (who.isFlying()) {
            me.getMotionMaster().moveIdle();
        }
    }
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        if (!me.isWithinCombatRange(me.getVictim(), minRange)) {
            doSpellAttackIfReady(me.spells[0]);
        } else {
            doMeleeAttackIfReady();
        }
    }
}