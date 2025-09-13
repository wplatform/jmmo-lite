package com.github.azeroth.game.pvp;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.LocalizedDo;
import com.github.azeroth.game.map.ZoneScript;

import java.util.ArrayList;
import java.util.HashMap;


// base class for specific outdoor pvp handlers
public class OutdoorPvP extends ZoneScript {
    private final ArrayList<ObjectGuid>[] m_players = new ArrayList<ObjectGuid>[2];
    private final Map m_map;
    // the map of the objectives belonging to this outdoorpvp
    public HashMap<Long, OPvPCapturePoint> m_capturePoints = new HashMap<Long, OPvPCapturePoint>();
    public OutdoorPvPTypes m_TypeId = OutdoorPvPTypes.values()[0];

    public OutdoorPvP(Map map) {
        m_TypeId = OutdoorPvPTypes.forValue(0);
        m_map = map;
        m_players[0] = new ArrayList<>();
        m_players[1] = new ArrayList<>();
    }

    public void handlePlayerEnterZone(Player player, int zone) {
        m_players[player.getTeamId()].add(player.getGUID());
    }

    public void handlePlayerLeaveZone(Player player, int zone) {
        // inform the objectives of the leaving
        for (var pair : m_capturePoints.entrySet()) {
            pair.getValue().handlePlayerLeave(player);
        }

        // remove the world state information from the player (we can't keep everyone up to date, so leave out those who are not in the concerning zones)
        if (!player.getSession().getPlayerLogout()) {
            sendRemoveWorldStates(player);
        }

        m_players[player.getTeamId()].remove(player.getGUID());
        Log.outDebug(LogFilter.Outdoorpvp, "Player {0} left an outdoorpvp zone", player.getName());
    }

    public void handlePlayerResurrects(Player player, int zone) {
    }

    public boolean update(int diff) {
        var objective_changed = false;

        for (var pair : m_capturePoints.entrySet()) {
            if (pair.getValue().update(diff)) {
                objective_changed = true;
            }
        }

        return objective_changed;
    }

    public final int getWorldState(int worldStateId) {
        return global.getWorldStateMgr().getValue(worldStateId, m_map);
    }

    public final void setWorldState(int worldStateId, int value) {
        global.getWorldStateMgr().setValue(worldStateId, value, false, m_map);
    }

    public void handleKill(Player killer, Unit killed) {
        var group = killer.getGroup();

        if (group) {
            for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                var groupGuy = refe.getSource();

                if (!groupGuy) {
                    continue;
                }

                // skip if too far away
                if (!groupGuy.isAtGroupRewardDistance(killed)) {
                    continue;
                }

                // creature kills must be notified, even if not inside objective / not outdoor pvp active
                // player kills only count if active and inside objective
                if ((groupGuy.isOutdoorPvPActive() && isInsideObjective(groupGuy)) || killed.isTypeId(TypeId.UNIT)) {
                    handleKillImpl(groupGuy, killed);
                }
            }
        } else {
            // creature kills must be notified, even if not inside objective / not outdoor pvp active
            if ((killer.isOutdoorPvPActive() && isInsideObjective(killer)) || killed.isTypeId(TypeId.UNIT)) {
                handleKillImpl(killer, killed);
            }
        }
    }

    public boolean handleCustomSpell(Player player, int spellId, GameObject go) {
        for (var pair : m_capturePoints.entrySet()) {
            if (pair.getValue().handleCustomSpell(player, spellId, go)) {
                return true;
            }
        }

        return false;
    }

    public boolean handleOpenGo(Player player, GameObject go) {
        for (var pair : m_capturePoints.entrySet()) {
            if (pair.getValue().handleOpenGo(player, go) >= 0) {
                return true;
            }
        }

        return false;
    }

    public boolean handleDropFlag(Player player, int id) {
        for (var pair : m_capturePoints.entrySet()) {
            if (pair.getValue().handleDropFlag(player, id)) {
                return true;
            }
        }

        return false;
    }

    public boolean handleAreaTrigger(Player player, int trigger, boolean entered) {
        return false;
    }

    public final void registerZone(int zoneId) {
        global.getOutdoorPvPMgr().addZone(zoneId, this);
    }

    public final boolean hasPlayer(Player player) {
        return m_players[player.getTeamId()].contains(player.getGUID());
    }

    public final void teamCastSpell(int teamIndex, int spellId) {
        for (var guid : m_players[teamIndex]) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                if (spellId > 0) {
                    player.castSpell(player, (int) spellId, true);
                } else {
                    player.removeAura((int) -spellId); // by stack?
                }
            }
        }
    }

    public final void teamApplyBuff(int teamIndex, int spellId, int spellId2) {
        teamCastSpell(teamIndex, (int) spellId);
        teamCastSpell((int) (teamIndex == TeamId.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE), spellId2 != 0 ? -(int) spellId2 : -(int) spellId);
    }

    @Override
    public void onGameObjectCreate(GameObject go) {
        if (go.getGoType() != GameObjectTypes.controlZone) {
            return;
        }

        var cp = getCapturePoint(go.getSpawnId());

        if (cp != null) {
            cp.m_capturePoint = go;
        }
    }

    @Override
    public void onGameObjectRemove(GameObject go) {
        if (go.getGoType() != GameObjectTypes.controlZone) {
            return;
        }

        var cp = getCapturePoint(go.getSpawnId());

        if (cp != null) {
            cp.m_capturePoint = null;
        }
    }

    public final void sendDefenseMessage(int zoneId, int id) {
        DefenseMessageBuilder builder = new DefenseMessageBuilder(zoneId, id);
        var localizer = new LocalizedDo(builder);
        broadcastWorker(localizer, zoneId);
    }

    // setup stuff
    public boolean setupOutdoorPvP() {
        return true;
    }

    public void handleKillImpl(Player killer, Unit killed) {
    }

    // awards rewards for player kill
    public void awardKillBonus(Player player) {
    }

    public final OutdoorPvPTypes getTypeId() {
        return m_TypeId;
    }

    public void sendRemoveWorldStates(Player player) {
    }

    public final void addCapturePoint(OPvPCapturePoint cp) {
        if (m_capturePoints.containsKey(cp.m_capturePointSpawnId)) {
            Log.outError(LogFilter.Outdoorpvp, "OutdoorPvP.AddCapturePoint: CapturePoint {0} already exists!", cp.m_capturePointSpawnId);
        }

        m_capturePoints.put(cp.m_capturePointSpawnId, cp);
    }

    public final Map getMap() {
        return m_map;
    }

    private boolean isInsideObjective(Player player) {
        for (var pair : m_capturePoints.entrySet()) {
            if (pair.getValue().isInsideObjective(player)) {
                return true;
            }
        }

        return false;
    }

    private void broadcastPacket(ServerPacket packet) {
        // This is faster than sWorld.SendZoneMessage
        for (var team = 0; team < 2; ++team) {
            for (var guid : m_players[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendPacket(packet);
                }
            }
        }
    }

    private void broadcastWorker(IDoWork<Player> _worker, int zoneId) {
        for (int i = 0; i < SharedConst.PvpTeamsCount; ++i) {
            for (var guid : m_players[i]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    if (player.getZone() == zoneId) {
                        _worker.invoke(player);
                    }
                }
            }
        }
    }

    private OPvPCapturePoint getCapturePoint(long lowguid) {
        return m_capturePoints.get(lowguid);
    }
}
