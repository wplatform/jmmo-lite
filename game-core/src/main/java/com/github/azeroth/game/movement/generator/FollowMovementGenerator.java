package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.ai.CreatureAI;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitMoveType;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.*;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;

import java.util.Objects;

public class FollowMovementGenerator extends MovementGenerator {

    private static final int CHECK_INTERVAL = 100;
    private static final float FOLLOW_RANGE_TOLERANCE = 1.0f;
    private final float range;
    private final TimeTracker checkTimer;
    private final AbstractFollower abstractFollower;
    private final ChaseAngle angle;
    private PathGenerator path;
    private Position lastTargetPosition;

    public FollowMovementGenerator(Unit target, float range, ChaseAngle angle) {
        abstractFollower = new AbstractFollower(target);
        this.range = range;
        this.angle = angle;

        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.NORMAL;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.FOLLOW;
        checkTimer = new TimeTracker(CHECK_INTERVAL);
    }

    private static boolean positionOkay(Unit owner, Unit target, float range) {
        return positionOkay(owner, target, range, null);
    }

    private static boolean positionOkay(Unit owner, Unit target, float range, ChaseAngle angle) {
        if (owner.getLocation().getExactDistSq(target.getLocation()) > Math.pow(owner.getCombatReach() + target.getCombatReach() + range, 2)) {
            return false;
        }
        return angle == null || angle.isAngleOkay(target.getLocation().getRelativeAngle(owner.getLocation()));
    }

    private static void doMovementInform(Unit owner, Unit target) {
        if (!owner.isCreature()) {
            return;
        }
        if (owner.getAi() instanceof CreatureAI ai) {
            ai.movementInform(MovementGeneratorType.FOLLOW, target.getGUID().entry());
        }
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.addFlag(MovementGeneratorFlag.INITIALIZED, MovementGeneratorFlag.INFORM_ENABLED);

        owner.stopMoving();
        updatePetSpeed(owner);
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
        // owner might be dead or gone
        if (owner == null || !owner.isAlive()) {
            return false;
        }

        // our target might have gone away
        var target = abstractFollower.getTarget();

        if (target == null || !target.isInWorld()) {
            return false;
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE) || owner.isMovementPreventedByCasting()) {
            path = null;
            owner.stopMoving();
            lastTargetPosition = null;

            return true;
        }

        checkTimer.update(diff);

        if (checkTimer.passed()) {
            checkTimer.reset(CHECK_INTERVAL);

            if (flags.hasFlag(MovementGeneratorFlag.INFORM_ENABLED) && positionOkay(owner, target, range, angle)) {
                flags.removeFlag(MovementGeneratorFlag.INFORM_ENABLED);
                path = null;
                owner.stopMoving();
                lastTargetPosition = new Position();
                doMovementInform(owner, target);

                return true;
            }
        }

        if (owner.hasUnitState(UnitState.FOLLOW_MOVE) && owner.getMoveSpline().finalized()) {
            flags.removeFlag(MovementGeneratorFlag.INFORM_ENABLED);
            path = null;
            owner.clearUnitState(UnitState.FOLLOW_MOVE);
            doMovementInform(owner, target);
        }

        if (lastTargetPosition == null || lastTargetPosition.getExactDistSq(target.getLocation()) > 0.0f) {
            lastTargetPosition = new Position(target.getLocation());

            if (owner.hasUnitState(UnitState.FOLLOW_MOVE) || !positionOkay(owner, target, range + FOLLOW_RANGE_TOLERANCE)) {
                if (path == null) {
                    path = new PathGenerator(owner);
                }


                // select angle
                float tAngle;
                var curAngle = target.getLocation().getRelativeAngle(owner.getLocation());

                if (angle.isAngleOkay(curAngle)) {
                    tAngle = curAngle;
                } else {
                    var diffUpper = Position.normalizeOrientation(curAngle - angle.upperBound());
                    var diffLower = Position.normalizeOrientation(angle.lowerBound() - curAngle);

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
                var oPet = owner.toPet();

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

                owner.addUnitState(UnitState.FOLLOW_MOVE);
                addFlag(MovementGeneratorFlag.INFORM_ENABLED);

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
        flags.addFlag(MovementGeneratorFlag.DEACTIVATED);
        flags.removeFlag(MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.INFORM_ENABLED);
        owner.clearUnitState(UnitState.FOLLOW_MOVE);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        flags.addFlag(MovementGeneratorFlag.FINALIZED);

        if (active) {
            owner.clearUnitState(UnitState.FOLLOW_MOVE);
            updatePetSpeed(owner);
        }
    }

    public final Unit getTarget() {
        return abstractFollower.getTarget();
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.FOLLOW;
    }

    @Override
    public void unitSpeedChanged() {
        lastTargetPosition = null;
    }

    private void updatePetSpeed(Unit owner) {
        var oPet = owner.toPet();

        if (oPet != null) {
            if (abstractFollower.getTarget() == null || Objects.equals(abstractFollower.getTarget().getGUID(), owner.getOwnerGUID())) {
                oPet.updateSpeed(UnitMoveType.RUN);
                oPet.updateSpeed(UnitMoveType.WALK);
                oPet.updateSpeed(UnitMoveType.SWIM);
            }
        }
    }
}
