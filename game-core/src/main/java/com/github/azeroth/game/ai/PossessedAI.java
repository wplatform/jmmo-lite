package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class PossessedAI extends CreatureAI {
    public PossessedAI(Creature creature) {
        super(creature);
        creature.reactState = ReactStates.Passive;
    }

    @Override
    public void attackStart(Unit target) {
        me.attack(target, true);
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (me.getVictim() != null) {
            if (!me.isValidAttackTarget(me.getVictim())) {
                me.attackStop();
            } else {
                doMeleeAttackIfReady();
            }
        }
    }
    @Override
    public void justDied(Unit unit) {
        // We died while possessed, disable our loot
        me.removeDynamicFlag(UnitDynFlags.Lootable);
    }

    @Override
    public void moveInLineOfSight(Unit who) {
    }

    @Override
    public void justEnteredCombat(Unit who) {
        engagementStart(who);
    }

    @Override
    public void justExitedCombat() {
        engagementOver();
    }

    @Override
    public void justStartedThreateningMe(Unit who) {
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
    }
}