package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.unit.UnitStandStateType;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class DistractMovementGenerator extends MovementGenerator {
    private final float orientation;

    private int timer;

    public DistractMovementGenerator(int timer, float orientation) {
        this.timer = timer;
        this.orientation = orientation;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.HIGHEST;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.DISTRACTED;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        // Distracted creatures stand up if not standing
        if (!owner.isStandState()) {
            owner.setStandState(UnitStandStateType.STAND);
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        init.moveTo(owner.getLocation().toVector3(), false);

        if (!owner.getTransGUID().isEmpty()) {
            init.disableTransportPathTransformations();
        }

        init.setFacing(orientation);
        init.launch();
    }

    @Override
    public void reset(Unit owner) {
        removeFlag(MovementGeneratorFlag.DEACTIVATED);
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null) {
            return false;
        }

        if (diff > timer) {
            addFlag(MovementGeneratorFlag.INFORM_ENABLED);

            return false;
        }

        timer -= diff;
        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlag.DEACTIVATED);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        // TODO: This code should be handled somewhere else
        // If this is a creature, then return orientation to original position (for idle movement creatures)
        if (movementInform && hasFlag(MovementGeneratorFlag.INFORM_ENABLED) && owner.isCreature()) {
            var angle = owner.toCreature().getHomePosition().getO();
            owner.setFacingTo(angle);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.DISTRACT;
    }
}
