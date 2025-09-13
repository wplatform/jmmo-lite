package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class FleeingMovementGenerator<T extends unit> extends MovementGeneratorMedium<T> {
    public static final float MIN_QUIET_DISTANCE = 28.0f;
    public static final float MAX_QUIET_DISTANCE = 43.0f;
    private final TimeTracker timer;
    private final ObjectGuid fleeTargetGUID;
    private PathGenerator path;

    public FleeingMovementGenerator(ObjectGuid fright) {
        fleeTargetGUID = fright;
        timer = new timeTracker();

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.Highest;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Fleeing;
    }

    @Override
    public void doInitialize(T owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Transitory.getValue().getValue() | MovementGeneratorFlags.Deactivated.getValue().getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (owner == null || !owner.isAlive()) {
            return;
        }

        // TODO: UNIT_FIELD_FLAGS should not be handled by generators
        owner.setUnitFlag(UnitFlag.Fleeing);
        path = null;
        setTargetLocation(owner);
    }

    @Override
    public void doReset(T owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        doInitialize(owner);
    }

    @Override
    public boolean doUpdate(T owner, int diff) {
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();
            path = null;

            return true;
        } else {
            removeFlag(MovementGeneratorFlags.Interrupted);
        }

        timer.update(diff);

        if ((hasFlag(MovementGeneratorFlags.SpeedUpdatePending) && !owner.getMoveSpline().finalized()) || (timer.Passed && owner.getMoveSpline().finalized())) {
            removeFlag(MovementGeneratorFlags.Transitory);
            setTargetLocation(owner);
        }

        return true;
    }

    @Override
    public void doDeactivate(T owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.FleeingMove);
    }

    @Override
    public void doFinalize(T owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            if (owner.isPlayer()) {
                owner.removeUnitFlag(UnitFlag.Fleeing);
                owner.clearUnitState(UnitState.FleeingMove);
                owner.stopMoving();
            } else {
                owner.removeUnitFlag(UnitFlag.Fleeing);
                owner.clearUnitState(UnitState.FleeingMove);

                if (owner.getVictim() != null) {
                    owner.setTarget(owner.getVictim().getGUID());
                }
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Fleeing;
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }

    private void setTargetLocation(T owner) {
        if (owner == null || !owner.isAlive()) {
            return;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
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

        owner.addUnitState(UnitState.FleeingMove);

        MoveSplineInit init = new MoveSplineInit(owner);
        init.movebyPath(path.getPath());
        init.setWalk(false);
        var traveltime = (int) init.launch();
        timer.reset(traveltime + RandomUtil.URand(800, 1500));
    }

    private void getPoint(T owner, Position position) {
        float casterDistance, casterAngle;
        var fleeTarget = global.getObjAccessor().GetUnit(owner, fleeTargetGUID);

        if (fleeTarget != null) {
            casterDistance = fleeTarget.getDistance(owner);

            if (casterDistance > 0.2f) {
                casterAngle = fleeTarget.getLocation().GetAbsoluteAngle(owner.getLocation());
            } else {
                casterAngle = RandomUtil.FRand(0.0f, 2.0f * (float) Math.PI);
            }
        } else {
            casterDistance = 0.0f;
            casterAngle = RandomUtil.FRand(0.0f, 2.0f * (float) Math.PI);
        }

        float distance, angle;

        if (casterDistance < MIN_QUIET_DISTANCE) {
            distance = RandomUtil.FRand(0.4f, 1.3f) * (MIN_QUIET_DISTANCE - casterDistance);
            angle = casterAngle + RandomUtil.FRand(-(float) Math.PI / 8.0f, (float) Math.PI / 8.0f);
        } else if (casterDistance > MAX_QUIET_DISTANCE) {
            distance = RandomUtil.FRand(0.4f, 1.0f) * (MAX_QUIET_DISTANCE - MIN_QUIET_DISTANCE);
            angle = -casterAngle + RandomUtil.FRand(-(float) Math.PI / 4.0f, (float) Math.PI / 4.0f);
        } else // we are inside quiet range
        {
            distance = RandomUtil.FRand(0.6f, 1.2f) * (MAX_QUIET_DISTANCE - MIN_QUIET_DISTANCE);
            angle = RandomUtil.FRand(0.0f, 2.0f * (float) Math.PI);
        }

        owner.movePositionToFirstCollision(position, distance, angle);
    }
}
