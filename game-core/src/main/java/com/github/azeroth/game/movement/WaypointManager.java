package game;


import com.github.azeroth.game.domain.misc.WaypointMoveType;
import com.github.azeroth.game.domain.misc.WaypointNode;
import com.github.azeroth.game.domain.misc.WaypointPath;

import java.util.ArrayList;
import java.util.HashMap;

public final class WaypointManager {
    private final HashMap<Integer, waypointPath> waypointStore = new HashMap<Integer, waypointPath>();

    private WaypointManager() {
    }

    public void load() {
        var oldMSTime = System.currentTimeMillis();

        //                                          0    1         2           3          4            5           6        7      8           9
        var result = DB.World.query("SELECT id, point, position_x, position_y, position_z, orientation, move_type, delay, action, action_chance FROM waypoint_data ORDER BY id, point");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 waypoints. DB table `waypoint_data` is empty!");

            return;
        }

        int count = 0;

        do {
            var pathId = result.<Integer>Read(0);

            var x = result.<Float>Read(2);
            var y = result.<Float>Read(3);
            var z = result.<Float>Read(4);
            Float o = null;

            if (!result.IsNull(5)) {
                o = result.<Float>Read(5);
            }

            x = MapDefine.normalizeMapCoord(x);
            y = MapDefine.normalizeMapCoord(y);

            WaypointNode waypoint = new WaypointNode();
            waypoint.id = result.<Integer>Read(1);
            waypoint.x = x;
            waypoint.y = y;
            waypoint.z = z;
            waypoint.orientation = o;
            waypoint.moveType = WaypointMoveType.forValue(result.<Integer>Read(6));

            if (waypoint.moveType.getValue() >= WaypointMoveType.max.getValue()) {
                Logs.SQL.error(String.format("Waypoint %1$s in waypoint_data has invalid move_type, ignoring", waypoint.id));

                continue;
            }

            waypoint.delay = result.<Integer>Read(7);
            waypoint.eventId = result.<Integer>Read(8);
            waypoint.eventChance = result.<Byte>Read(9);

            if (!waypointStore.containsKey(pathId)) {
                waypointStore.put(pathId, new waypointPath());
            }

            var path = waypointStore.get(pathId);
            path.id = pathId;
            path.nodes.add(waypoint);

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s waypoints in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public void reloadPath(int id) {
        waypointStore.remove(id);

        var stmt = DB.World.GetPreparedStatement(WorldStatements.SEL_WAYPOINT_DATA_BY_ID);
        stmt.AddValue(0, id);
        var result = DB.World.query(stmt);

        if (result.isEmpty()) {
            return;
        }

        ArrayList<WaypointNode> values = new ArrayList<>();

        do {
            var x = result.<Float>Read(1);
            var y = result.<Float>Read(2);
            var z = result.<Float>Read(3);
            Float o = null;

            if (!result.IsNull(4)) {
                o = result.<Float>Read(4);
            }

            x = MapDefine.normalizeMapCoord(x);
            y = MapDefine.normalizeMapCoord(y);

            WaypointNode waypoint = new WaypointNode();
            waypoint.id = result.<Integer>Read(0);
            waypoint.x = x;
            waypoint.y = y;
            waypoint.z = z;
            waypoint.orientation = o;
            waypoint.moveType = WaypointMoveType.forValue(result.<Integer>Read(5));

            if (waypoint.moveType.getValue() >= WaypointMoveType.max.getValue()) {
                Logs.SQL.error(String.format("Waypoint %1$s in waypoint_data has invalid move_type, ignoring", waypoint.id));

                continue;
            }

            waypoint.delay = result.<Integer>Read(6);
            waypoint.eventId = result.<Integer>Read(7);
            waypoint.eventChance = result.<Byte>Read(8);

            values.add(waypoint);
        } while (result.NextRow());

        waypointStore.put(id, new waypointPath(id, values));
    }

    public WaypointPath getPath(int id) {
        return waypointStore.get(id);
    }
}
