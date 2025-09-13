package com.github.azeroth.game.battlefield;


import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.grid.Cell;

import java.util.ArrayList;
import java.util.HashSet;

public class BfCapturePoint {
    // active Players in the area of the objective, 0 - alliance, 1 - horde
    private final HashSet<ObjectGuid>[] m_activePlayers = new HashSet<ObjectGuid>[SharedConst.PvpTeamsCount];
    protected int m_team;
    // Battlefield this objective belongs to
    protected BattleField m_Bf;
    // Total shift needed to capture the objective
    private float m_maxValue;
    private float m_minValue;

    // Maximum speed of capture
    private float m_maxSpeed;

    // The status of the objective
    private float m_value;

    // Objective states
    private BattleFieldObjectiveStates m_OldState = BattleFieldObjectiveStates.values()[0];
    private BattleFieldObjectiveStates m_State = BattleFieldObjectiveStates.values()[0];

    // Neutral value on capture bar
    private int m_neutralValuePct;

    // Capture point entry
    private int m_capturePointEntry;

    // Gameobject related to that capture point
    private ObjectGuid m_capturePointGUID = ObjectGuid.EMPTY;

    public BfCapturePoint(BattleField battlefield) {
        m_Bf = battlefield;
        m_capturePointGUID = ObjectGuid.Empty;
        m_team = TeamIds.Neutral;
        m_value = 0;
        m_minValue = 0.0f;
        m_maxValue = 0.0f;
        m_State = BattleFieldObjectiveStates.Neutral;
        m_OldState = BattleFieldObjectiveStates.Neutral;
        m_capturePointEntry = 0;
        m_neutralValuePct = 0;
        m_maxSpeed = 0;

        m_activePlayers[0] = new HashSet<ObjectGuid>();
        m_activePlayers[1] = new HashSet<ObjectGuid>();
    }

    public boolean handlePlayerEnter(Player player) {
        if (!m_capturePointGUID.isEmpty()) {
            var capturePoint = m_Bf.getGameObject(m_capturePointGUID);

            if (capturePoint) {
                player.sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldState1, 1);
                player.sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldstate2, (int) (Math.ceil((m_value + m_maxValue) / (2 * m_maxValue) * 100.0f)));
                player.sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldstate3, m_neutralValuePct);
            }
        }

        return m_activePlayers[player.getTeamId()].add(player.getGUID());
    }

    public void handlePlayerLeave(Player player) {
        if (!m_capturePointGUID.isEmpty()) {
            var capturePoint = m_Bf.getGameObject(m_capturePointGUID);

            if (capturePoint) {
                player.sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldState1, 0);
            }
        }

        m_activePlayers[player.getTeamId()].remove(player.getGUID());
    }

    public void sendChangePhase() {
        if (m_capturePointGUID.isEmpty()) {
            return;
        }

        var capturePoint = m_Bf.getGameObject(m_capturePointGUID);

        if (capturePoint) {
            // send this too, sometimes the slider disappears, dunno why :(
            sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldState1, 1);
            // send these updates to only the ones in this objective
            sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldstate2, (int) Math.ceil((m_value + m_maxValue) / (2 * m_maxValue) * 100.0f));
            // send this too, sometimes it resets :S
            sendUpdateWorldState(capturePoint.getTemplate().controlZone.worldstate3, m_neutralValuePct);
        }
    }

    public final boolean setCapturePointData(GameObject capturePoint) {
        Log.outError(LogFilter.Battlefield, "Creating capture point {0}", capturePoint.getEntry());

        m_capturePointGUID = capturePoint.getGUID();
        m_capturePointEntry = capturePoint.getEntry();

        // check info existence
        var goinfo = capturePoint.getTemplate();

        if (goinfo.type != GameObjectTypes.controlZone) {
            Log.outError(LogFilter.Server, "OutdoorPvP: GO {0} is not capture point!", capturePoint.getEntry());

            return false;
        }

        // get the needed values from goinfo
        m_maxValue = goinfo.controlZone.maxTime;
        m_maxSpeed = m_maxValue / (goinfo.controlZone.minTime != 0 ? goinfo.controlZone.minTime : 60);
        m_neutralValuePct = goinfo.controlZone.neutralPercent;
        m_minValue = m_maxValue * goinfo.controlZone.neutralPercent / 100;

        if (m_team == TeamId.ALLIANCE) {
            m_value = m_maxValue;
            m_State = BattleFieldObjectiveStates.Alliance;
        } else {
            m_value = -m_maxValue;
            m_State = BattleFieldObjectiveStates.Horde;
        }

        return true;
    }

    public boolean update(int diff) {
        if (m_capturePointGUID.isEmpty()) {
            return false;
        }

        var capturePoint = m_Bf.getGameObject(m_capturePointGUID);

        if (capturePoint) {
            float radius = capturePoint.getTemplate().controlZone.radius;

            for (byte team = 0; team < SharedConst.PvpTeamsCount; ++team) {
                for (var guid : m_activePlayers[team]) {
                    var player = global.getObjAccessor().findPlayer(guid);

                    if (player) {
                        if (!capturePoint.isWithinDistInMap(player, radius) || !player.isOutdoorPvPActive()) {
                            handlePlayerLeave(player);
                        }
                    }
                }
            }

            ArrayList<Unit> players = new ArrayList<>();
            var checker = new AnyPlayerInObjectRangeCheck(capturePoint, radius);
            var searcher = new PlayerListSearcher(capturePoint, players, checker);
            Cell.visitGrid(capturePoint, searcher, radius);

            for (Player player : players) {
                if (player.isOutdoorPvPActive()) {
                    if (m_activePlayers[player.getTeamId()].add(player.getGUID())) {
                        handlePlayerEnter(player);
                    }
                }
            }
        }

        // get the difference of numbers
        var fact_diff = ((float) m_activePlayers[TeamId.ALLIANCE].size() - m_activePlayers[TeamId.HORDE].size()) * diff / 1000;

        if (MathUtil.fuzzyEq(fact_diff, 0.0f)) {
            return false;
        }

        Team Challenger;
        var maxDiff = m_maxSpeed * diff;

        if (fact_diff < 0) {
            // horde is in majority, but it's already horde-controlled . no change
            if (m_State == BattleFieldObjectiveStates.Horde && m_value <= -m_maxValue) {
                return false;
            }

            if (fact_diff < -maxDiff) {
                fact_diff = -maxDiff;
            }

            Challenger = Team.Horde;
        } else {
            // ally is in majority, but it's already ally-controlled . no change
            if (m_State == BattleFieldObjectiveStates.Alliance && m_value >= m_maxValue) {
                return false;
            }

            if (fact_diff > maxDiff) {
                fact_diff = maxDiff;
            }

            Challenger = Team.ALLIANCE;
        }

        var oldValue = m_value;
        var oldTeam = m_team;

        m_OldState = m_State;

        m_value += fact_diff;

        if (m_value < -m_minValue) // red
        {
            if (m_value < -m_maxValue) {
                m_value = -m_maxValue;
            }

            m_State = BattleFieldObjectiveStates.Horde;
            m_team = TeamId.HORDE;
        } else if (m_value > m_minValue) // blue
        {
            if (m_value > m_maxValue) {
                m_value = m_maxValue;
            }

            m_State = BattleFieldObjectiveStates.Alliance;
            m_team = TeamId.ALLIANCE;
        } else if (oldValue * m_value <= 0) // grey, go through mid point
        {
            // if challenger is ally, then n.a challenge
            if (Challenger == Team.ALLIANCE) {
                m_State = BattleFieldObjectiveStates.NeutralAllianceChallenge;
            }
            // if challenger is horde, then n.h challenge
            else if (Challenger == Team.Horde) {
                m_State = BattleFieldObjectiveStates.NeutralHordeChallenge;
            }

            m_team = TeamIds.Neutral;
        } else // grey, did not go through mid point
        {
            // old phase and current are on the same side, so one team challenges the other
            if (Challenger == Team.ALLIANCE && (m_OldState == BattleFieldObjectiveStates.Horde || m_OldState == BattleFieldObjectiveStates.NeutralHordeChallenge)) {
                m_State = BattleFieldObjectiveStates.HordeAllianceChallenge;
            } else if (Challenger == Team.Horde && (m_OldState == BattleFieldObjectiveStates.Alliance || m_OldState == BattleFieldObjectiveStates.NeutralAllianceChallenge)) {
                m_State = BattleFieldObjectiveStates.AllianceHordeChallenge;
            }

            m_team = TeamIds.Neutral;
        }

        if (MathUtil.fuzzyNe(m_value, oldValue)) {
            sendChangePhase();
        }

        if (m_OldState != m_State) {
            if (oldTeam != m_team) {
                changeTeam(oldTeam);
            }

            return true;
        }

        return false;
    }

    public void changeTeam(int oldTeam) {
    }

    public final int getCapturePointEntry() {
        return m_capturePointEntry;
    }

    private GameObject getCapturePointGo() {
        return m_Bf.getGameObject(m_capturePointGUID);
    }

    private boolean delCapturePoint() {
        if (!m_capturePointGUID.isEmpty()) {
            var capturePoint = m_Bf.getGameObject(m_capturePointGUID);

            if (capturePoint) {
                capturePoint.setRespawnTime(0); // not save respawn time
                capturePoint.delete();
                capturePoint.close();
            }

            m_capturePointGUID.clear();
        }

        return true;
    }

    private void sendUpdateWorldState(int field, int value) {
        for (byte team = 0; team < SharedConst.PvpTeamsCount; ++team) {
            for (var guid : m_activePlayers[team]) // send to all players present in the area
            {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendUpdateWorldState(field, value);
                }
            }
        }
    }

    private void sendObjectiveComplete(int id, ObjectGuid guid) {
        int team;

        switch (m_State) {
            case Alliance:
                team = TeamId.ALLIANCE;

                break;
            case Horde:
                team = TeamId.HORDE;

                break;
            default:
                return;
        }

        // send to all players present in the area
        for (var _guid : m_activePlayers[team]) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                player.killedMonsterCredit(id, guid);
            }
        }
    }

    private boolean isInsideObjective(Player player) {
        return m_activePlayers[player.getTeamId()].contains(player.getGUID());
    }

    private int getTeamId() {
        return m_team;
    }
}
