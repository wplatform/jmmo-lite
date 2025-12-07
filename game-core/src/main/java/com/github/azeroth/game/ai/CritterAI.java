package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class CritterAI extends PassiveAI {
    public CritterAI(Creature c) {
        super(c);
        me.reactState = ReactStates.Passive;
    }

    @Override
    public void justEngagedWith(Unit who) {
        if (!me.hasUnitState(UnitState.Fleeing)) {
            me.setControlled(true, UnitState.Fleeing);
        }
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void MovementInform(MovementGeneratorType type, uint id)
    @Override
    public void movementInform(MovementGeneratorType type, int id) {
        if (type == MovementGeneratorType.TimedFleeing) {
            enterEvadeMode(EvadeReason.Other);
        }
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
        if (me.hasUnitState(UnitState.Fleeing)) {
            me.setControlled(false, UnitState.Fleeing);
        }

        super.enterEvadeMode(why);
    }
}