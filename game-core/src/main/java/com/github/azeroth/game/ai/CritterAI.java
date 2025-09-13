package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class CritterAI extends PassiveAI {
    public CritterAI(Creature c) {
        super(c);
        me.setReactState(ReactStates.Passive);
    }

    @Override
    public void justEngagedWith(Unit who) {
        if (!me.hasUnitState(UnitState.Fleeing)) {
            me.setControlled(true, UnitState.Fleeing);
        }
    }

    @Override
    public void movementInform(MovementGeneratorType type, int id) {
        if (type == MovementGeneratorType.TimedFleeing) {
            enterEvadeMode(EvadeReason.other);
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
