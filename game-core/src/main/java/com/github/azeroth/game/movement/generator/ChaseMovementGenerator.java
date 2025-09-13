package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

class ChaseMovementGenerator extends MovementGenerator {
    private static final int RANGE_CHECK_INTERVAL = 100; // time (ms) until we attempt to recalculate
    private final TimeTracker rangeCheckTimer;
    private final boolean movingTowards = true;
    private final AbstractFollower abstractFollower;

    private final ChaseRange range;
    private final ChaseAngle angle;

    private PathGenerator path;
    private Position lastTargetPosition;
    private boolean mutualChase = true;

    public ChaseMovementGenerator(Unit target, ChaseRange range, ChaseAngle angle) {
        abstractFollower = new AbstractFollower(target);
        range = range;
        angle = angle;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.chase;

        rangeCheckTimer = new timeTracker(RANGE_CHECK_INTERVAL);
    }

    private static boolean hasLostTarget(Unit owner, Unit target) {
        return owner.getVictim() != target;
    }

    private static boolean isMutualChase(Unit owner, Unit target) {
        if (target.getMotionMaster().getCurrentMovementGeneratorType() != MovementGeneratorType.chase) {
            return false;
        }

        MovementGenerator tempVar = target.getMotionMaster().getCurrentMovementGenerator();
        var movement = tempVar instanceof ChaseMovementGenerator ? (ChaseMovementGenerator) tempVar : null;

        if (movement != null) {
            return movement.getTarget() == owner;
        }

        return false;
    }

    private static boolean positionOkay(Unit owner, Unit target, Float minDistance, Float maxDistance, ChaseAngle angle) {
        var distSq = owner.getLocation().getExactDistSq(target.getLocation());

        if (minDistance != null && distSq < minDistance.floatValue() * minDistance.floatValue()) {
            return false;
        }

        if (maxDistance != null && distSq > maxDistance.floatValue() * maxDistance.floatValue()) {
            return false;
        }

        if (angle != null && !angle.getValue().isAngleOkay(target.getLocation().getRelativeAngle(owner.getLocation()))) {
            return false;
        }

        if (!owner.isWithinLOSInMap(target)) {
            return false;
        }

        return true;
    }

    private static void doMovementInform(Unit owner, Unit target) {
        if (!owner.isCreature()) {
            return;
        }

        var ai = owner.toCreature().getAI();

        if (ai != null) {
            ai.movementInform(MovementGeneratorType.chase, (int) target.getGUID().getCounter());
        }
    }

    @Override
    public void initialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized.getValue() | MovementGeneratorFlags.InformEnabled.getValue());

        path = null;
        lastTargetPosition = null;
    }

    @Override
    public void reset(Unit owner) {
        removeFlag(MovementGeneratorFlags.Deactivated);
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        // owner might be dead or gone (can we even get nullptr here?)
        if (!owner || !owner.isAlive()) {
            return false;
        }

        // our target might have gone away
        var target = abstractFollower.getTarget();

        if (target == null || !target.isInWorld()) {
            return false;
        }

        // the owner might be unable to move (rooted or casting), or we have lost the target, pause movement
        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting() || hasLostTarget(owner, target)) {
            owner.stopMoving();
            lastTargetPosition = null;
            var cOwner = owner.toCreature();

            if (cOwner != null) {
                cOwner.setCannotReachTarget(false);
            }

            return true;
        }

        var mutualChase = isMutualChase(owner, target);
        var hitboxSum = owner.getCombatReach() + target.getCombatReach();

        if (ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH > hitboxSum) {
            hitboxSum = ObjectDefine.DEFAULT_PLAYER_COMBAT_REACH;
        }

        var minRange = range != null ? range.getValue().minRange + hitboxSum : SharedConst.contactDistance;
        var minTarget = (range != null ? range.getValue().MinTolerance : 0.0f) + hitboxSum;
        var maxRange = range != null ? range.getValue().maxRange + hitboxSum : owner.getMeleeRange(target); // melee range already includes hitboxes
        var maxTarget = range != null ? range.getValue().maxTolerance + hitboxSum : SharedConst.contactDistance + hitboxSum;
        var angle = mutualChase ? null : angle;

        // periodically check if we're already in the expected range...
        rangeCheckTimer.update(diff);

        if (rangeCheckTimer.Passed) {
            rangeCheckTimer.reset(RANGE_CHECK_INTERVAL);

            if (hasFlag(MovementGeneratorFlags.InformEnabled) && positionOkay(owner, target, _movingTowards ? null : minTarget, _movingTowards ? maxTarget : null, angle)) {
                removeFlag(MovementGeneratorFlags.InformEnabled);
                path = null;

                var cOwner = owner.toCreature();

                if (cOwner != null) {
                    cOwner.setCannotReachTarget(false);
                }

                owner.stopMoving();
                owner.setInFront(target);
                doMovementInform(owner, target);

                return true;
            }
        }

        var isEvading = false;

        // if we're done moving, we want to clean up
        if (owner.hasUnitState(UnitState.ChaseMove) && owner.getMoveSpline().finalized()) {
            removeFlag(MovementGeneratorFlags.InformEnabled);
            path = null;
            var cOwner = owner.toCreature();

            if (cOwner != null) {
                cOwner.setCannotReachTarget(false);
            }

            owner.clearUnitState(UnitState.ChaseMove);
            owner.setInFront(target);
            doMovementInform(owner, target);
        }

        // if the target moved, we have to consider whether to adjust
        if (lastTargetPosition == null || target.getLocation() != lastTargetPosition || mutualChase != mutualChase) {
            lastTargetPosition = new Position(target.getLocation());
            mutualChase = mutualChase;

            if (owner.hasUnitState(UnitState.ChaseMove) || !positionOkay(owner, target, minRange, maxRange, angle)) {
                var cOwner = owner.toCreature();

                // can we get to the target?
                if (cOwner != null && !target.isInAccessiblePlaceFor(cOwner)) {
                    cOwner.setCannotReachTarget(true);
                    cOwner.stopMoving();
                    path = null;

                    return true;
                }

                // figure out which way we want to move
                var moveToward = !owner.getLocation().isInDist(target.getLocation(), maxRange);

                // make a new path if we have to...
                if (path == null || moveToward != movingTowards) {
                    path = new PathGenerator(owner);
                }

                var pos = new Position();
                boolean shortenPath;

                // if we want to move toward the target and there's no fixed angle...
                if (moveToward && !angle != null) {
                    // ...we'll pathfind to the center, then shorten the path
                    pos = target.getLocation().Copy();
                    shortenPath = true;
                } else {
                    // otherwise, we fall back to nearpoint finding
                    target.getNearPoint(owner, pos, (moveToward ? maxTarget : minTarget) - hitboxSum, angle != null ? target.getLocation().toAbsoluteAngle(angle.getValue().relativeAngle) : target.getLocation().getAbsoluteAngle(owner.getLocation()));
                    shortenPath = false;
                }

                if (owner.isHovering()) {
                    owner.updateAllowedPositionZ(pos);
                }

                var success = path.calculatePath(pos, owner.canFly());

                if (!success || path.getPathType().hasFlag(PathType.NOPATH)) {
                    if (cOwner) {
                        cOwner.setCannotReachTarget(true);
                    }

                    owner.stopMoving();

                    return true;
                }

                if (shortenPath) {
                    path.shortenPathUntilDist(target.getLocation(), maxTarget);
                }

                if (cOwner) {
                    cOwner.setCannotReachTarget(false);
                }

                cOwner.setCannotReachTarget(false);

                var walk = false;

                if (cOwner && !cOwner.isPet()) {
                    switch (cOwner.getMovementTemplate().getChase()) {
                        case CanWalk:
                            walk = owner.isWalking();

                            break;
                        case AlwaysWalk:
                            walk = true;

                            break;
                        default:
                            break;
                    }
                }

                owner.addUnitState(UnitState.ChaseMove);
                addFlag(MovementGeneratorFlags.InformEnabled);

                MoveSplineInit init = new MoveSplineInit(owner);
                init.movebyPath(path.getPath());
                init.setWalk(walk);
                init.setFacing(target);
                init.launch();
            }
        }

        // and then, finally, we're done for the tick
        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.InformEnabled.getValue());
        owner.clearUnitState(UnitState.ChaseMove);
        var cOwner = owner.toCreature();

        if (cOwner != null) {
            cOwner.setCannotReachTarget(false);
        }
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.ChaseMove);
            var cOwner = owner.toCreature();

            if (cOwner != null) {
                cOwner.setCannotReachTarget(false);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.chase;
    }

    @Override
    public void unitSpeedChanged() {
        lastTargetPosition = null;
    }

    public final Unit getTarget() {
        return abstractFollower.getTarget();
    }
}
