package com.github.azeroth.game.pvp;


import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.map.Map;
import com.github.azeroth.game.listener.interfaces.ioutdoorpvp.IOutdoorPvPGetOutdoorPvP;

import java.util.HashMap;


public class OutdoorPvpManager {
    // contains all initiated outdoor pvp events
    // used when initing / cleaning up
    private final MultiMap<Map, OutdoorPvP> m_OutdoorPvPByMap = new MultiMap<Map, OutdoorPvP>();

    // maps the zone ids to an outdoor pvp event
    // used in player event handling
	private final HashMap<(
        // Holds the outdoor PvP templates
    private final int[] m_OutdoorMapIds = {0, 530, 530, 530, 530, 1};,     private final HashMap<OutdoorPvPTypes, Integer> m_OutdoorPvPDatas = new HashMap<OutdoorPvPTypes, Integer>();),OutdoorPvP>m_OutdoorPvPMap =new HashMap<(
        private final limitedThreadTaskManager threadTaskManager = new limitedThreadTaskManager(ConfigMgr.GetDefaultValue("Map.ParellelUpdateTasks", 20));, Map map),OutdoorPvP>();
int zoneId
Map map
int zoneId
    // update interval
    private int m_UpdateTimer;

    private OutdoorPvpManager() {
    }

    public final void initOutdoorPvP() {
        var oldMSTime = System.currentTimeMillis();

        //                                             0       1
        var result = DB.World.query("SELECT TypeId, ScriptName FROM outdoorpvp_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 outdoor PvP definitions. DB table `outdoorpvp_template` is empty.");

            return;
        }

        int count = 0;

        do {
            var typeId = OutdoorPvPTypes.forValue(result.<Byte>Read(0));

            if (global.getDisableMgr().isDisabledFor(DisableType.OutdoorPVP, (int) typeId.getValue(), null)) {
                continue;
            }

            if (typeId.getValue() >= OutdoorPvPTypes.max.getValue()) {
                Logs.SQL.error("Invalid OutdoorPvPTypes value {0} in outdoorpvp_template; skipped.", typeId);

                continue;
            }

            m_OutdoorPvPDatas.put(typeId, global.getObjectMgr().getScriptId(result.<String>Read(1)));

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s outdoor PvP definitions in %2$s ms", count, time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void createOutdoorPvPForMap(Map map) {
        for (var outdoorPvpType = OutdoorPvPTypes.HellfirePeninsula; outdoorPvpType.getValue() < OutdoorPvPTypes.max.getValue(); ++outdoorPvpType) {
            if (map.getId() != m_OutdoorMapIds[outdoorPvpType.getValue()]) {
                continue;
            }

            if (!m_OutdoorPvPDatas.containsKey(outdoorPvpType)) {
                Logs.SQL.error("Could not initialize OutdoorPvP object for type ID {0}; no entry in database.", outdoorPvpType);

                continue;
            }

            var pvp = global.getScriptMgr().<IOutdoorPvPGetOutdoorPvP, OutdoorPvP>RunScriptRet(p -> p.getOutdoorPvP(map), m_OutdoorPvPDatas.get(outdoorPvpType), null);

            if (pvp == null) {
                Log.outError(LogFilter.Outdoorpvp, "Could not initialize OutdoorPvP object for type ID {0}; got NULL pointer from script.", outdoorPvpType);

                continue;
            }

            if (!pvp.setupOutdoorPvP()) {
                Log.outError(LogFilter.Outdoorpvp, "Could not initialize OutdoorPvP object for type ID {0}; SetupOutdoorPvP failed.", outdoorPvpType);

                continue;
            }

            m_OutdoorPvPByMap.add(map, pvp);
        }
    }

    public final void destroyOutdoorPvPForMap(Map map) {
        m_OutdoorPvPByMap.remove(map);
    }

    public final void addZone(int zoneid, OutdoorPvP handle) {
        m_OutdoorPvPMap.put((handle.getMap(), zoneid), handle);
    }

    public final void handlePlayerEnterZone(Player player, int zoneid) {
        var outdoor = getOutdoorPvPToZoneId(player.getMap(), zoneid);

        if (outdoor == null) {
            return;
        }

        if (outdoor.hasPlayer(player)) {
            return;
        }

        outdoor.handlePlayerEnterZone(player, zoneid);
        Log.outDebug(LogFilter.Outdoorpvp, "Player {0} entered outdoorpvp id {1}", player.getGUID().toString(), outdoor.getTypeId());
    }

    public final void handlePlayerLeaveZone(Player player, int zoneid) {
        var outdoor = getOutdoorPvPToZoneId(player.getMap(), zoneid);

        if (outdoor == null) {
            return;
        }

        // teleport: remove once in removefromworld, once in updatezone
        if (!outdoor.hasPlayer(player)) {
            return;
        }

        outdoor.handlePlayerLeaveZone(player, zoneid);
        Log.outDebug(LogFilter.Outdoorpvp, "Player {0} left outdoorpvp id {1}", player.getGUID().toString(), outdoor.getTypeId());
    }

    public final OutdoorPvP getOutdoorPvPToZoneId(Map map, int zoneid) {
        return m_OutdoorPvPMap.get((map, zoneid));
    }

    public final void update(int diff) {
        m_UpdateTimer += diff;

        if (m_UpdateTimer > 1000) {

            for (var(_, outdoor) : m_OutdoorPvPByMap.KeyValueList) {
                threadTaskManager.Schedule(() -> outdoor.update(m_UpdateTimer));
            }

            threadTaskManager.Wait();
            m_UpdateTimer = 0;
        }
    }

    public final boolean handleCustomSpell(Player player, int spellId, GameObject go) {
        var pvp = player.getOutdoorPvP();

        if (pvp != null && pvp.hasPlayer(player)) {
            return pvp.handleCustomSpell(player, spellId, go);
        }

        return false;
    }

    public final boolean handleOpenGo(Player player, GameObject go) {
        var pvp = player.getOutdoorPvP();

        if (pvp != null && pvp.hasPlayer(player)) {
            return pvp.handleOpenGo(player, go);
        }

        return false;
    }

    public final void handleDropFlag(Player player, int spellId) {
        var pvp = player.getOutdoorPvP();

        if (pvp != null && pvp.hasPlayer(player)) {
            pvp.handleDropFlag(player, spellId);
        }
    }

    public final void handlePlayerResurrects(Player player, int zoneid) {
        var pvp = player.getOutdoorPvP();

        if (pvp != null && pvp.hasPlayer(player)) {
            pvp.handlePlayerResurrects(player, zoneid);
        }
    }

    public final String getDefenseMessage(int zoneId, int id, Locale locale) {
        var bct = CliDB.BroadcastTextStorage.get(id);

        if (bct != null) {
            return global.getDB2Mgr().GetBroadcastTextValue(bct, locale);
        }

        Log.outError(LogFilter.Outdoorpvp, "Can not find DefenseMessage (Zone: {0}, Id: {1}). BroadcastText (Id: {2}) does not exist.", zoneId, id, id);

        return "";
    }
}
