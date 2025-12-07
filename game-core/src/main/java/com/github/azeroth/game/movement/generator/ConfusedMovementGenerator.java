package com.github.azeroth.game.movement.generator;


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

public class ConfusedMovementGenerator extends MovementGenerator {
    private final TimeTracker timer;

    private PathGenerator path;
    private final Position reference;

    public ConfusedMovementGenerator() {
        timer = new TimeTracker(0);
        reference = new Position();

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.HIGHEST;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.CONFUSED;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        if (owner == null || !owner.isAlive()) {
            return;
        }

        // TODO: UNIT_FIELD_FLAGS should not be handled by generators
        owner.setUnitFlag(UnitFlag.CONFUSED);
        owner.stopMoving();

        timer.reset(0);
        reference.relocate(owner.getLocation());
        path = null;
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
            addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();
            path = null;

            return true;
        } else {
            removeFlag(MovementGeneratorFlag.INTERRUPTED);
        }

        // waiting for next move
        timer.update(diff);

        if (hasFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING) && !owner.getMoveSpline().finalized() || timer.passed() && owner.getMoveSpline().finalized()) {
            removeFlag(MovementGeneratorFlag.TRANSITORY);

            Position destination = new Position(reference);
            var distance = 4.0f * RandomUtil.randomFloat(0.0f, 1.0f) - 2.0f;
            var angle = RandomUtil.randomFloat(0.0f, 1.0f) * (float) Math.PI * 2.0f;
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

            owner.addUnitState(UnitState.CONFUSED_MOVE);

            MoveSplineInit init = new MoveSplineInit(owner);
            init.movebyPath(path.getPath());
            init.setWalk(true);
            var traveltime = (int) init.launch();
            timer.reset(traveltime + RandomUtil.randomInt(800, 1500));
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        owner.clearUnitState(UnitState.CONFUSED_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        flags.addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            if (owner.isPlayer()) {
                owner.removeUnitFlag(UnitFlag.CONFUSED);
                owner.stopMoving();
            } else {
                owner.removeUnitFlag(UnitFlag.CONFUSED);
                owner.clearUnitState(UnitState.CONFUSED_MOVE);

                if (owner.getVictim() != null) {
                    owner.setTarget(owner.getVictim().getGUID());
                }
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.CONFUSED;
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlag.SPEED_UPDATE_PENDING);
    }
}
