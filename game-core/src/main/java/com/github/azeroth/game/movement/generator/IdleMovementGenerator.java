package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;

public class IdleMovementGenerator extends MovementGenerator {
    public idleMovementGenerator() {
        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.initialized;
        baseUnitState = UnitState.forValue(0);
    }

    @Override
    public void initialize(Unit owner) {
        owner.stopMoving();
    }

    @Override
    public void reset(Unit owner) {
        owner.stopMoving();
    }

    @Override
    public boolean update(Unit owner, int diff) {
        return true;
    }

    @Override
    public void deactivate(Unit owner) {
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.IDLE;
    }
}
