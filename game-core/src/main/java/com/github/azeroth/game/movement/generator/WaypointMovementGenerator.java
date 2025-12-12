package com.github.azeroth.game.movement.generator;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Assert;
import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.misc.WaypointNode;
import com.github.azeroth.game.domain.misc.WaypointPath;
import com.github.azeroth.game.domain.misc.WaypointPathFlag;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.PathGenerator;
import com.github.azeroth.game.movement.enums.*;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import com.github.azeroth.time.TimeTracker;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class WaypointMovementGenerator extends MovementGenerator {


    private static final Duration SEND_NEXT_POINT_EARLY_DELTA = Duration.ofMillis(1500);


    private int pathId;
    private WaypointPath path;
    private int currentNode;
    TimeTracker duration;
    Float speed;
    MovementWalkRunSpeedSelectionMode speedSelectionMode;
    Duration[] waitTimeRangeAtPathEnd;
    Float wanderDistanceAtPathEnds;
    Boolean followPathBackwardsFromEndToStart;
    Boolean exactSplinePath;
    boolean repeating;
    boolean generatePath;

    TimeTracker moveTimer;
    TimeTracker nextMoveTime;
    int[] waypointTransitionSplinePoints;
    int waypointTransitionSplinePointsIndex;
    boolean isReturningToStart;
    boolean loadedFromDB;




    public WaypointMovementGenerator(int pathId,
                                     boolean repeating,
                                     Duration duration,
                                     Float speed,
                                     MovementWalkRunSpeedSelectionMode speedSelectionMode,
                                     Duration[] waitTimeRangeAtPathEnd,
                                     Float wanderDistanceAtPathEnds,
                                     Boolean followPathBackwardsFromEndToStart,
                                     Boolean exactSplinePath,
                                     boolean generatePath) {
        this(null, repeating, duration, speed, speedSelectionMode, waitTimeRangeAtPathEnd, wanderDistanceAtPathEnds,
                followPathBackwardsFromEndToStart, exactSplinePath, generatePath);
        this.pathId = pathId;
        this.loadedFromDB = true;
    }

    public WaypointMovementGenerator(WaypointPath path,
                              boolean repeating,
                              Duration duration,
                              Float speed,
                              MovementWalkRunSpeedSelectionMode speedSelectionMode,
                              Duration[] waitTimeRangeAtPathEnd,
                              Float wanderDistanceAtPathEnds,
                              Boolean followPathBackwardsFromEndToStart,
                              Boolean exactSplinePath,
                              boolean generatePath) {
        this.path = path;
        this.repeating = repeating;
        this.duration = duration == null ? null : new TimeTracker(duration);
        this.speed = speed;
        this.speedSelectionMode = speedSelectionMode;
        this.waitTimeRangeAtPathEnd = waitTimeRangeAtPathEnd;
        this.wanderDistanceAtPathEnds = wanderDistanceAtPathEnds;
        this.followPathBackwardsFromEndToStart = followPathBackwardsFromEndToStart;
        this.exactSplinePath = exactSplinePath;
        this.generatePath = generatePath;
        this.moveTimer = new TimeTracker(0);
        this.nextMoveTime = new TimeTracker(0);
        this.waypointTransitionSplinePointsIndex = 0;
        this.isReturningToStart = false;

        this.mode = MovementGeneratorMode.DEFAULT;
        this.priority = MovementGeneratorPriority.NORMAL;
        this.flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        this.baseUnitState = UnitState.ROAMING;

        this.path.buildSegments();
    }



    @Override
    public void pause() {
        pause(0);
    }

    @Override
    public void pause(int timer) {
        if (timer != 0) {
            // Don't try to paused an already paused generator
            if (hasFlag(MovementGeneratorFlag.PAUSED)) {
                return;
            }

            addFlag(MovementGeneratorFlag.TIMED_PAUSED);
            nextMoveTime.reset(timer);
            removeFlag(MovementGeneratorFlag.PAUSED);
        } else {
            addFlag(MovementGeneratorFlag.PAUSED);
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
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
            nextMoveTime.reset(overrideTimer);
        }

        if (nextMoveTime.passed()) {
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
        }

        removeFlag(MovementGeneratorFlag.PAUSED);
    }

    @Override
    public Position getResetPosition(Unit owner) {

        // prevent a crash at empty waypoint path.
        if (path == null || path.nodes.isEmpty()) {
            return null;
        }

        Assert.isTrue(currentNode < path.nodes.size(),
                "WaypointMovementGenerator::GetResetPosition: tried to reference a node id ({}) which is not included in path ({}})", currentNode, path.id);

        var waypoint = path.nodes.get(currentNode);
        return new Position(waypoint.x, waypoint.y, waypoint.z);
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);
        var creature = owner.toCreature();
        if (loadedFromDB) {
            if (pathId == 0) {
                pathId = creature.getWaypointPathId();
            }
            path = creature.getWorldContext().getWayPointManager().getPath(pathId);
        }

        if (path == null) {
            Logs.SQL.error("WaypointMovementGenerator::DoInitialize: couldn't load path for creature ({}) (_pathId: {})", owner.getGUID(), pathId);

            return;
        }

        owner.stopMoving();

        nextMoveTime.reset(1000);
    }

    @Override
    public void reset(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.TRANSITORY, MovementGeneratorFlag.DEACTIVATED);

        owner.stopMoving();

        if (!flags.hasFlag(MovementGeneratorFlag.FINALIZED) && nextMoveTime.passed()) {
            nextMoveTime.reset(1); // Needed so that Update does not behave as if node was reached
        }
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || !owner.isAlive() || owner.toCreature() == null) {
            return true;
        }

        Creature creature = owner.toCreature();


        if (flags.hasFlag(MovementGeneratorFlag.FINALIZED, MovementGeneratorFlag.PAUSED)) {
            return true;
        }

        if (path == null || path.nodes.isEmpty())
            return true;


        if (duration != null)
        {
            duration.update(diff);
            if (duration.passed())
            {
                removeFlag(MovementGeneratorFlag.TRANSITORY);
                addFlag(MovementGeneratorFlag.INFORM_ENABLED);
                addFlag(MovementGeneratorFlag.FINALIZED);
                creature.updateCurrentWaypointInfo(0, 0);
                return false;
            }
        }

        if (owner.hasUnitState(UnitState.NOT_MOVE, UnitState.LOST_CONTROL) || owner.isMovementPreventedByCasting()) {
            addFlag(MovementGeneratorFlag.INTERRUPTED);
            owner.stopMoving();

            return true;
        }

        if (hasFlag(MovementGeneratorFlag.INTERRUPTED)) {
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
            if (hasFlag(MovementGeneratorFlag.INITIALIZED) && (nextMoveTime.passed() || !hasFlag(MovementGeneratorFlag.INFORM_ENABLED))) {
                startMove(creature, true);

                return true;
            }

            removeFlag(MovementGeneratorFlag.INTERRUPTED);
        }

        // if it's moving
        if (!updateMoveTimer(diff) && !creature.getMoveSpline().finalized()) {
            // set home position at place (every MotionMaster::UpdateMotion)
            if (creature.getTransGUID().isEmpty()) {
                creature.setHomePosition(owner.getLocation());
            }

            // handle switching points in continuous segments
            if (isExactSplinePath())
            {
                if (waypointTransitionSplinePointsIndex < waypointTransitionSplinePoints.length
                        && owner.getMoveSpline().currentPathIdx() >= waypointTransitionSplinePoints[waypointTransitionSplinePointsIndex])
                {
                    OnArrived(owner);
                    ++_waypointTransitionSplinePointsIndex;
                    if (ComputeNextNode())
                        if (CreatureAI* ai = owner->AI())
                            ai->WaypointStarted(path->Nodes[_currentNode].Id, path->Id);
                }
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
        var ai = owner.getAi();

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
        var ai = owner.getAi();

        if (ai != null) {
            ai.movementInform(MovementGeneratorType.Waypoint, (int) currentNode);
            ai.waypointReached(waypoint.id, path.id);
        }

        owner.updateCurrentWaypointInfo(waypoint.id, path.id);
    }

    void CreateSingularPointPath(Unit owner, WaypointPath path, int currentNode, boolean generatePath,
                                 List<Vector3> points, List<Integer> waypointTransitionSplinePoints)
    {
        WaypointNode waypoint = path.nodes.get(currentNode);
        points.add(new Vector3(owner.getPositionX(), owner.getPositionY(), owner.getPositionZ()));

        if (generatePath)
        {
            PathGenerator generator = new PathGenerator(owner);
            boolean result = generator.calculatePath(waypoint.x, waypoint.y, waypoint.z);
            if (result && !(generator.getPathType().hasFlag(PathType.NOPATH)))
                points.addAll(Arrays.asList(generator.getPath()).subList(1, generator.getPath().length));
            else
                points.add(new Vector3(waypoint.x, waypoint.y, waypoint.z));
        }
        else
            points.add(new Vector3(waypoint.x, waypoint.y, waypoint.z));

        waypointTransitionSplinePoints.add(points.size() - 1);
    }

    void CreateMergedPath(Unit owner, WaypointPath path, int previousNode, int currentNode,
                          boolean isReturningToStart, boolean generatePath, boolean isCyclic,
                          List<Vector3> points, List<Integer> waypointTransitionSplinePoints,
                          WaypointNode lastWaypointOnPath)
    {
        List<WaypointNode> segment = [&]
        {
            // find the continuous segment that our destination waypoint is on
            auto segmentItr = std::ranges::find_if(path->ContinuousSegments, [&](std::pair<std::size_t, std::size_t> const& segmentRange)
            {
                auto isInSegmentRange = [&](uint32 node) { return node >= segmentRange.first && node < segmentRange.first + segmentRange.second; };
                return isInSegmentRange(currentNode) && isInSegmentRange(previousNode);
            });

            // handle path returning directly from last point to first
            if (segmentItr == path->ContinuousSegments.end())
            {
                if (currentNode != 0 || previousNode != path->Nodes.size() - 1)
                    return std::span(&path->Nodes[currentNode], 1);

                segmentItr = path->ContinuousSegments.begin();
            }

            if (!isReturningToStart)
                return std::span(&path->Nodes[currentNode], segmentItr->second - (currentNode - segmentItr->first));

            return std::span(&path->Nodes[segmentItr->first], currentNode - segmentItr->first + 1);
        }();

    *lastWaypointOnPath = !isReturningToStart ? &segment.back() : &segment.front();

        waypointTransitionSplinePoints->clear();
        auto fillPath = [&]<typename iterator>(iterator itr, iterator end)
        {
            Optional<PathGenerator> generator;
            if (generatePath)
                generator.emplace(owner);

            Position source = owner->GetPosition();
            points->emplace_back(source.GetPositionX(), source.GetPositionY(), source.GetPositionZ());

            while (itr != end)
            {
                if (generator)
                {
                    bool result = generator->CalculatePath(source.GetPositionX(), source.GetPositionY(), source.GetPositionZ(), itr->X, itr->Y, itr->Z);
                    if (result && !(generator->GetPathType() & PATHFIND_NOPATH))
                        points->insert(points->end(), generator->GetPath().begin() + 1, generator->GetPath().end());
                    else
                        generator.reset(); // when path generation to a waypoint fails, add all remaining points without pathfinding (preserve legacy behavior of MoveSplineInit::MoveTo)
                }

                if (!generator)
                    points->emplace_back(itr->X, itr->Y, itr->Z);

                waypointTransitionSplinePoints->push_back(points->size() - 1);

                source.Relocate(itr->X, itr->Y, itr->Z);
                ++itr;
            }
        };

        if (isCyclic)
        {
            // create new cyclic path starting at current node
            std::vector<WaypointNode> cyclicPath = path->Nodes;
            std::rotate(cyclicPath.begin(), cyclicPath.begin() + currentNode, cyclicPath.end());
            fillPath(cyclicPath.begin(), cyclicPath.end());
            return;
        }

        if (!isReturningToStart)
            fillPath(segment.begin(), segment.end());
        else
            fillPath(segment.rbegin(), segment.rend());
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
                var ai = owner.getAi();

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
                var ai = owner.getAi();

                if (ai != null) {
                    ai.waypointPathEnded(currentWaypoint.id, path.id);
                }

                return;
            }
        } else if (!hasFlag(MovementGeneratorFlags.initialized)) {
            addFlag(MovementGeneratorFlags.initialized);

            // inform AI
            var ai = owner.getAi();

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
            case LAND:
                init.setAnimation(animTier.ground);

                break;
            case TAKEOFF:
                init.setAnimation(animTier.Hover);

                break;
            case Run:
                init.setWalk(false);

                break;
            case WALK:
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

    private boolean isExactSplinePath()
    {
        if (exactSplinePath != null)
            return exactSplinePath;

        return this.path.flags.hasFlag(WaypointPathFlag.ExactSplinePath);
    }

    private boolean updateMoveTimer(int diff) { return updateTimer(moveTimer, diff); }
    private boolean updateWaitTimer(int diff) { return updateTimer(nextMoveTime, diff); }

    private boolean updateTimer(TimeTracker timer, int diff) {
        timer.update(diff);
        if (timer.passed()) {
            timer.reset(0);
            return true;
        }
        return false;
    }
}
