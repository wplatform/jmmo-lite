package com.github.azeroth.game.movement.generator;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.movement.MovementGeneratorMedium;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import game.GameEvents;

import java.util.ArrayList;


public class FlightPathMovementGenerator extends MovementGeneratorMedium<Player> {
    private final ArrayList<TaxiPathNodeRecord> path = new ArrayList<>();
    private final ArrayList<TaxiNodeChangeInfo> pointsForPathSwitch = new ArrayList<>(); //! node indexes and costs where TaxiPath changes

    private float endGridX; //! X coord of last node location
    private float endGridY; //! Y coord of last node location
    private int endMapId; //! map Id of last node location
    private int preloadTargetNode; //! node index where preloading starts
    private int currentNode;

    public FlightPathMovementGenerator() {
        mode = MovementGeneratorMode.Default;
        priority = MovementGeneratorPriority.Highest;
        flags = MovementGeneratorFlags.InitializationPending;
        baseUnitState = UnitState.InFlight;
    }

    @Override
    public void doInitialize(Player owner) {
        removeFlag(MovementGeneratorFlags.InitializationPending.getValue() | MovementGeneratorFlags.Deactivated.getValue());
        addFlag(MovementGeneratorFlags.initialized);

        doReset(owner);
        initEndGridInfo();
    }

    @Override
    public void doReset(Player owner) {
        removeFlag(MovementGeneratorFlags.Deactivated);

        owner.combatStopWithPets();
        owner.setUnitFlag(UnitFlag.RemoveClientControl.getValue() | UnitFlag.OnTaxi.getValue());

        var end = getPathAtMapEnd();
        var currentNodeId = getCurrentNode();

        if (currentNodeId == end) {
            Log.outDebug(LogFilter.movement, String.format("FlightPathMovementGenerator::DoReset: trying to start a flypath from the end point. %1$s", owner.getDebugInfo()));

            return;
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        // Providing a starting vertex since the taxi paths do not provide such
        init.path().add(new Vector3(owner.getLocation().getX(), owner.getLocation().getY(), owner.getLocation().getZ()));

        for (var i = (int) currentNodeId; i != (int) end; ++i) {
            Vector3 vertice = new Vector3(path.get(i).loc.X, path.get(i).loc.Y, path.get(i).loc.Z);
            init.path().add(vertice);
        }

        init.setFirstPointId((int) getCurrentNode());
        init.setFly();
        init.setSmooth();
        init.setUncompressed();
        init.setWalk(true);
        init.setVelocity(30.0f);
        init.launch();
    }

    @Override
    public boolean doUpdate(Player owner, int diff) {
        if (owner == null) {
            return false;
        }

        // skipping the first spline path point because it's our starting point and not a taxi path point
        var pointId = (int) (owner.getMoveSpline().currentPathIdx() <= 0 ? 0 : owner.getMoveSpline().currentPathIdx() - 1);

        if (pointId > currentNode && currentNode < path.size() - 1) {
            var departureEvent = true;

            do {
                doEventIfAny(owner, path.get(currentNode), departureEvent);

                while (!pointsForPathSwitch.isEmpty() && pointsForPathSwitch.get(0).pathIndex <= currentNode) {
                    pointsForPathSwitch.remove(0);
                    owner.getTaxi().nextTaxiDestination();

                    if (!pointsForPathSwitch.isEmpty()) {
                        owner.updateCriteria(CriteriaType.MoneySpentOnTaxis, (int) pointsForPathSwitch.get(0).cost);
                        owner.modifyMoney(-_pointsForPathSwitch.get(0).cost);
                    }
                }

                if (pointId == currentNode) {
                    break;
                }

                if (currentNode == preloadTargetNode) {
                    preloadEndGrid(owner);
                }

                currentNode += (departureEvent ? 1 : 0);
                departureEvent = !departureEvent;
            } while (currentNode < path.size() - 1);
        }

        if (currentNode >= (path.size() - 1)) {
            addFlag(MovementGeneratorFlags.InformEnabled);

            return false;
        }

        return true;
    }

    @Override
    public void doDeactivate(Player owner) {
        addFlag(MovementGeneratorFlags.Deactivated);
    }

    @Override
    public void doFinalize(Player owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlags.Finalized);

        if (!active) {
            return;
        }

        var taxiNodeId = owner.getTaxi().getTaxiDestination();
        owner.getTaxi().clearTaxiDestinations();
        owner.dismount();
        owner.removeUnitFlag(UnitFlag.RemoveClientControl.getValue() | UnitFlag.OnTaxi.getValue());

        if (owner.getTaxi().isEmpty()) {
            // update z position to ground and orientation for landing point
            // this prevent cheating with landing  point at lags
            // when client side flight end early in comparison server side
            owner.stopMoving();
            // When the player reaches the last flight point, teleport to destination taxi node location
            var node = CliDB.TaxiNodesStorage.get(taxiNodeId);

            if (node != null) {
                owner.setFallInformation(0, node.pos.Z);
                owner.teleportTo(node.ContinentID, node.pos.X, node.pos.Y, node.pos.Z, owner.getLocation().getO());
            }
        }

        owner.removePlayerFlag(playerFlags.TaxiBenchmark);
    }


    public final void loadPath(Player player) {
        loadPath(player, 0);
    }

    public final void loadPath(Player player, int startNode) {
        path.clear();
        currentNode = (int) startNode;
        pointsForPathSwitch.clear();
        var taxi = player.getTaxi().getPath();
        var discount = player.getReputationPriceDiscount(player.getTaxi().getFlightMasterFactionTemplate());

        for (int src = 0, dst = 1; dst < taxi.size(); src = dst++) {
            int path;
            tangible.OutObject<Integer> tempOut_path = new tangible.OutObject<Integer>();
            int cost;
            tangible.OutObject<Integer> tempOut_cost = new tangible.OutObject<Integer>();
            global.getObjectMgr().getTaxiPath(taxi.get(src), taxi.get(dst), tempOut_path, tempOut_cost);
            cost = tempOut_cost.outArgValue;
            path = tempOut_path.outArgValue;

            if (path >= CliDB.TaxiPathNodesByPath.keySet().max()) {
                return;
            }

            var nodes = CliDB.TaxiPathNodesByPath.get(path);

            if (!nodes.isEmpty()) {
                var start = nodes[0];

                var end = nodes[ ^ 1];
                var passedPreviousSegmentProximityCheck = false;

                for (int i = 0; i < nodes.length; ++i) {
                    if (passedPreviousSegmentProximityCheck || src == 0 || path.isEmpty() || isNodeIncludedInShortenedPath(path.get(path.size() - 1), nodes[i])) {
                        if ((src == 0 || (isNodeIncludedInShortenedPath(start, nodes[i]) && i >= 2)) && (dst == taxi.size() - 1 || (isNodeIncludedInShortenedPath(end, nodes[i]) && i < nodes.length - 1))) {
                            passedPreviousSegmentProximityCheck = true;
                            path.add(nodes[i]);
                        }
                    } else {
                        path.remove(path.size() - 1);
                        pointsForPathSwitch.get( ^ 1).PathIndex -= 1;
                    }
                }
            }

            pointsForPathSwitch.add(new TaxiNodeChangeInfo((int) (path.size() - 1), (long) Math.ceil(cost * discount)));
        }
    }

    public final void setCurrentNodeAfterTeleport() {
        if (path.isEmpty() || currentNode >= path.size()) {
            return;
        }

        int map0 = path.get(currentNode).ContinentID;

        for (var i = currentNode + 1; i < path.size(); ++i) {
            if (path.get(i).ContinentID != map0) {
                currentNode = i;

                return;
            }
        }
    }

    @Override
    public String getDebugInfo() {
        return String.format("Current Node: %1$s\n%2$s\nStart Path Id: %3$s Path Size: %4$s HasArrived: %5$s End Grid X: %6$s ", getCurrentNode(), super.getDebugInfo(), getPathId(0), path.size(), hasArrived(), endGridX) + String.format("End Grid Y: %1$s End Map Id: %2$s Preloaded Target Node: %3$s", endGridY, endMapId, preloadTargetNode);
    }

    @Override
    public boolean getResetPosition(Unit u, tangible.OutObject<Float> x, tangible.OutObject<Float> y, tangible.OutObject<Float> z) {
        var node = path.get(currentNode);
        x.outArgValue = node.loc.X;
        y.outArgValue = node.loc.Y;
        z.outArgValue = node.loc.Z;

        return true;
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.flight;
    }

    public final ArrayList<TaxiPathNodeRecord> getPath() {
        return path;
    }

    public final void skipCurrentNode() {
        ++currentNode;
    }

    public final int getCurrentNode() {
        return (int) currentNode;
    }

    private int getPathAtMapEnd() {
        if (currentNode >= path.size()) {
            return (int) path.size();
        }

        int curMapId = path.get(currentNode).ContinentID;

        for (var i = currentNode; i < path.size(); ++i) {
            if (path.get(i).ContinentID != curMapId) {
                return (int) i;
            }
        }

        return (int) path.size();
    }

    private boolean isNodeIncludedInShortenedPath(TaxiPathNodeRecord p1, TaxiPathNodeRecord p2) {
        return p1.ContinentID != p2.ContinentID || Math.pow(p1.loc.X - p2.loc.X, 2) + Math.pow(p1.loc.Y - p2.loc.Y, 2) > (40.0f * 40.0f);
    }

    private void doEventIfAny(Player owner, TaxiPathNodeRecord node, boolean departure) {
        var eventid = departure ? node.DepartureEventID : node.ArrivalEventID;

        if (eventid != 0) {
            Log.outDebug(LogFilter.MapsScript, String.format("FlightPathMovementGenerator::DoEventIfAny: taxi %1$s event %2$s of node %3$s of path %4$s for player %5$s", (departure ? "departure" : "arrival"), eventid, node.NodeIndex, node.pathID, owner.getName()));
            GameEvents.trigger(eventid, owner, owner);
        }
    }

    private void initEndGridInfo() {
        var nodeCount = path.size(); //! Number of nodes in path.
        endMapId = path.get(nodeCount - 1).ContinentID; //! MapId of last node

        if (nodeCount < 3) {
            preloadTargetNode = 0;
        } else {
            preloadTargetNode = (int) nodeCount - 3;
        }

        while (path.get((int) preloadTargetNode).ContinentID != endMapId) {
            ++preloadTargetNode;
        }

        endGridX = path.get(nodeCount - 1).loc.X;
        endGridY = path.get(nodeCount - 1).loc.Y;
    }

    private void preloadEndGrid(Player owner) {
        // Used to preload the final grid where the flightmaster is
        var endMap = owner.getMap();

        // Load the grid
        if (endMap != null) {
            Log.outDebug(LogFilter.Server, "FlightPathMovementGenerator::PreloadEndGrid: Preloading grid ({0}, {1}) for map {2} at node index {3}/{4}", endGridX, endGridY, endMapId, preloadTargetNode, path.size() - 1);
            endMap.loadGrid(endGridX, endGridY);
        } else {
            Log.outDebug(LogFilter.Server, "FlightPathMovementGenerator::PreloadEndGrid: Unable to determine map to preload flightmaster grid");
        }
    }

    private int getPathId(int index) {
        if (index >= path.size()) {
            return 0;
        }

        return path.get(index).pathID;
    }

    private boolean hasArrived() {
        return currentNode >= path.size();
    }

    private static class TaxiNodeChangeInfo {
        public final long cost;
        public int pathIndex;

        public TaxiNodeChangeInfo(int pathIndex, long cost) {
            pathIndex = pathIndex;
            cost = cost;
        }
    }
}
