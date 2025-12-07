package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class PassiveAI extends CreatureAI {
    public PassiveAI(Creature creature) {
        super(creature);
        creature.reactState = ReactStates.Passive;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (me.isEngaged() && !me.isInCombat()) {
            enterEvadeMode(EvadeReason.NoHostiles);
        }
    }

    @Override
    public void attackStart(Unit victim) {
    }

    @Override
    public void moveInLineOfSight(Unit who) {
    }
}