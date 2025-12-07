package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;
import com.github.azeroth.utils.RandomUtil;

import java.time.Duration;

public class RandomMovementGenerator extends MovementGenerator {
    private final TimeTracker timer;

    private PathGenerator path;
    private Position reference;
    private float wanderDistance;
    private int wanderSteps;


    public RandomMovementGenerator(float spawnDist) {
        this(spawnDist, null);
    }

    public RandomMovementGenerator() {
        this(0.0f, null);
    }

    public RandomMovementGenerator(float spawnDist, Duration duration) {
        timer = new TimeTracker(duration);
        reference = new Position();
        wanderDistance = spawnDist;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.NORMAL;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.ROAMING;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED);

        if (owner == null || !owner.isAlive()) {
            return;
        }
        var creature = owner.toCreature();

        reference = owner.getLocation();
        owner.stopMoving();

        if (wanderDistance == 0f) {
            wanderDistance = creature.getWanderDistance();
        }

        // Retail seems to let a creature walk 2 up to 10 splines before triggering a pause
        wanderSteps = RandomUtil.randomInt(2, 10);

        timer.reset(0);
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

        if (flags.hasFlag(MovementGeneratorFlag.FINALIZED, MovementGeneratorFlag.PAUSED)) {
            return true;
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
            setRandomLocation(owner.toCreature());
        }

        if (timer.passed()) {
            flags.removeFlag(MovementGeneratorFlag.TRANSITORY);
            flags.addFlag(MovementGeneratorFlag.INFORM_ENABLED);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        owner.clearUnitState(UnitState.ROAMING_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.ROAMING_MOVE);
            owner.stopMoving();

            // TODO: Research if this modification is needed, which most likely isnt
            owner.setWalk(false);
        }

        if (movementInform && hasFlag(MovementGeneratorFlag.INFORM_ENABLED) && owner.isAIEnabled()) {
            if (owner.getAi() instanceof CreatureAI ai) {
                ai.movementInform(MovementGeneratorType.RANDOM, 0);
            }
        }
    }


    @Override
    public void pause() {
        pause(0);
    }

    @Override
    public void pause(int timer) {
        if (timer != 0) {
            addFlag(MovementGeneratorFlag.TIMED_PAUSED);
            this.timer.reset(timer);
            removeFlag(MovementGeneratorFlag.PAUSED);
        } else {
            addFlag(MovementGeneratorFlag.PAUSED);
            removeFlag(MovementGeneratorFlag.TIMED_PAUSED);
        }
    }


    @Override
    public void resume() {
        resume(0);
    }

    @Override
    public void resume(int overrideTimer) {
        if (overrideTimer != 0) {
            timer.reset(overrideTimer);
        }

        removeFlag(MovementGeneratorFlags.paused);
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.random;
    }

    private void setRandomLocation(Creature owner) {
        if (owner == null) {
            return;
        }

        if (owner.hasUnitState(UnitState.NotMove.getValue() | UnitState.LostControl.getValue()) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();
            path = null;

            return;
        }

        Position position = new Position(reference);
        var distance = RandomUtil.FRand(0.0f, wanderDistance);
        var angle = RandomUtil.FRand(0.0f, (float) Math.PI * 2.0f);
        owner.movePositionToFirstCollision(position, distance, angle);

        // Check if the destination is in LOS
        if (!owner.isWithinLOS(position.getX(), position.getY(), position.getZ())) {
            // Retry later on
            timer.reset(200);

            return;
        }

        if (path == null) {
            path = new PathGenerator(owner);
            path.setPathLengthLimit(30.0f);
        }

        var result = path.calculatePath(position);

        // PATHFIND_FARFROMPOLY shouldn't be checked as creatures in water are most likely far from poly
        if (!result || path.getPathType().hasFlag(PathType.NOPATH) || path.getPathType().hasFlag(PathType.SHORTCUT)) // || path.getPathType().hasFlag(PathType.FARFROMPOLY))
        {
            timer.reset(100);

            return;
        }

        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.TimedPaused.getValue());

        owner.addUnitState(UnitState.RoamingMove);

        var walk = true;

        switch (owner.getMovementTemplate().getRandom()) {
            case CanRun:
                walk = owner.isWalking();

                break;
            case AlwaysRun:
                walk = false;

                break;
            default:
                break;
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        init.movebyPath(path.getPath());
        init.setWalk(walk);
        var splineDuration = (int) init.launch();

        --_wanderSteps;

        if (wanderSteps != 0) // Creature has yet to do steps before pausing
        {
            timer.reset(splineDuration);
        } else {
            // Creature has made all its steps, time for a little break
            timer.reset(splineDuration + RandomUtil.URand(4, 10) * time.InMilliseconds); // Retails seems to use rounded numbers so we do as well
            wanderSteps = RandomUtil.URand(2, 10);
        }

        // Call for creature group update
        owner.signalFormationMovement();
    }
}
