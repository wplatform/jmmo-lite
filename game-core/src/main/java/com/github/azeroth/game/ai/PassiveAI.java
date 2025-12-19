package com.github.azeroth.game.ai;


import com.github.azeroth.game.ai.enums.EvadeReason;
import com.github.azeroth.game.domain.unit.ReactState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class PassiveAI extends CreatureAI {
    public PassiveAI(Creature creature) {
        super(creature);
        me.setReactState(ReactState.PASSIVE);
    }

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