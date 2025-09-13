package com.github.azeroth.game.movement;


import com.github.azeroth.game.domain.misc.WaypointPath;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;

public class WaypointMovementGenerator extends MovementGeneratorMedium<Creature> {
    private final TimeTracker nextMoveTime;
    private final boolean repeating;
    private final boolean loadedFromDB;
    private int pathId;

    private WaypointPath path;
    private int currentNode;


    public WaypointMovementGenerator(int pathId) {
        this(pathId, true);
    }

    public WaypointMovementGenerator() {
        this(0, true);
    }

    public WaypointMovementGenerator(int pathId, boolean repeating) {
        nextMoveTime = new timeTracker(0);
        pathId = pathId;
        repeating = repeating;
        loadedFromDB = true;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }


    public WaypointMovementGenerator(WaypointPath path) {
        this(path, true);
    }

    public WaypointMovementGenerator(WaypointPath path, boolean repeating) {
        nextMoveTime = new timeTracker(0);
        repeating = repeating;
        path = path;

        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.NORMAL;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.Roaming;
    }


    @Override
    public void pause() {
        pause(0);
    }

    @Override
    public void pause(int timer) {
        if (timer != 0) {
            // Don't try to paused an already paused generator
            if (hasFlag(MovementGeneratorFlags.paused)) {
                return;
            }

            addFlag(MovementGeneratorFlags.TimedPaused);
            nextMoveTime.reset(timer);
            removeFlag(MovementGeneratorFlags.paused);
        } else {
            addFlag(MovementGeneratorFlags.paused);
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
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
            nextMoveTime.reset(overrideTimer);
        }

        if (nextMoveTime.Passed) {
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
        }

        removeFlag(MovementGeneratorFlags.paused);
    }

    @Override
    public boolean getResetPosition(Unit owner, tangible.OutObject<Float> x, tangible.OutObject<Float> y, tangible.OutObject<Float> z) {
        x.outArgValue = y.outArgValue = z.outArgValue = 0;

        // prevent a crash at empty waypoint path.
        if (path == null || path.nodes.isEmpty()) {
            return false;
        }

        var waypoint = path.nodes.ElementAt(currentNode);

        x.outArgValue = waypoint.x;
        y.outArgValue = waypoint.y;
        z.outArgValue = waypoint.z;

        return true;
    }

    @Override
    public void doInitialize(Creature owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Transitory.getValue().getValue() | MovementGeneratorFlags.Deactivated.getValue().getValue());

        if (loadedFromDB) {
            if (pathId == 0) {
                pathId = owner.getWaypointPath();
            }

            path = global.getWaypointMgr().getPath(pathId);
        }

        if (path == null) {
            Logs.SQL.error(String.format("WaypointMovementGenerator::DoInitialize: couldn't load path for creature (%1$s) (_pathId: %2$s)", owner.getGUID(), pathId));

            return;
        }

        owner.stopMoving();

        nextMoveTime.reset(1000);
    }

    @Override
    public void doReset(Creature owner) {
        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.Deactivated.getValue());

        owner.stopMoving();

        if (!hasFlag(MovementGeneratorFlags.Finalized) && nextMoveTime.Passed) {
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
        }
    }

    @Override
    public boolean doUpdate(Creature owner, int diff) {
        if (!owner || !owner.isAlive()) {
            return true;
        }

        if (hasFlag(MovementGeneratorFlags.Finalized.getValue() | MovementGeneratorFlags.paused.getValue()) || path == null || path.nodes.isEmpty()) {
            return true;
        }

        if (owner.hasUnitState(UnitState.NotMove.getValue() | UnitState.LostControl.getValue()) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlags.Interrupted);
            owner.stopMoving();

            return true;
        }

        if (hasFlag(MovementGeneratorFlags.Interrupted)) {
            /*
             *  relaunch only if
             *  - has a tiner? -> was it interrupted while not waiting aka moving? need to check both:
             *      -> has a timer - is it because its waiting to start next node?
             *      -> has a timer - is it because something set it while moving (like timed pause)?
             *
             *  - doesnt have a timer? -> is movement valid?
             *
             *  TODO: ((nextMoveTime.Passed() && VALID_MOVEMENT) || (!nextMoveTime.Passed() && !hasFlag(MOVEMENTGENERATOR_FLAG_INFORM_ENABLED)))
             */
            if (hasFlag(MovementGeneratorFlags.initialized) && (nextMoveTime.Passed || !hasFlag(MovementGeneratorFlags.InformEnabled))) {
                startMove(owner, true);

                return true;
            }

            removeFlag(MovementGeneratorFlags.Interrupted);
        }

        // if it's moving
        if (!owner.getMoveSpline().finalized()) {
            // set home position at place (every MotionMaster::UpdateMotion)
            if (owner.getTransGUID().isEmpty()) {
                owner.setHomePosition(owner.getLocation());
            }

            // relaunch movement if its speed has changed
            if (hasFlag(MovementGeneratorFlags.SpeedUpdatePending)) {
                startMove(owner, true);
            }
        } else if (!nextMoveTime.Passed) // it's not moving, is there a timer?
        {
            if (updateTimer(diff)) {
                if (!hasFlag(MovementGeneratorFlags.initialized)) // initial movement call
                {
                    startMove(owner);

                    return true;
                } else if (!hasFlag(MovementGeneratorFlags.InformEnabled)) // timer set before node was reached, resume now
                {
                    startMove(owner, true);

                    return true;
                }
            } else {
                return true; // keep waiting
            }
        } else // not moving, no timer
        {
            if (hasFlag(MovementGeneratorFlags.initialized) && !hasFlag(MovementGeneratorFlags.InformEnabled)) {
                onArrived(owner); // hooks and wait timer reset (if necessary)
                addFlag(MovementGeneratorFlags.InformEnabled); // signals to future StartMove that it reached a node
            }

            if (nextMoveTime.Passed) // OnArrived might have set a timer
            {
                startMove(owner); // check path status, get next point and move if necessary & can
            }
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

            // TODO: Research if this modification is needed, which most likely isnt
            owner.setWalk(false);
        }
    }

    @Override
    public String getDebugInfo() {
        return String.format("Current Node: %1$s\n%2$s", currentNode, super.getDebugInfo());
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.Waypoint;
    }

    @Override
    public void unitSpeedChanged() {
        addFlag(MovementGeneratorFlags.SpeedUpdatePending);
    }

    private void movementInform(Creature owner) {
        var ai = owner.getAI();

        if (ai != null) {
            ai.movementInform(MovementGeneratorType.Waypoint, (int) currentNode);
        }
    }

    private void onArrived(Creature owner) {
        if (path == null || path.nodes.isEmpty()) {
            return;
        }

        var waypoint = path.nodes.ElementAt((int) currentNode);

        if (waypoint.delay != 0) {
            owner.clearUnitState(UnitState.RoamingMove);
            nextMoveTime.reset(waypoint.delay);
        }

        if (waypoint.eventId != 0 && RandomUtil.URand(0, 99) < waypoint.eventChance) {
            Log.outDebug(LogFilter.MapsScript, String.format("Creature movement start script %1$s at point %2$s for %3$s.", waypoint.eventId, currentNode, owner.getGUID()));
            owner.clearUnitState(UnitState.RoamingMove);
            owner.getMap().scriptsStart(ScriptsType.Waypoint, waypoint.eventId, owner, null);
        }

        // inform AI
        var ai = owner.getAI();

        if (ai != null) {
            ai.movementInform(MovementGeneratorType.Waypoint, (int) currentNode);
            ai.waypointReached(waypoint.id, path.id);
        }

        owner.updateCurrentWaypointInfo(waypoint.id, path.id);
    }


    private void startMove(Creature owner) {
        startMove(owner, false);
    }

    private void startMove(Creature owner, boolean relaunch) {
        // sanity checks
        if (owner == null || !owner.isAlive() || hasFlag(MovementGeneratorFlags.Finalized) || path == null || path.nodes.isEmpty() || (relaunch && (hasFlag(MovementGeneratorFlags.InformEnabled) || !hasFlag(MovementGeneratorFlags.initialized)))) {
            return;
        }

        if (owner.hasUnitState(UnitState.NotMove) || owner.isMovementPreventedByCasting() || (owner.isFormationLeader() && !owner.isFormationLeaderMoveAllowed())) // if cannot move OR cannot move because of formation
        {
            nextMoveTime.reset(1000); // delay 1s

            return;
        }

        var transportPath = !owner.getTransGUID().isEmpty();

        if (hasFlag(MovementGeneratorFlags.InformEnabled) && hasFlag(MovementGeneratorFlags.initialized)) {
            if (computeNextNode()) {
                // inform AI
                var ai = owner.getAI();

                if (ai != null) {
                    ai.waypointStarted(path.nodes.get(currentNode).id, path.id);
                }
            } else {
                var currentWaypoint = path.nodes.get(currentNode);
                var pos = new Position(currentWaypoint.x, currentWaypoint.y, currentWaypoint.z, owner.getLocation().getO());

                if (!transportPath) {
                    owner.setHomePosition(pos);
                } else {
                    var trans = owner.getTransport();

                    if (trans != null) {
                        pos.setO(pos.getO() - trans.getTransportOrientation());
                        owner.setTransportHomePosition(pos);
                        trans.calculatePassengerPosition(pos);
                        owner.setHomePosition(pos);
                    }
                    // else if (vehicle) - this should never happen, vehicle offsets are const
                }

                addFlag(MovementGeneratorFlags.Finalized);
                owner.updateCurrentWaypointInfo(0, 0);

                // inform AI
                var ai = owner.getAI();

                if (ai != null) {
                    ai.waypointPathEnded(currentWaypoint.id, path.id);
                }

                return;
            }
        } else if (!hasFlag(MovementGeneratorFlags.initialized)) {
            addFlag(MovementGeneratorFlags.initialized);

            // inform AI
            var ai = owner.getAI();

            if (ai != null) {
                ai.waypointStarted(path.nodes.get(currentNode).id, path.id);
            }
        }

        var waypoint = path.nodes.get(currentNode);

        removeFlag(MovementGeneratorFlags.Transitory.getValue() | MovementGeneratorFlags.InformEnabled.getValue().getValue() | MovementGeneratorFlags.TimedPaused.getValue().getValue());

        owner.addUnitState(UnitState.RoamingMove);

        MoveSplineInit init = new MoveSplineInit(owner);

        //! If creature is on transport, we assume waypoints set in DB are already transport offsets
        if (transportPath) {
            init.disableTransportPathTransformations();
        }

        //! Do not use formationDest here, MoveTo requires transport offsets due to disableTransportPathTransformations() call
        //! but formationDest contains global coordinates
        init.moveTo(waypoint.x, waypoint.y, waypoint.z);

        if (waypoint.orientation != null && waypoint.delay != 0) {
            init.setFacing(waypoint.orientation.floatValue());
        }

        switch (waypoint.moveType) {
            case Land:
                init.setAnimation(animTier.ground);

                break;
            case Takeoff:
                init.setAnimation(animTier.Hover);

                break;
            case Run:
                init.setWalk(false);

                break;
            case Walk:
                init.setWalk(true);

                break;
        }

        init.launch();

        // inform formation
        owner.signalFormationMovement();
    }

    private boolean computeNextNode() {
        if ((currentNode == path.nodes.size() - 1) && !repeating) {
            return false;
        }

        currentNode = (currentNode + 1) % path.nodes.size();

        return true;
    }

    private boolean updateTimer(int diff) {
        nextMoveTime.update(diff);

        if (nextMoveTime.Passed) {
            nextMoveTime.reset(0);

            return true;
        }

        return false;
    }
}
