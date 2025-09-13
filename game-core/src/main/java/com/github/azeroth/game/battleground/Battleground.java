package com.github.azeroth.game.battleground;


import com.github.azeroth.game.WorldSafeLocsEntry;
import com.github.azeroth.game.chat.BroadcastTextBuilder;
import com.github.azeroth.game.chat.CypherStringChatBuilder;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.KillRewarder;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.group.PlayerGroup;
import com.github.azeroth.game.map.BattlegroundMap;
import com.github.azeroth.game.map.LocalizedDo;
import game.GameEvents;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class Battleground extends ZoneScript implements Closeable {
    private final HashMap<ObjectGuid, BattlegroundPlayer> m_Players = new HashMap<ObjectGuid, BattlegroundPlayer>();
    // Spirit Guide guid + Player list GUIDS
    private final MultiMap<ObjectGuid, ObjectGuid> m_ReviveQueue = new MultiMap<ObjectGuid, ObjectGuid>();
    // Player lists
    private final ArrayList<ObjectGuid> m_ResurrectQueue = new ArrayList<>(); // Player GUID
    private final ArrayList<ObjectGuid> m_OfflineQueue = new ArrayList<>(); // Player GUID
    // Raid Group
    private final PlayerGroup[] m_BgRaids = new PlayerGroup[SharedConst.PvpTeamsCount]; // 0 - team.Alliance, 1 - team.Horde
    // Players count by team
    private final int[] m_PlayersCount = new int[SharedConst.PvpTeamsCount];
    // Arena team ids by team
    private final int[] m_ArenaTeamIds = new int[SharedConst.PvpTeamsCount];
    private final int[] m_ArenaTeamMMR = new int[SharedConst.PvpTeamsCount];
    private final BattlegroundTemplate battlegroundTemplate;
    private final ArrayList<BattlegroundPlayerPosition> playerPositions = new ArrayList<>();
    public BattlegroundStartTimeIntervals[] startDelayTimes = new BattlegroundStartTimeIntervals[4];
    // this must be filled inructors!
    public int[] startMessageIds = new int[4];
    public boolean m_BuffChange;
    public BGHonorMode m_HonorMode = BGHonorMode.values()[0];
    public int[] m_TeamScores = new int[SharedConst.PvpTeamsCount];
    public int[] buff_Entries = {BattlegroundConst.SpeedBuff, BattlegroundConst.RegenBuff, BattlegroundConst.BerserkerBuff};
    protected HashMap<ObjectGuid, battlegroundScore> playerScores = new HashMap<ObjectGuid, battlegroundScore>(); // Player scores
    protected ObjectGuid[] bgObjects; // = new Dictionary<int, ObjectGuid>();
    protected ObjectGuid[] bgCreatures; // = new Dictionary<int, ObjectGuid>();
    // these are important variables used for starting messages
    private BattlegroundEventFlags m_Events = BattlegroundEventFlags.values()[0];
    private boolean m_IsRandom;
    // Battleground
    private battlegroundQueueTypeId m_queueId = new battlegroundQueueTypeId();
    private BattlegroundTypeId m_RandomTypeID = BattlegroundTypeId.values()[0];
    private int m_InstanceID; // Battleground Instance's GUID!
    private BattlegroundStatus m_Status = BattlegroundStatus.values()[0];
    private int m_ClientInstanceID; // the instance-id which is sent to the client and without any other internal use
    private int m_StartTime;
    private int m_CountdownTimer;
    private int m_ResetStatTimer;
    private int m_ValidStartPositionTimer;
    private int m_EndTime; // it is set to 120000 when bg is ending and it decreases itself
    private int m_LastResurrectTime;
    private ArenaTypes m_ArenaType = ArenaTypes.values()[0]; // 2=2v2, 3=3v3, 5=5v5
    private boolean m_InBGFreeSlotQueue; // used to make sure that BG is only once inserted into the BattlegroundMgr.BGFreeSlotQueue[bgTypeId] deque
    private boolean m_SetDeleteThis; // used for safe deletion of the bg after end / all players leave
    private PvPTeamId winnerTeamId = PvPTeamId.values()[0];
    private int m_StartDelayTime;
    private boolean m_IsRated; // is this battle rated?
    private boolean m_PrematureCountDown;
    private int m_PrematureCountDownTimer;
    private int m_LastPlayerPositionBroadcast;
    // Invited counters are useful for player invitation to BG - do not allow, if BG is started to one faction to have 2 more players than another faction
    // Invited counters will be changed only when removing already invited player from queue, removing player from Battleground and inviting player to BG
    // Invited players counters
    private int m_InvitedAlliance;
    private int m_InvitedHorde;
    // Start location
    private BattlegroundMap m_Map;
    private PvpDifficultyRecord pvpDifficultyEntry;

    public Battleground(BattlegroundTemplate battlegroundTemplate) {
        battlegroundTemplate = battlegroundTemplate;
        m_RandomTypeID = BattlegroundTypeId.NONE;
        m_Status = BattlegroundStatus.NONE;
        winnerTeamId = PvPTeamId.Neutral;

        m_HonorMode = BGHonorMode.NORMAL;

        StartDelayTimes[BattlegroundConst.EventIdFirst] = BattlegroundStartTimeIntervals.Delay2m;
        StartDelayTimes[BattlegroundConst.EventIdSecond] = BattlegroundStartTimeIntervals.Delay1m;
        StartDelayTimes[BattlegroundConst.EventIdThird] = BattlegroundStartTimeIntervals.Delay30s;
        StartDelayTimes[BattlegroundConst.EventIdFourth] = BattlegroundStartTimeIntervals.NONE;

        StartMessageIds[BattlegroundConst.EventIdFirst] = BattlegroundBroadcastTexts.StartTwoMinutes;
        StartMessageIds[BattlegroundConst.EventIdSecond] = BattlegroundBroadcastTexts.startOneMinute;
        StartMessageIds[BattlegroundConst.EventIdThird] = BattlegroundBroadcastTexts.startHalfMinute;
        StartMessageIds[BattlegroundConst.EventIdFourth] = BattlegroundBroadcastTexts.HasBegun;
    }

    public static int getTeamIndexByTeamId(Team team) {
        return team == Team.ALLIANCE ? TeamId.ALLIANCE : TeamId.HORDE;
    }

    public void close() throws IOException {
        // remove objects and creatures
        // (this is done automatically in mapmanager update, when the instance is reset after the reset time)
        for (var i = 0; i < bgCreatures.length; ++i) {
            delCreature(i);
        }

        for (var i = 0; i < bgObjects.length; ++i) {
            delObject(i);
        }

        global.getBattlegroundMgr().removeBattleground(getTypeID(), getInstanceID());

        // unload map
        if (m_Map) {
            m_Map.unloadAll(); // unload all objects (they may hold a reference to bg in their ZoneScript pointer)
            m_Map.setUnload(); // mark for deletion by MapManager

            //unlink to prevent crash, always unlink all pointer reference before destruction
            m_Map.setBG(null);
            m_Map = null;
        }

        // remove from bg free slot queue
        removeFromBGFreeSlotQueue();
    }

    public final Battleground getCopy() {
        return (Battleground) clone();
    }

    public final void update(int diff) {
        if (!preUpdateImpl(diff)) {
            return;
        }

        if (getPlayersSize() == 0) {
            //BG is empty
            // if there are no players invited, delete BG
            // this will delete arena or bg object, where any player entered
            // [[   but if you use Battleground object again (more battles possible to be played on 1 instance)
            //      then this condition should be removed and code:
            //      if (!getInvitedCount(team.Horde) && !getInvitedCount(team.Alliance))
            //          this.AddToFreeBGObjectsQueue(); // not yet implemented
            //      should be used instead of current
            // ]]
            // Battleground Template instance cannot be updated, because it would be deleted
            if (getInvitedCount(Team.Horde) == 0 && getInvitedCount(Team.ALLIANCE) == 0) {
                m_SetDeleteThis = true;
            }

            return;
        }

        switch (getStatus()) {
            case WaitJoin:
                if (getPlayersSize() != 0) {
                    _ProcessJoin(diff);
                    _CheckSafePositions(diff);
                }

                break;
            case InProgress:
                _ProcessOfflineQueue();
                _ProcessPlayerPositionBroadcast(diff);

                // after 47 time.Minutes without one team losing, the arena closes with no winner and no rating change
                if (isArena()) {
                    if (getElapsedTime() >= 47 * time.Minute * time.InMilliseconds) {
                        endBattleground(0);

                        return;
                    }
                } else {
                    _ProcessRessurect(diff);

                    if (global.getBattlegroundMgr().getPrematureFinishTime() != 0 && (getPlayersCountByTeam(Team.ALLIANCE) < getMinPlayersPerTeam() || getPlayersCountByTeam(Team.Horde) < getMinPlayersPerTeam())) {
                        _ProcessProgress(diff);
                    } else if (m_PrematureCountDown) {
                        m_PrematureCountDown = false;
                    }
                }

                break;
            case WaitLeave:
                _ProcessLeave(diff);

                break;
            default:
                break;
        }

        // Update start time and reset stats timer
        setElapsedTime(getElapsedTime() + diff);

        if (getStatus() == BattlegroundStatus.WaitJoin) {
            m_ResetStatTimer += diff;
            m_CountdownTimer += diff;
        }

        postUpdateImpl(diff);
    }

    public Team getPrematureWinner() {
        Team winner = Team.forValue(0);

        if (getPlayersCountByTeam(Team.ALLIANCE) >= getMinPlayersPerTeam()) {
            winner = Team.ALLIANCE;
        } else if (getPlayersCountByTeam(Team.Horde) >= getMinPlayersPerTeam()) {
            winner = Team.Horde;
        }

        return winner;
    }

    public final Player _GetPlayer(ObjectGuid guid, boolean offlineRemove, String context) {
        Player player = null;

        if (!offlineRemove) {
            player = global.getObjAccessor().findPlayer(guid);

            if (!player) {
                Log.outError(LogFilter.Battleground, String.format("Battleground.%1$s: player (%2$s) not found for BG (map: %3$s, instance id: %4$s)!", context, guid, getMapId(), m_InstanceID));
            }
        }

        return player;
    }

    public final Player _GetPlayer(java.util.Map.entry<ObjectGuid, BattlegroundPlayer> pair, String context) {
        return _GetPlayer(pair.getKey(), pair.getValue().offlineRemoveTime != 0, context);
    }

    public final BattlegroundMap getBgMap() {
        return m_Map;
    }

    public final void setBgMap(BattlegroundMap map) {
        m_Map = map;
    }

    public final WorldSafeLocsEntry getTeamStartPosition(int teamId) {
        return battlegroundTemplate.StartLocation[teamId];
    }

    public final void sendPacketToAll(ServerPacket packet) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayer(pair, "SendPacketToAll");

            if (player) {
                player.sendPacket(packet);
            }
        }
    }

    public final void sendChatMessage(Creature source, byte textId) {
        sendChatMessage(source, textId, null);
    }

    public final void sendChatMessage(Creature source, byte textId, WorldObject target) {
        global.getCreatureTextMgr().sendChat(source, textId, target);
    }

    public final void sendBroadcastText(int id, ChatMsg msgType) {
        sendBroadcastText(id, msgType, null);
    }

    public final void sendBroadcastText(int id, ChatMsg msgType, WorldObject target) {
        if (!CliDB.BroadcastTextStorage.containsKey(id)) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.SendBroadcastText: `broadcast_text` (ID: %1$s) was not found", id));

            return;
        }

        BroadcastTextBuilder builder = new BroadcastTextBuilder(null, msgType, id, gender.Male, target);
        LocalizedDo localizer = new LocalizedDo(builder);
        broadcastWorker(localizer);
    }

    public final void playSoundToAll(int soundID) {
        sendPacketToAll(new playSound(ObjectGuid.Empty, soundID, 0));
    }

    public final void castSpellOnTeam(int spellID, Team team) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayerForTeam(team, pair, "CastSpellOnTeam");

            if (player) {
                player.castSpell(player, spellID, true);
            }
        }
    }

    public final void rewardHonorToTeam(int honor, Team team) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayerForTeam(team, pair, "RewardHonorToTeam");

            if (player) {
                updatePlayerScore(player, ScoreType.bonusHonor, honor);
            }
        }
    }

    public final void rewardReputationToTeam(int faction_id, int Reputation, Team team) {
        var factionEntry = CliDB.FactionStorage.get(faction_id);

        if (factionEntry == null) {
            return;
        }

        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayerForTeam(team, pair, "RewardReputationToTeam");

            if (!player) {
                continue;
            }

            if (player.hasPlayerFlagEx(playerFlagsEx.MercenaryMode)) {
                continue;
            }

            var repGain = Reputation;
            tangible.RefObject<Integer> tempRef_repGain = new tangible.RefObject<Integer>(repGain);
            MathUtil.AddPct(tempRef_repGain, player.getTotalAuraModifier(AuraType.ModReputationGain));
            repGain = tempRef_repGain.refArgValue;
            tangible.RefObject<Integer> tempRef_repGain2 = new tangible.RefObject<Integer>(repGain);
            MathUtil.AddPct(tempRef_repGain2, player.getTotalAuraModifierByMiscValue(AuraType.ModFactionReputationGain, (int) faction_id));
            repGain = tempRef_repGain2.refArgValue;
            player.getReputationMgr().modifyReputation(factionEntry, (int) repGain);
        }
    }

    public final void updateWorldState(int worldStateId, int value) {
        updateWorldState(worldStateId, value, false);
    }

    public final void updateWorldState(int worldStateId, int value, boolean hidden) {
        global.getWorldStateMgr().setValue(worldStateId, value, hidden, getBgMap());
    }

    public final void updateWorldState(int worldStateId, int value) {
        updateWorldState(worldStateId, value, false);
    }

    public final void updateWorldState(int worldStateId, int value, boolean hidden) {
        global.getWorldStateMgr().setValue((int) worldStateId, value, hidden, getBgMap());
    }

    public void endBattleground(Team winner) {
        removeFromBGFreeSlotQueue();

        var guildAwarded = false;

        if (winner == Team.ALLIANCE) {
            if (isBattleground()) {
                sendBroadcastText(BattlegroundBroadcastTexts.AllianceWins, ChatMsg.BgSystemNeutral);
            }

            playSoundToAll((int) BattlegroundSounds.AllianceWins.getValue());
            setWinner(PvPTeamId.Alliance);
        } else if (winner == Team.Horde) {
            if (isBattleground()) {
                sendBroadcastText(BattlegroundBroadcastTexts.HordeWins, ChatMsg.BgSystemNeutral);
            }

            playSoundToAll((int) BattlegroundSounds.HordeWins.getValue());
            setWinner(PvPTeamId.Horde);
        } else {
            setWinner(PvPTeamId.Neutral);
        }

        PreparedStatement stmt;
        long battlegroundId = 1;

        if (isBattleground() && WorldConfig.getBoolValue(WorldCfg.BattlegroundStoreStatisticsEnable)) {
            stmt = DB.characters.GetPreparedStatement(CharStatements.SEL_PVPSTATS_MAXID);
            var result = DB.characters.query(stmt);

            if (!result.isEmpty()) {
                battlegroundId = result.<Long>Read(0) + 1;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PVPSTATS_BATTLEGROUND);
            stmt.AddValue(0, battlegroundId);
            stmt.AddValue(1, (byte) getWinner().getValue());
            stmt.AddValue(2, getUniqueBracketId());
            stmt.AddValue(3, (byte) getTypeID(true).getValue());
            DB.characters.execute(stmt);
        }

        setStatus(BattlegroundStatus.WaitLeave);
        //we must set it this way, because end time is sent in packet!
        setRemainingTime(BattlegroundConst.AutocloseBattleground);

        PVPMatchComplete pvpMatchComplete = new PVPMatchComplete();
        pvpMatchComplete.winner = (byte) getWinner().getValue();
        pvpMatchComplete.duration = (int) Math.max(0, (getElapsedTime() - BattlegroundStartTimeIntervals.Delay2m.getValue()) / time.InMilliseconds);
        tangible.OutObject<PVPMatchStatistics> tempOut_LogData = new tangible.OutObject<PVPMatchStatistics>();
        buildPvPLogDataPacket(tempOut_LogData);
        pvpMatchComplete.logData = tempOut_LogData.outArgValue;
        pvpMatchComplete.write();

        for (var pair : m_Players.entrySet()) {
            var team = pair.getValue().team;

            var player = _GetPlayer(pair, "EndBattleground");

            if (!player) {
                continue;
            }

            // should remove spirit of redemption
            if (player.hasAuraType(AuraType.SpiritOfRedemption)) {
                player.removeAurasByType(AuraType.ModShapeshift);
            }

            if (!player.isAlive()) {
                player.resurrectPlayer(1.0f);
                player.spawnCorpseBones();
            } else {
                //needed cause else in av some creatures will kill the players at the end
                player.combatStop();
            }

            // remove temporary currency bonus auras before rewarding player
            player.removeAura(BattlegroundConst.SpellHonorableDefender25y);
            player.removeAura(BattlegroundConst.SpellHonorableDefender60y);

            var winnerKills = player.getRandomWinner() ? WorldConfig.getUIntValue(WorldCfg.BgRewardWinnerHonorLast) : WorldConfig.getUIntValue(WorldCfg.BgRewardWinnerHonorFirst);
            var loserKills = player.getRandomWinner() ? WorldConfig.getUIntValue(WorldCfg.BgRewardLoserHonorLast) : WorldConfig.getUIntValue(WorldCfg.BgRewardLoserHonorFirst);

            if (isBattleground() && WorldConfig.getBoolValue(WorldCfg.BattlegroundStoreStatisticsEnable)) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_PVPSTATS_PLAYER);
                var score = playerScores.get(player.getGUID());

                stmt.AddValue(0, battlegroundId);
                stmt.AddValue(1, player.getGUID().getCounter());
                stmt.AddValue(2, team == winner);
                stmt.AddValue(3, score.killingBlows);
                stmt.AddValue(4, score.deaths);
                stmt.AddValue(5, score.honorableKills);
                stmt.AddValue(6, score.bonusHonor);
                stmt.AddValue(7, score.damageDone);
                stmt.AddValue(8, score.healingDone);
                stmt.AddValue(9, score.getAttr1());
                stmt.AddValue(10, score.getAttr2());
                stmt.AddValue(11, score.getAttr3());
                stmt.AddValue(12, score.getAttr4());
                stmt.AddValue(13, score.getAttr5());

                DB.characters.execute(stmt);
            }

            // Reward winner team
            if (team == winner) {
                if (isRandom() || global.getBattlegroundMgr().isBGWeekend(getTypeID())) {
                    updatePlayerScore(player, ScoreType.bonusHonor, getBonusHonorFromKill(winnerKills));

                    if (!player.getRandomWinner()) {
                        player.setRandomWinner(true);
                    }
                    // TODO: win honor xp
                } else {
                    // TODO: lose honor xp
                }

                player.updateCriteria(CriteriaType.WinBattleground, player.getLocation().getMapId());

                if (!guildAwarded) {
                    guildAwarded = true;
                    var guildId = getBgMap().getOwnerGuildId(player.getBgTeam());

                    if (guildId != 0) {
                        var guild = global.getGuildMgr().getGuildById(guildId);

                        if (guild) {
                            guild.updateCriteria(CriteriaType.WinBattleground, player.getLocation().getMapId(), 0, 0, null, player);
                        }
                    }
                }
            } else {
                if (isRandom() || global.getBattlegroundMgr().isBGWeekend(getTypeID())) {
                    updatePlayerScore(player, ScoreType.bonusHonor, getBonusHonorFromKill(loserKills));
                }
            }

            player.resetAllPowers();
            player.combatStopWithPets(true);

            blockMovement(player);

            player.sendPacket(pvpMatchComplete);

            player.updateCriteria(CriteriaType.ParticipateInBattleground, player.getLocation().getMapId());
        }
    }

    public final int getBonusHonorFromKill(int kills) {
        //variable kills means how many honorable kills you scored (so we need kills * honor_for_one_kill)
        var maxLevel = Math.min(getMaxLevel(), 80);

        return Formulas.HKHonorAtLevel(maxLevel, kills);
    }

    public void removePlayerAtLeave(ObjectGuid guid, boolean transport, boolean SendPacket) {
        var team = getPlayerTeam(guid);
        var participant = false;
        // Remove from lists/maps
        var bgPlayer = m_Players.get(guid);

        if (bgPlayer != null) {
            updatePlayersCountByTeam(team, true); // -1 player
            m_Players.remove(guid);
            // check if the player was a participant of the match, or only entered through gm command (goname)
            participant = true;
        }

        if (playerScores.containsKey(guid)) {
            playerScores.remove(guid);
        }

        removePlayerFromResurrectQueue(guid);

        var player = global.getObjAccessor().findPlayer(guid);

        if (player) {
            // should remove spirit of redemption
            if (player.hasAuraType(AuraType.SpiritOfRedemption)) {
                player.removeAurasByType(AuraType.ModShapeshift);
            }

            player.removeAurasByType(AuraType.Mounted);
            player.removeAura(BattlegroundConst.SpellMercenaryHorde1);
            player.removeAura(BattlegroundConst.SpellMercenaryHordeReactions);
            player.removeAura(BattlegroundConst.SpellMercenaryAlliance1);
            player.removeAura(BattlegroundConst.SpellMercenaryAllianceReactions);
            player.removeAura(BattlegroundConst.SpellMercenaryShapeshift);
            player.removePlayerFlagEx(playerFlagsEx.MercenaryMode);

            if (!player.isAlive()) // resurrect on exit
            {
                player.resurrectPlayer(1.0f);
                player.spawnCorpseBones();
            }
        } else {
            player.offlineResurrect(guid, null);
        }

        removePlayer(player, guid, team); // BG subclass specific code

        var bgQueueTypeId = getQueueId();

        if (participant) // if the player was a match participant, remove auras, calc rating, update queue
        {
            if (player) {
                player.clearAfkReports();

                // if arena, remove the specific arena auras
                if (isArena()) {
                    // unsummon current and summon old pet if there was one and there isn't a current pet
                    player.removePet(null, PetSaveMode.NotInSlot);
                    player.resummonPetTemporaryUnSummonedIfAny();
                }

                if (SendPacket) {
                    BattlefieldStatusNone battlefieldStatus;
                    tangible.OutObject<BattlefieldStatusNone> tempOut_battlefieldStatus = new tangible.OutObject<BattlefieldStatusNone>();
                    global.getBattlegroundMgr().buildBattlegroundStatusNone(tempOut_battlefieldStatus, player, player.getBattlegroundQueueIndex(bgQueueTypeId), player.getBattlegroundQueueJoinTime(bgQueueTypeId));
                    battlefieldStatus = tempOut_battlefieldStatus.outArgValue;
                    player.sendPacket(battlefieldStatus);
                }

                // this call is important, because player, when joins to Battleground, this method is not called, so it must be called when leaving bg
                player.removeBattlegroundQueueId(bgQueueTypeId);
            }

            // remove from raid group if player is member
            var group = getBgRaid(team);

            if (group) {
                if (!group.removeMember(guid)) // group was disbanded
                {
                    setBgRaid(team, null);
                }
            }

            decreaseInvitedCount(team);

            //we should update Battleground queue, but only if bg isn't ending
            if (isBattleground() && getStatus() < BattlegroundStatus.WaitLeave.getValue()) {
                // a player has left the Battleground, so there are free slots . add to queue
                addToBGFreeSlotQueue();
                global.getBattlegroundMgr().scheduleQueueUpdate(0, bgQueueTypeId, getBracketId());
            }

            // Let others know
            BattlegroundPlayerLeft playerLeft = new BattlegroundPlayerLeft();
            playerLeft.guid = guid;
            sendPacketToTeam(team, playerLeft, player);
        }

        if (player) {
            // Do next only if found in Battleground
            player.setBattlegroundId(0, BattlegroundTypeId.NONE); // We're not in BG.
            // reset destination bg team
            player.setBgTeam(0);

            // remove all criterias on bg leave
            player.resetCriteria(CriteriaFailEvent.LeaveBattleground, getMapId(), true);

            if (transport) {
                player.teleportToBGEntryPoint();
            }

            Log.outDebug(LogFilter.Battleground, "Removed player {0} from Battleground.", player.getName());
        }

        //Battleground object will be deleted next Battleground.update() call
    }

    // this method is called when no players remains in Battleground
    public void reset() {
        setWinner(PvPTeamId.Neutral);
        setStatus(BattlegroundStatus.WaitQueue);
        setElapsedTime(0);
        setRemainingTime(0);
        setLastResurrectTime(0);
        m_Events = BattlegroundEventFlags.forValue(0);

        if (m_InvitedAlliance > 0 || m_InvitedHorde > 0) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.Reset: one of the counters is not 0 (team.Alliance: %1$s, team.Horde: %2$s) for BG (map: %3$s, instance id: %4$s)!", m_InvitedAlliance, m_InvitedHorde, getMapId(), m_InstanceID));
        }

        m_InvitedAlliance = 0;
        m_InvitedHorde = 0;
        m_InBGFreeSlotQueue = false;

        m_Players.clear();

        playerScores.clear();

        playerPositions.clear();
    }

    public final void startBattleground() {
        setElapsedTime(0);
        setLastResurrectTime(0);
        // add BG to free slot queue
        addToBGFreeSlotQueue();

        // add bg to update list
        // This must be done here, because we need to have already invited some players when first BG.update() method is executed
        // and it doesn't matter if we call startBattleground() more times, because m_Battlegrounds is a map and instance id never changes
        global.getBattlegroundMgr().addBattleground(this);

        if (m_IsRated) {
            Log.outDebug(LogFilter.Arena, "Arena match type: {0} for Team1Id: {1} - Team2Id: {2} started.", m_ArenaType, m_ArenaTeamIds[TeamId.ALLIANCE], m_ArenaTeamIds[TeamId.HORDE]);
        }
    }

    public final void teleportPlayerToExploitLocation(Player player) {
        var loc = getExploitTeleportLocation(player.getBgTeam());

        if (loc != null) {
            player.teleportTo(loc.loc);
        }
    }

    public void addPlayer(Player player) {
        // remove afk from player
        if (player.isAFK()) {
            player.toggleAFK();
        }

        // score struct must be created in inherited class

        var guid = player.getGUID();
        var team = player.getBgTeam();

        BattlegroundPlayer bp = new BattlegroundPlayer();
        bp.offlineRemoveTime = 0;
        bp.team = team;
        bp.activeSpec = (int) player.getPrimarySpecialization();
        bp.mercenary = player.isMercenaryForBattlegroundQueueType(getQueueId());

        var isInBattleground = isPlayerInBattleground(player.getGUID());
        // Add to list/maps
        m_Players.put(guid, bp);

        if (!isInBattleground) {
            updatePlayersCountByTeam(team, false); // +1 player
        }

        BattlegroundPlayerJoined playerJoined = new BattlegroundPlayerJoined();
        playerJoined.guid = player.getGUID();
        sendPacketToTeam(team, playerJoined, player);

        PVPMatchInitialize pvpMatchInitialize = new PVPMatchInitialize();
        pvpMatchInitialize.mapID = getMapId();

        switch (getStatus()) {
            case None:
            case WaitQueue:
                pvpMatchInitialize.state = PVPMatchInitialize.MatchState.inactive;

                break;
            case WaitJoin:
            case InProgress:
                pvpMatchInitialize.state = PVPMatchInitialize.MatchState.inProgress;

                break;
            case WaitLeave:
                pvpMatchInitialize.state = PVPMatchInitialize.MatchState.Complete;

                break;
            default:
                break;
        }

        if (getElapsedTime() >= BattlegroundStartTimeIntervals.Delay2m.getValue()) {
            pvpMatchInitialize.duration = (int) (getElapsedTime() - BattlegroundStartTimeIntervals.Delay2m.getValue()) / time.InMilliseconds;
            pvpMatchInitialize.startTime = gameTime.GetGameTime() - pvpMatchInitialize.duration;
        }

        pvpMatchInitialize.arenaFaction = (byte) (player.getBgTeam() == Team.Horde ? PvPTeamId.Horde : PvPTeamId.Alliance);
        pvpMatchInitialize.battlemasterListID = (int) getTypeID().getValue();
        pvpMatchInitialize.registered = false;
        pvpMatchInitialize.affectsRating = isRated();

        player.sendPacket(pvpMatchInitialize);

        player.removeAurasByType(AuraType.Mounted);

        // add arena specific auras
        if (isArena()) {
            player.removeArenaEnchantments(EnchantmentSlot.Temp);

            player.destroyConjuredItems(true);
            player.unsummonPetTemporaryIfAny();

            if (getStatus() == BattlegroundStatus.WaitJoin) // not started yet
            {
                player.castSpell(player, BattlegroundConst.SpellArenaPreparation, true);
                player.resetAllPowers();
            }
        } else {
            if (getStatus() == BattlegroundStatus.WaitJoin) // not started yet
            {
                player.castSpell(player, BattlegroundConst.SpellPreparation, true); // reduces all mana cost of spells.

                var countdownMaxForBGType = isArena() ? BattlegroundConst.ArenaCountdownMax : BattlegroundConst.BattlegroundCountdownMax;
                StartTimer timer = new startTimer();
                timer.type = TimerType.Pvp;
                timer.timeLeft = countdownMaxForBGType - (getElapsedTime() / 1000);
                timer.totalTime = countdownMaxForBGType;

                player.sendPacket(timer);
            }

            if (bp.mercenary) {
                if (bp.team == Team.Horde) {
                    player.castSpell(player, BattlegroundConst.SpellMercenaryHorde1, true);
                    player.castSpell(player, BattlegroundConst.SpellMercenaryHordeReactions, true);
                } else if (bp.team == Team.ALLIANCE) {
                    player.castSpell(player, BattlegroundConst.SpellMercenaryAlliance1, true);
                    player.castSpell(player, BattlegroundConst.SpellMercenaryAllianceReactions, true);
                }

                player.castSpell(player, BattlegroundConst.SpellMercenaryShapeshift);
                player.setPlayerFlagEx(playerFlagsEx.MercenaryMode);
            }
        }

        // reset all map criterias on map enter
        if (!isInBattleground) {
            player.resetCriteria(CriteriaFailEvent.LeaveBattleground, getMapId(), true);
        }

        // setup BG group membership
        playerAddedToBGCheckIfBGIsRunning(player);
        addOrSetPlayerToCorrectBgGroup(player, team);
    }

    // this method adds player to his team's bg group, or sets his correct group if player is already in bg group
    public final void addOrSetPlayerToCorrectBgGroup(Player player, Team team) {
        var playerGuid = player.getGUID();
        var group = getBgRaid(team);

        if (!group) // first player joined
        {
            group = new PlayerGroup();
            setBgRaid(team, group);
            group.create(player);
        } else // raid already exist
        {
            if (group.isMember(playerGuid)) {
                var subgroup = group.getMemberGroup(playerGuid);
                player.setBattlegroundOrBattlefieldRaid(group, subgroup);
            } else {
                group.addMember(player);
                var originalGroup = player.getOriginalGroup();

                if (originalGroup) {
                    if (originalGroup.isLeader(playerGuid)) {
                        group.changeLeader(playerGuid);
                        group.sendUpdate();
                    }
                }
            }
        }
    }

    // This method should be called when player logs into running Battleground
    public final void eventPlayerLoggedIn(Player player) {
        var guid = player.getGUID();

        // player is correct pointer
        for (var id : m_OfflineQueue) {
            if (Objects.equals(id, guid)) {
                m_OfflineQueue.remove(id);

                break;
            }
        }

        m_Players.get(guid).offlineRemoveTime = 0;
        playerAddedToBGCheckIfBGIsRunning(player);
        // if Battleground is starting, then add preparation aura
        // we don't have to do that, because preparation aura isn't removed when player logs out
    }

    // This method should be called when player logs out from running Battleground
    public final void eventPlayerLoggedOut(Player player) {
        var guid = player.getGUID();

        if (!isPlayerInBattleground(guid)) // Check if this player really is in Battleground (might be a GM who teleported inside)
        {
            return;
        }

        // player is correct pointer, it is checked in WorldSession.logoutPlayer()
        m_OfflineQueue.add(player.getGUID());
        m_Players.get(guid).offlineRemoveTime = gameTime.GetGameTime() + BattlegroundConst.MaxOfflineTime;

        if (getStatus() == BattlegroundStatus.inProgress) {
            // drop flag and handle other cleanups
            removePlayer(player, guid, getPlayerTeam(guid));

            // 1 player is logging out, if it is the last alive, then end arena!
            if (isArena() && player.isAlive()) {
                if (getAlivePlayersCountByTeam(player.getBgTeam()) <= 1 && getPlayersCountByTeam(getOtherTeam(player.getBgTeam())) != 0) {
                    endBattleground(getOtherTeam(player.getBgTeam()));
                }
            }
        }
    }

    // This method removes this Battleground from free queue - it must be called when deleting Battleground
    public final void removeFromBGFreeSlotQueue() {
        if (m_InBGFreeSlotQueue) {
            global.getBattlegroundMgr().removeFromBGFreeSlotQueue(getQueueId(), m_InstanceID);
            m_InBGFreeSlotQueue = false;
        }
    }

    // get the number of free slots for team
    // returns the number how many players can join Battleground to MaxPlayersPerTeam
    public final int getFreeSlotsForTeam(Team team) {
        // if BG is starting and WorldCfg.BattlegroundInvitationType == BattlegroundQueueInvitationTypeB.NoBalance, invite anyone
        if (getStatus() == BattlegroundStatus.WaitJoin && WorldConfig.getIntValue(WorldCfg.BattlegroundInvitationType) == BattlegroundQueueInvitationType.NoBalance.getValue()) {
            return (getInvitedCount(team) < getMaxPlayersPerTeam()) ? getMaxPlayersPerTeam() - getInvitedCount(team) : 0;
        }

        // if BG is already started or WorldCfg.BattlegroundInvitationType != BattlegroundQueueInvitationType.NoBalance, do not allow to join too much players of one faction
        int otherTeamInvitedCount;
        int thisTeamInvitedCount;
        int otherTeamPlayersCount;
        int thisTeamPlayersCount;

        if (team == Team.ALLIANCE) {
            thisTeamInvitedCount = getInvitedCount(Team.ALLIANCE);
            otherTeamInvitedCount = getInvitedCount(Team.Horde);
            thisTeamPlayersCount = getPlayersCountByTeam(Team.ALLIANCE);
            otherTeamPlayersCount = getPlayersCountByTeam(Team.Horde);
        } else {
            thisTeamInvitedCount = getInvitedCount(Team.Horde);
            otherTeamInvitedCount = getInvitedCount(Team.ALLIANCE);
            thisTeamPlayersCount = getPlayersCountByTeam(Team.Horde);
            otherTeamPlayersCount = getPlayersCountByTeam(Team.ALLIANCE);
        }

        if (getStatus() == BattlegroundStatus.inProgress || getStatus() == BattlegroundStatus.WaitJoin) {
            // difference based on ppl invited (not necessarily entered battle)
            // default: allow 0
            int diff = 0;

            // allow join one person if the sides are equal (to fill up bg to minPlayerPerTeam)
            if (otherTeamInvitedCount == thisTeamInvitedCount) {
                diff = 1;
            }
            // allow join more ppl if the other side has more players
            else if (otherTeamInvitedCount > thisTeamInvitedCount) {
                diff = otherTeamInvitedCount - thisTeamInvitedCount;
            }

            // difference based on max players per team (don't allow inviting more)
            var diff2 = (thisTeamInvitedCount < getMaxPlayersPerTeam()) ? getMaxPlayersPerTeam() - thisTeamInvitedCount : 0;
            // difference based on players who already entered
            // default: allow 0
            int diff3 = 0;

            // allow join one person if the sides are equal (to fill up bg minPlayerPerTeam)
            if (otherTeamPlayersCount == thisTeamPlayersCount) {
                diff3 = 1;
            }
            // allow join more ppl if the other side has more players
            else if (otherTeamPlayersCount > thisTeamPlayersCount) {
                diff3 = otherTeamPlayersCount - thisTeamPlayersCount;
            }
            // or other side has less than minPlayersPerTeam
            else if (thisTeamInvitedCount <= getMinPlayersPerTeam()) {
                diff3 = getMinPlayersPerTeam() - thisTeamInvitedCount + 1;
            }

            // return the minimum of the 3 differences

            // min of diff and diff 2
            diff = Math.min(diff, diff2);

            // min of diff, diff2 and diff3
            return Math.min(diff, diff3);
        }

        return 0;
    }

    public final boolean isArena() {
        return battlegroundTemplate.isArena();
    }

    public final boolean isBattleground() {
        return !isArena();
    }

    public final boolean hasFreeSlots() {
        return getPlayersSize() < getMaxPlayers();
    }

    public void buildPvPLogDataPacket(tangible.OutObject<PVPMatchStatistics> pvpLogData) {
        pvpLogData.outArgValue = new PVPMatchStatistics();

        for (var score : playerScores.entrySet()) {
            var playerData;

            score.getValue().buildPvPLogPlayerDataPacket(out playerData);

            var player = global.getObjAccessor().getPlayer(getBgMap(), playerData.playerGUID);

            if (player) {
                playerData.isInWorld = true;
                playerData.primaryTalentTree = (int) player.getPrimarySpecialization();
                playerData.sex = (int) player.gender;
                playerData.playerRace = player.race;
                playerData.playerClass = (int) player.class;
                playerData.honorLevel = (int) player.honorLevel;
            }

            pvpLogData.outArgValue.statistics.add(playerData);
        }

        pvpLogData.outArgValue.PlayerCount[PvPTeamId.Horde.getValue()] = (byte) getPlayersCountByTeam(Team.Horde);
        pvpLogData.outArgValue.PlayerCount[PvPTeamId.Alliance.getValue()] = (byte) getPlayersCountByTeam(Team.ALLIANCE);
    }

    public boolean updatePlayerScore(Player player, ScoreType type, int value) {
        return updatePlayerScore(player, type, value, true);
    }

    public boolean updatePlayerScore(Player player, ScoreType type, int value, boolean doAddHonor) {
        var bgScore = playerScores.get(player.getGUID());

        if (bgScore == null) // player not found...
        {
            return false;
        }

        if (type == ScoreType.bonusHonor && doAddHonor && isBattleground()) {
            player.rewardHonor(null, 1, (int) value);
        } else {
            bgScore.updateScore(type, value);
        }

        return true;
    }

    public final void addPlayerToResurrectQueue(ObjectGuid npc_guid, ObjectGuid player_guid) {
        m_ReviveQueue.add(npc_guid, player_guid);

        var player = global.getObjAccessor().findPlayer(player_guid);

        if (!player) {
            return;
        }

        player.castSpell(player, BattlegroundConst.SpellWaitingForResurrect, true);
    }

    public final void removePlayerFromResurrectQueue(ObjectGuid player_guid) {
        m_ReviveQueue.RemoveIfMatching((tangible.Func1Param<java.util.Map.entry<ObjectGuid, ObjectGuid>, Boolean>) ((pair) ->
        {
            if (Objects.equals(pair.value, player_guid)) {
                var player = global.getObjAccessor().findPlayer(player_guid);

                if (player) {
                    player.removeAura(BattlegroundConst.SpellWaitingForResurrect);
                }

                return true;
            }

            return false;
        }));
    }

    public final void relocateDeadPlayers(ObjectGuid guideGuid) {
        // Those who are waiting to resurrect at this node are taken to the closest own node's graveyard
        var ghostList = m_ReviveQueue.get(guideGuid);

        if (!ghostList.isEmpty()) {
            WorldSafeLocsEntry closestGrave = null;

            for (var guid : ghostList) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (!player) {
                    continue;
                }

                if (closestGrave == null) {
                    closestGrave = getClosestGraveYard(player);
                }

                if (closestGrave != null) {
                    player.teleportTo(closestGrave.loc);
                }
            }

            ghostList.clear();
        }
    }

    public final boolean addObject(int type, int entry, float x, float y, float z, float o, float rotation0, float rotation1, float rotation2, float rotation3, int respawnTime) {
        return addObject(type, entry, x, y, z, o, rotation0, rotation1, rotation2, rotation3, respawnTime, GOState.Ready);
    }

    public final boolean addObject(int type, int entry, float x, float y, float z, float o, float rotation0, float rotation1, float rotation2, float rotation3) {
        return addObject(type, entry, x, y, z, o, rotation0, rotation1, rotation2, rotation3, 0, GOState.Ready);
    }

    public final boolean addObject(int type, int entry, float x, float y, float z, float o, float rotation0, float rotation1, float rotation2, float rotation3, int respawnTime, GOState goState) {
        Map map = findBgMap();

        if (!map) {
            return false;
        }

        Quaternion rotation = new Quaternion(rotation0, rotation1, rotation2, rotation3);

        // Temporally add safety check for bad spawns and send log (object rotations need to be rechecked in sniff)
        if (rotation0 == 0 && rotation1 == 0 && rotation2 == 0 && rotation3 == 0) {
            Log.outDebug(LogFilter.Battleground, String.format("Battleground.AddObject: gameoobject [entry: %1$s, object type: %2$s] for BG (map: %3$s) has zeroed rotation fields, ", entry, type, getMapId()) + "orientation used temporally, but please fix the spawn");

            rotation = Quaternion.CreateFromRotationMatrix(Extensions.fromEulerAnglesZYX(o, 0.0f, 0.0f));
        }

        // Must be created this way, adding to godatamap would add it to the base map of the instance
        // and when loading it (in go.loadFromDB()), a new guid would be assigned to the object, and a new object would be created
        // So we must create it specific for this instance
        var go = gameObject.createGameObject(entry, getBgMap(), new Position(x, y, z, o), rotation, 255, goState);

        if (!go) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.AddObject: cannot create gameobject (entry: %1$s) for BG (map: %2$s, instance id: %3$s)!", entry, getMapId(), m_InstanceID));

            return false;
        }

        // Add to world, so it can be later looked up from HashMapHolder
        if (!map.addToMap(go)) {
            return false;
        }

        BgObjects[type] = go.getGUID();

        return true;
    }

    public final boolean addObject(int type, int entry, Position pos, float rotation0, float rotation1, float rotation2, float rotation3, int respawnTime) {
        return addObject(type, entry, pos, rotation0, rotation1, rotation2, rotation3, respawnTime, GOState.Ready);
    }

    public final boolean addObject(int type, int entry, Position pos, float rotation0, float rotation1, float rotation2, float rotation3) {
        return addObject(type, entry, pos, rotation0, rotation1, rotation2, rotation3, 0, GOState.Ready);
    }

    public final boolean addObject(int type, int entry, Position pos, float rotation0, float rotation1, float rotation2, float rotation3, int respawnTime, GOState goState) {
        return addObject(type, entry, pos.getX(), pos.getY(), pos.getZ(), pos.getO(), rotation0, rotation1, rotation2, rotation3, respawnTime, goState);
    }

    // Some doors aren't despawned so we cannot handle their closing in gameobject.update()
    // It would be nice to correctly implement GO_ACTIVATED state and open/close doors in gameobject code
    public final void doorClose(int type) {
        var obj = getBgMap().getGameObject(BgObjects[type]);

        if (obj) {
            // If doors are open, close it
            if (obj.getLootState() == LootState.Activated && obj.getGoState() != GOState.Ready) {
                obj.setLootState(LootState.Ready);
                obj.setGoState(GOState.Ready);
            }
        } else {
            Log.outError(LogFilter.Battleground, String.format("Battleground.DoorClose: door gameobject (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgObjects[type], getMapId(), m_InstanceID));
        }
    }

    public final void doorOpen(int type) {
        var obj = getBgMap().getGameObject(BgObjects[type]);

        if (obj) {
            obj.setLootState(LootState.Activated);
            obj.setGoState(GOState.active);
        } else {
            Log.outError(LogFilter.Battleground, String.format("Battleground.DoorOpen: door gameobject (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgObjects[type], getMapId(), m_InstanceID));
        }
    }

    public final GameObject getBGObject(int type) {
        if (BgObjects[type].isEmpty()) {
            return null;
        }

        var obj = getBgMap().getGameObject(BgObjects[type]);

        if (!obj) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.GetBGObject: gameobject (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgObjects[type], getMapId(), m_InstanceID));
        }

        return obj;
    }

    public final Creature getBGCreature(int type) {
        if (BgCreatures[type].isEmpty()) {
            return null;
        }

        var creature = getBgMap().getCreature(BgCreatures[type]);

        if (!creature) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.GetBGCreature: creature (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgCreatures[type], getMapId(), m_InstanceID));
        }

        return creature;
    }

    public final int getMapId() {
        return (int) battlegroundTemplate.battlemasterEntry.MapId[0];
    }

    public final void spawnBGObject(int type, int respawntime) {
        Map map = findBgMap();

        if (map != null) {
            var obj = map.getGameObject(BgObjects[type]);

            if (obj) {
                if (respawntime != 0) {
                    obj.setLootState(LootState.JustDeactivated);

                    {
                        var goOverride = obj.getGameObjectOverride();

                        if (goOverride != null) {
                            if (goOverride.flags.hasFlag(GameObjectFlags.NoDespawn)) {
                                // This function should be called in GameObject::Update() but in case of
                                // GO_FLAG_NODESPAWN flag the function is never called, so we call it here
                                obj.sendGameObjectDespawn();
                            }
                        }
                    }
                } else if (obj.getLootState() == LootState.JustDeactivated) {
                    // Change state from GO_JUST_DEACTIVATED to GO_READY in case battleground is starting again
                    obj.setLootState(LootState.Ready);
                }

                obj.setRespawnTime((int) respawntime);
                map.addToMap(obj);
            }
        }
    }

    public Creature addCreature(int entry, int type, float x, float y, float z, float o, int teamIndex, int respawntime) {
        return addCreature(entry, type, x, y, z, o, teamIndex, respawntime, null);
    }

    public Creature addCreature(int entry, int type, float x, float y, float z, float o, int teamIndex) {
        return addCreature(entry, type, x, y, z, o, teamIndex, 0, null);
    }

    public Creature addCreature(int entry, int type, float x, float y, float z, float o) {
        return addCreature(entry, type, x, y, z, o, TeamIds.Neutral, 0, null);
    }

    public Creature addCreature(int entry, int type, float x, float y, float z, float o, int teamIndex, int respawntime, Transport transport) {
        Map map = findBgMap();

        if (!map) {
            return null;
        }

        if (global.getObjectMgr().getCreatureTemplate(entry) == null) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.AddCreature: creature template (entry: %1$s) does not exist for BG (map: %2$s, instance id: %3$s)!", entry, getMapId(), m_InstanceID));

            return null;
        }


        if (transport) {
            Creature transCreature = transport.summonPassenger(entry, new Position(x, y, z, o), TempSummonType.ManualDespawn);

            if (transCreature) {
                BgCreatures[type] = transCreature.getGUID();

                return transCreature;
            }

            return null;
        }

        Position pos = new Position(x, y, z, o);

        var creature = CREATURE.createCreature(entry, map, pos);

        if (!creature) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.AddCreature: cannot create creature (entry: %1$s) for BG (map: %2$s, instance id: %3$s)!", entry, getMapId(), m_InstanceID));

            return null;
        }

        creature.setHomePosition(pos);

        if (!map.addToMap(creature)) {
            return null;
        }

        BgCreatures[type] = creature.getGUID();

        if (respawntime != 0) {
            creature.setRespawnDelay(respawntime);
        }

        return creature;
    }

    public final Creature addCreature(int entry, int type, Position pos, int teamIndex, int respawntime) {
        return addCreature(entry, type, pos, teamIndex, respawntime, null);
    }

    public final Creature addCreature(int entry, int type, Position pos, int teamIndex) {
        return addCreature(entry, type, pos, teamIndex, 0, null);
    }

    public final Creature addCreature(int entry, int type, Position pos) {
        return addCreature(entry, type, pos, TeamIds.Neutral, 0, null);
    }

    public final Creature addCreature(int entry, int type, Position pos, int teamIndex, int respawntime, Transport transport) {
        return addCreature(entry, type, pos.getX(), pos.getY(), pos.getZ(), pos.getO(), teamIndex, respawntime, transport);
    }

    public final boolean delCreature(int type) {
        if (BgCreatures[type].isEmpty()) {
            return true;
        }

        var creature = getBgMap().getCreature(BgCreatures[type]);

        if (creature) {
            creature.addObjectToRemoveList();
            BgCreatures[type].clear();

            return true;
        }

        Log.outError(LogFilter.Battleground, String.format("Battleground.DelCreature: creature (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgCreatures[type], getMapId(), m_InstanceID));
        BgCreatures[type].clear();

        return false;
    }

    public final boolean delObject(int type) {
        if (BgObjects[type].isEmpty()) {
            return true;
        }

        var obj = getBgMap().getGameObject(BgObjects[type]);

        if (obj) {
            obj.setRespawnTime(0); // not save respawn time
            obj.delete();
            BgObjects[type].clear();

            return true;
        }

        Log.outError(LogFilter.Battleground, String.format("Battleground.DelObject: gameobject (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgObjects[type], getMapId(), m_InstanceID));
        BgObjects[type].clear();

        return false;
    }

    public final boolean addSpiritGuide(int type, float x, float y, float z, float o, int teamIndex) {
        var entry = (int) (teamIndex == TeamId.ALLIANCE ? BattlegroundCreatures.A_SpiritGuide : BattlegroundCreatures.H_SpiritGuide);

        var creature = addCreature(entry, type, x, y, z, o);

        if (creature) {
            creature.setDeathState(deathState.Dead);
            creature.addChannelObject(creature.getGUID());

            // aura
            //todo Fix display here
            // creature.setVisibleAura(0, SPELL_SPIRIT_HEAL_CHANNEL);
            // casting visual effect
            creature.setChannelSpellId(BattlegroundConst.SpellSpiritHealChannel);

            creature.setChannelVisual(new spellCastVisual(BattlegroundConst.SpellSpiritHealChannelVisual, 0));

            //creature.castSpell(creature, SPELL_SPIRIT_HEAL_CHANNEL, true);
            return true;
        }

        Log.outError(LogFilter.Battleground, String.format("Battleground.AddSpiritGuide: cannot create spirit guide (type: %1$s, entry: %2$s) for BG (map: %3$s, instance id: %4$s)!", type, entry, getMapId(), m_InstanceID));
        endNow();

        return false;
    }

    public final boolean addSpiritGuide(int type, Position pos) {
        return addSpiritGuide(type, pos, TeamIds.Neutral);
    }

    public final boolean addSpiritGuide(int type, Position pos, int teamIndex) {
        return addSpiritGuide(type, pos.getX(), pos.getY(), pos.getZ(), pos.getO(), teamIndex);
    }

    public final void sendMessageToAll(SysMessage entry, ChatMsg msgType) {
        sendMessageToAll(entry, msgType, null);
    }

    public final void sendMessageToAll(SysMessage entry, ChatMsg msgType, Player source) {
        if (entry == 0) {
            return;
        }

        CypherStringChatBuilder builder = new CypherStringChatBuilder(null, msgType, entry, source);
        LocalizedDo localizer = new LocalizedDo(builder);
        broadcastWorker(localizer);
    }

    public final void sendMessageToAll(SysMessage entry, ChatMsg msgType, Player source, object... args) {
        if (entry == 0) {
            return;
        }

        CypherStringChatBuilder builder = new CypherStringChatBuilder(null, msgType, entry, source, args);
        LocalizedDo localizer = new LocalizedDo(builder);
        broadcastWorker(localizer);
    }

    public final void addPlayerPosition(BattlegroundPlayerPosition position) {
        playerPositions.add(position);
    }

    public final void removePlayerPosition(ObjectGuid guid) {
        tangible.ListHelper.removeAll(playerPositions, playerPosition -> Objects.equals(playerPosition.guid, guid));
    }

    // IMPORTANT NOTICE:
    // buffs aren't spawned/despawned when players captures anything
    // buffs are in their positions when Battleground starts
    public final void handleTriggerBuff(ObjectGuid goGuid) {
        if (!findBgMap()) {
            Log.outError(LogFilter.Battleground, String.format("Battleground::HandleTriggerBuff called with null bg map, %1$s", goGuid));

            return;
        }

        var obj = getBgMap().getGameObject(goGuid);

        if (!obj || obj.getGoType() != GameObjectTypes.trap || !obj.isSpawned()) {
            return;
        }

        // Change buff type, when buff is used:
        var index = bgObjects.length - 1;

        while (index >= 0 && ObjectGuid.opNotEquals(BgObjects[index], goGuid)) {
            index--;
        }

        if (index < 0) {
            Log.outError(LogFilter.Battleground, String.format("Battleground.HandleTriggerBuff: cannot find buff gameobject (%1$s, entry: %2$s, type: %3$s) in internal data for BG (map: %4$s, instance id: %5$s)!", goGuid, obj.getEntry(), obj.getGoType(), getMapId(), m_InstanceID));

            return;
        }

        // Randomly select new buff
        var buff = RandomUtil.IRand(0, 2);
        var entry = obj.getEntry();

        if (m_BuffChange && entry != Buff_Entries[buff]) {
            // Despawn current buff
            spawnBGObject(index, BattlegroundConst.RespawnOneDay);

            // Set index for new one
            for (byte currBuffTypeIndex = 0; currBuffTypeIndex < 3; ++currBuffTypeIndex) {
                if (entry == Buff_Entries[currBuffTypeIndex]) {
                    index -= currBuffTypeIndex;
                    index += buff;
                }
            }
        }

        spawnBGObject(index, BattlegroundConst.BuffRespawnTime);
    }

    public void handleKillPlayer(Player victim, Player killer) {
        // Keep in mind that for arena this will have to be changed a bit

        // Add +1 deaths
        updatePlayerScore(victim, ScoreType.deaths, 1);

        // Add +1 kills to group and +1 killing_blows to killer
        if (killer) {
            // Don't reward credit for killing ourselves, like fall damage of hellfire (warlock)
            if (killer == victim) {
                return;
            }

            var killerTeam = getPlayerTeam(killer.getGUID());

            updatePlayerScore(killer, ScoreType.honorableKills, 1);
            updatePlayerScore(killer, ScoreType.killingBlows, 1);


            for (var(guid, player) : m_Players) {
                var creditedPlayer = global.getObjAccessor().findPlayer(guid);

                if (!creditedPlayer || creditedPlayer == killer) {
                    continue;
                }

                if (player.team == killerTeam && creditedPlayer.isAtGroupRewardDistance(victim)) {
                    updatePlayerScore(creditedPlayer, ScoreType.honorableKills, 1);
                }
            }
        }

        if (!isArena()) {
            // To be able to remove insignia -- ONLY IN Battlegrounds
            victim.setUnitFlag(UnitFlag.Skinnable);
            rewardXPAtKill(killer, victim);
        }
    }

    public void handleKillUnit(Creature creature, Player killer) {
    }

    // Return the player's team based on Battlegroundplayer info
    // Used in same faction arena matches mainly
    public final Team getPlayerTeam(ObjectGuid guid) {
        var player = m_Players.get(guid);

        if (player != null) {
            return player.team;
        }

        return 0;
    }

    public final Team getOtherTeam(Team teamId) {
        switch (teamId) {
            case Alliance:
                return Team.Horde;
            case Horde:
                return Team.ALLIANCE;
            default:
                return Team.other;
        }
    }

    public final boolean isPlayerInBattleground(ObjectGuid guid) {
        return m_Players.containsKey(guid);
    }

    public final boolean isPlayerMercenaryInBattleground(ObjectGuid guid) {
        var player = m_Players.get(guid);

        if (player != null) {
            return player.mercenary;
        }

        return false;
    }

    public final int getAlivePlayersCountByTeam(Team team) {
        int count = 0;

        for (var pair : m_Players.entrySet()) {
            if (pair.getValue().team == team) {
                var player = global.getObjAccessor().findPlayer(pair.getKey());

                if (player && player.isAlive()) {
                    ++count;
                }
            }
        }

        return count;
    }

    public final void setHoliday(boolean is_holiday) {
        m_HonorMode = is_holiday ? BGHonorMode.Holiday : BGHonorMode.NORMAL;
    }

    public WorldSafeLocsEntry getClosestGraveYard(Player player) {
        return global.getObjectMgr().getClosestGraveYard(player.getLocation(), getPlayerTeam(player.getGUID()), player);
    }

    @Override
    public void triggerGameEvent(int gameEventId, WorldObject source) {
        triggerGameEvent(gameEventId, source, null);
    }

    @Override
    public void triggerGameEvent(int gameEventId) {
        triggerGameEvent(gameEventId, null, null);
    }

    @Override
    public void triggerGameEvent(int gameEventId, WorldObject source, WorldObject target) {
        processEvent(target, gameEventId, source);
        GameEvents.triggerForMap(gameEventId, getBgMap(), source, target);

        for (var guid : getPlayers().keySet()) {
            var player = global.getObjAccessor().findPlayer(guid);

            if (player) {
                GameEvents.triggerForPlayer(gameEventId, player);
            }
        }
    }

    public final void setBracket(PvpDifficultyRecord bracketEntry) {
        pvpDifficultyEntry = bracketEntry;
    }

    public final int getTeamScore(int teamIndex) {
        if (teamIndex == TeamId.ALLIANCE || teamIndex == TeamId.HORDE) {
            return m_TeamScores[teamIndex];
        }

        Log.outError(LogFilter.Battleground, "GetTeamScore with wrong Team {0} for BG {1}", teamIndex, getTypeID());

        return 0;
    }

    public void handleAreaTrigger(Player player, int trigger, boolean entered) {
        Log.outDebug(LogFilter.Battleground, "Unhandled AreaTrigger {0} in Battleground {1}. Player coords (x: {2}, y: {3}, z: {4})", trigger, player.getLocation().getMapId(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
    }

    public boolean setupBattleground() {
        return true;
    }

    public final String getName() {
        return battlegroundTemplate.battlemasterEntry.name.charAt(global.getWorldMgr().getDefaultDbcLocale());
    }

    public final BattlegroundTypeId getTypeID() {
        return getTypeID(false);
    }

    public final BattlegroundTypeId getTypeID(boolean getRandom) {
        return getRandom ? m_RandomTypeID : battlegroundTemplate.id;
    }

    public final BattlegroundBracketId getBracketId() {
        return pvpDifficultyEntry.getBracketId();
    }

    public final int getMinLevel() {
        if (pvpDifficultyEntry != null) {
            return pvpDifficultyEntry.minLevel;
        }

        return battlegroundTemplate.getMinLevel();
    }

    public final int getMaxLevel() {
        if (pvpDifficultyEntry != null) {
            return pvpDifficultyEntry.maxLevel;
        }

        return battlegroundTemplate.getMaxLevel();
    }

    public final int getMaxPlayersPerTeam() {
        if (isArena()) {
            switch (getArenaType()) {
                case Team2v2:
                    return 2;
                case Team3v3:
                    return 3;
                case Team5v5: // removed
                    return 5;
                default:
                    break;
            }
        }

        return battlegroundTemplate.getMaxPlayersPerTeam();
    }

    public final int getMinPlayersPerTeam() {
        return battlegroundTemplate.getMinPlayersPerTeam();
    }

    public void startingEventCloseDoors() {
    }

    public void startingEventOpenDoors() {
    }

    public void destroyGate(Player player, GameObject go) {
    }

    public final BattlegroundQueueTypeId getQueueId() {
        return m_queueId;
    }

    public final void setQueueId(BattlegroundQueueTypeId queueId) {
        m_queueId = queueId;
    }

    public final int getInstanceID() {
        return m_InstanceID;
    }

    //here we can count minlevel and maxlevel for players
    public final void setInstanceID(int instanceID) {
        m_InstanceID = instanceID;
    }

    public final BattlegroundStatus getStatus() {
        return m_Status;
    }

    public final void setStatus(BattlegroundStatus status) {
        m_Status = status;
    }


//	public static implicit operator bool(Battleground bg)
//		{
//			return bg != null;
//		}

    public final int getClientInstanceID() {
        return m_ClientInstanceID;
    }

    public final void setClientInstanceID(int instanceID) {
        m_ClientInstanceID = instanceID;
    }

    public final int getElapsedTime() {
        return m_StartTime;
    }

    public final void setElapsedTime(int time) {
        m_StartTime = time;
    }

    public final int getRemainingTime() {
        return (int) m_EndTime;
    }

    public final void setRemainingTime(int time) {
        m_EndTime = (int) time;
    }

    public final int getLastResurrectTime() {
        return m_LastResurrectTime;
    }

    public final void setLastResurrectTime(int time) {
        m_LastResurrectTime = time;
    }

    public final ArenaTypes getArenaType() {
        return m_ArenaType;
    }

    public final void setArenaType(ArenaTypes type) {
        m_ArenaType = type;
    }

    public final boolean isRandom() {
        return m_IsRandom;
    }

    public final void setRandom(boolean isRandom) {
        m_IsRandom = isRandom;
    }

    public final void setRandomTypeID(BattlegroundTypeId typeID) {
        m_RandomTypeID = typeID;
    }

    public final void decreaseInvitedCount(Team team) {
        if (team == Team.ALLIANCE) {
            --m_InvitedAlliance;
        } else {
            --m_InvitedHorde;
        }
    }

    public final void increaseInvitedCount(Team team) {
        if (team == Team.ALLIANCE) {
            ++m_InvitedAlliance;
        } else {
            ++m_InvitedHorde;
        }
    }

    public final boolean isRated() {
        return m_IsRated;
    }

    public final void setRated(boolean state) {
        m_IsRated = state;
    }

    public final HashMap<ObjectGuid, BattlegroundPlayer> getPlayers() {
        return m_Players;
    }

    public final int getPlayersCountByTeam(Team team) {
        return m_PlayersCount[GetTeamIndexByTeamId(team)];
    }

    public void checkWinConditions() {
    }

    public final void setArenaTeamIdForTeam(Team team, int arenaTeamId) {
        m_ArenaTeamIds[GetTeamIndexByTeamId(team)] = arenaTeamId;
    }

    public final int getArenaTeamIdForTeam(Team team) {
        return m_ArenaTeamIds[GetTeamIndexByTeamId(team)];
    }

    public final int getArenaTeamIdByIndex(int index) {
        return m_ArenaTeamIds[index];
    }

    public final void setArenaMatchmakerRating(Team team, int MMR) {
        m_ArenaTeamMMR[GetTeamIndexByTeamId(team)] = MMR;
    }

    public final int getArenaMatchmakerRating(Team team) {
        return m_ArenaTeamMMR[GetTeamIndexByTeamId(team)];
    }

    // Battleground events
    public void eventPlayerDroppedFlag(Player player) {
    }

    public void eventPlayerClickedOnFlag(Player player, GameObject target_obj) {
    }

    @Override
    public void processEvent(WorldObject obj, int eventId) {
        processEvent(obj, eventId, null);
    }

    @Override
    public void processEvent(WorldObject obj, int eventId, WorldObject invoker) {
    }

    // this function can be used by spell to interact with the BG map
    public void doAction(int action, long arg) {
    }

    public void handlePlayerResurrect(Player player) {
    }

    public WorldSafeLocsEntry getExploitTeleportLocation(Team team) {
        return null;
    }

    public boolean handlePlayerUnderMap(Player player) {
        return false;
    }

    public final boolean toBeDeleted() {
        return m_SetDeleteThis;
    }

    public ObjectGuid getFlagPickerGUID() {
        return getFlagPickerGUID(-1);
    }

    public ObjectGuid getFlagPickerGUID(int teamIndex) {
        return ObjectGuid.Empty;
    }

    public void setDroppedFlagGUID(ObjectGuid guid) {
        setDroppedFlagGUID(guid, -1);
    }

    public void setDroppedFlagGUID(ObjectGuid guid, int teamIndex) {
    }

    public void handleQuestComplete(int questid, Player player) {
    }


    ///#region Fields

    public boolean canActivateGO(int entry, int team) {
        return true;
    }
    // Player lists, those need to be accessible by inherited classes

    public boolean isSpellAllowed(int spellId, Player player) {
        return true;
    }

    public void removePlayer(Player player, ObjectGuid guid, Team team) {
    }

    public boolean preUpdateImpl(int diff) {
        return true;
    }

    public void postUpdateImpl(int diff) {
    }

    private void _CheckSafePositions(int diff) {
        var maxDist = getStartMaxDist();

        if (maxDist == 0.0f) {
            return;
        }

        m_ValidStartPositionTimer += diff;

        if (m_ValidStartPositionTimer >= BattlegroundConst.CheckPlayerPositionInverval) {
            m_ValidStartPositionTimer = 0;

            for (var guid : getPlayers().keySet()) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    if (player.isGameMaster()) {
                        continue;
                    }

                    Position pos = player.getLocation();
                    var startPos = getTeamStartPosition(getTeamIndexByTeamId(player.getBgTeam()));

                    if (pos.getExactDistSq(startPos.loc) > maxDist) {
                        Log.outDebug(LogFilter.Battleground, String.format("Battleground: Sending %1$s back to start location (map: %2$s) (possible exploit)", player.getName(), getMapId()));
                        player.teleportTo(startPos.loc);
                    }
                }
            }
        }
    }

    private void _ProcessPlayerPositionBroadcast(int diff) {
        m_LastPlayerPositionBroadcast += diff;

        if (m_LastPlayerPositionBroadcast >= BattlegroundConst.PlayerPositionUpdateInterval) {
            m_LastPlayerPositionBroadcast = 0;

            BattlegroundPlayerPositions playerPositions = new BattlegroundPlayerPositions();

            for (var i = 0; i < playerPositions.size(); ++i) {
                var playerPosition = playerPositions.get(i);
                // Update position data if we found player.
                var player = global.getObjAccessor().getPlayer(getBgMap(), playerPosition.guid);

                if (player != null) {
                    playerPosition.pos = player.getLocation();
                }

                playerPositions.flagCarriers.add(playerPosition);
            }

            sendPacketToAll(playerPositions);
        }
    }

    private void _ProcessOfflineQueue() {
        // remove offline players from bg after 5 time.Minutes
        if (!m_OfflineQueue.isEmpty()) {
            var guid = m_OfflineQueue.FirstOrDefault();
            var bgPlayer = m_Players.get(guid);

            if (bgPlayer != null) {
                if (bgPlayer.offlineRemoveTime <= gameTime.GetGameTime()) {
                    removePlayerAtLeave(guid, true, true); // remove player from BG
                    m_OfflineQueue.remove(0); // remove from offline queue
                }
            }
        }
    }

    private void _ProcessRessurect(int diff) {
        // *********************************************************
        // ***        Battleground RESSURECTION SYSTEM           ***
        // *********************************************************
        // this should be handled by spell system
        m_LastResurrectTime += diff;

        if (m_LastResurrectTime >= BattlegroundConst.ResurrectionInterval) {
            if (getReviveQueueSize() != 0) {
                Creature sh = null;

                for (var pair : m_ReviveQueue.KeyValueList) {
                    var player = global.getObjAccessor().findPlayer(pair.value);

                    if (!player) {
                        continue;
                    }

                    if (!sh && player.isInWorld()) {
                        sh = player.getMap().getCreature(pair.key);

                        // only for visual effect
                        if (sh) {
                            // Spirit Heal, effect 117
                            sh.castSpell(sh, BattlegroundConst.SpellSpiritHeal, true);
                        }
                    }

                    // Resurrection visual
                    player.castSpell(player, BattlegroundConst.SpellResurrectionVisual, true);
                    m_ResurrectQueue.add(pair.value);
                }

                m_ReviveQueue.clear();
                m_LastResurrectTime = 0;
            } else {
                // queue is clear and time passed, just update last resurrection time
                m_LastResurrectTime = 0;
            }
        } else if (m_LastResurrectTime > 500) // Resurrect players only half a second later, to see spirit heal effect on NPC
        {
            for (var guid : m_ResurrectQueue) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (!player) {
                    continue;
                }

                player.resurrectPlayer(1.0f);
                player.castSpell(player, 6962, true);
                player.castSpell(player, BattlegroundConst.SpellSpiritHealMana, true);
                player.spawnCorpseBones(false);
            }

            m_ResurrectQueue.clear();
        }
    }

    private void _ProcessProgress(int diff) {
        // *********************************************************
        // ***           Battleground BALLANCE SYSTEM            ***
        // *********************************************************
        // if less then minimum players are in on one side, then start premature finish timer
        if (!m_PrematureCountDown) {
            m_PrematureCountDown = true;
            m_PrematureCountDownTimer = global.getBattlegroundMgr().getPrematureFinishTime();
        } else if (m_PrematureCountDownTimer < diff) {
            // time's up!
            endBattleground(getPrematureWinner());
            m_PrematureCountDown = false;
        } else if (!global.getBattlegroundMgr().isTesting()) {
            var newtime = m_PrematureCountDownTimer - diff;

            // announce every time.Minute
            if (newtime > (time.Minute * time.InMilliseconds)) {
                if (newtime / (time.Minute * time.InMilliseconds) != m_PrematureCountDownTimer / (time.Minute * time.InMilliseconds)) {
                    sendMessageToAll(SysMessage.BattlegroundPrematureFinishWarning, ChatMsg.System, null, m_PrematureCountDownTimer / (time.Minute * time.InMilliseconds));
                }
            } else {
                //announce every 15 seconds
                if (newtime / (15 * time.InMilliseconds) != m_PrematureCountDownTimer / (15 * time.InMilliseconds)) {
                    sendMessageToAll(SysMessage.BattlegroundPrematureFinishWarningSecs, ChatMsg.System, null, m_PrematureCountDownTimer / time.InMilliseconds);
                }
            }

            m_PrematureCountDownTimer = newtime;
        }
    }

    private void _ProcessJoin(int diff) {
        // *********************************************************
        // ***           Battleground STARTING SYSTEM            ***
        // *********************************************************
        modifyStartDelayTime((int) diff);

        if (!isArena()) {
            setRemainingTime(300000);
        }

        if (m_ResetStatTimer > 5000) {
            m_ResetStatTimer = 0;

            for (var guid : getPlayers().keySet()) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.resetAllPowers();
                }
            }
        }

        // Send packet every 10 seconds until the 2nd field reach 0
        if (m_CountdownTimer >= 10000) {
            var countdownMaxForBGType = isArena() ? BattlegroundConst.ArenaCountdownMax : BattlegroundConst.BattlegroundCountdownMax;

            StartTimer timer = new startTimer();
            timer.type = TimerType.Pvp;
            timer.timeLeft = countdownMaxForBGType - (getElapsedTime() / 1000);
            timer.totalTime = countdownMaxForBGType;

            for (var guid : getPlayers().keySet()) {
                var player = global.getObjAccessor().findPlayer(guid);

                if (player) {
                    player.sendPacket(timer);
                }
            }

            m_CountdownTimer = 0;
        }

        if (!m_Events.hasFlag(BattlegroundEventFlags.Event1)) {
            m_Events = BattlegroundEventFlags.forValue(m_Events.getValue() | BattlegroundEventFlags.Event1.getValue());

            if (!findBgMap()) {
                Log.outError(LogFilter.Battleground, String.format("Battleground._ProcessJoin: map (map id: %1$s, instance id: %2$s) is not created!", getMapId(), m_InstanceID));
                endNow();

                return;
            }

            // Setup here, only when at least one player has ported to the map
            if (!setupBattleground()) {
                endNow();

                return;
            }

            startingEventCloseDoors();
            setStartDelayTime(StartDelayTimes[BattlegroundConst.EventIdFirst]);

            // First start warning - 2 or 1 Minute
            if (StartMessageIds[BattlegroundConst.EventIdFirst] != 0) {
                sendBroadcastText(StartMessageIds[BattlegroundConst.EventIdFirst], ChatMsg.BgSystemNeutral);
            }
        }
        // After 1 time.Minute or 30 seconds, warning is signaled
        else if (getStartDelayTime() <= (int) StartDelayTimes[BattlegroundConst.EventIdSecond] && !m_Events.hasFlag(BattlegroundEventFlags.Event2)) {
            m_Events = BattlegroundEventFlags.forValue(m_Events.getValue() | BattlegroundEventFlags.Event2.getValue());

            if (StartMessageIds[BattlegroundConst.EventIdSecond] != 0) {
                sendBroadcastText(StartMessageIds[BattlegroundConst.EventIdSecond], ChatMsg.BgSystemNeutral);
            }
        }
        // After 30 or 15 seconds, warning is signaled
        else if (getStartDelayTime() <= (int) StartDelayTimes[BattlegroundConst.EventIdThird] && !m_Events.hasFlag(BattlegroundEventFlags.Event3)) {
            m_Events = BattlegroundEventFlags.forValue(m_Events.getValue() | BattlegroundEventFlags.Event3.getValue());

            if (StartMessageIds[BattlegroundConst.EventIdThird] != 0) {
                sendBroadcastText(StartMessageIds[BattlegroundConst.EventIdThird], ChatMsg.BgSystemNeutral);
            }
        }
        // Delay expired (after 2 or 1 time.Minute)
        else if (getStartDelayTime() <= 0 && !m_Events.hasFlag(BattlegroundEventFlags.Event4)) {
            m_Events = BattlegroundEventFlags.forValue(m_Events.getValue() | BattlegroundEventFlags.Event4.getValue());

            startingEventOpenDoors();

            if (StartMessageIds[BattlegroundConst.EventIdFourth] != 0) {
                sendBroadcastText(StartMessageIds[BattlegroundConst.EventIdFourth], ChatMsg.RaidBossEmote);
            }

            setStatus(BattlegroundStatus.inProgress);
            setStartDelayTime(StartDelayTimes[BattlegroundConst.EventIdFourth]);

            // Remove preparation
            if (isArena()) {
                //todo add arena sound playSoundToAll(SOUND_ARENA_START);
                for (var guid : getPlayers().keySet()) {
                    var player = global.getObjAccessor().findPlayer(guid);

                    if (player) {
                        // Correctly display EnemyUnitFrame
                        player.setArenaFaction((byte) player.getBgTeam().getValue());

                        player.removeAura(BattlegroundConst.SpellArenaPreparation);
                        player.resetAllPowers();

                        if (!player.isGameMaster()) {
                            // remove auras with duration lower than 30s
                            player.getAppliedAurasQuery().isPermanent(false).isPositive().alsoMatches(aurApp ->
                            {
                                var aura = aurApp.base;

                                return aura.duration <= 30 * time.InMilliseconds && !aura.spellInfo.hasAttribute(SpellAttr0.NoImmunities) && !aura.hasEffectType(AuraType.ModInvisibility);
                            }).execute(player::RemoveAura);
                        }
                    }
                }

                checkWinConditions();
            } else {
                playSoundToAll((int) BattlegroundSounds.BgStart.getValue());

                for (var guid : getPlayers().keySet()) {
                    var player = global.getObjAccessor().findPlayer(guid);

                    if (player) {
                        player.removeAura(BattlegroundConst.SpellPreparation);
                        player.resetAllPowers();
                    }
                }

                // Announce BG starting
                if (WorldConfig.getBoolValue(WorldCfg.BattlegroundQueueAnnouncerEnable)) {
                    global.getWorldMgr().sendWorldText(SysMessage.BgStartedAnnounceWorld, getName(), getMinLevel(), getMaxLevel());
                }
            }
        }

        if (getRemainingTime() > 0 && (m_EndTime -= (int) diff) > 0) {
            setRemainingTime(getRemainingTime() - diff);
        }
    }

    private void _ProcessLeave(int diff) {
        // *********************************************************
        // ***           Battleground ENDING SYSTEM              ***
        // *********************************************************
        // remove all players from Battleground after 2 time.Minutes
        setRemainingTime(getRemainingTime() - diff);

        if (getRemainingTime() <= 0) {
            setRemainingTime(0);

            for (var guid : m_Players.keySet()) {
                removePlayerAtLeave(guid, true, true); // remove player from BG
            }
            // do not change any Battleground's private variables
        }
    }

    private Player _GetPlayerForTeam(Team teamId, java.util.Map.entry<ObjectGuid, BattlegroundPlayer> pair, String context) {
        var player = _GetPlayer(pair, context);

        if (player) {
            var team = pair.getValue().team;

            if (team == 0) {
                team = player.getEffectiveTeam();
            }

            if (team != teamId) {
                player = null;
            }
        }

        return player;
    }

    private float getStartMaxDist() {
        return battlegroundTemplate.maxStartDistSq;
    }

    private void sendPacketToTeam(Team team, ServerPacket packet) {
        sendPacketToTeam(team, packet, null);
    }

    private void sendPacketToTeam(Team team, ServerPacket packet, Player except) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayerForTeam(team, pair, "SendPacketToTeam");

            if (player) {
                if (player != except) {
                    player.sendPacket(packet);
                }
            }
        }
    }

    private void playSoundToTeam(int soundID, Team team) {
        sendPacketToTeam(team, new playSound(ObjectGuid.Empty, soundID, 0));
    }

    private void removeAuraOnTeam(int spellID, Team team) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayerForTeam(team, pair, "RemoveAuraOnTeam");

            if (player) {
                player.removeAura(spellID);
            }
        }
    }

    private int getScriptId() {
        return battlegroundTemplate.scriptId;
    }

    private void blockMovement(Player player) {
        // movement disabled NOTE: the effect will be automatically removed by client when the player is teleported from the battleground, so no need to send with uint8(1) in removePlayerAtLeave()
        player.setClientControl(player, false);
    }

    // This method should be called only once ... it adds pointer to queue
    private void addToBGFreeSlotQueue() {
        if (!m_InBGFreeSlotQueue && isBattleground()) {
            global.getBattlegroundMgr().addToBGFreeSlotQueue(getQueueId(), this);
            m_InBGFreeSlotQueue = true;
        }
    }

    private boolean removeObjectFromWorld(int type) {
        if (BgObjects[type].isEmpty()) {
            return true;
        }

        var obj = getBgMap().getGameObject(BgObjects[type]);

        if (obj != null) {
            obj.removeFromWorld();
            BgObjects[type].clear();

            return true;
        }

        Log.outInfo(LogFilter.Battleground, String.format("Battleground::RemoveObjectFromWorld: gameobject (type: %1$s, %2$s) not found for BG (map: %3$s, instance id: %4$s)!", type, BgObjects[type], getMapId(), m_InstanceID));

        return false;
    }

    private void endNow() {
        removeFromBGFreeSlotQueue();
        setStatus(BattlegroundStatus.WaitLeave);
        setRemainingTime(0);
    }

    private void playerAddedToBGCheckIfBGIsRunning(Player player) {
        if (getStatus() != BattlegroundStatus.WaitLeave) {
            return;
        }

        blockMovement(player);

        PVPMatchStatisticsMessage pvpMatchStatistics = new PVPMatchStatisticsMessage();
        tangible.OutObject<PVPMatchStatistics> tempOut_Data = new tangible.OutObject<PVPMatchStatistics>();
        buildPvPLogDataPacket(tempOut_Data);
        pvpMatchStatistics.data = tempOut_Data.outArgValue;
        player.sendPacket(pvpMatchStatistics);
    }

    private int getObjectType(ObjectGuid guid) {
        for (var i = 0; i < bgObjects.length; ++i) {
            if (Objects.equals(BgObjects[i], guid)) {
                return i;
            }
        }

        Log.outError(LogFilter.Battleground, String.format("Battleground.GetObjectType: player used gameobject (%1$s) which is not in internal data for BG (map: %2$s, instance id: %3$s), cheating?", guid, getMapId(), m_InstanceID));

        return -1;
    }

    private void setBgRaid(Team team, PlayerGroup bg_raid) {
        var old_raid = m_BgRaids[GetTeamIndexByTeamId(team)];

        if (old_raid) {
            old_raid.setBattlegroundGroup(null);
        }

        if (bg_raid) {
            bg_raid.setBattlegroundGroup(this);
        }

        m_BgRaids[GetTeamIndexByTeamId(team)] = bg_raid;
    }

    private void rewardXPAtKill(Player killer, Player victim) {
        if (WorldConfig.getBoolValue(WorldCfg.BgXpForKill) && killer && victim) {
            (new KillRewarder(new Player[]{killer}, victim, true)).reward();
        }
    }

    private byte getUniqueBracketId() {
        return (byte) ((getMinLevel() / 5) - 1); // 10 - 1, 15 - 2, 20 - 3, etc.
    }

    private int getMaxPlayers() {
        return getMaxPlayersPerTeam() * 2;
    }

    private int getMinPlayers() {
        return getMinPlayersPerTeam() * 2;
    }

    private int getStartDelayTime() {
        return m_StartDelayTime;
    }

    private void setStartDelayTime(BattlegroundStartTimeIntervals time) {
        m_StartDelayTime = time.getValue();
    }

    private PvPTeamId getWinner() {
        return winnerTeamId;
    }

    public final void setWinner(PvPTeamId winnerTeamId) {
        winnerTeamId = winnerTeamId;
    }

    private void modifyStartDelayTime(int diff) {
        m_StartDelayTime -= diff;
    }

    private int getInvitedCount(Team team) {
        return (team == Team.ALLIANCE) ? m_InvitedAlliance : m_InvitedHorde;
    }

    private int getPlayersSize() {
        return (int) m_Players.size();
    }

    private int getPlayerScoresSize() {
        return (int) playerScores.size();
    }

    private int getReviveQueueSize() {
        return (int) m_ReviveQueue.count;
    }

    private BattlegroundMap findBgMap() {
        return m_Map;
    }

    private PlayerGroup getBgRaid(Team team) {
        return m_BgRaids[GetTeamIndexByTeamId(team)];
    }

    private void updatePlayersCountByTeam(Team team, boolean remove) {
        if (remove) {
            --m_PlayersCount[GetTeamIndexByTeamId(team)];
        } else {
            ++m_PlayersCount[GetTeamIndexByTeamId(team)];
        }
    }

    private void setDeleteThis() {
        m_SetDeleteThis = true;
    }

    private boolean canAwardArenaPoints() {
        return getMinLevel() >= 71;
    }

    private void broadcastWorker(IDoWork<Player> _do) {
        for (var pair : m_Players.entrySet()) {
            var player = _GetPlayer(pair, "BroadcastWorker");

            if (player) {
                _do.invoke(player);
            }
        }
    }


    ///#endregion
}
