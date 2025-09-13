package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class ConfusedMovementGenerator<T extends unit> extends MovementGeneratorMedium<T> {
    private final TimeTracker timer;

    private PathGenerator path;
    private Position reference;

    public ConfusedMovementGenerator() {
        timer = new timeTracker();
        reference = new Position();

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.Highest;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Confused;
    }

    @Override
    public void doInitialize(T owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Transitory.getValue().getValue() | MovementGeneratorFlags.Deactivated.getValue().getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (!owner || !owner.isAlive()) {
            return;
        }

        // TODO: UNIT_FIELD_FLAGS should not be handled by generators
        owner.setUnitFlag(UnitFlag.Confused);
        owner.stopMoving();

        timer.reset(0);
        reference = owner.getLocation();
        path = null;
    }

    @Override
    public void doReset(T owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        doInitialize(owner);
    }

    @Override
    public boolean doUpdate(T owner, int diff) {
        if (!owner || !owner.isAlive()) {
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

        // waiting for next move
        timer.update(diff);

        if ((hasFlag(MovementGeneratorFlags.SpeedUpdatePending) && !owner.getMoveSpline().finalized()) || (timer.Passed && owner.getMoveSpline().finalized())) {
            removeFlag(MovementGeneratorFlags.Transitory);

            Position destination = new Position(reference);
            var distance = (float) (4.0f * RandomUtil.FRand(0.0f, 1.0f) - 2.0f);
            var angle = RandomUtil.FRand(0.0f, 1.0f) * (float) Math.PI * 2.0f;
            owner.movePositionToFirstCollision(destination, distance, angle);

            // Check if the destination is in LOS
            if (!owner.isWithinLOS(destination.getX(), destination.getY(), destination.getZ())) {
                // Retry later on
                timer.reset(200);

                return true;
            }

            if (path == null) {
                path = new PathGenerator(owner);
                path.setPathLengthLimit(30.0f);
            }

            var result = path.calculatePath(destination);

            if (!result || path.getPathType().hasFlag(PathType.NOPATH) || path.getPathType().hasFlag(PathType.SHORTCUT) || path.getPathType().hasFlag(PathType.FARFROMPOLY)) {
                timer.reset(100);

                return true;
            }

            owner.addUnitState(UnitState.ConfusedMove);

            MoveSplineInit init = new MoveSplineInit(owner);
            init.movebyPath(path.getPath());
            init.setWalk(true);
            var traveltime = (int) init.launch();
            timer.reset(traveltime + RandomUtil.URand(800, 1500));
        }

        return true;
    }

    @Override
    public void doDeactivate(T owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.ConfusedMove);
    }

    @Override
    public void doFinalize(T owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            if (owner.isPlayer()) {
                owner.removeUnitFlag(UnitFlag.Confused);
                owner.stopMoving();
            } else {
                owner.removeUnitFlag(UnitFlag.Confused);
                owner.clearUnitState(UnitState.ConfusedMove);

                if (owner.getVictim()) {
                    owner.setTarget(owner.getVictim().getGUID());
                }
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Confused;
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }
}
