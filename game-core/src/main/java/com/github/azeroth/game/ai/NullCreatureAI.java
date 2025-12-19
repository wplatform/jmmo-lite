package com.github.azeroth.game.ai;


import com.github.azeroth.game.ai.enums.EvadeReason;
import com.github.azeroth.game.domain.unit.ReactState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class NullCreatureAI extends CreatureAI {
    public NullCreatureAI(Creature creature) {
        super(creature);
        creature.setReactState(ReactState.PASSIVE);
    }

    @Override
    public void moveInLineOfSight(Unit unit) {
    }
    @Override
    public void attackStart(Unit unit) {
    }
    @Override
    public void justStartedThreateningMe(Unit unit) {
    }
    @Override
    public void justEnteredCombat(Unit who) {
    }

//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
    }
    @Override
    public void justAppeared() {
    }
    @Override
    public void enterEvadeMode(EvadeReason why) {
    }
    @Override
    public void onCharmed(boolean isNew) {
    }
}