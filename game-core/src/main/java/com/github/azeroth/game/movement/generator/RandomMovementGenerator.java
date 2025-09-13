package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class RandomMovementGenerator extends MovementGeneratorMedium<Creature> {
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
        timer = new timeTracker(duration);
        reference = new Position();
        wanderDistance = spawnDist;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }

    @Override
    public void doInitialize(Creature owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Transitory.getValue().getValue() | MovementGeneratorFlags.Deactivated.getValue().getValue().getValue() | MovementGeneratorFlags.paused.getValue().getValue().getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (owner == null || !owner.isAlive()) {
            return;
        }

        reference = owner.getLocation();
        owner.stopMoving();

        if (wanderDistance == 0f) {
            wanderDistance = owner.getWanderDistance();
        }

        // Retail seems to let a creature walk 2 up to 10 splines before triggering a pause
        wanderSteps = RandomUtil.URand(2, 10);

        timer.reset(0);
        path = null;
    }

    @Override
    public void doReset(Creature owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        doInitialize(owner);
    }

    @Override
    public boolean doUpdate(Creature owner, int diff) {
        if (!owner || !owner.isAlive()) {
            return true;
        }

        if (hasFlag(MovementGeneratorFlags.Finalized.getValue() | MovementGeneratorFlags.paused.getValue())) {
            return true;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();
            path = null;

            return true;
        } else {
            removeFlag(MovementGeneratorFlags.Interrupted);
        }

        synchronized (reference) {
            timer.update(diff);

            if ((hasFlag(MovementGeneratorFlags.SpeedUpdatePending) && !owner.getMoveSpline().finalized()) || (timer.Passed && owner.getMoveSpline().finalized())) {
                setRandomLocation(owner);
            }
        }

        if (timer.Passed) {
            removeFlag(MovementGeneratorFlags.Transitory);
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void doDeactivate(Creature owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.RoamingMove);
    }

    @Override
    public void doFinalize(Creature owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.RoamingMove);
            owner.stopMoving();

            // TODO: Research if this modification is needed, which most likely isnt
            owner.setWalk(false);
        }

        com.github.azeroth.game.ai.CreatureAI ai;
        tangible.OutObject<com.github.azeroth.game.ai.CreatureAI> tempOut_ai = new tangible.OutObject<com.github.azeroth.game.ai.CreatureAI>();
        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled) && owner.isAIEnabled() && owner.tryGetCreatureAI(tempOut_ai)) {
            ai = tempOut_ai.outArgValue;
            ai.movementInform(MovementGeneratorType.random, 0);
        } else {
            ai = tempOut_ai.outArgValue;
        }
    }


    @Override
    public void pause() {
        pause(0);
    }

    @Override
    public void pause(int timer) {
        if (timer != 0) {
            addFlag(MovementGeneratorFlags.TimedPaused);
            timer.reset(timer);
            removeFlag(MovementGeneratorFlags.paused);
        } else {
            addFlag(MovementGeneratorFlags.paused);
            removeFlag(MovementGeneratorFlags.TimedPaused);
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
