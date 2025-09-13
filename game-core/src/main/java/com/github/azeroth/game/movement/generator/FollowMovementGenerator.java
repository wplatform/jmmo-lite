package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.enums.PathType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class FollowMovementGenerator extends MovementGenerator {

    private static final int CHECK_INTERVAL = 100;
    private static final float FOLLOW_RANGE_TOLERANCE = 1.0f;
    private final float range;
    private final TimeTracker checkTimer;
    private final AbstractFollower abstractFollower;
    private chaseAngle angle = new chaseAngle();
    private PathGenerator path;
    private Position lastTargetPosition;

    public FollowMovementGenerator(Unit target, float range, ChaseAngle angle) {
        abstractFollower = new AbstractFollower(target);
        range = range;
        angle = angle;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Follow;

        checkTimer = new timeTracker(CHECK_INTERVAL);
    }

    private static boolean positionOkay(Unit owner, Unit target, float range) {
        return positionOkay(owner, target, range, null);
    }

    private static boolean positionOkay(Unit owner, Unit target, float range, ChaseAngle angle) {
        if (owner.getLocation().getExactDistSq(target.getLocation()) > (owner.getCombatReach() + target.getCombatReach() + range) * (owner.getCombatReach() + target.getCombatReach() + range)) {
            return false;
        }

        return !angle != null || angle.getValue().isAngleOkay(target.getLocation().getRelativeAngle(owner.getLocation()));
    }

    private static void doMovementInform(Unit owner, Unit target) {
        if (!owner.isCreature()) {
            return;
        }

        var ai = owner.toCreature().getAI();

        if (ai != null) {
            ai.movementInform(MovementGeneratorType.Follow, (int) target.getGUID().getCounter());
        }
    }

    @Override
    public void initialize(Unit owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized.getValue() | MovementGeneratorFlags.InformEnabled.getValue());

        owner.stopMoving();
        updatePetSpeed(owner);
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
        // owner might be dead or gone
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        // our target might have gone away
        var target = abstractFollower.getTarget();

        if (target == null || !target.isInWorld()) {
            return false;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            path = null;
            owner.stopMoving();
            lastTargetPosition = null;

            return true;
        }

        checkTimer.update(diff);

        if (checkTimer.Passed) {
            checkTimer.reset(CHECK_INTERVAL);

            if (hasFlag(MovementGeneratorFlags.InformEnabled) && positionOkay(owner, target, range, angle)) {
                removeFlag(MovementGeneratorFlags.InformEnabled);
                path = null;
                owner.stopMoving();
                lastTargetPosition = new Position();
                doMovementInform(owner, target);

                return true;
            }
        }

        if (owner.hasUnitState(UnitState.FollowMove) && owner.getMoveSpline().finalized()) {
            removeFlag(MovementGeneratorFlags.InformEnabled);
            path = null;
            owner.clearUnitState(UnitState.FollowMove);
            doMovementInform(owner, target);
        }

        if (lastTargetPosition == null || lastTargetPosition.getExactDistSq(target.getLocation()) > 0.0f) {
            lastTargetPosition = new Position(target.getLocation());

            if (owner.hasUnitState(UnitState.FollowMove) || !positionOkay(owner, target, range + FOLLOW_RANGE_TOLERANCE)) {
                if (path == null) {
                    path = new PathGenerator(owner);
                }


                // select angle
                float tAngle;
                var curAngle = target.getLocation().getRelativeAngle(owner.getLocation());

                if (angle.isAngleOkay(curAngle)) {
                    tAngle = curAngle;
                } else {
                    var diffUpper = position.normalizeOrientation(curAngle - angle.upperBound());
                    var diffLower = position.normalizeOrientation(angle.lowerBound() - curAngle);

                    if (diffUpper < diffLower) {
                        tAngle = angle.upperBound();
                    } else {
                        tAngle = angle.lowerBound();
                    }
                }

                var newPos = new Position();
                target.getNearPoint(owner, newPos, range, target.getLocation().toAbsoluteAngle(tAngle));

                if (owner.isHovering()) {
                    owner.updateAllowedPositionZ(newPos);
                }

                // pets are allowed to "cheat" on pathfinding when following their master
                var allowShortcut = false;
                var oPet = owner.getAsPet();

                if (oPet != null) {
                    if (Objects.equals(target.getGUID(), oPet.getOwnerGUID())) {
                        allowShortcut = true;
                    }
                }

                var success = path.calculatePath(newPos, allowShortcut);

                if (!success || path.getPathType().hasFlag(PathType.NOPATH)) {
                    owner.stopMoving();

                    return true;
                }

                owner.addUnitState(UnitState.FollowMove);
                addFlag(MovementGeneratorFlags.InformEnabled);

                MoveSplineInit init = new MoveSplineInit(owner);
                init.movebyPath(path.getPath());
                init.setWalk(target.isWalking());
                init.setFacing(target.getLocation().getO());
                init.launch();
            }
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.InformEnabled.getValue());
        owner.clearUnitState(UnitState.FollowMove);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.FollowMove);
            updatePetSpeed(owner);
        }
    }

    public final Unit getTarget() {
        return abstractFollower.getTarget();
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Follow;
    }

    @Override
    public void unitSpeedChanged() {
        lastTargetPosition = null;
    }

    private void updatePetSpeed(Unit owner) {
        var oPet = owner.getAsPet();

        if (oPet != null) {
            if (!abstractFollower.getTarget() || Objects.equals(abstractFollower.getTarget().getGUID(), owner.getOwnerGUID())) {
                oPet.updateSpeed(UnitMoveType.run);
                oPet.updateSpeed(UnitMoveType.Walk);
                oPet.updateSpeed(UnitMoveType.swim);
            }
        }
    }
}
