package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitMoveType;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.RotateDirection;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;

import java.time.Duration;
import java.util.Optional;

public class RotateMovementGenerator extends MovementGenerator {

    public static final float MIN_ANGLE_DELTA_FOR_FACING_UPDATE = 0.05f;


    private final int id;
    private final TimeTracker duration;
    private final Float turnSpeed;         ///< radians per sec
    private Float totalTurnAngle;
    private final RotateDirection direction;
    private int diffSinceLastUpdate;




    public RotateMovementGenerator(int id, RotateDirection direction, Duration duration, Float turnSpeed, Float totalTurnAngle) {
        this.id = id;
        this.duration = Optional.ofNullable(duration).map(TimeTracker::new).orElse(null);
        this.direction = direction;
        this.turnSpeed = turnSpeed;
        this.totalTurnAngle = totalTurnAngle;
        this.mode = MovementGeneratorMode.DEFAULT;
        this.priority = MovementGeneratorPriority.NORMAL;
        this.baseUnitState = UnitState.ROTATING;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        addFlag(MovementGeneratorFlag.INITIALIZED);

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
        flags.removeFlag(MovementGeneratorFlag.DEACTIVATED);
        initialize(owner);
    }


    @Override
    public boolean update(Unit owner, int diff) {
        diffSinceLastUpdate += diff;

        float currentAngle = owner.getLocation().getO();


        float angleDelta =  (turnSpeed == null ? owner.getSpeed(UnitMoveType.TURN_RATE) : turnSpeed) * (diffSinceLastUpdate / 1000.0f);

        if (duration != null)
            duration.update(diff);

        if (totalTurnAngle != null)
            totalTurnAngle = totalTurnAngle - angleDelta;

        boolean expired = (duration != null && duration.passed()) || (totalTurnAngle != null && totalTurnAngle < 0.0f);

        if (angleDelta >= MIN_ANGLE_DELTA_FOR_FACING_UPDATE || expired)
        {
            float newAngle = Position.normalizeOrientation(currentAngle + angleDelta * (direction == RotateDirection.LEFT ? 1.0f : -1.0f));

            MoveSplineInit init = new MoveSplineInit(owner);
            init.moveTo(PositionToVector3(owner.getLocation()), false);
            if (!owner.getTransGUID().isEmpty())
                init.DisableTransportPathTransformations();
            init.SetFacing(newAngle);
            init.Launch();

            _diffSinceLastUpdate = 0;
        }

        if (expired)
        {
            AddFlag(MOVEMENTGENERATOR_FLAG_INFORM_ENABLED);
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
            owner.toCreature().getAi().movementInform(MovementGeneratorType.Rotate, id);
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Rotate;
    }
}
