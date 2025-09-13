package com.github.azeroth.game.movement;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;

public abstract class MovementGeneratorMedium<T extends unit> extends MovementGenerator {
    private boolean isActive;

    public final boolean isActive() {
        return isActive;
    }

    public final void setActive(boolean value) {
        isActive = value;
    }

    @Override
    public void initialize(Unit owner) {
        doInitialize((T) owner);
        setActive(true);
    }

    @Override
    public void reset(Unit owner) {
        doReset((T) owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        return doUpdate((T) owner, diff);
    }

    @Override
    public void deactivate(Unit owner) {
        doDeactivate((T) owner);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        doFinalize((T) owner, active, movementInform);
    }

    public abstract void doInitialize(T owner);

    public abstract void doFinalize(T owner, boolean active, boolean movementInform);

    public abstract void doReset(T owner);

    public abstract boolean doUpdate(T owner, int diff);

    public abstract void doDeactivate(T owner);

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.max;
    }
}
