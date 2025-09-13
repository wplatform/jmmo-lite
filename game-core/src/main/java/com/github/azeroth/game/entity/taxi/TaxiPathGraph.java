package com.github.azeroth.game.entity.taxi;

import Framework.Algorithms.*;
import com.github.azeroth.game.entity.player.Player;
import game.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaxiPathGraph {
    private static final ArrayList<TaxiNodesRecord> NODESBYVERTEX = new ArrayList<>();

    private static final HashMap<Integer, Integer> VERTICESBYNODE = new HashMap<Integer, Integer>();
    private static EdgeWeightedDigraph GRAPH;

    public static void initialize() {
        if (GRAPH != null) {
            return;
        }

        ArrayList<Tuple<Tuple<Integer, Integer>, Integer>> edges = new ArrayList<Tuple<Tuple<Integer, Integer>, Integer>>();

        // Initialize here
        for (var path : CliDB.TaxiPathStorage.values()) {
            var from = CliDB.TaxiNodesStorage.get(path.FromTaxiNode);
            var to = CliDB.TaxiNodesStorage.get(path.ToTaxiNode);

            if (from != null && to != null && from.flags.hasFlag(TaxiNodeFlags.Alliance.getValue() | TaxiNodeFlags.Horde.getValue()) && to.flags.hasFlag(TaxiNodeFlags.Alliance.getValue() | TaxiNodeFlags.Horde.getValue())) {
                addVerticeAndEdgeFromNodeInfo(from, to, path.id, edges);
            }
        }

        // create graph
        GRAPH = new EdgeWeightedDigraph(NODESBYVERTEX.size());

        for (var j = 0; j < edges.size(); ++j) {
            GRAPH.AddEdge(new DirectedEdge(edges.get(j).Item1.Item1, edges.get(j).Item1.item2, edges.get(j).item2));
        }
    }


    public static int getCompleteNodeRoute(TaxiNodesRecord from, TaxiNodesRecord to, Player player, ArrayList<Integer> shortestPath) {
		/*
		    Information about node algorithm from client
		    Since client does not give information about *ALL* nodes you have to pass by when going from sourceNodeID to destinationNodeID, we need to use Dijkstra algorithm.
		    Examining several paths I discovered the following algorithm:
		    * If destinationNodeID has is the next destination, connected directly to sourceNodeID, then, client just pick up this route regardless of distance
		    * else we use dijkstra to find the shortest path.
		    * When early landing is requested, according to behavior on retail, you can never end in a node you did not discovered before
		*/

        // Find if we have a direct path
        int pathId;
        tangible.OutObject<Integer> tempOut_pathId = new tangible.OutObject<Integer>();
        tangible.OutObject<Integer> tempOut__ = new tangible.OutObject<Integer>();
        global.getObjectMgr().getTaxiPath(from.id, to.id, tempOut_pathId, tempOut__);
        _ = tempOut__.outArgValue;
        pathId = tempOut_pathId.outArgValue;

        if (pathId != 0) {
            shortestPath.add(from.id);
            shortestPath.add(to.id);
        } else {
            shortestPath.clear();
            // We want to use Dijkstra on this graph
            DijkstraShortestPath g = new DijkstraShortestPath(GRAPH, (int) getVertexIDFromNodeID(from));
            var path = g.PathTo((int) getVertexIDFromNodeID(to));
            // found a path to the goal
            shortestPath.add(from.id);

            for (var edge : path) {
                //todo  test me No clue about this....
                var To = NODESBYVERTEX.get((int) edge.To);
                var requireFlag = (player.getTeam() == Team.ALLIANCE) ? TaxiNodeFlags.Alliance : TaxiNodeFlags.Horde;

                if (!To.flags.hasFlag(requireFlag)) {
                    continue;
                }

                var condition = CliDB.PlayerConditionStorage.get(To.ConditionID);

                if (condition != null) {
                    if (!ConditionManager.isPlayerMeetingCondition(player, condition)) {
                        continue;
                    }
                }

                shortestPath.add(getNodeIDFromVertexID(edge.To));
            }
        }

        return shortestPath.size();
    }

    //todo test me
    public static void getReachableNodesMask(TaxiNodesRecord from, byte[] mask) {
        DepthFirstSearch depthFirst = new DepthFirstSearch(GRAPH, getVertexIDFromNodeID(from), vertex ->
        {
            var taxiNode = CliDB.TaxiNodesStorage.get(getNodeIDFromVertexID(vertex));

            if (taxiNode != null) {
                mask[(taxiNode.Id - 1) / 8] |= (byte) (1 << (int) ((taxiNode.Id - 1) % 8));
            }
        });
    }

    private static void getTaxiMapPosition(Vector3 position, int mapId, tangible.OutObject<Vector2> uiMapPosition, tangible.OutObject<Integer> uiMapId) {
        if (!global.getDB2Mgr().GetUiMapPosition(position.X, position.Y, position.Z, mapId, 0, 0, 0, UiMapSystem.Adventure, false, uiMapId, uiMapPosition)) {
            global.getDB2Mgr().GetUiMapPosition(position.X, position.Y, position.Z, mapId, 0, 0, 0, UiMapSystem.taxi, false, uiMapId, uiMapPosition);
        }
    }


    private static int createVertexFromFromNodeInfoIfNeeded(TaxiNodesRecord node) {
        if (!VERTICESBYNODE.containsKey(node.id)) {
            VERTICESBYNODE.put(node.id, (int) NODESBYVERTEX.size());
            NODESBYVERTEX.add(node);
        }

        return VERTICESBYNODE.get(node.id);
    }


    private static void addVerticeAndEdgeFromNodeInfo(TaxiNodesRecord from, TaxiNodesRecord to, int pathId, ArrayList<Tuple<Tuple<Integer, Integer>, Integer>> edges) {
        if (from.id != to.id) {
            var fromVertexId = createVertexFromFromNodeInfoIfNeeded(from);
            var toVertexId = createVertexFromFromNodeInfoIfNeeded(to);

            var totalDist = 0.0f;
            var nodes = CliDB.TaxiPathNodesByPath.get(pathId);

            if (nodes.length < 2) {
                edges.add(Tuple.create(Tuple.create(fromVertexId, toVertexId), 0xFFFF));

                return;
            }

            var last = nodes.length;
            var first = 0;

            if (nodes.length > 2) {
                --last;
                ++first;
            }

            for (var i = first + 1; i < last; ++i) {
                if (nodes[i - 1].flags.hasFlag(TaxiPathNodeFlags.Teleport)) {
                    continue;
                }


                Vector2 pos1;
                tangible.OutObject<Vector2> tempOut_pos1 = new tangible.OutObject<Vector2>();
                int uiMap1;
                tangible.OutObject<Integer> tempOut_uiMap1 = new tangible.OutObject<Integer>();
                getTaxiMapPosition(nodes[i - 1].loc, nodes[i - 1].ContinentID, tempOut_pos1, tempOut_uiMap1);
                uiMap1 = tempOut_uiMap1.outArgValue;
                pos1 = tempOut_pos1.outArgValue;
                Vector2 pos2;
                tangible.OutObject<Vector2> tempOut_pos2 = new tangible.OutObject<Vector2>();
                int uiMap2;
                tangible.OutObject<Integer> tempOut_uiMap2 = new tangible.OutObject<Integer>();
                getTaxiMapPosition(nodes[i].loc, nodes[i].ContinentID, tempOut_pos2, tempOut_uiMap2);
                uiMap2 = tempOut_uiMap2.outArgValue;
                pos2 = tempOut_pos2.outArgValue;

                if (uiMap1 != uiMap2) {
                    continue;
                }

                totalDist += (float) Math.sqrt((float) Math.pow(pos2.X - pos1.X, 2) + (float) Math.pow(pos2.Y - pos1.Y, 2));
            }

            var dist = (int) (totalDist * 32767.0f);

            if (dist > 0xFFFF) {
                dist = 0xFFFF;
            }

            edges.add(Tuple.create(Tuple.create(fromVertexId, toVertexId), dist));
        }
    }


    private static int getVertexIDFromNodeID(TaxiNodesRecord node) {
        return VERTICESBYNODE.containsKey(node.id) ? VERTICESBYNODE.get(node.id) : Integer.MAX_VALUE;
    }


    private static int getNodeIDFromVertexID(int vertexID) {
        if (vertexID < NODESBYVERTEX.size()) {
            return NODESBYVERTEX.get((int) vertexID).id;
        }

        return Integer.MAX_VALUE;
    }
}
