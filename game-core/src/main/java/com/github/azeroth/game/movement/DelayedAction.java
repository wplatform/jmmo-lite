package com.github.azeroth.game.movement;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MotionMasterDelayedActionType;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

@RequiredArgsConstructor
public class DelayedAction implements Runnable {


    private final MotionMasterDelayedActionType type;
    private final Predicate<Unit> validator;


    @Override
    public void run() {

    }
}
