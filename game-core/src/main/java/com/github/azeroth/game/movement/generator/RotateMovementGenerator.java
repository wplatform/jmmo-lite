package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class RotateMovementGenerator extends MovementGenerator {

    private final int id;

    private final int maxDuration;
    private final RotateDirection direction;

    private int duration;


    public RotateMovementGenerator(int id, int time, RotateDirection direction) {
        id = id;
        duration = time;
        maxDuration = time;
        direction = direction;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Rotating;
    }

    @Override
    public void initialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        owner.stopMoving();

        /*
         *  TODO: This code should be handled somewhere else, like MovementInform
         *
         *  if (owner->GetVictim())
         *      owner->SetInFront(owner->GetVictim());
         *
         *  owner->AttackStop();
         */
    }

    @Override
    public void reset(Unit owner) {
        removeFlag(MovementGeneratorFlags.Deactivated);
        initialize(owner);
    }


    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null) {
            return false;
        }

        var angle = owner.getLocation().getO();
        angle += diff * MathUtil.TwoPi / _maxDuration * (direction == RotateDirection.Left ? 1.0f : -1.0f);
        angle = Math.Clamp(angle, 0.0f, MathUtil.PI * 2);

        MoveSplineInit init = new MoveSplineInit(owner);
        init.moveTo(owner.getLocation(), false);

        if (!owner.getTransGUID().isEmpty()) {
            init.disableTransportPathTransformations();
        }

        init.setFacing(angle);
        init.launch();

        if (duration > diff) {
            _duration -= diff;
        } else {
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (movementInform && owner.isCreature()) {
            owner.toCreature().getAI().movementInform(MovementGeneratorType.Rotate, id);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Rotate;
    }
}
