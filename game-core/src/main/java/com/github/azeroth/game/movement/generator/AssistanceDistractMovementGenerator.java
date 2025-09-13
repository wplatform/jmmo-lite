package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;

public class AssistanceDistractMovementGenerator extends DistractMovementGenerator {

    public AssistanceDistractMovementGenerator(int timer, float orientation) {
        super(timer, orientation);
        priority = MovementGeneratorPriority.NORMAL;
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        owner.clearUnitState(UnitState.Distracted);
        owner.toCreature().setReactState(ReactStates.Aggressive);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.AssistanceDistract;
    }
}
