package com.github.azeroth.game.movement;


class DelayedAction {
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
}
