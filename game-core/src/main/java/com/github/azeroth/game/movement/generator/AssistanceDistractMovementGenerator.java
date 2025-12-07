package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.unit.ReactState;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public class AssistanceDistractMovementGenerator extends DistractMovementGenerator {

    public AssistanceDistractMovementGenerator(int timer, float orientation) {
        super(timer, orientation);
        priority = MovementGeneratorPriority.NORMAL;
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        owner.clearUnitState(UnitState.DISTRACTED);
        owner.toCreature().setReactState(ReactState.AGGRESSIVE);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.ASSISTANCE_DISTRACT;
    }
}
