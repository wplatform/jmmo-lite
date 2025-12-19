package com.github.azeroth.game.ai;


import com.github.azeroth.game.ai.enums.EvadeReason;
import com.github.azeroth.game.domain.unit.ReactState;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public class CritterAI extends PassiveAI {
    public CritterAI(Creature c) {
        super(c);
        me.setReactState(ReactState.PASSIVE);
    }

    @Override
    public void justEngagedWith(Unit who) {
        if (!me.hasUnitState(UnitState.FLEEING)) {
            me.setControlled(true, UnitState.FLEEING);
        }
    }

    @Override
    public void movementInform(MovementGeneratorType type, int id) {
        if (type == MovementGeneratorType.TIMED_FLEEING) {
            enterEvadeMode(EvadeReason.Other);
        }
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
        if (me.hasUnitState(UnitState.FLEEING)) {
            me.setControlled(false, UnitState.FLEEING);
        }

        super.enterEvadeMode(why);
    }
}