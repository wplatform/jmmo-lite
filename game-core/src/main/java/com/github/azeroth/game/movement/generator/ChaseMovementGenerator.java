package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;

public class ChaseMovementGenerator extends MovementGenerator {
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
        this.abstractFollower = new AbstractFollower(target);
        this.range = range;
        this.angle = angle;

        this.mode = MovementGeneratorMode.DEFAULT;
        this.priority = MovementGeneratorPriority.NORMAL;
        this.flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        this.baseUnitState = UnitState.CHASE;

        rangeCheckTimer = new TimeTracker(RANGE_CHECK_INTERVAL);
    }

    private static boolean hasLostTarget(Unit owner, Unit target) {
        return owner.getVictim() != target;
    }

    private static boolean isMutualChase(Unit owner, Unit target) {
        if (target.getMotionMaster().getCurrentMovementGeneratorType() != MovementGeneratorType.CHASE) {
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

        if (angle != null && !angle.isAngleOkay(target.getLocation().getRelativeAngle(owner.getLocation()))) {
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

        if (owner.getAi() instanceof CreatureAI ai) {
            ai.movementInform(MovementGeneratorType.CHASE, target.getGUID().entry());
        }
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED, MovementGeneratorFlag.INFORM_ENABLED);
        path = null;
        lastTargetPosition = null;
    }

    @Override
    public void reset(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.DEACTIVATED);
        initialize(owner);
    }

    @Override
    public boolean update(Unit owner, int diff) {
        // owner might be dead or gone (can we even get nullptr here?)
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        // our target might have gone away
        var target = abstractFollower.getTarget();

        if (target == null || !target.isInWorld()) {
            return false;
        }

        // the owner might be unable to move (rooted or casting), or we have lost the target, pause movement
        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting() || hasLostTarget(owner, target)) {
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

        var minRange = range != null ? range.minRange + hitboxSum : ObjectDefine.CONTACT_DISTANCE;
        var minTarget = (range != null ? range.minTolerance : 0.0f) + hitboxSum;
        var maxRange = range != null ? range.maxRange + hitboxSum : owner.getMeleeRange(target); // melee range already includes hitboxes
        var maxTarget = range != null ? range.maxTolerance + hitboxSum : ObjectDefine.CONTACT_DISTANCE + hitboxSum;
        var angle = mutualChase ? null : this.angle;

        // periodically check if we're already in the expected range...
        rangeCheckTimer.update(diff);

        if (rangeCheckTimer.passed()) {
            rangeCheckTimer.reset(RANGE_CHECK_INTERVAL);

            if (hasFlag(MovementGeneratorFlag.INFORM_ENABLED) && positionOkay(owner, target, movingTowards ? null : minTarget, movingTowards ? maxTarget : null, angle)) {
                removeFlag(MovementGeneratorFlag.INFORM_ENABLED);
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
        if (owner.hasUnitState(UnitState.CHASE_MOVE) && owner.getMoveSpline().finalized()) {
            removeFlag(MovementGeneratorFlag.INFORM_ENABLED);
            path = null;
            var cOwner = owner.toCreature();

            if (cOwner != null) {
                cOwner.setCannotReachTarget(false);
            }

            owner.clearUnitState(UnitState.CHASE_MOVE);
            owner.setInFront(target);
            doMovementInform(owner, target);
        }

        // if the target moved, we have to consider whether to adjust
        if (lastTargetPosition == null || target.getLocation() != lastTargetPosition || this.mutualChase != mutualChase) {
            lastTargetPosition = new Position(target.getLocation());
            this.mutualChase = mutualChase;

            if (owner.hasUnitState(UnitState.CHASE_MOVE) || !positionOkay(owner, target, minRange, maxRange, angle)) {
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
                if (moveToward && angle != null) {
                    // ...we'll pathfind to the center, then shorten the path
                    pos.relocate(target.getLocation());
                    shortenPath = true;
                } else {
                    // otherwise, we fall back to nearpoint finding
                    target.getNearPoint(owner, pos, (moveToward ? maxTarget : minTarget) - hitboxSum, angle != null ? target.getLocation().toAbsoluteAngle(angle.relativeAngle) : target.getLocation().getAbsoluteAngle(owner.getLocation()));
                    shortenPath = false;
                }

                if (owner.isHovering()) {
                    owner.updateAllowedPositionZ(pos);
                }

                var success = path.calculatePath(pos, owner.canFly());

                if (!success || path.getPathType().hasFlag(PathType.NOPATH)) {
                    if (cOwner != null) {
                        cOwner.setCannotReachTarget(true);
                    }

                    owner.stopMoving();

                    return true;
                }

                if (shortenPath) {
                    path.shortenPathUntilDist(target.getLocation(), maxTarget);
                }

                if (cOwner != null) {
                    cOwner.setCannotReachTarget(false);
                }

                var walk = false;

                if (cOwner != null && !cOwner.isPet()) {
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

                owner.addUnitState(UnitState.CHASE_MOVE);
                addFlag(MovementGeneratorFlag.INFORM_ENABLED);

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
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        flags.removeFlag(MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.INFORM_ENABLED);
        owner.clearUnitState(UnitState.CHASE_MOVE);
        var cOwner = owner.toCreature();

        if (cOwner != null) {
            cOwner.setCannotReachTarget(false);
        }
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        flags.addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.CHASE_MOVE);
            var cOwner = owner.toCreature();

            if (cOwner != null) {
                cOwner.setCannotReachTarget(false);
            }
        }
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.CHASE;
    }

    @Override
    public void unitSpeedChanged() {
        lastTargetPosition = null;
    }

    public final Unit getTarget() {
        return abstractFollower.getTarget();
    }
}
