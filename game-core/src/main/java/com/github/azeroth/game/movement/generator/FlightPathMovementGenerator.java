package com.github.azeroth.game.movement.generator;


import com.badlogic.gdx.math.Vector3;
import com.github.azeroth.common.Logs;

import com.github.azeroth.dbc.defines.CriteriaType;
import com.github.azeroth.dbc.defines.TaxiPathNodeFlag;
import com.github.azeroth.dbc.domain.TaxiPathNode;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.enums.PlayerFlag;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.event.GameEvent;
import com.github.azeroth.game.movement.MovementGenerator;
import com.github.azeroth.game.movement.enums.MovementGeneratorFlag;
import com.github.azeroth.game.movement.enums.MovementGeneratorMode;
import com.github.azeroth.game.movement.enums.MovementGeneratorPriority;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.spline.MoveSplineInit;
import lombok.AllArgsConstructor;
import lombok.Data;


import java.util.*;


public class FlightPathMovementGenerator extends MovementGenerator {

    private static final byte FLIGHT_TRAVEL_UPDATE = 100;
    private static final short TIMEDIFF_NEXT_WP = 250;
    private static final float SKIP_SPLINE_POINT_DISTANCE_SQ = 40.f * 40.f;
    private static final float PLAYER_FLIGHT_SPEED = 32.0f;


    private final LinkedList<TaxiPathNode> path = new LinkedList<>();
    private final ArrayList<TaxiNodeChangeInfo> pointsForPathSwitch = new ArrayList<>(); //! node indexes and costs where TaxiPath changes

    private float endGridX; //! X coord of last node location
    private float endGridY; //! Y coord of last node location
    private int endMapId; //! map Id of last node location
    private int preloadTargetNode; //! node index where preloading starts
    private int currentNode;

    public FlightPathMovementGenerator() {
        mode = MovementGeneratorMode.DEFAULT;
        priority = MovementGeneratorPriority.HIGHEST;
        flags.set(MovementGeneratorFlag.INITIALIZATION_PENDING);
        baseUnitState = UnitState.IN_FLIGHT;
    }

    @Override
    public void initialize(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.INITIALIZATION_PENDING, MovementGeneratorFlag.DEACTIVATED);
        flags.set(MovementGeneratorFlag.INITIALIZED);

        reset(owner);
        initEndGridInfo();
    }

    @Override
    public void reset(Unit owner) {
        flags.removeFlag(MovementGeneratorFlag.DEACTIVATED);

        owner.combatStopWithPets();
        owner.setUnitFlag(UnitFlag.REMOVE_CLIENT_CONTROL, UnitFlag.ON_TAXI);

        var end = getPathAtMapEnd();
        var currentNodeId = getCurrentNode();

        if (currentNodeId == end) {
            Logs.FLIGHT_PATH.debug("FlightPathMovementGenerator::reset: trying to start a flypath from the end point. {}", owner);

            return;
        }

        MoveSplineInit init = new MoveSplineInit(owner);
        // Providing a starting vertex since the taxi paths do not provide such
        init.path().add(owner.getLocation().toVector3());

        for (var i = currentNodeId; i != end; ++i) {
            Vector3 vertice = new Vector3(path.get(i).getLocX(), path.get(i).getLocY(), path.get(i).getLocZ());
            init.path().add(vertice);
        }

        init.setFirstPointId(getCurrentNode());
        init.setFly();
        init.setSmooth();
        init.setUncompressed();
        init.setWalk(true);
        init.setVelocity(PLAYER_FLIGHT_SPEED);
        init.launch();
    }

    @Override
    public boolean update(Unit owner, int diff) {
        if (owner == null || !owner.isPlayer()) {
            return false;
        }
        Player player = owner.toPlayer();
        // skipping the first spline path point because it's our starting point and not a taxi path point
        var pointId = owner.getMoveSpline().currentPathIdx() <= 0 ? 0 : owner.getMoveSpline().currentPathIdx() - 1;

        if (pointId > currentNode && currentNode < path.size() - 1) {
            var departureEvent = true;

            do {
                doEventIfAny(player, path.get(currentNode), departureEvent);

                while (!pointsForPathSwitch.isEmpty() && pointsForPathSwitch.getFirst().pathIndex <= currentNode) {
                    pointsForPathSwitch.removeFirst();
                    player.getTaxi().nextTaxiDestination();

                    if (!pointsForPathSwitch.isEmpty()) {
                        player.updateCriteria(CriteriaType.MoneySpentOnTaxis, (int) pointsForPathSwitch.getFirst().cost);
                        player.modifyMoney(-pointsForPathSwitch.getFirst().cost);
                    }
                }

                if (pointId == currentNode) {
                    break;
                }

                if (currentNode == preloadTargetNode) {
                    preloadEndGrid(player);
                }

                currentNode += (departureEvent ? 1 : 0);
                departureEvent = !departureEvent;
            } while (currentNode < path.size() - 1);
        }

        if (currentNode >= (path.size() - 1)) {
            addFlag(MovementGeneratorFlag.INFORM_ENABLED);

            return false;
        }

        return true;
    }

    @Override
    public void deactivate(Unit owner) {
        addFlag(MovementGeneratorFlag.DEACTIVATED);
    }

    @Override
    public void finalize(Unit owner, boolean active, boolean movementInform) {
        addFlag(MovementGeneratorFlag.FINALIZED);
        Player player = owner.toPlayer();
        if (!active || player == null) {
            return;
        }

        var taxiNodeId = player.getTaxi().getTaxiDestination();
        player.getTaxi().clearTaxiDestinations();
        player.dismount();
        player.removeUnitFlag(UnitFlag.REMOVE_CLIENT_CONTROL, UnitFlag.ON_TAXI);

        // update z position to ground and orientation for landing point
        // this prevent cheating with landing  point at lags
        // when client side flight end early in comparison server side
        player.stopMoving();

        // When the player reaches the last flight point, teleport to destination taxi node location
        if (!path.isEmpty() && (path.size() < 2 || (path.get(path.size() - 2).getFlags() & TaxiPathNodeFlag.TELEPORT.value) == 0)) {
            var dbcObjectManager = player.getWorldContext().getDbcObjectManager();
            var lastPath = dbcObjectManager.taxiPath(path.getLast().getPathID());
            var node = dbcObjectManager.taxiNode(lastPath.getToTaxiNode());
            if (node != null) {
                player.setFallInformation(0, node.getPosZ());
                player.teleportTo(node.getContinentID(), node.getPosX(), node.getPosY(), node.getPosZ(), player.getOrientation());
            }
        }
        player.removePlayerFlag(PlayerFlag.TAXI_BENCHMARK);
    }


    public final void loadPath(Player player) {
        loadPath(player, 0);
    }

    public final void loadPath(Player player, int startNode) {
        path.clear();
        currentNode = startNode;
        pointsForPathSwitch.clear();
        var taxi = player.getTaxi().getPath();
        var discount = player.getReputationPriceDiscount(player.getTaxi().getFlightMasterFactionTemplate());
        var dbcManager = player.getWorldContext().getDbcObjectManager();

        for (int src = 0, dst = 1; dst < taxi.size(); src = dst++) {
            int pathId = 0, cost = 0;
            var taxiPath = dbcManager.getTaxiPath(taxi.get(src), taxi.get(dst));
            if (taxiPath != null) {
                pathId = taxiPath.getId();
                cost = taxiPath.getCost();
            }


            var taxiPathNodesByPath = dbcManager.getTaxiPathNodesByPath();
            if (pathId >= taxiPathNodesByPath.size()) {
                return;
            }

            var nodes = taxiPathNodesByPath.get(pathId);

            if (!nodes.isEmpty()) {
                var start = nodes.getFirst();
                var end = nodes.getLast();
                var passedPreviousSegmentProximityCheck = false;

                for (int i = 0; i < nodes.size(); ++i) {
                    if (passedPreviousSegmentProximityCheck || src == 0 || path.isEmpty() || isNodeIncludedInShortenedPath(path.getLast(), nodes.get(i))) {
                        // skip consecutive teleports, only keep the first one
                        if ((src == 0 || (isNodeIncludedInShortenedPath(start, nodes.get(i)) && i >= 2)) &&
                                (dst == taxi.size() - 1 || (isNodeIncludedInShortenedPath(end, nodes.get(i)) && (i < nodes.size() - 1 || path.isEmpty()))) &&
                                ((nodes.get(i).getFlags() & TaxiPathNodeFlag.TELEPORT.value) == 0 || path.isEmpty() || (path.getLast().getFlags() & TaxiPathNodeFlag.TELEPORT.value) == 0)) {
                            passedPreviousSegmentProximityCheck = true;
                            path.addLast(nodes.get(i));
                        }
                    } else {
                        path.pollLast();
                        TaxiNodeChangeInfo last = pointsForPathSwitch.getLast();
                        last.setPathIndex(last.getPathIndex() - 1);
                    }
                }
            }

            pointsForPathSwitch.add(new TaxiNodeChangeInfo(Math.max(path.size() - 1, 1), (long) Math.ceil(cost * discount)));
        }
    }

    public final void setCurrentNodeAfterTeleport() {
        if (path.isEmpty() || currentNode >= path.size()) {
            return;
        }

        int map0 = path.get(currentNode).getContinentID();

        for (var i = currentNode + 1; i < path.size(); ++i) {
            if (path.get(i).getContinentID() != map0) {
                currentNode = i;

                return;
            }
        }
    }

    @Override
    public String toString() {
        return ("FlightPathMovementGenerator(Current Node: %d, %s, Start Path Id: %d, Path Size: %d, HasArrived: %s, " +
                "End Grid X: %s, End Grid Y: %s, End Map Id: %d, Preloaded Target Node: %d)")
                .formatted(currentNode, super.toString(), getPathId(0), path.size(), hasArrived(), endGridX, endGridY, endMapId, preloadTargetNode);

    }

    @Override
    public Position getResetPosition(Unit unit) {
        var node = path.get(currentNode);
        return new Position(node.getLocX(), node.getLocY(), node.getLocZ());
    }

    @Override
    public MovementGeneratorType getMovementGeneratorType() {
        return MovementGeneratorType.FLIGHT;
    }

    public final List<TaxiPathNode> getPath() {
        return path;
    }

    public final void skipCurrentNode() {
        ++currentNode;
    }

    public final int getCurrentNode() {
        return currentNode;
    }

    private int getPathAtMapEnd() {
        if (currentNode >= path.size()) {
            return path.size();
        }

        int curMapId = path.get(currentNode).getContinentID();

        for (var i = currentNode; i < path.size(); ++i) {
            if (path.get(i).getContinentID() != curMapId) {
                return i;
            }
            if (i > 0 && (path.get(i - 1).getFlags() & TaxiPathNodeFlag.TELEPORT.value) != 0) {
                return i;
            }
        }

        return path.size();
    }

    private boolean isNodeIncludedInShortenedPath(TaxiPathNode p1, TaxiPathNode p2) {
        return p1.getContinentID() != p2.getContinentID()
                || Math.pow(p1.getLocX() - p2.getLocX(), 2) + Math.pow(p1.getLocY() - p2.getLocY(), 2) > SKIP_SPLINE_POINT_DISTANCE_SQ
                || (p2.getFlags() & TaxiPathNodeFlag.TELEPORT.value) != 0
                || (p2.getFlags() & TaxiPathNodeFlag.STOP.value) != 0 && p2.getDelay() > 0;
    }

    private void doEventIfAny(Player owner, TaxiPathNode node, boolean departure) {
        var eventId = departure ? node.getDepartureEventID() : node.getArrivalEventID();

        if (eventId != 0) {
            Logs.MAPS_SCRIPT.debug("FlightPathMovementGenerator::DoEventIfAny: taxi {} event {} of node {} of path {} for player {}", departure ? "departure" : "arrival", eventId, node.getNodeIndex(), node.getPathID(), owner.getName());
            owner.getWorldContext().getWorldEventPublisher().publish(new GameEvent(eventId, owner, owner));
        }
    }

    private void initEndGridInfo() {
        var nodeCount = path.size(); //! Number of nodes in path.
        endMapId = path.get(nodeCount - 1).getContinentID(); //! MapId of last node

        if (nodeCount < 3) {
            preloadTargetNode = 0;
        } else {
            preloadTargetNode = (int) nodeCount - 3;
        }

        while (path.get(preloadTargetNode).getContinentID() != endMapId) {
            ++preloadTargetNode;
        }

        endGridX = path.get(nodeCount - 1).getLocX();
        endGridY = path.get(nodeCount - 1).getLocY();
    }

    private void preloadEndGrid(Player owner) {
        // Used to preload the final grid where the flightmaster is
        var endMap = owner.getMap();

        // Load the grid
        if (endMap != null) {
            Logs.FLIGHT_PATH.debug("FlightPathMovementGenerator::PreloadEndGrid: preloading grid ({}, {}) for map {} at node index {}/{}",
                    endGridX, endGridY, endMapId, preloadTargetNode, path.size() - 1);
            endMap.loadGrid(endGridX, endGridY);
        } else {
            Logs.FLIGHT_PATH.debug("FlightPathMovementGenerator::PreloadEndGrid: unable to determine map to preload flight master grid");
        }
    }

    private int getPathId(int index) {
        if (index >= path.size()) {
            return 0;
        }

        return path.get(index).getPathID();
    }

    private boolean hasArrived() {
        return currentNode >= path.size();
    }

    @Data
    @AllArgsConstructor
    private static final class TaxiNodeChangeInfo {
        private int pathIndex;
        private long cost;
    }
}
