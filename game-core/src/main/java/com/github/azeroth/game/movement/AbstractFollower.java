package com.github.azeroth.game.movement;

import com.github.azeroth.game.entity.unit.Unit;

public class AbstractFollower {
    private Unit target;


    public AbstractFollower() {
        this(null);
    }

    public AbstractFollower(Unit target) {
        setTarget(target);
    }

    public final Unit getTarget() {
        return target;
    }

    public final void setTarget(Unit unit) {
        if (unit == target) {
            return;
        }

        if (target != null) {
            target.followerRemoved(this);
        }

        target = unit;

        if (target != null) {
            target.followerAdded(this);
        }
    }
}
