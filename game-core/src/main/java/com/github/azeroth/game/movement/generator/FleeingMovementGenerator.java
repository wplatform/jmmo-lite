package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;
import com.github.azeroth.utils.RandomUtil;

public class FleeingMovementGenerator extends MovementGenerator {
    public static final float MIN_QUIET_DISTANCE = 28.0f;
    public static final float MAX_QUIET_DISTANCE = 43.0f;
    private final TimeTracker timer;
    private final ObjectGuid fleeTargetGUID;
    private PathGenerator path;

    public FleeingMovementGenerator(ObjectGuid fright) {
        fleeTargetGUID = fright;
        timer = new TimeTracker(0);

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.HIGHEST;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.FLEEING;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        if (owner == null || !owner.isAlive()) {
            return;
        }

        // TODO: UNIT_FIELD_FLAGS should not be handled by generators
        owner.setUnitFlag(UnitFlag.FLEEING);
        path = null;
        setTargetLocation(owner);
    }

    @Override
    public void reset(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting()) {
            flags.addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();
            path = null;

            return true;
        } else {
            flags.removeFlag(MovementGeneratorFlag.INTERRUPTED);
        }

        timer.update(diff);

        if ((flags.hasFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING) && !owner.getMoveSpline().finalized()) || (timer.passed() && owner.getMoveSpline().finalized())) {
            flags.removeFlag(MovementGeneratorFlag.TRANSITORY);
            setTargetLocation(owner);
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        owner.clearUnitState(UnitState.FLEEING_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        flags.addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            if (owner.isPlayer()) {
                owner.removeUnitFlag(UnitFlag.FLEEING);
                owner.clearUnitState(UnitState.FLEEING_MOVE);
                owner.stopMoving();
            } else {
                owner.removeUnitFlag(UnitFlag.FLEEING);
                owner.clearUnitState(UnitState.FLEEING_MOVE);

                if (owner.getVictim() != null) {
                    owner.setTarget(owner.getVictim().getGUID());
                }
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.FLEEING;
    }

    @Override
    public void unitSpeedChanged() {
        flags.addFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING);
    }

    private void setTargetLocation(Unit owner) {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting()) {
            flags.addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();
            path = null;

            return;
        }

        Position destination = new Position(owner.getLocation());
        getPoint(owner, destination);

        // Add LOS check for target point
        if (!owner.isWithinLOS(destination.getX(), destination.getY(), destination.getZ())) {
            timer.reset(200);

            return;
        }

        if (path == null) {
            path = new PathGenerator(owner);
            path.setPathLengthLimit(30.0f);
        }

        var result = path.calculatePath(destination);

        if (!result || path.getPathType().hasFlag(PathType.NOPATH) || path.getPathType().hasFlag(PathType.SHORTCUT) || path.getPathType().hasFlag(PathType.FARFROMPOLY)) {
            timer.reset(100);

            return;
        }

        owner.addUnitState(UnitState.FLEEING_MOVE);

        MoveSplineInit init = new MoveSplineInit(owner);
        init.movebyPath(path.getPath());
        init.setWalk(false);
        var traveltime = init.launch();
        timer.reset(traveltime + RandomUtil.randomInt(800, 1500));
    }

    private void getPoint(Unit owner, Position position) {
        float casterDistance, casterAngle;
        var fleeTarget = owner.getWorldContext().getUnit(owner, fleeTargetGUID);

        if (fleeTarget != null) {
            casterDistance = fleeTarget.getDistance(owner);

            if (casterDistance > 0.2f) {
                casterAngle = fleeTarget.getLocation().getAbsoluteAngle(owner.getLocation());
            } else {
                casterAngle = RandomUtil.randomFloat(0.0f, 2.0f * (float) Math.PI);
            }
        } else {
            casterDistance = 0.0f;
            casterAngle = RandomUtil.randomFloat(0.0f, 2.0f * (float) Math.PI);
        }

        float distance, angle;

        if (casterDistance < MIN_QUIET_DISTANCE) {
            distance = RandomUtil.randomFloat(0.4f, 1.3f) * (MIN_QUIET_DISTANCE - casterDistance);
            angle = casterAngle + RandomUtil.randomFloat(-(float) Math.PI / 8.0f, (float) Math.PI / 8.0f);
        } else if (casterDistance > MAX_QUIET_DISTANCE) {
            distance = RandomUtil.randomFloat(0.4f, 1.0f) * (MAX_QUIET_DISTANCE - MIN_QUIET_DISTANCE);
            angle = -casterAngle + RandomUtil.randomFloat(-(float) Math.PI / 4.0f, (float) Math.PI / 4.0f);
        } else // we are inside quiet range
        {
            distance = RandomUtil.randomFloat(0.6f, 1.2f) * (MAX_QUIET_DISTANCE - MIN_QUIET_DISTANCE);
            angle = RandomUtil.randomFloat(0.0f, 2.0f * (float) Math.PI);
        }

        owner.movePositionToFirstCollision(position, distance, angle);
    }
}
