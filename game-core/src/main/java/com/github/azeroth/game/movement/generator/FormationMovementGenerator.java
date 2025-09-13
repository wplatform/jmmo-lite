package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.AbstractFollower;
import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class FormationMovementGenerator extends MovementGeneratorMedium<Creature> {
    private static final int FORMATION_MOVEMENT_INTERVAL = 1200; // sniffed (3 batch update cycles)
    private final AbstractFollower abstractFollower;
    private final float range;
    private final int point1;
    private final int point2;
    private final timeTracker nextMoveTimer = new timeTracker();
    private float angle;
    private int lastLeaderSplineID;
    private boolean hasPredictedDestination;

    private Position lastLeaderPosition;

    public FormationMovementGenerator(Unit leader, float range, float angle, int point1, int point2) {
        abstractFollower = new AbstractFollower(leader);
        range = range;
        angle = angle;
        point1 = point1;
        point2 = point2;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.FollowFormation;
    }

    @Override
    public void doInitialize(Creature owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Transitory.getValue().getValue() | MovementGeneratorFlags.Deactivated.getValue().getValue());
        addFlag(MovementGeneratorFlags.initialized);

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();

            return;
        }

        nextMoveTimer.reset(0);
    }

    @Override
    public void doReset(Creature owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());

        doInitialize(owner);
    }

    @Override
    public boolean doUpdate(Creature owner, int diff) {
        var target = abstractFollower.getTarget();

        if (owner == null || target == null) {
            return false;
        }

        // Owner cannot move. Reset all fields and wait for next action
        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();
            nextMoveTimer.reset(0);
            hasPredictedDestination = false;

            return true;
        }

        // Update home position
        // If target is not moving and destination has been predicted and if we are on the same spline, we stop as well
        if (target.getMoveSpline().finalized() && target.getMoveSpline().getId() == lastLeaderSplineID && hasPredictedDestination) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();
            nextMoveTimer.reset(0);
            hasPredictedDestination = false;

            return true;
        }

        if (!owner.getMoveSpline().finalized()) {
            owner.setHomePosition(owner.getLocation());
        }

        // Formation leader has launched a new spline, launch a new one for our member as well
        // This action does not reset the regular movement launch cycle interval
        if (!target.getMoveSpline().finalized() && target.getMoveSpline().getId() != lastLeaderSplineID) {
            // Update formation angle
            if (point1 != 0 && target.isCreature()) {
                var formation = target.toCreature().getFormation();

                if (formation != null) {
                    var leader = formation.getLeader();

                    if (leader != null) {
                        var currentWaypoint = leader.CurrentWaypointInfo.nodeId;

                        if (currentWaypoint == point1 || currentWaypoint == point2) {
                            angle = (float) Math.PI * 2 - angle;
                        }
                    }
                }
            }

            launchMovement(owner, target);
            lastLeaderSplineID = target.getMoveSpline().getId();

            return true;
        }

        nextMoveTimer.update(diff);

        if (nextMoveTimer.Passed) {
            nextMoveTimer.reset(FORMATION_MOVEMENT_INTERVAL);

            // Our leader has a different position than on our last check, launch movement.
            if (lastLeaderPosition != target.getLocation()) {
                launchMovement(owner, target);

                return true;
            }
        }

        // We have reached our destination before launching a new movement. Alling facing with leader
        if (owner.hasUnitState(UnitState.FollowFormationMove) && owner.getMoveSpline().finalized()) {
            owner.clearUnitState(UnitState.FollowFormationMove);
            owner.setFacingTo(target.getLocation().getO());
            movementInform(owner);
        }

        return true;
    }

    @Override
    public void doDeactivate(Creature owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
        owner.clearUnitState(UnitState.FollowFormationMove);
    }

    @Override
    public void doFinalize(Creature owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (active) {
            owner.clearUnitState(UnitState.FollowFormationMove);
        }

        if (movementInform && hasFlag(MovementGeneratorFlags.InformEnabled)) {
            movementInform(owner);
        }
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.formation;
    }

    private void launchMovement(Creature owner, Unit target) {
        var relativeAngle = 0.0f;

        // Determine our relative angle to our current spline destination point
        if (!target.getMoveSpline().finalized()) {
            relativeAngle = target.getLocation().getRelativeAngle(new Position(target.getMoveSpline().currentDestination()));
        }

        // Destination calculation
		/*
			According to sniff data, formation members have a periodic move interal of 1,2s.
			Each of these splines has a exact duration of 1650ms +- 1ms when no pathfinding is involved.
			To get a representative result like that we have to predict our formation leader's path
			and apply our formation shape based on that destination.
		*/
        var dest = new Position(target.getLocation());
        var velocity = 0.0f;

        // Formation leader is moving. Predict our destination
        if (!target.getMoveSpline().finalized()) {
            // Pick up leader's spline velocity
            velocity = target.getMoveSpline().velocity;

            // Calculate travel distance to get a 1650ms result
            var travelDist = velocity * 1.65f;

            // Move destination ahead...
            target.movePositionToFirstCollision(dest, travelDist, relativeAngle);
            // ... and apply formation shape
            target.movePositionToFirstCollision(dest, range, angle + relativeAngle);

            var distance = owner.getLocation().getExactDist(dest);

            // Calculate catchup speed mod (Limit to a maximum of 50% of our original velocity
            var velocityMod = Math.min(distance / travelDist, 1.5f);

            // Now we will always stay synch with our leader
            velocity *= velocityMod;
            hasPredictedDestination = true;
        } else {
            // Formation leader is not moving. Just apply the base formation shape on his position.
            target.movePositionToFirstCollision(dest, range, angle + relativeAngle);
            hasPredictedDestination = false;
        }

        // Leader is not moving, so just pick up his default walk speed
        if (velocity == 0.0f) {
            velocity = target.getSpeed(UnitMoveType.Walk);
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        init.moveTo(dest);
        init.setVelocity(velocity);
        init.launch();

        lastLeaderPosition = new Position(target.getLocation());
        owner.addUnitState(UnitState.FollowFormationMove);
        removeFlag(MovementGeneratorFlags.Interrupted);
    }

    private void movementInform(Creature owner) {
        if (owner.getAI() != null) {
            owner.getAI().movementInform(MovementGeneratorType.formation, 0);
        }
    }
}
