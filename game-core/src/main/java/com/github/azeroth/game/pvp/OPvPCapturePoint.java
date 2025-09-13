package com.github.azeroth.game.pvp;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.grid.Cell;

import java.util.ArrayList;
import java.util.HashSet;

public class OPvPCapturePoint {
    public long m_capturePointSpawnId;

    public GameObject m_capturePoint;

    // active players in the area of the objective, 0 - alliance, 1 - horde
    public HashSet<ObjectGuid>[] m_activePlayers = new HashSet<ObjectGuid>[2];

    // total shift needed to capture the objective
    public float m_maxValue;

    // the status of the objective
    public float m_value;

    // neutral second on capture bar
    public int m_neutralValuePct;

    private float m_minValue;

    // maximum speed of capture
    private float m_maxSpeed;

    private int m_team;

    // objective states
    private ObjectiveStates oldState = ObjectiveStates.values()[0];
    private Objectivestates state = ObjectiveStates.values()[0];
    // pointer to the OutdoorPvP this objective belongs to
    private OutdoorpvP pvP;

    public OPvPCapturePoint(OutdoorPvP pvp) {
        m_team = TeamIds.Neutral;
        setOldState(ObjectiveStates.Neutral);
        setState(ObjectiveStates.Neutral);
        setPvP(pvp);

        m_activePlayers[0] = new HashSet<ObjectGuid>();
        m_activePlayers[1] = new HashSet<ObjectGuid>();
    }

    public final ObjectiveStates getOldState() {
        return oldState;
    }

    public final void setOldState(ObjectiveStates value) {
        oldState = value;
    }

    public final ObjectiveStates getState() {
        return state;
    }

    public final void setState(ObjectiveStates value) {
        state = value;
    }

    public final OutdoorPvP getPvP() {
        return pvP;
    }

    public final void setPvP(OutdoorPvP value) {
        pvP = value;
    }

    public boolean handlePlayerEnter(Player player) {
        if (m_capturePoint) {
            player.sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldState1, 1);
            player.sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldstate2, (int) Math.ceil((m_value + m_maxValue) / (2 * m_maxValue) * 100.0f));
            player.sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldstate3, m_neutralValuePct);
        }

        return m_activePlayers[player.getTeamId()].add(player.getGUID());
    }

    public void handlePlayerLeave(Player player) {
        if (m_capturePoint) {
            player.sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldState1, 0);
        }

        m_activePlayers[player.getTeamId()].remove(player.getGUID());
    }

    public void sendChangePhase() {
        if (!m_capturePoint) {
            return;
        }

        // send this too, sometimes the slider disappears, dunno why :(
        sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldState1, 1);
        // send these updates to only the ones in this objective
        sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldstate2, (int) Math.ceil((m_value + m_maxValue) / (2 * m_maxValue) * 100.0f));
        // send this too, sometimes it resets :S
        sendUpdateWorldState(m_capturePoint.getTemplate().controlZone.worldstate3, m_neutralValuePct);
    }

    public final boolean setCapturePointData(int entry) {
        Log.outDebug(LogFilter.Outdoorpvp, "Creating capture point {0}", entry);

        // check info existence
        var goinfo = global.getObjectMgr().getGameObjectTemplate(entry);

        if (goinfo == null || goinfo.type != GameObjectTypes.controlZone) {
            Log.outError(LogFilter.Outdoorpvp, "OutdoorPvP: GO {0} is not capture point!", entry);

            return false;
        }

        // get the needed values from goinfo
        m_maxValue = goinfo.controlZone.maxTime;
        m_maxSpeed = m_maxValue / (goinfo.controlZone.minTime != 0 ? goinfo.controlZone.minTime : 60);
        m_neutralValuePct = goinfo.controlZone.neutralPercent;
        m_minValue = MathUtil.CalculatePct(m_maxValue, m_neutralValuePct);

        return true;
    }

    public boolean update(int diff) {
        if (!m_capturePoint) {
            return false;
        }

        float radius = m_capturePoint.getTemplate().controlZone.radius;

        for (var team = 0; team < 2; ++team) {
            for (var playerGuid : m_activePlayers[team].ToList()) {
                var player = global.getObjAccessor().findPlayer(playerGuid);

                if (player) {
                    if (!m_capturePoint.isWithinDistInMap(player, radius) || !player.isOutdoorPvPActive()) {
                        handlePlayerLeave(player);
                    }
                }
            }
        }

        ArrayList<Unit> players = new ArrayList<>();
        var checker = new AnyPlayerInObjectRangeCheck(m_capturePoint, radius);
        var searcher = new PlayerListSearcher(m_capturePoint, players, checker);
        Cell.visitGrid(m_capturePoint, searcher, radius);

        for (Player player : players) {
            if (player.isOutdoorPvPActive()) {
                if (m_activePlayers[player.getTeamId()].add(player.getGUID())) {
                    handlePlayerEnter(player);
                }
            }
        }

        // get the difference of numbers
        var fact_diff = (float) (m_activePlayers[0].size() - m_activePlayers[1].size()) * diff / 1000;

        if (fact_diff == 0.0f) {
            return false;
        }

        Team Challenger;
        var maxDiff = m_maxSpeed * diff;

        if (fact_diff < 0) {
            // horde is in majority, but it's already horde-controlled . no change
            if (getState() == ObjectiveStates.Horde && m_value <= -m_maxValue) {
                return false;
            }

            if (fact_diff < -maxDiff) {
                fact_diff = -maxDiff;
            }

            Challenger = Team.Horde;
        } else {
            // ally is in majority, but it's already ally-controlled . no change
            if (getState() == ObjectiveStates.Alliance && m_value >= m_maxValue) {
                return false;
            }

            if (fact_diff > maxDiff) {
                fact_diff = maxDiff;
            }

            Challenger = Team.ALLIANCE;
        }

        var oldValue = m_value;
        var oldTeam = m_team;

        setOldState(getState());

        m_value += fact_diff;

        if (m_value < -m_minValue) // red
        {
            if (m_value < -m_maxValue) {
                m_value = -m_maxValue;
            }

            setState(ObjectiveStates.Horde);
            m_team = TeamId.HORDE;
        } else if (m_value > m_minValue) // blue
        {
            if (m_value > m_maxValue) {
                m_value = m_maxValue;
            }

            setState(ObjectiveStates.Alliance);
            m_team = TeamId.ALLIANCE;
        } else if (oldValue * m_value <= 0) // grey, go through mid point
        {
            // if challenger is ally, then n.a challenge
            if (Challenger == Team.ALLIANCE) {
                setState(ObjectiveStates.NeutralAllianceChallenge);
            }
            // if challenger is horde, then n.h challenge
            else if (Challenger == Team.Horde) {
                setState(ObjectiveStates.NeutralHordeChallenge);
            }

            m_team = TeamIds.Neutral;
        } else // grey, did not go through mid point
        {
            // old phase and current are on the same side, so one team challenges the other
            if (Challenger == Team.ALLIANCE && (getOldState() == ObjectiveStates.Horde || getOldState() == ObjectiveStates.NeutralHordeChallenge)) {
                setState(ObjectiveStates.HordeAllianceChallenge);
            } else if (Challenger == Team.Horde && (getOldState() == ObjectiveStates.Alliance || getOldState() == ObjectiveStates.NeutralAllianceChallenge)) {
                setState(ObjectiveStates.AllianceHordeChallenge);
            }

            m_team = TeamIds.Neutral;
        }

        if (m_value != oldValue) {
            sendChangePhase();
        }

        if (getOldState() != getState()) {
            if (oldTeam != m_team) {
                changeTeam(oldTeam);
            }

            changeState();

            return true;
        }

        return false;
    }

    public final void sendUpdateWorldState(int field, int value) {
        for (var team = 0; team < 2; ++team) {
            // send to all players present in the area
            for (var guid : m_activePlayers[team]) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendUpdateWorldState(field, value);
                }
            }
        }
    }

    public final void sendObjectiveComplete(int id, ObjectGuid guid) {
        int team;

        switch (getState()) {
            case Alliance:
                team = 0;

                break;
            case Horde:
                team = 1;

                break;
            default:
                return;
        }

        // send to all players present in the area
        for (var playerGuid : m_activePlayers[team]) {
            var player = global.getObjAccessor().findPlayer(playerGuid);

            if (player) {
                player.killedMonsterCredit(id, guid);
            }
        }
    }

    public final boolean isInsideObjective(Player player) {
        var plSet = m_activePlayers[player.getTeamId()];

        return plSet.contains(player.getGUID());
    }

    public boolean handleCustomSpell(Player player, int spellId, GameObject go) {
        if (!player.isOutdoorPvPActive()) {
            return false;
        }

        return false;
    }

    public boolean handleDropFlag(Player player, int id) {
        return false;
    }

    public int handleOpenGo(Player player, GameObject go) {
        return -1;
    }

    public void changeState() {
    }

    public void changeTeam(int oldTeam) {
    }
}
