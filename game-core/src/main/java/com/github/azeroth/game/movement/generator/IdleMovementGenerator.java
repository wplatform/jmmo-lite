package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public class IdleMovementGenerator extends MovementGenerator {
    public IdleMovementGenerator() {
        this.mode = MovementGeneratorMode.DEFAULT;
        this.priority = MovementGeneratorPriority.NORMAL;
        this.flags.set(MovementGeneratorFlag.INITIALIZED);
        this.baseUnitState = UnitState.ROAMING;
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
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        this.flags.addFlag(MovementGeneratorFlag.FINALIZED);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.IDLE;
    }
}
