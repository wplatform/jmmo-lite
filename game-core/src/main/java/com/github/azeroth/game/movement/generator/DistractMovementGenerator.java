package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class DistractMovementGenerator extends MovementGenerator {
    private final float orientation;

    private int timer;

    public DistractMovementGenerator(int timer, float orientation) {
        timer = timer;
        orientation = orientation;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.Highest;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Distracted;
    }

    @Override
    public void initialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        // Distracted creatures stand up if not standing
        if (!owner.isStandState()) {
            owner.setStandState(UnitStandStateType.Stand);
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        init.moveTo(owner.getLocation(), false);

        if (!owner.getTransGUID().isEmpty()) {
            init.disableTransportPathTransformations();
        }

        init.setFacing(orientation);
        init.launch();
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

        if (diff > timer) {
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        _timer -= diff;

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        // TODO: This code should be handled somewhere else
        // If this is a creature, then return orientation to original position (for idle movement creatures)
        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled) && owner.isCreature()) {
            var angle = owner.toCreature().getHomePosition().getO();
            owner.setFacingTo(angle);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Distract;
    }
}
