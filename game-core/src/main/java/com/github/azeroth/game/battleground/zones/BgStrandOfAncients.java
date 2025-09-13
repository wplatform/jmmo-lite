package com.github.azeroth.game.battleground.zones;


import com.github.azeroth.game.WorldSafeLocsEntry;
import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.UpdateObject;

import java.util.HashMap;


public class BgStrandOfAncients extends Battleground {
    // Status of each gate (Destroy/Damage/Intact)
    private final SAGateState[] gateStatus = new SAGateState[SAMiscConst.GATES.length];

    // Team witch conntrol each graveyard
    private final int[] graveyardStatus = new int[SAGraveyards.Max];

    // Score of each round
    private final SARoundScore[] roundScores = new SARoundScore[2];
    private final HashMap<Integer, Integer> demoliserRespawnList = new HashMap<Integer, Integer>();

    /**
     * Id of attacker team
     */
    private int attackers;

    // Totale elapsed time of current round
    private int totalTime;

    // Max time of round
    private int endRoundTimer;

    // For know if boats has start moving or not yet
    private boolean shipsStarted;

    // Statu of battle (Start or not, and what round)
    private SAstatus status = SAStatus.values()[0];

    // used for know we are in timer phase or not (used for worldstate update)
    private boolean timerEnabled;

    // 5secs before starting the 1min countdown for second round
    private int updateWaitTimer;

    // for know if warning about second round start has been sent
    private boolean signaledRoundTwo;

    // for know if warning about second round start has been sent
    private boolean signaledRoundTwoHalfMin;

    // for know if second round has been init
    private boolean initSecondRound;

    public BgStrandOfAncients(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        StartMessageIds[BattlegroundConst.EventIdFourth] = 0;

        bgObjects = new ObjectGuid[SAObjectTypes.MaxObj];
        bgCreatures = new ObjectGuid[SACreatureTypes.max + SAGraveyards.Max];
        timerEnabled = false;
        updateWaitTimer = 0;
        signaledRoundTwo = false;
        signaledRoundTwoHalfMin = false;
        initSecondRound = false;
        attackers = TeamId.ALLIANCE;
        totalTime = 0;
        endRoundTimer = 0;
        shipsStarted = false;
        status = SAStatus.NotStarted;

        for (byte i = 0; i < gateStatus.length; ++i) {
            GateStatus[i] = SAGateState.HordeGateOk;
        }

        for (byte i = 0; i < 2; i++) {
            RoundScores[i].winner = TeamId.ALLIANCE;
            RoundScores[i].time = 0;
        }
    }

    @Override
    public void reset() {
        totalTime = 0;
        attackers = (RandomUtil.URand(0, 1) != 0 ? TeamId.ALLIANCE : TeamId.HORDE);

        for (byte i = 0; i <= 5; i++) {
            GateStatus[i] = SAGateState.HordeGateOk;
        }

        shipsStarted = false;
        status = SAStatus.Warmup;
    }

    @Override
    public boolean setupBattleground() {
        return resetObjs();
    }

    @Override
    public void postUpdateImpl(int diff) {
        if (initSecondRound) {
            if (updateWaitTimer < diff) {
                if (!signaledRoundTwo) {
                    signaledRoundTwo = true;
                    initSecondRound = false;
                    sendBroadcastText(SABroadcastTexts.roundTwoStartOneMinute, ChatMsg.BgSystemNeutral);
                }
            } else {
                UpdateWaitTimer -= diff;

                return;
            }
        }

        totalTime += diff;

        if (status == SAStatus.Warmup) {
            endRoundTimer = SATimers.roundLength;
            updateWorldState(SAWorldStateIds.timer, (int) (gameTime.GetGameTime() + endRoundTimer));

            if (totalTime >= SATimers.warmupLength) {
                var c = getBGCreature(SACreatureTypes.KANRETHAD);

                if (c) {
                    sendChatMessage(c, SATextIds.roundStarted);
                }

                totalTime = 0;
                toggleTimer();
                demolisherStartState(false);
                status = SAStatus.RoundOne;
                triggerGameEvent(attackers == TeamId.ALLIANCE ? 23748 : 21702);
            }

            if (totalTime >= SATimers.boatStart) {
                startShips();
            }

            return;
        } else if (status == SAStatus.SecondWarmup) {
            if (RoundScores[0].time < SATimers.roundLength) {
                endRoundTimer = RoundScores[0].time;
            } else {
                endRoundTimer = SATimers.roundLength;
            }

            updateWorldState(SAWorldStateIds.timer, (int) (gameTime.GetGameTime() + endRoundTimer));

            if (totalTime >= 60000) {
                var c = getBGCreature(SACreatureTypes.KANRETHAD);

                if (c) {
                    sendChatMessage(c, SATextIds.roundStarted);
                }

                totalTime = 0;
                toggleTimer();
                demolisherStartState(false);
                status = SAStatus.RoundTwo;
                triggerGameEvent(attackers == TeamId.ALLIANCE ? 23748 : 21702);
                // status was set to STATUS_WAIT_JOIN manually for preparation, set it back now
                setStatus(BattlegroundStatus.inProgress);

                for (var pair : getPlayers().entrySet()) {
                    var p = global.getObjAccessor().findPlayer(pair.getKey());

                    if (p) {
                        p.removeAura(BattlegroundConst.SpellPreparation);
                    }
                }
            }

            if (totalTime >= 30000) {
                if (!signaledRoundTwoHalfMin) {
                    signaledRoundTwoHalfMin = true;
                    sendBroadcastText(SABroadcastTexts.roundTwoStartHalfMinute, ChatMsg.BgSystemNeutral);
                }
            }

            startShips();

            return;
        } else if (getStatus() == BattlegroundStatus.inProgress) {
            if (status == SAStatus.RoundOne) {
                if (totalTime >= SATimers.roundLength) {
                    castSpellOnTeam(SASpellIds.endOfRound, Team.ALLIANCE);
                    castSpellOnTeam(SASpellIds.endOfRound, Team.Horde);
                    RoundScores[0].winner = (int) attackers;
                    RoundScores[0].time = SATimers.roundLength;
                    totalTime = 0;
                    status = SAStatus.SecondWarmup;
                    attackers = (attackers == TeamId.ALLIANCE) ? TeamId.HORDE : TeamId.ALLIANCE;
                    updateWaitTimer = 5000;
                    signaledRoundTwo = false;
                    signaledRoundTwoHalfMin = false;
                    initSecondRound = true;
                    toggleTimer();
                    resetObjs();
                    getBgMap().updateAreaDependentAuras();

                    return;
                }
            } else if (status == SAStatus.RoundTwo) {
                if (totalTime >= endRoundTimer) {
                    castSpellOnTeam(SASpellIds.endOfRound, Team.ALLIANCE);
                    castSpellOnTeam(SASpellIds.endOfRound, Team.Horde);
                    RoundScores[1].time = SATimers.roundLength;
                    RoundScores[1].winner = (int) ((attackers == TeamId.ALLIANCE) ? TeamId.HORDE : TeamId.ALLIANCE);

                    if (RoundScores[0].time == RoundScores[1].time) {
                        endBattleground(0);
                    } else if (RoundScores[0].time < RoundScores[1].time) {
                        endBattleground(RoundScores[0].winner == TeamId.ALLIANCE ? Team.ALLIANCE : Team.Horde);
                    } else {
                        endBattleground(RoundScores[1].winner == TeamId.ALLIANCE ? Team.ALLIANCE : Team.Horde);
                    }

                    return;
                }
            }

            if (status == SAStatus.RoundOne || status == SAStatus.RoundTwo) {
                updateDemolisherSpawns();
            }
        }
    }

    @Override
    public void addPlayer(Player player) {
        var isInBattleground = isPlayerInBattleground(player.getGUID());
        super.addPlayer(player);

        if (!isInBattleground) {
            playerScores.put(player.getGUID(), new BattlegroundSAScore(player.getGUID(), player.getBgTeam()));
        }

        sendTransportInit(player);

        if (!isInBattleground) {
            teleportToEntrancePosition(player);
        }
    }

    @Override
    public void removePlayer(Player player, ObjectGuid guid, Team team) {
    }

    @Override
    public void handleAreaTrigger(Player source, int trigger, boolean entered) {
        // this is wrong way to implement these things. On official it done by gameobject spell cast.
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }
    }


    @Override
    public void processEvent(WorldObject obj, int eventId) {
        processEvent(obj, eventId, null);
    }

    @Override
    public void processEvent(WorldObject obj, int eventId, WorldObject invoker) {
        var go = obj.toGameObject();

        if (go) {
            switch (go.getGoType()) {
                case Goober:
                    if (invoker) {
                        if (eventId == (int) SAEventIds.BG_SA_EVENT_TITAN_RELIC_ACTIVATED.getValue()) {
                            titanRelicActivated(invoker.toPlayer());
                        }
                    }

                    break;
                case DestructibleBuilding: {
                    var gate = getGate(obj.getEntry());

                    if (gate != null) {
                        var gateId = gate.gateId;

                        // damaged
                        if (eventId == go.getTemplate().destructibleBuilding.damagedEvent) {
                            GateStatus[gateId] = attackers == TeamId.HORDE ? SAGateState.AllianceGateDamaged : SAGateState.HordeGateDamaged;

                            var c = obj.findNearestCreature(SharedConst.worldTrigger, 500.0f);

                            if (c) {
                                sendChatMessage(c, (byte) gate.damagedText, invoker);
                            }

                            playSoundToAll(attackers == TeamId.ALLIANCE ? SASoundIds.WallAttackedAlliance : SASoundIds.wallAttackedHorde);
                        }
                        // destroyed
                        else if (eventId == go.getTemplate().destructibleBuilding.destroyedEvent) {
                            GateStatus[gate.GateId] = attackers == TeamId.HORDE ? SAGateState.AllianceGateDestroyed : SAGateState.HordeGateDestroyed;

                            if (gateId < 5) {
                                delObject((int) gateId + 14);
                            }

                            var c = obj.findNearestCreature(SharedConst.worldTrigger, 500.0f);

                            if (c) {
                                sendChatMessage(c, (byte) gate.destroyedText, invoker);
                            }

                            playSoundToAll(attackers == TeamId.ALLIANCE ? SASoundIds.WallDestroyedAlliance : SASoundIds.wallDestroyedHorde);

                            var rewardHonor = true;

                            switch (gateId) {
                                case SAObjectTypes.GreenGate:
                                    if (isGateDestroyed(SAObjectTypes.blueGate)) {
                                        rewardHonor = false;
                                    }

                                    break;
                                case SAObjectTypes.BlueGate:
                                    if (isGateDestroyed(SAObjectTypes.greenGate)) {
                                        rewardHonor = false;
                                    }

                                    break;
                                case SAObjectTypes.RedGate:
                                    if (isGateDestroyed(SAObjectTypes.purpleGate)) {
                                        rewardHonor = false;
                                    }

                                    break;
                                case SAObjectTypes.PurpleGate:
                                    if (isGateDestroyed(SAObjectTypes.redGate)) {
                                        rewardHonor = false;
                                    }

                                    break;
                                default:
                                    break;
                            }

                            if (invoker) {
                                var unit = invoker.toUnit();

                                if (unit) {
                                    var player = unit.getCharmerOrOwnerPlayerOrPlayerItself();

                                    if (player) {
                                        updatePlayerScore(player, ScoreType.DestroyedWall, 1);

                                        if (rewardHonor) {
                                            updatePlayerScore(player, ScoreType.bonusHonor, getBonusHonorFromKill(1));
                                        }
                                    }
                                }
                            }

                            updateObjectInteractionFlags();
                        } else {
                            break;
                        }

                        updateWorldState(gate.worldState, (int) GateStatus[gateId]);
                    }

                    break;
                }
                default:
                    break;
            }
        }
    }

    @Override
    public void handleKillUnit(Creature creature, Player killer) {
        if (creature.getEntry() == SACreatureIds.demolisher) {
            updatePlayerScore(killer, ScoreType.DestroyedDemolisher, 1);
            var worldStateId = attackers == TeamId.HORDE ? SAWorldStateIds.DestroyedHordeVehicles : SAWorldStateIds.destroyedAllianceVehicles;
            var currentDestroyedVehicles = global.getWorldStateMgr().getValue((int) worldStateId, getBgMap());
            updateWorldState(worldStateId, currentDestroyedVehicles + 1);
        }
    }

    @Override
    public void destroyGate(Player player, GameObject go) {
    }

    @Override
    public WorldSafeLocsEntry getClosestGraveYard(Player player) {
        int safeloc;

        var teamId = getTeamIndexByTeamId(getPlayerTeam(player.getGUID()));

        if (teamId == attackers) {
            safeloc = SAMiscConst.GYEntries[SAGraveyards.BeachGy];
        } else {
            safeloc = SAMiscConst.GYEntries[SAGraveyards.DefenderLastGy];
        }

        var closest = global.getObjectMgr().getWorldSafeLoc(safeloc);
        var nearest = player.getLocation().getExactDistSq(closest.loc);

        for (byte i = (byte) SAGraveyards.RIGHTCAPTURABLEGY; i < SAGraveyards.max; i++) {
            if (GraveyardStatus[i] != teamId) {
                continue;
            }

            var ret = global.getObjectMgr().getWorldSafeLoc(SAMiscConst.GYEntries[i]);
            var dist = player.getLocation().getExactDistSq(ret.loc);

            if (dist < nearest) {
                closest = ret;
                nearest = dist;
            }
        }

        return closest;
    }

    @Override
    public void eventPlayerClickedOnFlag(Player source, GameObject go) {
        switch (go.getEntry()) {
            case 191307:
            case 191308:
                if (canInteractWithObject(SAObjectTypes.LEFTFLAG)) {
                    captureGraveyard(SAGraveyards.LEFTCAPTURABLEGY, source);
                }

                break;
            case 191305:
            case 191306:
                if (canInteractWithObject(SAObjectTypes.RIGHTFLAG)) {
                    captureGraveyard(SAGraveyards.RIGHTCAPTURABLEGY, source);
                }

                break;
            case 191310:
            case 191309:
                if (canInteractWithObject(SAObjectTypes.CENTRALFLAG)) {
                    captureGraveyard(SAGraveyards.CENTRALCAPTURABLEGY, source);
                }

                break;
            default:
                return;
        }
    }

    @Override
    public void endBattleground(Team winner) {
        // honor reward for winning
        if (winner == Team.ALLIANCE) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.ALLIANCE);
        } else if (winner == Team.Horde) {
            rewardHonorToTeam(getBonusHonorFromKill(1), Team.Horde);
        }

        // complete map_end rewards (even if no team wins)
        rewardHonorToTeam(getBonusHonorFromKill(2), Team.ALLIANCE);
        rewardHonorToTeam(getBonusHonorFromKill(2), Team.Horde);

        super.endBattleground(winner);
    }

    @Override
    public boolean isSpellAllowed(int spellId, Player player) {
        switch (spellId) {
            case SASpellIds.AllianceControlPhaseShift:
                return attackers == TeamId.HORDE;
            case SASpellIds.HordeControlPhaseShift:
                return attackers == TeamId.ALLIANCE;
            case BattlegroundConst.SpellPreparation:
                return status == SAStatus.Warmup || status == SAStatus.SecondWarmup;
            default:
                break;
        }

        return true;
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
            case DestroyedDemolisher:
                player.updateCriteria(CriteriaType.TrackedWorldStateUIModified, (int) SAObjectives.demolishersDestroyed.getValue());

                break;
            case DestroyedWall:
                player.updateCriteria(CriteriaType.TrackedWorldStateUIModified, (int) SAObjectives.gatesDestroyed.getValue());

                break;
            default:
                break;
        }

        return true;
    }

    private boolean resetObjs() {
        for (var pair : getPlayers().entrySet()) {
            var player = global.getObjAccessor().findPlayer(pair.getKey());

            if (player) {
                sendTransportsRemove(player);
            }
        }

        var atF = SAMiscConst.Factions[Attackers];
        var defF = SAMiscConst.Factions[Attackers != 0 ? TeamId.ALLIANCE : TeamId.HORDE];

        for (byte i = 0; i < SAObjectTypes.MAXOBJ; i++) {
            delObject(i);
        }

        for (byte i = 0; i < SACreatureTypes.max; i++) {
            delCreature(i);
        }

        for (byte i = (byte) SACreatureTypes.max; i < SACreatureTypes.max + SAGraveyards.max; i++) {
            delCreature(i);
        }

        for (byte i = 0; i < gateStatus.length; ++i) {
            GateStatus[i] = attackers == TeamId.HORDE ? SAGateState.AllianceGateOk : SAGateState.HordeGateOk;
        }

        if (!addCreature(SAMiscConst.NpcEntries[SACreatureTypes.Kanrethad], SACreatureTypes.KANRETHAD, SAMiscConst.NpcSpawnlocs[SACreatureTypes.Kanrethad])) {
            Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn KANRETHAD, aborted. Entry: %1$s", SAMiscConst.NpcEntries[SACreatureTypes.Kanrethad]));

            return false;
        }

        for (byte i = 0; i <= SAObjectTypes.PORTALDEFFENDERRED; i++) {
            if (!addObject(i, SAMiscConst.ObjEntries[i], SAMiscConst.ObjSpawnlocs[i], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn BG_SA_PORTAL_DEFFENDER_RED, Entry: %1$s", SAMiscConst.ObjEntries[i]));

                continue;
            }
        }

        for (var i = SAObjectTypes.BOATONE; i <= SAObjectTypes.BOATTWO; i++) {
            int boatid = 0;

            switch (i) {
                case SAObjectTypes.BoatOne:
                    boatid = attackers != 0 ? SAGameObjectIds.BoatOneH : SAGameObjectIds.boatOneA;

                    break;
                case SAObjectTypes.BoatTwo:
                    boatid = attackers != 0 ? SAGameObjectIds.BoatTwoH : SAGameObjectIds.boatTwoA;

                    break;
                default:
                    break;
            }

            if (!addObject(i, boatid, SAMiscConst.ObjSpawnlocs[i].getX(), SAMiscConst.ObjSpawnlocs[i].getY(), SAMiscConst.ObjSpawnlocs[i].getZ() + (attackers != 0 ? -3.750f : 0), SAMiscConst.ObjSpawnlocs[i].getO(), 0, 0, 0, 0, BattlegroundConst.RespawnOneDay)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn one of the BG_SA_BOAT, Entry: %1$s", boatid));

                continue;
            }
        }

        for (byte i = (byte) SAObjectTypes.SIGIL1; i <= SAObjectTypes.LEFTFLAGPOLE; i++) {
            if (!addObject(i, SAMiscConst.ObjEntries[i], SAMiscConst.ObjSpawnlocs[i], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn Sigil, Entry: %1$s", SAMiscConst.ObjEntries[i]));

                continue;
            }
        }

        // MAD props for Kiper for discovering those values - 4 hours of his work.
        getBGObject(SAObjectTypes.BOATONE).setParentRotation(new Quaternion(0.0f, 0.0f, 1.0f, 0.0002f));
        getBGObject(SAObjectTypes.BOATTWO).setParentRotation(new Quaternion(0.0f, 0.0f, 1.0f, 0.00001f));
        spawnBGObject(SAObjectTypes.BOATONE, BattlegroundConst.RespawnImmediately);
        spawnBGObject(SAObjectTypes.BOATTWO, BattlegroundConst.RespawnImmediately);

        //Cannons and demolishers - NPCs are spawned
        //By capturing GYs.
        for (byte i = 0; i < SACreatureTypes.DEMOLISHER5; i++) {
            if (!addCreature(SAMiscConst.NpcEntries[i], i, SAMiscConst.NpcSpawnlocs[i], attackers == TeamId.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE, 600)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn Cannon or demolisher, Entry: %1$s, Attackers: %2$s", SAMiscConst.NpcEntries[i], (attackers == TeamId.ALLIANCE ? "Horde(1)" : "Alliance(0)")));

                continue;
            }
        }

        overrideGunFaction();
        demolisherStartState(true);

        for (byte i = 0; i <= SAObjectTypes.PORTALDEFFENDERRED; i++) {
            spawnBGObject(i, BattlegroundConst.RespawnImmediately);
            getBGObject(i).setFaction(defF);
        }

        getBGObject(SAObjectTypes.TITANRELIC).setFaction(atF);
        getBGObject(SAObjectTypes.TITANRELIC).refresh();

        totalTime = 0;
        shipsStarted = false;

        //Graveyards
        for (byte i = 0; i < SAGraveyards.max; i++) {
            var sg = global.getObjectMgr().getWorldSafeLoc(SAMiscConst.GYEntries[i]);

            if (sg == null) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: Can't find GY entry %1$s", SAMiscConst.GYEntries[i]));

                return false;
            }

            if (i == SAGraveyards.BEACHGY) {
                GraveyardStatus[i] = attackers;
                addSpiritGuide(i + SACreatureTypes.max, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), SAMiscConst.GYOrientation[i], attackers);
            } else {
                GraveyardStatus[i] = ((attackers == TeamId.HORDE) ? TeamId.ALLIANCE : TeamId.HORDE);

                if (!addSpiritGuide(i + SACreatureTypes.max, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), SAMiscConst.GYOrientation[i], attackers == TeamId.HORDE ? TeamId.ALLIANCE : TeamId.HORDE)) {
                    Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn GY: %1$s", i));
                }
            }
        }

        //GY capture points
        for (byte i = (byte) SAObjectTypes.CENTRALFLAG; i <= SAObjectTypes.LEFTFLAG; i++) {
            if (!addObject(i, (SAMiscConst.ObjEntries[i] - (attackers == TeamId.ALLIANCE ? 1 : 0)), SAMiscConst.ObjSpawnlocs[i], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn Central Flag Entry: %1$s", SAMiscConst.ObjEntries[i] - (attackers == TeamId.ALLIANCE ? 1 : 0)));

                continue;
            }

            getBGObject(i).setFaction(atF);
        }

        updateObjectInteractionFlags();

        for (byte i = (byte) SAObjectTypes.BOMB; i < SAObjectTypes.MAXOBJ; i++) {
            if (!addObject(i, SAMiscConst.ObjEntries[SAObjectTypes.Bomb], SAMiscConst.ObjSpawnlocs[i], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay)) {
                Log.outError(LogFilter.Battleground, String.format("SOTA: couldn't spawn SA Bomb Entry: %1$s", SAMiscConst.ObjEntries[SAObjectTypes.Bomb] + i));

                continue;
            }

            getBGObject(i).setFaction(atF);
        }

        //Player may enter BEFORE we set up BG - lets update his worldstates anyway...
        updateWorldState(SAWorldStateIds.rightGyHorde, GraveyardStatus[SAGraveyards.RightCapturableGy] == TeamId.HORDE ? 1 : 0);
        updateWorldState(SAWorldStateIds.leftGyHorde, GraveyardStatus[SAGraveyards.LeftCapturableGy] == TeamId.HORDE ? 1 : 0);
        updateWorldState(SAWorldStateIds.centerGyHorde, GraveyardStatus[SAGraveyards.CentralCapturableGy] == TeamId.HORDE ? 1 : 0);

        updateWorldState(SAWorldStateIds.rightGyAlliance, GraveyardStatus[SAGraveyards.RightCapturableGy] == TeamId.ALLIANCE ? 1 : 0);
        updateWorldState(SAWorldStateIds.leftGyAlliance, GraveyardStatus[SAGraveyards.LeftCapturableGy] == TeamId.ALLIANCE ? 1 : 0);
        updateWorldState(SAWorldStateIds.centerGyAlliance, GraveyardStatus[SAGraveyards.CentralCapturableGy] == TeamId.ALLIANCE ? 1 : 0);

        if (attackers == TeamId.ALLIANCE) {
            updateWorldState(SAWorldStateIds.allyAttacks, 1);
            updateWorldState(SAWorldStateIds.hordeAttacks, 0);

            updateWorldState(SAWorldStateIds.rightAttTokenAll, 1);
            updateWorldState(SAWorldStateIds.leftAttTokenAll, 1);
            updateWorldState(SAWorldStateIds.rightAttTokenHrd, 0);
            updateWorldState(SAWorldStateIds.leftAttTokenHrd, 0);

            updateWorldState(SAWorldStateIds.hordeDefenceToken, 1);
            updateWorldState(SAWorldStateIds.allianceDefenceToken, 0);
        } else {
            updateWorldState(SAWorldStateIds.hordeAttacks, 1);
            updateWorldState(SAWorldStateIds.allyAttacks, 0);

            updateWorldState(SAWorldStateIds.rightAttTokenAll, 0);
            updateWorldState(SAWorldStateIds.leftAttTokenAll, 0);
            updateWorldState(SAWorldStateIds.rightAttTokenHrd, 1);
            updateWorldState(SAWorldStateIds.leftAttTokenHrd, 1);

            updateWorldState(SAWorldStateIds.hordeDefenceToken, 0);
            updateWorldState(SAWorldStateIds.allianceDefenceToken, 1);
        }

        updateWorldState(SAWorldStateIds.attackerTeam, attackers);
        updateWorldState(SAWorldStateIds.purpleGate, 1);
        updateWorldState(SAWorldStateIds.redGate, 1);
        updateWorldState(SAWorldStateIds.blueGate, 1);
        updateWorldState(SAWorldStateIds.greenGate, 1);
        updateWorldState(SAWorldStateIds.yellowGate, 1);
        updateWorldState(SAWorldStateIds.ancientGate, 1);

        for (var i = SAObjectTypes.BOATONE; i <= SAObjectTypes.BOATTWO; i++) {
            for (var pair : getPlayers().entrySet()) {
                var player = global.getObjAccessor().findPlayer(pair.getKey());

                if (player) {
                    sendTransportInit(player);
                }
            }
        }

        // set status manually so preparation is cast correctly in 2nd round too
        setStatus(BattlegroundStatus.WaitJoin);

        teleportPlayers();

        return true;
    }

    private void startShips() {
        if (shipsStarted) {
            return;
        }

        getBGObject(SAObjectTypes.BOATONE).setGoState(GOState.TransportStopped);
        getBGObject(SAObjectTypes.BOATTWO).setGoState(GOState.TransportStopped);

        for (var i = SAObjectTypes.BOATONE; i <= SAObjectTypes.BOATTWO; i++) {
            for (var pair : getPlayers().entrySet()) {
                var p = global.getObjAccessor().findPlayer(pair.getKey());

                if (p) {
                    UpdateData data = new UpdateData(p.getLocation().getMapId());
                    getBGObject(i).buildValuesUpdateBlockForPlayer(data, p);

                    UpdateObject pkt;
                    tangible.OutObject<UpdateObject> tempOut_pkt = new tangible.OutObject<UpdateObject>();
                    data.buildPacket(tempOut_pkt);
                    pkt = tempOut_pkt.outArgValue;
                    p.sendPacket(pkt);
                }
            }
        }

        shipsStarted = true;
    }

    private void teleportPlayers() {
        for (var pair : getPlayers().entrySet()) {
            var player = global.getObjAccessor().findPlayer(pair.getKey());

            if (player) {
                // should remove spirit of redemption
                if (player.hasAuraType(AuraType.SpiritOfRedemption)) {
                    player.removeAurasByType(AuraType.ModShapeshift);
                }

                if (!player.isAlive()) {
                    player.resurrectPlayer(1.0f);
                    player.spawnCorpseBones();
                }

                player.resetAllPowers();
                player.combatStopWithPets(true);

                player.castSpell(player, BattlegroundConst.SpellPreparation, true);

                teleportToEntrancePosition(player);
            }
        }
    }

    private void teleportToEntrancePosition(Player player) {
        if (getTeamIndexByTeamId(getPlayerTeam(player.getGUID())) == attackers) {
            if (!shipsStarted) {
                // player.addUnitMovementFlag(MOVEMENTFLAG_ONTRANSPORT);

                if (RandomUtil.URand(0, 1) != 0) {
                    player.teleportTo(607, 2682.936f, -830.368f, 15.0f, 2.895f, 0);
                } else {
                    player.teleportTo(607, 2577.003f, 980.261f, 15.0f, 0.807f, 0);
                }
            } else {
                player.teleportTo(607, 1600.381f, -106.263f, 8.8745f, 3.78f, 0);
            }
        } else {
            player.teleportTo(607, 1209.7f, -65.16f, 70.1f, 0.0f, 0);
        }
    }

    /*
    You may ask what the fuck does it do?
    Prevents owner overwriting guns faction with own.
    */
    private void overrideGunFaction() {
        if (BgCreatures[0].isEmpty()) {
            return;
        }

        for (byte i = (byte) SACreatureTypes.GUN1; i <= SACreatureTypes.GUN10; i++) {
            var gun = getBGCreature(i);

            if (gun) {
                gun.setFaction(SAMiscConst.Factions[Attackers != 0 ? TeamId.ALLIANCE : TeamId.HORDE]);
            }
        }

        for (byte i = (byte) SACreatureTypes.DEMOLISHER1; i <= SACreatureTypes.DEMOLISHER4; i++) {
            var dem = getBGCreature(i);

            if (dem) {
                dem.setFaction(SAMiscConst.Factions[Attackers]);
            }
        }
    }

    private void demolisherStartState(boolean start) {
        if (BgCreatures[0].isEmpty()) {
            return;
        }

        // set flags only for the demolishers on the beach, factory ones dont need it
        for (byte i = (byte) SACreatureTypes.DEMOLISHER1; i <= SACreatureTypes.DEMOLISHER4; i++) {
            var dem = getBGCreature(i);

            if (dem) {
                if (start) {
                    dem.setUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue());
                } else {
                    dem.removeUnitFlag(UnitFlag.NonAttackable.getValue() | UnitFlag.Uninteractible.getValue());
                }
            }
        }
    }

    private boolean canInteractWithObject(int objectId) {
        switch (objectId) {
            case SAObjectTypes.TitanRelic:
                if (!isGateDestroyed(SAObjectTypes.ancientGate) || !isGateDestroyed(SAObjectTypes.yellowGate)) {
                    return false;
                }

            case SAObjectTypes.CentralFlag:
                if (!isGateDestroyed(SAObjectTypes.redGate) && !isGateDestroyed(SAObjectTypes.purpleGate)) {
                    return false;
                }

            case SAObjectTypes.LeftFlag:
            case SAObjectTypes.RightFlag:
                if (!isGateDestroyed(SAObjectTypes.greenGate) && !isGateDestroyed(SAObjectTypes.blueGate)) {
                    return false;
                }

                break;
            default:
                //ABORT();
                break;
        }

        return true;
    }

    private void updateObjectInteractionFlags(int objectId) {
        var go = getBGObject((int) objectId);

        if (go) {
            if (canInteractWithObject(objectId)) {
                go.removeFlag(GameObjectFlags.NotSelectable);
            } else {
                go.setFlag(GameObjectFlags.NotSelectable);
            }
        }
    }

    private void updateObjectInteractionFlags() {
        for (byte i = (byte) SAObjectTypes.CENTRALFLAG; i <= SAObjectTypes.LEFTFLAG; ++i) {
            updateObjectInteractionFlags(i);
        }

        updateObjectInteractionFlags(SAObjectTypes.TITANRELIC);
    }

    private void captureGraveyard(int i, Player source) {
        if (GraveyardStatus[i] == attackers) {
            return;
        }

        delCreature(SACreatureTypes.max + i);
        var teamId = getTeamIndexByTeamId(getPlayerTeam(source.getGUID()));
        GraveyardStatus[i] = teamId;
        var sg = global.getObjectMgr().getWorldSafeLoc(SAMiscConst.GYEntries[i]);

        if (sg == null) {
            Log.outError(LogFilter.Battleground, String.format("CaptureGraveyard: non-existant GY entry: %1$s", SAMiscConst.GYEntries[i]));

            return;
        }

        addSpiritGuide(i + SACreatureTypes.max, sg.loc.getX(), sg.loc.getY(), sg.loc.getZ(), SAMiscConst.GYOrientation[i], GraveyardStatus[i]);

        int npc;
        int flag;

        switch (i) {
            case SAGraveyards.LeftCapturableGy: {
                flag = SAObjectTypes.LEFTFLAG;
                delObject(flag);

                addObject(flag, (SAMiscConst.ObjEntries[flag] - (teamId == TeamId.ALLIANCE ? 0 : 1)), SAMiscConst.ObjSpawnlocs[flag], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay);

                npc = SACreatureTypes.RIGSPARK;
                var rigspark = addCreature(SAMiscConst.NpcEntries[npc], (int) npc, SAMiscConst.NpcSpawnlocs[npc], attackers);

                if (rigspark) {
                    rigspark.getAI().talk(SATextIds.sparklightRigsparkSpawn);
                }

                for (byte j = (byte) SACreatureTypes.DEMOLISHER7; j <= SACreatureTypes.DEMOLISHER8; j++) {
                    addCreature(SAMiscConst.NpcEntries[j], j, SAMiscConst.NpcSpawnlocs[j], (attackers == TeamId.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE), 600);
                    var dem = getBGCreature(j);

                    if (dem) {
                        dem.setFaction(SAMiscConst.Factions[Attackers]);
                    }
                }

                updateWorldState(SAWorldStateIds.leftGyAlliance, GraveyardStatus[i] == TeamId.ALLIANCE ? 1 : 0);
                updateWorldState(SAWorldStateIds.leftGyHorde, GraveyardStatus[i] == TeamId.HORDE ? 1 : 0);

                var c = source.findNearestCreature(SharedConst.worldTrigger, 500.0f);

                if (c) {
                    sendChatMessage(c, teamId == TeamId.ALLIANCE ? SATextIds.WestGraveyardCapturedA : SATextIds.westGraveyardCapturedH, source);
                }
            }

            break;
            case SAGraveyards.RightCapturableGy: {
                flag = SAObjectTypes.RIGHTFLAG;
                delObject(flag);

                addObject(flag, (SAMiscConst.ObjEntries[flag] - (teamId == TeamId.ALLIANCE ? 0 : 1)), SAMiscConst.ObjSpawnlocs[flag], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay);

                npc = SACreatureTypes.SPARKLIGHT;
                var sparklight = addCreature(SAMiscConst.NpcEntries[npc], (int) npc, SAMiscConst.NpcSpawnlocs[npc], attackers);

                if (sparklight) {
                    sparklight.getAI().talk(SATextIds.sparklightRigsparkSpawn);
                }

                for (byte j = (byte) SACreatureTypes.DEMOLISHER5; j <= SACreatureTypes.DEMOLISHER6; j++) {
                    addCreature(SAMiscConst.NpcEntries[j], j, SAMiscConst.NpcSpawnlocs[j], attackers == TeamId.ALLIANCE ? TeamId.HORDE : TeamId.ALLIANCE, 600);

                    var dem = getBGCreature(j);

                    if (dem) {
                        dem.setFaction(SAMiscConst.Factions[Attackers]);
                    }
                }

                updateWorldState(SAWorldStateIds.rightGyAlliance, GraveyardStatus[i] == TeamId.ALLIANCE ? 1 : 0);
                updateWorldState(SAWorldStateIds.rightGyHorde, GraveyardStatus[i] == TeamId.HORDE ? 1 : 0);

                var c = source.findNearestCreature(SharedConst.worldTrigger, 500.0f);

                if (c) {
                    sendChatMessage(c, teamId == TeamId.ALLIANCE ? SATextIds.EastGraveyardCapturedA : SATextIds.eastGraveyardCapturedH, source);
                }
            }

            break;
            case SAGraveyards.CentralCapturableGy: {
                flag = SAObjectTypes.CENTRALFLAG;
                delObject(flag);

                addObject(flag, (SAMiscConst.ObjEntries[flag] - (teamId == TeamId.ALLIANCE ? 0 : 1)), SAMiscConst.ObjSpawnlocs[flag], 0, 0, 0, 0, BattlegroundConst.RespawnOneDay);

                updateWorldState(SAWorldStateIds.centerGyAlliance, GraveyardStatus[i] == TeamId.ALLIANCE ? 1 : 0);
                updateWorldState(SAWorldStateIds.centerGyHorde, GraveyardStatus[i] == TeamId.HORDE ? 1 : 0);

                var c = source.findNearestCreature(SharedConst.worldTrigger, 500.0f);

                if (c) {
                    sendChatMessage(c, teamId == TeamId.ALLIANCE ? SATextIds.SouthGraveyardCapturedA : SATextIds.southGraveyardCapturedH, source);
                }
            }

            break;
            default:
                //ABORT();
                break;
        }
    }

    private void titanRelicActivated(Player clicker) {
        if (!clicker) {
            return;
        }

        if (canInteractWithObject(SAObjectTypes.TITANRELIC)) {
            var clickerTeamId = getTeamIndexByTeamId(getPlayerTeam(clicker.getGUID()));

            if (clickerTeamId == attackers) {
                if (clickerTeamId == TeamId.ALLIANCE) {
                    sendBroadcastText(SABroadcastTexts.allianceCapturedTitanPortal, ChatMsg.BgSystemNeutral);
                } else {
                    sendBroadcastText(SABroadcastTexts.hordeCapturedTitanPortal, ChatMsg.BgSystemNeutral);
                }

                if (status == SAStatus.RoundOne) {
                    RoundScores[0].winner = (int) attackers;
                    RoundScores[0].time = totalTime;

                    // Achievement Storm the Beach (1310)
                    for (var pair : getPlayers().entrySet()) {
                        var player = global.getObjAccessor().findPlayer(pair.getKey());

                        if (player) {
                            if (getTeamIndexByTeamId(getPlayerTeam(player.getGUID())) == attackers) {
                                player.updateCriteria(CriteriaType.BeSpellTarget, 65246);
                            }
                        }
                    }

                    attackers = (attackers == TeamId.ALLIANCE) ? TeamId.HORDE : TeamId.ALLIANCE;
                    status = SAStatus.SecondWarmup;
                    totalTime = 0;
                    toggleTimer();

                    var c = getBGCreature(SACreatureTypes.KANRETHAD);

                    if (c) {
                        sendChatMessage(c, SATextIds.round1Finished);
                    }

                    updateWaitTimer = 5000;
                    signaledRoundTwo = false;
                    signaledRoundTwoHalfMin = false;
                    initSecondRound = true;
                    resetObjs();
                    getBgMap().updateAreaDependentAuras();
                    castSpellOnTeam(SASpellIds.endOfRound, Team.ALLIANCE);
                    castSpellOnTeam(SASpellIds.endOfRound, Team.Horde);
                } else if (status == SAStatus.RoundTwo) {
                    RoundScores[1].winner = (int) attackers;
                    RoundScores[1].time = totalTime;
                    toggleTimer();

                    // Achievement Storm the Beach (1310)
                    for (var pair : getPlayers().entrySet()) {
                        var player = global.getObjAccessor().findPlayer(pair.getKey());

                        if (player) {
                            if (getTeamIndexByTeamId(getPlayerTeam(player.getGUID())) == attackers && RoundScores[1].winner == attackers) {
                                player.updateCriteria(CriteriaType.BeSpellTarget, 65246);
                            }
                        }
                    }

                    if (RoundScores[0].time == RoundScores[1].time) {
                        endBattleground(0);
                    } else if (RoundScores[0].time < RoundScores[1].time) {
                        endBattleground(RoundScores[0].winner == TeamId.ALLIANCE ? Team.ALLIANCE : Team.Horde);
                    } else {
                        endBattleground(RoundScores[1].winner == TeamId.ALLIANCE ? Team.ALLIANCE : Team.Horde);
                    }
                }
            }
        }
    }

    private void toggleTimer() {
        timerEnabled = !timerEnabled;
        updateWorldState(SAWorldStateIds.enableTimer, TimerEnabled ? 1 : 0);
    }

    private void updateDemolisherSpawns() {
        for (byte i = (byte) SACreatureTypes.DEMOLISHER1; i <= SACreatureTypes.DEMOLISHER8; i++) {
            if (!BgCreatures[i].isEmpty()) {
                var demolisher = getBGCreature(i);

                if (demolisher) {
                    if (demolisher.isDead()) {
                        // Demolisher is not in list
                        if (!demoliserRespawnList.containsKey(i)) {
                            demoliserRespawnList.put(i, gameTime.GetGameTimeMS() + 30000);
                        } else {
                            if (demoliserRespawnList.get(i).compareTo(gameTime.GetGameTimeMS()) < 0) {
                                demolisher.getLocation().relocate(SAMiscConst.NpcSpawnlocs[i]);
                                demolisher.respawn();
                                demoliserRespawnList.remove(i);
                            }
                        }
                    }
                }
            }
        }
    }

    private void sendTransportInit(Player player) {
        if (!BgObjects[SAObjectTypes.BoatOne].isEmpty() || !BgObjects[SAObjectTypes.BoatTwo].isEmpty()) {
            UpdateData transData = new UpdateData(player.getLocation().getMapId());

            if (!BgObjects[SAObjectTypes.BoatOne].isEmpty()) {
                getBGObject(SAObjectTypes.BOATONE).buildCreateUpdateBlockForPlayer(transData, player);
            }

            if (!BgObjects[SAObjectTypes.BoatTwo].isEmpty()) {
                getBGObject(SAObjectTypes.BOATTWO).buildCreateUpdateBlockForPlayer(transData, player);
            }

            UpdateObject packet;
            tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
            transData.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }

    private void sendTransportsRemove(Player player) {
        if (!BgObjects[SAObjectTypes.BoatOne].isEmpty() || !BgObjects[SAObjectTypes.BoatTwo].isEmpty()) {
            UpdateData transData = new UpdateData(player.getLocation().getMapId());

            if (!BgObjects[SAObjectTypes.BoatOne].isEmpty()) {
                getBGObject(SAObjectTypes.BOATONE).buildOutOfRangeUpdateBlock(transData);
            }

            if (!BgObjects[SAObjectTypes.BoatTwo].isEmpty()) {
                getBGObject(SAObjectTypes.BOATTWO).buildOutOfRangeUpdateBlock(transData);
            }

            UpdateObject packet;
            tangible.OutObject<UpdateObject> tempOut_packet = new tangible.OutObject<UpdateObject>();
            transData.buildPacket(tempOut_packet);
            packet = tempOut_packet.outArgValue;
            player.sendPacket(packet);
        }
    }

    private boolean isGateDestroyed(int gateId) {
        return GateStatus[gateId] == SAGateState.AllianceGateDestroyed || GateStatus[gateId] == SAGateState.HordeGateDestroyed;
    }

    private SAGateInfo getGate(int entry) {
        for (var gate : SAMiscConst.GATES) {
            if (gate.gameObjectId == entry) {
                return gate;
            }
        }

        return null;
    }
}
