package com.github.azeroth.game.movement.waypoint;

import com.github.azeroth.common.Logs;
import com.github.azeroth.common.Pair;
import com.github.azeroth.game.domain.misc.WaypointNode;
import com.github.azeroth.game.domain.misc.WaypointPath;
import com.github.azeroth.game.domain.misc.WaypointPathFlag;
import com.github.azeroth.game.domain.object.ObjectDefine;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.creature.TempSummon;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.repository.MiscRepository;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class WayPointManager {

    private static final int WAYPOINT_PATH_FLAG_FOLLOW_PATH_BACKWARDS_MINIMUM_NODES = 2;

    private final HashMap<Integer /*pathId*/, WaypointPath> pathStore = new HashMap<>();
    private final HashMap<Pair<Integer /*pathId*/, Integer /*nodeId*/>, ObjectGuid> nodeToVisualWaypointGUIDsMap = new HashMap<>();
    private final HashMap<ObjectGuid, Pair<WaypointPath, WaypointNode>> visualWaypointGUIDToNodeMap = new HashMap<>();


    private final MiscRepository miscRepository;


    public void reloadPath(int pathId) {
        loadPaths();
    }

    public void loadPaths() {
        AtomicInteger count = new AtomicInteger();
        long oldMSTime = System.currentTimeMillis();
        try (var items = miscRepository.streamAllWaypointPaths()) {
            items.forEach(e -> {
                pathStore.put(e.id, e);
                count.getAndIncrement();
            });
        }
        Logs.SERVER_LOADING.info(">> Loaded {} waypoint paths in {} ms", count, System.currentTimeMillis() - oldMSTime);

        count.set(0);
        oldMSTime = System.currentTimeMillis();
        try (var items = miscRepository.streamAllWaypointPathNodes()) {
            items.forEach(e -> {
                if(!pathStore.containsKey(e.id)) {
                    Logs.SQL.error("PathId {} in `waypoint_path_node` does not exist in `waypoint_path`, ignoring", e.id);
                    return;
                }
                pathStore.get(e.id).nodes.add(e);
            });
        }
        Logs.SERVER_LOADING.error(">> Loaded {} waypoint path nodes in {} ms", count, System.currentTimeMillis() - oldMSTime);

        doPostLoadingChecks();
    }

    private void doPostLoadingChecks() {

        pathStore.forEach((pathId, pathInfo) -> {
            if (pathInfo.nodes.isEmpty())
                Logs.SQL.error("PathId {} in `waypoint_path` has no assigned nodes in `waypoint_path_node`", pathInfo.id);

            if (pathInfo.flags.hasFlag(WaypointPathFlag.FollowPathBackwardsFromEndToStart) && pathInfo.nodes.size() < WAYPOINT_PATH_FLAG_FOLLOW_PATH_BACKWARDS_MINIMUM_NODES)
                Logs.SQL.error("PathId {} in `waypoint_path` has FollowPathBackwardsFromEndToStart set, but only {} nodes, requires {}", pathInfo.id, pathInfo.nodes.size(), WAYPOINT_PATH_FLAG_FOLLOW_PATH_BACKWARDS_MINIMUM_NODES);

        });

    }

    public void visualizePath(Unit owner, WaypointPath path, Integer displayId) {
        for (WaypointNode node : path.nodes)
        {
            var pathNodePair = Pair.of(path.id, node.id);



            var itr = nodeToVisualWaypointGUIDsMap.get(pathNodePair);
            if (itr != null)
                continue;

            TempSummon summon = owner.summonCreature(ObjectDefine.VISUAL_WAYPOINT, node.x, node.y, node.z, node.orientation != null ? node.orientation : 0.0f);
            if (summon == null)
                continue;

            if (displayId != null)
            {
                summon.setDisplayId(displayId, true);
                summon.setObjectScale(0.5f);
            }

            nodeToVisualWaypointGUIDsMap.put(pathNodePair, summon.getGUID());
            visualWaypointGUIDToNodeMap.put(summon.getGUID(), Pair.of(path, node));
        }
    }

    public void deVisualizePath(Unit owner, WaypointPath path) {
        for (WaypointNode node : path.nodes)
        {
            var pathNodePair = Pair.of(path.id, node.id);
            var objectGuid = nodeToVisualWaypointGUIDsMap.get(pathNodePair);
            if (objectGuid == null)
                continue;

            Creature creature = owner.getWorldContext().getCreature(owner, objectGuid);
            if (creature == null)
                continue;
            visualWaypointGUIDToNodeMap.remove(objectGuid);
            nodeToVisualWaypointGUIDsMap.remove(pathNodePair);

            creature.despawnOrUnsummon();
        }

    }

    public void moveNode(WaypointPath path, WaypointNode node, Position pos) {
        miscRepository.updateWaypointPathNodePosition(pos.getX(), pos.getY(), pos.getZ(), pos.getO(), path.id, node.id);
    }

    public void deleteNode(WaypointPath path, WaypointNode node) {
        miscRepository.deleteWaypointPathNode(path.id, node.id);
        miscRepository.updateWaypointPathNodeIds(path.id, node.id);
    }

    public void deleteNode(int pathId, int nodeId) {
        WaypointPath path = getPath(pathId);
        if (path == null)
            return;

        WaypointNode node = getNode(path, nodeId);
        if (node == null)
            return;

        deleteNode(path, node);
    }

    public WaypointPath getPath(int pathId) {
        return pathStore.get(pathId);
    }

    public WaypointNode getNode(WaypointPath path, int nodeId) {
        return path.nodes.stream().filter(node -> node.id == nodeId).findFirst().orElse(null);
    }

    public WaypointNode getNode(int pathId, int nodeId) {
        WaypointPath path = getPath(pathId);
        if (path == null)
            return null;
        return getNode(path, nodeId);
    }

    public WaypointPath getPathByVisualGUID(ObjectGuid guid) {
        var pathNodePair = visualWaypointGUIDToNodeMap.get(guid);
        if (pathNodePair == null)
            return null;
        return pathNodePair.first();
    }

    public WaypointNode getNodeByVisualGUID(ObjectGuid guid) {
        var pathNodePair = visualWaypointGUIDToNodeMap.get(guid);
        if (pathNodePair == null)
            return null;
        return pathNodePair.second();
    }

    public ObjectGuid getVisualGUIDByNode(int pathId, int nodeId) {
        var pathNodePair = Pair.of(pathId, nodeId);
        return nodeToVisualWaypointGUIDsMap.get(pathNodePair);
    }


}
