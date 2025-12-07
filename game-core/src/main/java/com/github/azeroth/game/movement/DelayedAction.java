package com.github.azeroth.game.movement;


import com.github.azeroth.game.movement.enums.MotionMasterDelayedActionType;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DelayedAction implements Delayed {
    private final tangible.Action0Param tangible.Action0Param;
    private final tangible.Func0Param<Boolean> validator;
    private final MotionMasterDelayedActiontype type;

    public DelayedAction(tangible.Action0Param action, tangible.Func0Param<Boolean> validator, MotionMasterDelayedActionType type) {
        tangible.Action0Param = action;
        validator = validator;
        type = type;
    }

    public DelayedAction(tangible.Action0Param action, MotionMasterDelayedActionType type) {
        tangible.Action0Param = action;
        validator = () -> true;
        type = type;
    }

    public final void resolve() {
        if (validator.invoke()) {
            tangible.Action0Param.invoke();
        }
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(Delayed o) {
        return 0;
    }
}
