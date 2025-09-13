package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.WorldSafeLocsEntry;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;


class BgArathiBasin extends Battleground {
    //Const
    public static final int notABBGWeekendHonorTicks = 260;
    public static final int ABBGWeekendHonorTicks = 160;
    public static final int notABBGWeekendReputationTicks = 160;
    public static final int ABBGWeekendReputationTicks = 120;

    public static final int EVENTSTARTBATTLE = 9158; // Achievement: Let's Get This Done

    public static final int SOUNDCLAIMED = 8192;
    public static final int SOUNDCAPTUREDALLIANCE = 8173;
    public static final int SOUNDCAPTUREDHORDE = 8213;
    public static final int soundAssaultedAlliance = 8212;
    public static final int soundAssaultedHorde = 8174;
    public static final int SOUNDNEARVICTORYALLIANCE = 8456;
    public static final int SOUNDNEARVICTORYHORDE = 8457;

    public static final int FLAGCAPTURINGTIME = 60000;

    public static final int WARNINGNEARVICTORYSCORE = 1400;
    public static final int MAXTEAMSCORE = 1500;

    public static final int exploitTeleportLocationAlliance = 3705;
    public static final int exploitTeleportLocationHorde = 3706;

    public static Position[] NODEPOSITIONS =
            {
                    new Position(1166.785f, 1200.132f, -56.70859f, 0.9075713f),
                    new Position(977.0156f, 1046.616f, -44.80923f, -2.600541f),
                    new Position(806.1821f, 874.2723f, -55.99371f, -2.303835f),
                    new Position(856.1419f, 1148.902f, 11.18469f, -2.303835f),
                    new Position(1146.923f, 848.1782f, -110.917f, -0.7330382f)
            };

    // x, y, z, o, rot0, rot1, rot2, rot3
    public static float[][] DOORPOSITIONS =
            {
                    new float[]{1284.597f, 1281.167f, -15.97792f, 0.7068594f, 0.012957f, -0.060288f, 0.344959f, 0.93659f},
                    new float[]{708.0903f, 708.4479f, -17.8342f, -2.391099f, 0.050291f, 0.015127f, 0.929217f, -0.365784f}
            };

    // Tick intervals and given points: case 0, 1, 2, 3, 4, 5 captured nodes
    public static int[] TICKINTERVALS = {0, 12000, 9000, 6000, 3000, 1000};

    public static int[] TICKPOINTS = {0, 10, 10, 10, 10, 30};

    // WorldSafeLocs ids for 5 nodes, and for ally, and horde starting location
    public static int[] GRAVEYARDIDS = {895, 894, 893, 897, 896, 898, 899};

    // x, y, z, o
    public static float[][] BUFFPOSITIONS =
            {
                    new float[]{1185.566f, 1184.629f, -56.36329f, 2.303831f},
                    new float[]{990.1131f, 1008.73f, -42.60328f, 0.8203033f},
                    new float[]{818.0089f, 842.3543f, -56.54062f, 3.176533f},
                    new float[]{808.8463f, 1185.417f, 11.92161f, 5.619962f},
                    new float[]{1147.091f, 816.8362f, -98.39896f, 6.056293f}
            };

    public static Position[] SPIRITGUIDEPOS =
            {
                    new Position(1200.03f, 1171.09f, -56.47f, 5.15f),
                    new Position(1017.43f, 960.61f, -42.95f, 4.88f),
                    new Position(833.00f, 793.00f, -57.25f, 5.27f),
                    new Position(775.17f, 1206.40f, 15.79f, 1.90f),
                    new Position(1207.48f, 787.00f, -83.36f, 5.51f),
                    new Position(1354.05f, 1275.48f, -11.30f, 4.77f),
                    new Position(714.61f, 646.15f, -10.87f, 4.34f)
            };

    public static int[] NODESTATES = {1767, 1782, 1772, 1792, 1787};

    public static int[] NODEICONS = {1842, 1846, 1845, 1844, 1843};

    /**
     * Nodes info:
     * 0: neutral
     * 1: ally contested
     * 2: horde contested
     * 3: ally occupied
     * 4: horde occupied
     */
    private final ABNodeStatus[] m_Nodes = new ABNodeStatus[ABBattlegroundNodes.DynamicNodesCount];

    private final ABNodeStatus[] m_prevNodes = new ABNodeStatus[ABBattlegroundNodes.DynamicNodesCount];
    private final BannerTimer[] m_BannerTimers = new BannerTimer[ABBattlegroundNodes.DynamicNodesCount];
    private final int[] m_NodeTimers = new int[ABBattlegroundNodes.DynamicNodesCount];
    private final int[] m_lastTick = new int[SharedConst.PvpTeamsCount];
    private final int[] m_HonorScoreTics = new int[SharedConst.PvpTeamsCount];
    private final int[] m_ReputationScoreTics = new int[SharedConst.PvpTeamsCount];
    private boolean m_IsInformedNearVictory;
    private int m_HonorTics;
    private int m_ReputationTics;

    public BgArathiBasin(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        m_IsInformedNearVictory = false;
        m_BuffChange = true;
        bgObjects = new ObjectGuid[ABObjectTypes.Max];
        bgCreatures = new ObjectGuid[ABBattlegroundNodes.ALLCOUNT + 5]; //+5 for aura triggers

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            m_Nodes[i] = ABNodeStatus.forValue(0);
            m_prevNodes[i] = ABNodeStatus.forValue(0);
            m_NodeTimers[i] = 0;
            m_BannerTimers[i].timer = 0;
            m_BannerTimers[i].type = 0;
            m_BannerTimers[i].teamIndex = 0;
        }

        for (byte i = 0; i < SharedConst.PvpTeamsCount; ++i) {
            m_lastTick[i] = 0;
            m_HonorScoreTics[i] = 0;
            m_ReputationScoreTics[i] = 0;
        }

        m_HonorTics = 0;
        m_ReputationTics = 0;
    }

    @Override
    public void postUpdateImpl(int diff) {
        if (getStatus() == BattlegroundStatus.inProgress) {
            int[] team_points = {0, 0};

            for (byte node = 0; node < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++node) {
                // 3 sec delay to spawn new banner instead previous despawned one
                if (m_BannerTimers[node].timer != 0) {
                    if (m_BannerTimers[node].timer > diff) {
                        m_BannerTimers[node].timer -= diff;
                    } else {
                        m_BannerTimers[node].timer = 0;
                        _CreateBanner(node, ABNodeStatus.forValue(m_BannerTimers[node].type), m_BannerTimers[node].teamIndex, false);
                    }
                }

                // 1-minute to occupy a node from contested state
                if (m_NodeTimers[node] != 0) {
                    if (m_NodeTimers[node] > diff) {
                        m_NodeTimers[node] -= diff;
                    } else {
                        m_NodeTimers[node] = 0;
                        // Change from contested to occupied !
                        var teamIndex = (int) m_Nodes[node] - 1;
                        m_prevNodes[node] = m_Nodes[node];
                        m_Nodes[node] += 2;
                        // burn current contested banner
                        _DelBanner(node, ABNodeStatus.Contested, (byte) teamIndex);
                        // create new occupied banner
                        _CreateBanner(node, ABNodeStatus.Occupied, teamIndex, true);
                        _SendNodeUpdate(node);
                        _NodeOccupied(node, (teamIndex == TeamId.ALLIANCE) ? Team.ALLIANCE : Team.Horde);
                        // Message to chatlog

                        if (teamIndex == 0) {
                            sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceTaken, ChatMsg.BgSystemAlliance);
                            playSoundToAll(SOUNDCAPTUREDALLIANCE);
                        } else {
                            sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeTaken, ChatMsg.BgSystemHorde);
                            playSoundToAll(SOUNDCAPTUREDHORDE);
                        }
                    }
                }

                for (var team = 0; team < SharedConst.PvpTeamsCount; ++team) {
                    if (m_Nodes[node] == team + ABNodeStatus.Occupied) {
                        ++team_points[team];
                    }
                }
            }

            // Accumulate points
            for (var team = 0; team < SharedConst.PvpTeamsCount; ++team) {
                var points = team_points[team];

                if (points == 0) {
                    continue;
                }

                m_lastTick[team] += diff;

                if (m_lastTick[team] > TickIntervals[points]) {
                    m_lastTick[team] -= TickIntervals[points];
                    m_TeamScores[team] += TickPoints[points];
                    m_HonorScoreTics[team] += TickPoints[points];
                    m_ReputationScoreTics[team] += TickPoints[points];

                    if (m_ReputationScoreTics[team] >= m_ReputationTics) {
                        if (team == TeamId.ALLIANCE) {
                            rewardReputationToTeam(509, 10, Team.ALLIANCE);
                        } else {
                            rewardReputationToTeam(510, 10, Team.Horde);
                        }

                        m_ReputationScoreTics[team] -= m_ReputationTics;
                    }

                    if (m_HonorScoreTics[team] >= m_HonorTics) {
                        rewardHonorToTeam(getBonusHonorFromKill(1), (team == TeamId.ALLIANCE) ? Team.ALLIANCE : Team.Horde);
                        m_HonorScoreTics[team] -= m_HonorTics;
                    }

                    if (!m_IsInformedNearVictory && m_TeamScores[team] > WARNINGNEARVICTORYSCORE) {
                        if (team == TeamId.ALLIANCE) {
                            sendBroadcastText(ABBattlegroundBroadcastTexts.allianceNearVictory, ChatMsg.BgSystemNeutral);
                            playSoundToAll(SOUNDNEARVICTORYALLIANCE);
                        } else {
                            sendBroadcastText(ABBattlegroundBroadcastTexts.hordeNearVictory, ChatMsg.BgSystemNeutral);
                            playSoundToAll(SOUNDNEARVICTORYHORDE);
                        }

                        m_IsInformedNearVictory = true;
                    }

                    if (m_TeamScores[team] > MAXTEAMSCORE) {
                        m_TeamScores[team] = MAXTEAMSCORE;
                    }

                    if (team == TeamId.ALLIANCE) {
                        updateWorldState(ABWorldStates.RESOURCESALLY, (int) m_TeamScores[team]);
                    } else {
                        updateWorldState(ABWorldStates.RESOURCESHORDE, (int) m_TeamScores[team]);
                    }

                    // update achievement flags
                    // we increased m_TeamScores[team] so we just need to check if it is 500 more than other teams resources
                    var otherTeam = (team + 1) % SharedConst.PvpTeamsCount;

                    if (m_TeamScores[team] > m_TeamScores[otherTeam] + 500) {
                        if (team == TeamId.ALLIANCE) {
                            updateWorldState(ABWorldStates.HAD500DISADVANTAGEHORDE, 1);
                        } else {
                            updateWorldState(ABWorldStates.HAD500DISADVANTAGEALLIANCE, 1);
                        }
                    }
                }
            }

            // Test win condition
            if (m_TeamScores[TeamId.ALLIANCE] >= MAXTEAMSCORE) {
                endBattleground(Team.ALLIANCE);
            } else if (m_TeamScores[TeamId.HORDE] >= MAXTEAMSCORE) {
                endBattleground(Team.Horde);
            }
        }
    }

    @Override
    public void startingEventCloseDoors() {
        // despawn banners, auras and buffs
        for (var obj = ABObjectTypes.BANNERNEUTRAL; obj < ABBattlegroundNodes.DynamicNodesCount * 8; ++obj) {
            spawnBGObject(obj, BattlegroundConst.RespawnOneDay);
        }

        for (var i = 0; i < ABBattlegroundNodes.DynamicNodesCount * 3; ++i) {
            spawnBGObject(ABObjectTypes.SPEEDBUFFSTABLES + i, BattlegroundConst.RespawnOneDay);
        }

        // Starting doors
        doorClose(ABObjectTypes.GATEA);
        doorClose(ABObjectTypes.GATEH);
        spawnBGObject(ABObjectTypes.GATEA, BattlegroundConst.RespawnImmediately);
        spawnBGObject(ABObjectTypes.GATEH, BattlegroundConst.RespawnImmediately);

        // Starting base spirit guides
        _NodeOccupied((byte) ABBattlegroundNodes.SPIRITALIANCE, Team.ALLIANCE);
        _NodeOccupied((byte) ABBattlegroundNodes.SPIRITHORDE, Team.Horde);
    }

    @Override
    public void startingEventOpenDoors() {
        // spawn neutral banners
        for (int banner = ABObjectTypes.BANNERNEUTRAL, i = 0; i < 5; banner += 8, ++i) {
            spawnBGObject(banner, BattlegroundConst.RespawnImmediately);
        }

        for (var i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            //randomly select buff to spawn
            var buff = RandomUtil.IRand(0, 2);
            spawnBGObject(ABObjectTypes.SPEEDBUFFSTABLES + buff + i * 3, BattlegroundConst.RespawnImmediately);
        }

        doorOpen(ABObjectTypes.GATEA);
        doorOpen(ABObjectTypes.GATEH);

        // Achievement: Let's Get This Done
        triggerGameEvent(EVENTSTARTBATTLE);
    }

    @Override
    public void addPlayer(Player player) {
        var isInBattleground = isPlayerInBattleground(player.getGUID());
        super.addPlayer(player);

        if (!isInBattleground) {
            playerScores.put(player.getGUID(), new BattlegroundABScore(player.getGUID(), player.getBgTeam()));
        }
    }

    @Override
    public void removePlayer(Player player, ObjectGuid guid, Team team) {
    }

    @Override
    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        switch (trigger) {
            case 6635: // Horde Start
            case 6634: // Alliance Start
                if (getStatus() == BattlegroundStatus.WaitJoin && !entered) {
                    teleportPlayerToExploitLocation(player);
                }

                break;
            case 3948: // Arathi Basin Alliance Exit.
            case 3949: // Arathi Basin Horde Exit.
            case 3866: // Stables
            case 3869: // Gold Mine
            case 3867: // Farm
            case 3868: // Lumber Mill
            case 3870: // Black Smith
            case 4020: // Unk1
            case 4021: // Unk2
            case 4674: // Unk3
            default:
                super.handleAreaTrigger(player, trigger, entered);

                break;
        }
    }

    //Invoked if a player used a banner as a gameobject
    @Override
    public void eventPlayerClickedOnFlag(Player source, GameObject target_obj) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        byte node = (byte) ABBattlegroundNodes.NODESTABLES;
        var obj = getBgMap().getGameObject(BgObjects[node * 8 + 7]);

        while ((node < ABBattlegroundNodes.DYNAMICNODESCOUNT) && ((!obj) || (!source.isWithinDistInMap(obj, 10)))) {
            ++node;
            obj = getBgMap().getGameObject(BgObjects[node * 8 + ABObjectTypes.AuraContested]);
        }

        if (node == ABBattlegroundNodes.DYNAMICNODESCOUNT) {
            // this means our player isn't close to any of banners - maybe cheater ??
            return;
        }

        var teamIndex = getTeamIndexByTeamId(getPlayerTeam(source.getGUID()));

        // Check if player really could use this banner, not cheated
        if (!(m_Nodes[node] == 0 || teamIndex == (int) m_Nodes[node] % 2)) {
            return;
        }

        source.removeAurasWithInterruptFlags(SpellAuraInterruptFlags.PvPActive);
        int sound;

        // If node is neutral, change to contested
        if (m_Nodes[node] == ABNodeStatus.Neutral) {
            updatePlayerScore(source, ScoreType.basesAssaulted, 1);
            m_prevNodes[node] = m_Nodes[node];
            m_Nodes[node] = ABNodeStatus.forValue(teamIndex + 1);
            // burn current neutral banner
            _DelBanner(node, ABNodeStatus.Neutral, (byte) 0);
            // create new contested banner
            _CreateBanner(node, ABNodeStatus.Contested, (byte) teamIndex, true);
            _SendNodeUpdate(node);
            m_NodeTimers[node] = FLAGCAPTURINGTIME;

            // FIXME: team and node names not localized
            if (teamIndex == TeamId.ALLIANCE) {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceClaims, ChatMsg.BgSystemAlliance, source);
            } else {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeClaims, ChatMsg.BgSystemHorde, source);
            }

            sound = SOUNDCLAIMED;
        }
        // If node is contested
        else if ((m_Nodes[node] == ABNodeStatus.AllyContested) || (m_Nodes[node] == ABNodeStatus.HordeContested)) {
            // If last state is NOT occupied, change node to enemy-contested
            if (m_prevNodes[node].getValue() < ABNodeStatus.Occupied.getValue()) {
                updatePlayerScore(source, ScoreType.basesAssaulted, 1);
                m_prevNodes[node] = m_Nodes[node];
                m_Nodes[node] = (ABNodeStatus.Contested + teamIndex);
                // burn current contested banner
                _DelBanner(node, ABNodeStatus.Contested, (byte) teamIndex);
                // create new contested banner
                _CreateBanner(node, ABNodeStatus.Contested, (byte) teamIndex, true);
                _SendNodeUpdate(node);
                m_NodeTimers[node] = FLAGCAPTURINGTIME;

                if (teamIndex == TeamId.ALLIANCE) {
                    sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceAssaulted, ChatMsg.BgSystemAlliance, source);
                } else {
                    sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeAssaulted, ChatMsg.BgSystemHorde, source);
                }
            }
            // If contested, change back to occupied
            else {
                updatePlayerScore(source, ScoreType.basesDefended, 1);
                m_prevNodes[node] = m_Nodes[node];
                m_Nodes[node] = (ABNodeStatus.Occupied + teamIndex);
                // burn current contested banner
                _DelBanner(node, ABNodeStatus.Contested, (byte) teamIndex);
                // create new occupied banner
                _CreateBanner(node, ABNodeStatus.Occupied, (byte) teamIndex, true);
                _SendNodeUpdate(node);
                m_NodeTimers[node] = 0;
                _NodeOccupied(node, (teamIndex == TeamId.ALLIANCE) ? Team.ALLIANCE : Team.Horde);

                if (teamIndex == TeamId.ALLIANCE) {
                    sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceDefended, ChatMsg.BgSystemAlliance, source);
                } else {
                    sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeDefended, ChatMsg.BgSystemHorde, source);
                }
            }

            sound = (teamIndex == TeamId.ALLIANCE) ? SoundAssaultedAlliance : soundAssaultedHorde;
        }
        // If node is occupied, change to enemy-contested
        else {
            updatePlayerScore(source, ScoreType.basesAssaulted, 1);
            m_prevNodes[node] = m_Nodes[node];
            m_Nodes[node] = (ABNodeStatus.Contested + teamIndex);
            // burn current occupied banner
            _DelBanner(node, ABNodeStatus.Occupied, (byte) teamIndex);
            // create new contested banner
            _CreateBanner(node, ABNodeStatus.Contested, (byte) teamIndex, true);
            _SendNodeUpdate(node);
            _NodeDeOccupied(node);
            m_NodeTimers[node] = FLAGCAPTURINGTIME;

            if (teamIndex == TeamId.ALLIANCE) {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceAssaulted, ChatMsg.BgSystemAlliance, source);
            } else {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeAssaulted, ChatMsg.BgSystemHorde, source);
            }

            sound = (teamIndex == TeamId.ALLIANCE) ? SoundAssaultedAlliance : soundAssaultedHorde;
        }

        // If node is occupied again, send "X has taken the Y" msg.
        if (m_Nodes[node].getValue() >= ABNodeStatus.Occupied.getValue()) {
            // FIXME: team and node names not localized
            if (teamIndex == TeamId.ALLIANCE) {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textAllianceTaken, ChatMsg.BgSystemAlliance);
            } else {
                sendBroadcastText(ABBattlegroundBroadcastTexts.ABNodes[node].textHordeTaken, ChatMsg.BgSystemHorde);
            }
        }

        playSoundToAll(sound);
    }

    @Override
    public Team getPrematureWinner() {
        // How many bases each team owns
        byte ally = 0, horde = 0;

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            if (m_Nodes[i] == ABNodeStatus.AllyOccupied) {
                ++ally;
            } else if (m_Nodes[i] == ABNodeStatus.HordeOccupied) {
                ++horde;
            }
        }

        if (ally > horde) {
            return Team.ALLIANCE;
        } else if (horde > ally) {
            return Team.Horde;
        }

        // If the values are equal, fall back to the original result (based on number of players on each team)
        return super.getPrematureWinner();
    }

    @Override
    public boolean setupBattleground() {
        var result = true;

        for (var i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            result &= addObject(ABObjectTypes.BANNERNEUTRAL + 8 * i, (int) (NodeObjectId.banner0 + i), NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.BANNERCONTA + 8 * i, ABObjectIds.BANNERCONTA, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.BANNERCONTH + 8 * i, ABObjectIds.BANNERCONTH, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.BANNERALLY + 8 * i, ABObjectIds.bannerA, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.BANNERHORDE + 8 * i, ABObjectIds.bannerH, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.AURAALLY + 8 * i, ABObjectIds.auraA, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.AURAHORDE + 8 * i, ABObjectIds.auraH, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.AURACONTESTED + 8 * i, ABObjectIds.auraC, NodePositions[i], 0, 0, (float) Math.sin(NodePositions[i].getO() / 2), (float) Math.cos(NodePositions[i].getO() / 2), BattlegroundConst.RespawnOneDay);

            if (!result) {
                Logs.SQL.error("BatteGroundAB: Failed to spawn some object Battleground not created!");

                return false;
            }
        }

        result &= addObject(ABObjectTypes.GATEA, ABObjectIds.GATEA, DoorPositions[0][0], DoorPositions[0][1], DoorPositions[0][2], DoorPositions[0][3], DoorPositions[0][4], DoorPositions[0][5], DoorPositions[0][6], DoorPositions[0][7], BattlegroundConst.RespawnImmediately);
        result &= addObject(ABObjectTypes.GATEH, ABObjectIds.GATEH, DoorPositions[1][0], DoorPositions[1][1], DoorPositions[1][2], DoorPositions[1][3], DoorPositions[1][4], DoorPositions[1][5], DoorPositions[1][6], DoorPositions[1][7], BattlegroundConst.RespawnImmediately);

        if (!result) {
            Logs.SQL.error("BatteGroundAB: Failed to spawn door object Battleground not created!");

            return false;
        }

        //buffs
        for (var i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            result &= addObject(ABObjectTypes.SPEEDBUFFSTABLES + 3 * i, Buff_Entries[0], BuffPositions[i][0], BuffPositions[i][1], BuffPositions[i][2], BuffPositions[i][3], 0, 0, (float) Math.sin(BuffPositions[i][3] / 2), (float) Math.cos(BuffPositions[i][3] / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.SPEEDBUFFSTABLES + 3 * i + 1, Buff_Entries[1], BuffPositions[i][0], BuffPositions[i][1], BuffPositions[i][2], BuffPositions[i][3], 0, 0, (float) Math.sin(BuffPositions[i][3] / 2), (float) Math.cos(BuffPositions[i][3] / 2), BattlegroundConst.RespawnOneDay);
            result &= addObject(ABObjectTypes.SPEEDBUFFSTABLES + 3 * i + 2, Buff_Entries[2], BuffPositions[i][0], BuffPositions[i][1], BuffPositions[i][2], BuffPositions[i][3], 0, 0, (float) Math.sin(BuffPositions[i][3] / 2), (float) Math.cos(BuffPositions[i][3] / 2), BattlegroundConst.RespawnOneDay);

            if (!result) {
                Logs.SQL.error("BatteGroundAB: Failed to spawn buff object!");

                return false;
            }
        }

        updateWorldState(ABWorldStates.RESOURCESMAX, MAXTEAMSCORE);
        updateWorldState(ABWorldStates.RESOURCESWARNING, WARNINGNEARVICTORYSCORE);

        return true;
    }

    @Override
    public void reset() {
        //call parent's class reset
        super.reset();

        for (var i = 0; i < SharedConst.PvpTeamsCount; ++i) {
            m_TeamScores[i] = 0;
            m_lastTick[i] = 0;
            m_HonorScoreTics[i] = 0;
            m_ReputationScoreTics[i] = 0;
        }

        m_IsInformedNearVictory = false;
        var isBGWeekend = global.getBattlegroundMgr().isBGWeekend(getTypeID());
        m_HonorTics = (isBGWeekend) ? ABBGWeekendHonorTicks : notABBGWeekendHonorTicks;
        m_ReputationTics = (isBGWeekend) ? ABBGWeekendReputationTicks : notABBGWeekendReputationTicks;

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            m_Nodes[i] = ABNodeStatus.forValue(0);
            m_prevNodes[i] = ABNodeStatus.forValue(0);
            m_NodeTimers[i] = 0;
            m_BannerTimers[i].timer = 0;
        }
    }

    @Override
    public void endBattleground(Team winner) {
        // Win reward
        if (winner == Team.ALLIANCE) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.ALLIANCE);
        }

        if (winner == Team.Horde) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.Horde);
        }

        // Complete map_end rewards (even if no team wins)
        rewardHonorToTeam(getBonusHonorFromKill(1), Team.Horde);
        rewardHonorToTeam(getBonusHonorFromKill(1), Team.ALLIANCE);

        super.endBattleground(winner);
    }

    @Override
    public WorldSafeLocsEntry getClosestGraveYard(Player player) {
        var teamIndex = getTeamIndexByTeamId(getPlayerTeam(player.getGUID()));

        // Is there any occupied node for this team?
        ArrayList<Byte> nodes = new ArrayList<>();

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            if (m_Nodes[i] == ABNodeStatus.Occupied + teamIndex) {
                nodes.add(i);
            }
        }

        WorldSafeLocsEntry good_entry = null;

        // If so, select the closest node to place ghost on
        if (!nodes.isEmpty()) {
            var plr_x = player.getLocation().getX();
            var plr_y = player.getLocation().getY();

            var mindist = 999999.0f;

            for (byte i = 0; i < nodes.size(); ++i) {
                var entry = global.getObjectMgr().getWorldSafeLoc(GraveyardIds[nodes.get(i)]);

                if (entry == null) {
                    continue;
                }

                var dist = (entry.loc.getX() - plr_x) * (entry.loc.getX() - plr_x) + (entry.loc.getY() - plr_y) * (entry.loc.getY() - plr_y);

                if (mindist > dist) {
                    mindist = dist;
                    good_entry = entry;
                }
            }

            nodes.clear();
        }

        // If not, place ghost on starting location
        if (good_entry == null) {
            good_entry = global.getObjectMgr().getWorldSafeLoc(GraveyardIds[teamIndex + 5]);
        }

        return good_entry;
    }

    @Override
    public WorldSafeLocsEntry getExploitTeleportLocation(Team team) {
        return global.getObjectMgr().getWorldSafeLoc(team == Team.ALLIANCE ? ExploitTeleportLocationAlliance : exploitTeleportLocationHorde);
    }


    @Override
    public boolean updatePlayerScore(Player player, ScoreType type, int value) {
        return updatePlayerScore(player, type, value, true);
    }

    @Override
    public boolean updatePlayerScore(Player player, ScoreType type, int value, boolean doAddHonor) {
        if (!super.updatePlayerScore(player, type, value, doAddHonor)) {
            return false;
        }

        switch (type) {
            case BasesAssaulted:
                player.updateCriteria(CriteriaType.TrackedWorldStateUIModified, (int) ABObjectives.AssaultBase.getValue());

                break;
            case BasesDefended:
                player.updateCriteria(CriteriaType.TrackedWorldStateUIModified, (int) ABObjectives.DefendBase.getValue());

                break;
            default:
                break;
        }

        return true;
    }

    private void _CreateBanner(byte node, ABNodeStatus type, int teamIndex, boolean delay) {
        // Just put it into the queue
        if (delay) {
            m_BannerTimers[node].timer = 2000;
            m_BannerTimers[node].type = (byte) type.getValue();
            m_BannerTimers[node].teamIndex = (byte) teamIndex;

            return;
        }

        var obj = node * 8 + (byte) type.getValue() + teamIndex;

        spawnBGObject(obj, BattlegroundConst.RespawnImmediately);

        // handle aura with banner
        if (type == 0) {
            return;
        }

        obj = node * 8 + ((type == ABNodeStatus.Occupied) ? (5 + teamIndex) : 7);
        spawnBGObject(obj, BattlegroundConst.RespawnImmediately);
    }

    private void _DelBanner(byte node, ABNodeStatus type, byte teamIndex) {
        var obj = node * 8 + (byte) type.getValue() + teamIndex;
        spawnBGObject(obj, BattlegroundConst.RespawnOneDay);

        // handle aura with banner
        if (type == 0) {
            return;
        }

        obj = node * 8 + ((type == ABNodeStatus.Occupied) ? (5 + teamIndex) : 7);
        spawnBGObject(obj, BattlegroundConst.RespawnOneDay);
    }

    private void _SendNodeUpdate(byte node) {
        // Send node owner state update to refresh map icons on client
        int[] idPlusArray = {0, 2, 3, 0, 1};

        int[] statePlusArray = {0, 2, 0, 2, 0};

        if (m_prevNodes[node] != 0) {
            updateWorldState(NodeStates[node] + idPlusArray[(int) m_prevNodes[node]], 0);
        } else {
            updateWorldState(NodeIcons[node], 0);
        }

        updateWorldState(NodeStates[node] + idPlusArray[(byte) m_Nodes[node]], 1);

        switch (node) {
            case ABBattlegroundNodes.NodeStables:
                updateWorldState(ABWorldStates.STABLESICONNEW, (int) m_Nodes[node] + statePlusArray[(int) m_Nodes[node]]);
                updateWorldState(ABWorldStates.STABLESHORDECONTROLSTATE, m_Nodes[node] == ABNodeStatus.HordeOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.HordeContested ? 1 : 0));
                updateWorldState(ABWorldStates.STABLESALLIANCECONTROLSTATE, m_Nodes[node] == ABNodeStatus.AllyOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.AllyContested ? 1 : 0));

                break;
            case ABBattlegroundNodes.NodeBlacksmith:
                updateWorldState(ABWorldStates.BLACKSMITHICONNEW, (int) m_Nodes[node] + statePlusArray[(int) m_Nodes[node]]);
                updateWorldState(ABWorldStates.BLACKSMITHHORDECONTROLSTATE, m_Nodes[node] == ABNodeStatus.HordeOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.HordeContested ? 1 : 0));
                updateWorldState(ABWorldStates.BLACKSMITHALLIANCECONTROLSTATE, m_Nodes[node] == ABNodeStatus.AllyOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.AllyContested ? 1 : 0));

                break;
            case ABBattlegroundNodes.NodeFarm:
                updateWorldState(ABWorldStates.FARMICONNEW, (int) m_Nodes[node] + statePlusArray[(int) m_Nodes[node]]);
                updateWorldState(ABWorldStates.FARMHORDECONTROLSTATE, m_Nodes[node] == ABNodeStatus.HordeOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.HordeContested ? 1 : 0));
                updateWorldState(ABWorldStates.FARMALLIANCECONTROLSTATE, m_Nodes[node] == ABNodeStatus.AllyOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.AllyContested ? 1 : 0));

                break;
            case ABBattlegroundNodes.NodeLumberMill:
                updateWorldState(ABWorldStates.LUMBERMILLICONNEW, (int) m_Nodes[node] + statePlusArray[(int) m_Nodes[node]]);
                updateWorldState(ABWorldStates.LUMBERMILLHORDECONTROLSTATE, m_Nodes[node] == ABNodeStatus.HordeOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.HordeContested ? 1 : 0));
                updateWorldState(ABWorldStates.LUMBERMILLALLIANCECONTROLSTATE, m_Nodes[node] == ABNodeStatus.AllyOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.AllyContested ? 1 : 0));

                break;
            case ABBattlegroundNodes.NodeGoldMine:
                updateWorldState(ABWorldStates.GOLDMINEICONNEW, (int) m_Nodes[node] + statePlusArray[(int) m_Nodes[node]]);
                updateWorldState(ABWorldStates.GOLDMINEHORDECONTROLSTATE, m_Nodes[node] == ABNodeStatus.HordeOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.HordeContested ? 1 : 0));
                updateWorldState(ABWorldStates.GOLDMINEALLIANCECONTROLSTATE, m_Nodes[node] == ABNodeStatus.AllyOccupied ? 2 : (m_Nodes[node] == ABNodeStatus.AllyContested ? 1 : 0));

                break;
            default:
                break;
        }

        // How many bases each team owns
        byte ally = 0, horde = 0;

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            if (m_Nodes[i] == ABNodeStatus.AllyOccupied) {
                ++ally;
            } else if (m_Nodes[i] == ABNodeStatus.HordeOccupied) {
                ++horde;
            }
        }

        updateWorldState(ABWorldStates.OCCUPIEDBASESALLY, ally);
        updateWorldState(ABWorldStates.OCCUPIEDBASESHORDE, horde);
    }

    private void _NodeOccupied(byte node, Team team) {
        if (!addSpiritGuide(node, SpiritGuidePos[node], getTeamIndexByTeamId(team))) {
            Log.outError(LogFilter.Battleground, "Failed to spawn spirit guide! point: {0}, team: {1}, ", node, team);
        }

        if (node >= ABBattlegroundNodes.DYNAMICNODESCOUNT) //only dynamic nodes, no start points
        {
            return;
        }

        byte capturedNodes = 0;

        for (byte i = 0; i < ABBattlegroundNodes.DYNAMICNODESCOUNT; ++i) {
            if (m_Nodes[i] == ABNodeStatus.Occupied + getTeamIndexByTeamId(team) && m_NodeTimers[i] == 0) {
                ++capturedNodes;
            }
        }

        if (capturedNodes >= 5) {
            castSpellOnTeam(BattlegroundConst.AbQuestReward5Bases, team);
        }

        if (capturedNodes >= 4) {
            castSpellOnTeam(BattlegroundConst.AbQuestReward4Bases, team);
        }

        var trigger = !BgCreatures[node + 7].isEmpty() ? getBGCreature(node + 7) : null; // 0-6 spirit guides

        if (!trigger) {
            trigger = addCreature(SharedConst.worldTrigger, node + 7, NodePositions[node], getTeamIndexByTeamId(team));
        }

        //add bonus honor aura trigger creature when node is accupied
        //cast bonus aura (+50% honor in 25yards)
        //aura should only apply to players who have accupied the node, set correct faction for trigger
        if (trigger) {
            trigger.faction = team == Team.ALLIANCE ? 84 : 83;
            trigger.castSpell(trigger, BattlegroundConst.SpellHonorableDefender25y, false);
        }
    }

    private void _NodeDeOccupied(byte node) {
        //only dynamic nodes, no start points
        if (node >= ABBattlegroundNodes.DYNAMICNODESCOUNT) {
            return;
        }

        //remove bonus honor aura trigger creature when node is lost
        delCreature(node + 7); //null checks are in DelCreature! 0-6 spirit guides

        relocateDeadPlayers(BgCreatures[node]);

        delCreature(node);

        // buff object isn't despawned
    }
}
