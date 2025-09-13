package com.github.azeroth.game.battleground;


import Framework.Threading.*;
import com.github.azeroth.game.domain.condition.DisableType;
import com.github.azeroth.game.arena.*;
import com.github.azeroth.game.battleground.zones.BgArathiBasin;
import com.github.azeroth.game.battleground.zones.BgEyeofStorm;
import com.github.azeroth.game.battleground.zones.BgStrandOfAncients;
import com.github.azeroth.game.battleground.zones.BgWarsongGluch;
import com.github.azeroth.game.entity.player.Player;

import java.util.ArrayList;
import java.util.HashMap;


public class BattlegroundManager {
    private final HashMap<BattlegroundTypeId, BattlegroundData> bgDataStore = new HashMap<BattlegroundTypeId, BattlegroundData>();
    private final HashMap<BattlegroundQueueTypeId, BattlegroundQueue> m_BattlegroundQueues = new HashMap<BattlegroundQueueTypeId, BattlegroundQueue>();
    private final MultiMap<BattlegroundQueueTypeId, Battleground> m_BGFreeSlotQueue = new MultiMap<BattlegroundQueueTypeId, Battleground>();
    private final HashMap<Integer, BattlegroundTypeId> mBattleMastersMap = new HashMap<Integer, BattlegroundTypeId>();
    private final HashMap<BattlegroundTypeId, BattlegroundTemplate> battlegroundTemplates = new HashMap<BattlegroundTypeId, BattlegroundTemplate>();
    private final HashMap<Integer, BattlegroundTemplate> battlegroundMapTemplates = new HashMap<Integer, BattlegroundTemplate>();
    private final limitedThreadTaskManager threadTaskManager = new limitedThreadTaskManager(ConfigMgr.GetDefaultValue("Map.ParellelUpdateTasks", 20));
    private ArrayList<ScheduledQueueUpdate> m_QueueUpdateScheduler = new ArrayList<>();
    private int m_NextRatedArenaUpdate;
    private int m_UpdateTimer;
    private boolean m_ArenaTesting;
    private boolean m_Testing;

    private BattlegroundManager() {
        m_NextRatedArenaUpdate = WorldConfig.getUIntValue(WorldCfg.ArenaRatedUpdateTimer);
    }

    public final void deleteAllBattlegrounds() {
        for (var data : bgDataStore.values().ToList()) {
            while (!data.m_Battlegrounds.isEmpty()) {
                data.m_Battlegrounds.first().value.dispose();
            }
        }

        bgDataStore.clear();

        for (var bg : m_BGFreeSlotQueue.VALUES.ToList()) {
            bg.dispose();
        }

        m_BGFreeSlotQueue.clear();
    }

    public final void update(int diff) {
        m_UpdateTimer += diff;

        if (m_UpdateTimer > 1000) {
            for (var data : bgDataStore.values()) {
                var bgs = data.m_Battlegrounds;

                // first one is template and should not be deleted
                for (var pair : bgs.ToList()) {
                    var bg = pair.value;

                    threadTaskManager.Schedule(() ->
                    {
                        bg.update(m_UpdateTimer);

                        if (bg.toBeDeleted()) {
                            bgs.remove(pair.key);
                            var clients = data.m_ClientBattlegroundIds[(int) bg.getBracketId()];

                            if (!clients.isEmpty()) {
                                clients.remove(bg.getClientInstanceID());
                            }

                            bg.dispose();
                        }
                    });
                }
            }

            threadTaskManager.Wait();
            m_UpdateTimer = 0;
        }

        // update events timer
        for (var pair : m_BattlegroundQueues.entrySet()) {
            threadTaskManager.Schedule(() -> pair.getValue().updateEvents(diff));
        }

        threadTaskManager.Wait();

        // update scheduled queues
        if (!m_QueueUpdateScheduler.isEmpty()) {
            ArrayList<ScheduledQueueUpdate> scheduled = new ArrayList<>();
            tangible.RefObject<T> tempRef_scheduled = new tangible.RefObject<T>(scheduled);
            tangible.RefObject<T> tempRef_m_QueueUpdateScheduler = new tangible.RefObject<T>(m_QueueUpdateScheduler);
            Extensions.Swap(tempRef_scheduled, tempRef_m_QueueUpdateScheduler);
            m_QueueUpdateScheduler = tempRef_m_QueueUpdateScheduler.refArgValue;
            scheduled = tempRef_scheduled.refArgValue;

            for (byte i = 0; i < scheduled.size(); i++) {
                var arenaMMRating = scheduled.get(i).arenaMatchmakerRating;
                var bgQueueTypeId = scheduled.get(i).queueId;
                var bracket_id = scheduled.get(i).bracketId;
                getBattlegroundQueue(bgQueueTypeId).battlegroundQueueUpdate(diff, bracket_id, arenaMMRating);
            }
        }

        // if rating difference counts, maybe force-update queues
        if (WorldConfig.getIntValue(WorldCfg.ArenaMaxRatingDifference) != 0 && WorldConfig.getIntValue(WorldCfg.ArenaRatedUpdateTimer) != 0) {
            // it's time to force update
            if (m_NextRatedArenaUpdate < diff) {
                // forced update for rated arenas (scan all, but skipped non rated)
                Log.outDebug(LogFilter.Arena, "BattlegroundMgr: UPDATING ARENA QUEUES");

                for (var teamSize : new ArenaTypes[]{ArenaTypes.Team2v2, ArenaTypes.Team3v3, ArenaTypes.Team5v5}) {
                    var ratedArenaQueueId = BGQueueTypeId((short) BattlegroundTypeId.AA.getValue(), BattlegroundQueueIdType.Arena, true, teamSize);

                    for (var bracket = BattlegroundBracketId.first; bracket.getValue() < BattlegroundBracketId.max.getValue(); ++bracket) {
                        getBattlegroundQueue(ratedArenaQueueId).battlegroundQueueUpdate(diff, bracket, 0);
                    }
                }

                m_NextRatedArenaUpdate = WorldConfig.getUIntValue(WorldCfg.ArenaRatedUpdateTimer);
            } else {
                m_NextRatedArenaUpdate -= diff;
            }
        }
    }

    public final void buildBattlegroundStatusNone(tangible.OutObject<BattlefieldStatusNone> battlefieldStatus, Player player, int ticketId, int joinTime) {
        battlefieldStatus.outArgValue = new BattlefieldStatusNone();
        battlefieldStatus.outArgValue.ticket.requesterGuid = player.getGUID();
        battlefieldStatus.outArgValue.ticket.id = ticketId;
        battlefieldStatus.outArgValue.ticket.type = RideType.Battlegrounds;
        battlefieldStatus.outArgValue.ticket.time = (int) joinTime;
    }

    public final void buildBattlegroundStatusNeedConfirmation(tangible.OutObject<BattlefieldStatusNeedConfirmation> battlefieldStatus, Battleground bg, Player player, int ticketId, int joinTime, int timeout, ArenaTypes arenaType) {
        battlefieldStatus.outArgValue = new BattlefieldStatusNeedConfirmation();
        buildBattlegroundStatusHeader(battlefieldStatus.outArgValue.hdr, bg, player, ticketId, joinTime, bg.getQueueId(), arenaType);
        battlefieldStatus.outArgValue.mapid = bg.getMapId();
        battlefieldStatus.outArgValue.timeout = timeout;
        battlefieldStatus.outArgValue.role = 0;
    }

    public final void buildBattlegroundStatusActive(tangible.OutObject<BattlefieldStatusActive> battlefieldStatus, Battleground bg, Player player, int ticketId, int joinTime, ArenaTypes arenaType) {
        battlefieldStatus.outArgValue = new BattlefieldStatusActive();
        buildBattlegroundStatusHeader(battlefieldStatus.outArgValue.hdr, bg, player, ticketId, joinTime, bg.getQueueId(), arenaType);
        battlefieldStatus.outArgValue.shutdownTimer = bg.getRemainingTime();
        battlefieldStatus.outArgValue.arenaFaction = (byte) (player.getBgTeam() == Team.Horde ? TeamId.HORDE : TeamId.ALLIANCE);
        battlefieldStatus.outArgValue.leftEarly = false;
        battlefieldStatus.outArgValue.startTimer = bg.getElapsedTime();
        battlefieldStatus.outArgValue.mapid = bg.getMapId();
    }

    public final void buildBattlegroundStatusQueued(tangible.OutObject<BattlefieldStatusQueued> battlefieldStatus, Battleground bg, Player player, int ticketId, int joinTime, BattlegroundQueueTypeId queueId, int avgWaitTime, ArenaTypes arenaType, boolean asGroup) {
        battlefieldStatus.outArgValue = new BattlefieldStatusQueued();
        buildBattlegroundStatusHeader(battlefieldStatus.outArgValue.hdr, bg, player, ticketId, joinTime, queueId, arenaType);
        battlefieldStatus.outArgValue.averageWaitTime = avgWaitTime;
        battlefieldStatus.outArgValue.asGroup = asGroup;
        battlefieldStatus.outArgValue.suspendedQueue = false;
        battlefieldStatus.outArgValue.eligibleForMatchmaking = true;
        battlefieldStatus.outArgValue.waitTime = time.GetMSTimeDiffToNow(joinTime);
    }


    public final void buildBattlegroundStatusFailed(tangible.OutObject<BattlefieldStatusFailed> battlefieldStatus, BattlegroundQueueTypeId queueId, Player pPlayer, int ticketId, GroupJoinBattlegroundResult result) {
        buildBattlegroundStatusFailed(battlefieldStatus, queueId, pPlayer, ticketId, result, null);
    }

    public final void buildBattlegroundStatusFailed(tangible.OutObject<BattlefieldStatusFailed> battlefieldStatus, BattlegroundQueueTypeId queueId, Player pPlayer, int ticketId, GroupJoinBattlegroundResult result, ObjectGuid errorGuid) {
        battlefieldStatus.outArgValue = new BattlefieldStatusFailed();
        battlefieldStatus.outArgValue.ticket.requesterGuid = pPlayer.getGUID();
        battlefieldStatus.outArgValue.ticket.id = ticketId;
        battlefieldStatus.outArgValue.ticket.type = RideType.Battlegrounds;
        battlefieldStatus.outArgValue.ticket.time = (int) pPlayer.getBattlegroundQueueJoinTime(queueId);
        battlefieldStatus.outArgValue.queueID = queueId.getPacked();
        battlefieldStatus.outArgValue.reason = result.getValue();

        if (!errorGuid.isEmpty() && (result == GroupJoinBattlegroundResult.NotInBattleground || result == GroupJoinBattlegroundResult.JoinTimedOut)) {
            battlefieldStatus.outArgValue.clientID = errorGuid;
        }
    }

    public final Battleground getBattleground(int instanceId, BattlegroundTypeId bgTypeId) {
        if (instanceId == 0) {
            return null;
        }

        if (bgTypeId != BattlegroundTypeId.NONE || bgTypeId == BattlegroundTypeId.RB || bgTypeId == BattlegroundTypeId.RandomEpic) {
            var data = bgDataStore.get(bgTypeId);

            return data.m_Battlegrounds.get(instanceId);
        }

        for (var it : bgDataStore.entrySet()) {
            var bgs = it.getValue().m_Battlegrounds;
            var bg = bgs.get(instanceId);

            if (bg) {
                return bg;
            }
        }

        return null;
    }

    public final Battleground getBattlegroundTemplate(BattlegroundTypeId bgTypeId) {
        if (bgDataStore.containsKey(bgTypeId)) {
            return bgDataStore.get(bgTypeId).template;
        }

        return null;
    }

    // create a new Battleground that will really be used to play
    public final Battleground createNewBattleground(BattlegroundQueueTypeId queueId, PvpDifficultyRecord bracketEntry) {
        var bgTypeId = getRandomBG(BattlegroundTypeId.forValue(queueId.battlemasterListId));

        // get the template BG
        var bg_template = getBattlegroundTemplate(bgTypeId);

        if (bg_template == null) {
            Log.outError(LogFilter.Battleground, "Battleground: CreateNewBattleground - bg template not found for {0}", bgTypeId);

            return null;
        }

        if (bgTypeId == BattlegroundTypeId.RB || bgTypeId == BattlegroundTypeId.AA || bgTypeId == BattlegroundTypeId.RandomEpic) {
            return null;
        }

        // create a copy of the BG template
        var bg = bg_template.getCopy();

        var isRandom = bgTypeId != BattlegroundTypeId.forValue(queueId.battlemasterListId) && !bg.isArena();

        bg.setQueueId(queueId);
        bg.setBracket(bracketEntry);
        bg.setInstanceID(global.getMapMgr().GenerateInstanceId());
        bg.setClientInstanceID(createClientVisibleInstanceId(BattlegroundTypeId.forValue(queueId.battlemasterListId), bracketEntry.getBracketId()));
        bg.reset(); // reset the new bg (set status to status_wait_queue from status_none)
        bg.setStatus(BattlegroundStatus.WaitJoin); // start the joining of the bg
        bg.setArenaType(ArenaTypes.forValue(queueId.teamSize));
        bg.setRandomTypeID(bgTypeId);
        bg.setRated(queueId.rated);
        bg.setRandom(isRandom);

        return bg;
    }

    public final void loadBattlegroundTemplates() {
        var oldMSTime = System.currentTimeMillis();

        //                                         0   1                 2              3             4       5
        var result = DB.World.query("SELECT ID, AllianceStartLoc, HordeStartLoc, StartMaxDist, weight, ScriptName FROM battleground_template");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 Battlegrounds. DB table `Battleground_template` is empty.");

            return;
        }

        int count = 0;

        do {
            var bgTypeId = BattlegroundTypeId.forValue(result.<Integer>Read(0));

            if (global.getDisableMgr().isDisabledFor(DisableType.Battleground, (int) bgTypeId.getValue(), null)) {
                continue;
            }

            // can be overwrite by values from DB
            var bl = CliDB.BattlemasterListStorage.get(bgTypeId);

            if (bl == null) {
                Log.outError(LogFilter.Battleground, "Battleground ID {0} not found in BattlemasterList.dbc. Battleground not created.", bgTypeId);

                continue;
            }

            BattlegroundTemplate bgTemplate = new BattlegroundTemplate();
            bgTemplate.id = bgTypeId;
            var dist = result.<Float>Read(3);
            bgTemplate.maxStartDistSq = dist * dist;
            bgTemplate.weight = result.<Byte>Read(4);

            bgTemplate.scriptId = global.getObjectMgr().getScriptId(result.<String>Read(5));
            bgTemplate.battlemasterEntry = bl;

            if (bgTemplate.id != BattlegroundTypeId.AA && bgTemplate.id != BattlegroundTypeId.RB && bgTemplate.id != BattlegroundTypeId.RandomEpic) {
                var startId = result.<Integer>Read(1);
                var start = global.getObjectMgr().getWorldSafeLoc(startId);

                if (start != null) {
                    bgTemplate.StartLocation[TeamId.ALLIANCE] = start;
                } else if (bgTemplate.StartLocation[TeamId.ALLIANCE] != null) // reload case
                {
                    Logs.SQL.error(String.format("Table `battleground_template` for id %1$s contains a non-existing WorldSafeLocs.dbc id %2$s in field `AllianceStartLoc`. Ignoring.", bgTemplate.id, startId));
                } else {
                    Logs.SQL.error(String.format("Table `Battleground_template` for Id %1$s has a non-existed WorldSafeLocs.dbc id %2$s in field `AllianceStartLoc`. BG not created.", bgTemplate.id, startId));

                    continue;
                }

                startId = result.<Integer>Read(2);
                start = global.getObjectMgr().getWorldSafeLoc(startId);

                if (start != null) {
                    bgTemplate.StartLocation[TeamId.HORDE] = start;
                } else if (bgTemplate.StartLocation[TeamId.HORDE] != null) // reload case
                {
                    Logs.SQL.error(String.format("Table `battleground_template` for id %1$s contains a non-existing WorldSafeLocs.dbc id %2$s in field `HordeStartLoc`. Ignoring.", bgTemplate.id, startId));
                } else {
                    Logs.SQL.error(String.format("Table `Battleground_template` for Id %1$s has a non-existed WorldSafeLocs.dbc id %2$s in field `HordeStartLoc`. BG not created.", bgTemplate.id, startId));

                    continue;
                }
            }

            if (!createBattleground(bgTemplate)) {
                Log.outError(LogFilter.Battleground, String.format("Could not create battleground template class (%1$s)!", bgTemplate.id));

                continue;
            }

            battlegroundTemplates.put(bgTypeId, bgTemplate);

            if (bgTemplate.battlemasterEntry.MapId[1] == -1) // in this case we have only one mapId
            {
                battlegroundMapTemplates.put((int) bgTemplate.battlemasterEntry.MapId[0], battlegroundTemplates.get(bgTypeId));
            }

            ++count;
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} Battlegrounds in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void sendBattlegroundList(Player player, ObjectGuid guid, BattlegroundTypeId bgTypeId) {
        var bgTemplate = getBattlegroundTemplateByTypeId(bgTypeId);

        if (bgTemplate == null) {
            return;
        }

        BattlefieldList battlefieldList = new BattlefieldList();
        battlefieldList.battlemasterGuid = guid;
        battlefieldList.battlemasterListID = bgTypeId.getValue();
        battlefieldList.minLevel = bgTemplate.getMinLevel();
        battlefieldList.maxLevel = bgTemplate.getMaxLevel();
        battlefieldList.pvpAnywhere = guid.isEmpty();
        battlefieldList.hasRandomWinToday = player.getRandomWinner();
        player.sendPacket(battlefieldList);
    }

    public final void sendToBattleground(Player player, int instanceId, BattlegroundTypeId bgTypeId) {
        var bg = getBattleground(instanceId, bgTypeId);

        if (bg) {
            var mapid = bg.getMapId();
            var team = player.getBgTeam();

            var pos = bg.getTeamStartPosition(Battleground.getTeamIndexByTeamId(team));
            Log.outDebug(LogFilter.Battleground, String.format("BattlegroundMgr.SendToBattleground: Sending %1$s to map %2$s, %3$s (bgType %4$s)", player.getName(), mapid, pos.loc, bgTypeId));
            player.teleportTo(pos.loc);
        } else {
            Log.outError(LogFilter.Battleground, String.format("BattlegroundMgr.SendToBattleground: Instance %1$s (bgType %2$s) not found while trying to teleport player %3$s", instanceId, bgTypeId, player.getName()));
        }
    }

    public final void sendAreaSpiritHealerQuery(Player player, Battleground bg, ObjectGuid guid) {
        var time = 30000 - bg.getLastResurrectTime(); // resurrect every 30 seconds

        if (time == 0xFFFFFFFF) {
            time = 0;
        }

        AreaSpiritHealerTime areaSpiritHealerTime = new AreaSpiritHealerTime();
        areaSpiritHealerTime.healerGuid = guid;
        areaSpiritHealerTime.timeLeft = time;

        player.sendPacket(areaSpiritHealerTime);
    }

    public final BattlegroundQueueTypeId BGQueueTypeId(short battlemasterListId, BattlegroundQueueIdType type, boolean rated, ArenaTypes teamSize) {
        return new battlegroundQueueTypeId(battlemasterListId, (byte) type.getValue(), rated, (byte) teamSize.getValue());
    }

    public final void toggleTesting() {
        m_Testing = !m_Testing;
        global.getWorldMgr().sendWorldText(m_Testing ? SysMessage.DebugBgOn : SysMessage.DebugBgOff);
    }

    public final void toggleArenaTesting() {
        m_ArenaTesting = !m_ArenaTesting;
        global.getWorldMgr().sendWorldText(m_ArenaTesting ? SysMessage.DebugArenaOn : SysMessage.DebugArenaOff);
    }

    public final void resetHolidays() {
        for (var i = BattlegroundTypeId.AV; i.getValue() < BattlegroundTypeId.max.getValue(); i++) {
            var bg = getBattlegroundTemplate(i);

            if (bg != null) {
                bg.setHoliday(false);
            }
        }
    }

    public final void setHolidayActive(int battlegroundId) {
        var bg = getBattlegroundTemplate(BattlegroundTypeId.forValue(battlegroundId));

        if (bg != null) {
            bg.setHoliday(true);
        }
    }

    public final boolean isValidQueueId(BattlegroundQueueTypeId bgQueueTypeId) {
        var battlemasterList = CliDB.BattlemasterListStorage.get(bgQueueTypeId.battlemasterListId);

        if (battlemasterList == null) {
            return false;
        }

        switch (BattlegroundQueueIdType.forValue(bgQueueTypeId.bgType)) {
            case Battleground:
                if (battlemasterList.instanceType != MapTypes.Battleground.getValue()) {
                    return false;
                }

                if (bgQueueTypeId.teamSize != 0) {
                    return false;
                }

                break;
            case Arena:
                if (battlemasterList.instanceType != MapTypes.Arena.getValue()) {
                    return false;
                }

                if (!bgQueueTypeId.rated) {
                    return false;
                }

                if (bgQueueTypeId.teamSize == 0) {
                    return false;
                }

                break;
            case Wargame:
                if (bgQueueTypeId.rated) {
                    return false;
                }

                break;
            case ArenaSkirmish:
                if (battlemasterList.instanceType != MapTypes.Arena.getValue()) {
                    return false;
                }

                if (bgQueueTypeId.rated) {
                    return false;
                }

                if (bgQueueTypeId.teamSize != 0) {
                    return false;
                }

                break;
            default:
                return false;
        }

        return true;
    }

    public final void scheduleQueueUpdate(int arenaMatchmakerRating, BattlegroundQueueTypeId bgQueueTypeId, BattlegroundBracketId bracket_id) {
        //we will use only 1 number created of bgTypeId and bracket_id
        ScheduledQueueUpdate scheduleId = new ScheduledQueueUpdate(arenaMatchmakerRating, bgQueueTypeId, bracket_id);

        if (!m_QueueUpdateScheduler.contains(scheduleId)) {
            m_QueueUpdateScheduler.add(scheduleId);
        }
    }

    public final int getMaxRatingDifference() {
        // this is for stupid people who can't use brain and set max rating difference to 0
        var diff = WorldConfig.getUIntValue(WorldCfg.ArenaMaxRatingDifference);

        if (diff == 0) {
            diff = 5000;
        }

        return diff;
    }

    public final int getRatingDiscardTimer() {
        return WorldConfig.getUIntValue(WorldCfg.ArenaRatingDiscardTimer);
    }

    public final int getPrematureFinishTime() {
        return WorldConfig.getUIntValue(WorldCfg.BattlegroundPrematureFinishTimer);
    }

    public final void loadBattleMastersEntry() {
        var oldMSTime = System.currentTimeMillis();

        mBattleMastersMap.clear(); // need for reload case

        var result = DB.World.query("SELECT entry, bg_template FROM battlemaster_entry");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 battlemaster entries. DB table `battlemaster_entry` is empty!");

            return;
        }

        int count = 0;

        do {
            var entry = result.<Integer>Read(0);
            var cInfo = global.getObjectMgr().getCreatureTemplate(entry);

            if (cInfo != null) {
                if (!cInfo.npcflag.hasFlag((int) NPCFlags.BattleMaster.getValue())) {
                    Logs.SQL.error("Creature (Entry: {0}) listed in `battlemaster_entry` is not a battlemaster.", entry);
                }
            } else {
                Logs.SQL.error("Creature (Entry: {0}) listed in `battlemaster_entry` does not exist.", entry);

                continue;
            }

            var bgTypeId = result.<Integer>Read(1);

            if (!CliDB.BattlemasterListStorage.containsKey(bgTypeId)) {
                Logs.SQL.error("Table `battlemaster_entry` contain entry {0} for not existed Battleground type {1}, ignored.", entry, bgTypeId);

                continue;
            }

            ++count;
            mBattleMastersMap.put(entry, BattlegroundTypeId.forValue(bgTypeId));
        } while (result.NextRow());

        checkBattleMasters();

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} battlemaster entries in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final BattlegroundTypeId weekendHolidayIdToBGType(HolidayIds holiday) {
        switch (holiday) {
            case CallToArmsAv:
                return BattlegroundTypeId.AV;
            case CallToArmsEs:
                return BattlegroundTypeId.EY;
            case CallToArmsWg:
                return BattlegroundTypeId.WS;
            case CallToArmsSa:
                return BattlegroundTypeId.SA;
            case CallToArmsAb:
                return BattlegroundTypeId.AB;
            case CallToArmsIc:
                return BattlegroundTypeId.IC;
            case CallToArmsTp:
                return BattlegroundTypeId.TP;
            case CallToArmsBg:
                return BattlegroundTypeId.BFG;
            default:
                return BattlegroundTypeId.NONE;
        }
    }

    public final boolean isBGWeekend(BattlegroundTypeId bgTypeId) {
        return global.getGameEventMgr().isHolidayActive(BGTypeToWeekendHolidayId(bgTypeId));
    }

    public final ArrayList<Battleground> getBGFreeSlotQueueStore(BattlegroundQueueTypeId bgTypeId) {
        return m_BGFreeSlotQueue.get(bgTypeId);
    }

    public final void addToBGFreeSlotQueue(BattlegroundQueueTypeId bgTypeId, Battleground bg) {
        m_BGFreeSlotQueue.add(bgTypeId, bg);
    }

    public final void removeFromBGFreeSlotQueue(BattlegroundQueueTypeId bgTypeId, int instanceId) {
        var queues = m_BGFreeSlotQueue.get(bgTypeId);

        for (var bg : queues) {
            if (bg.getInstanceID() == instanceId) {
                queues.remove(bg);

                return;
            }
        }
    }

    public final void addBattleground(Battleground bg) {
        if (bg) {
            bgDataStore.get(bg.getTypeID()).m_Battlegrounds.put(bg.getInstanceID(), bg);
        }
    }

    public final void removeBattleground(BattlegroundTypeId bgTypeId, int instanceId) {
        bgDataStore.get(bgTypeId).m_Battlegrounds.remove(instanceId);
    }

    public final BattlegroundQueue getBattlegroundQueue(BattlegroundQueueTypeId bgQueueTypeId) {
        if (!m_BattlegroundQueues.containsKey(bgQueueTypeId)) {
            m_BattlegroundQueues.put(bgQueueTypeId, new BattlegroundQueue(bgQueueTypeId));
        }

        return m_BattlegroundQueues.get(bgQueueTypeId);
    }

    public final boolean isArenaTesting() {
        return m_ArenaTesting;
    }

    public final boolean isTesting() {
        return m_Testing;
    }

    public final BattlegroundTypeId getBattleMasterBG(int entry) {
        return mBattleMastersMap.get(entry);
    }

    private void buildBattlegroundStatusHeader(BattlefieldStatusHeader header, Battleground bg, Player player, int ticketId, int joinTime, BattlegroundQueueTypeId queueId, ArenaTypes arenaType) {
        header.ticket = new rideTicket();
        header.ticket.requesterGuid = player.getGUID();
        header.ticket.id = ticketId;
        header.ticket.type = RideType.Battlegrounds;
        header.ticket.time = (int) joinTime;
        header.queueID.add(queueId.getPacked());
        header.rangeMin = (byte) bg.getMinLevel();
        header.rangeMax = (byte) bg.getMaxLevel();
        header.teamSize = (byte) (bg.isArena() ? arenaType : 0);
        header.instanceID = bg.getClientInstanceID();
        header.registeredMatch = bg.isRated();
        header.tournamentRules = false;
    }

    private int createClientVisibleInstanceId(BattlegroundTypeId bgTypeId, BattlegroundBracketId bracket_id) {
        if (isArenaType(bgTypeId)) {
            return 0; //arenas don't have client-instanceids
        }

        // we create here an instanceid, which is just for
        // displaying this to the client and without any other use..
        // the client-instanceIds are unique for each Battleground-type
        // the instance-id just needs to be as low as possible, beginning with 1
        // the following works, because std.set is default ordered with "<"
        // the optimalization would be to use as bitmask std.vector<uint32> - but that would only make code unreadable

        var clientIds = bgDataStore.get(bgTypeId).m_ClientBattlegroundIds[bracket_id.getValue()];
        int lastId = 0;

        for (var id : clientIds) {
            if (++lastId != id) //if there is a gap between the ids, we will break..
            {
                break;
            }

            lastId = id;
        }

        clientIds.add(++lastId);

        return lastId;
    }

    // used to create the BG templates
    private boolean createBattleground(BattlegroundTemplate bgTemplate) {
        var bg = getBattlegroundTemplate(bgTemplate.id);

        if (!bg) {
            // Create the BG
            switch (bgTemplate.id) {
                //case BattlegroundTypeId.AV:
                // bg = new BattlegroundAV(bgTemplate);
                //break;
                case WS:
                    bg = new BgWarsongGluch(bgTemplate);

                    break;
                case AB:
                case DomAb:
                    bg = new BgArathiBasin(bgTemplate);

                    break;
                case NA:
                    bg = new NagrandArena(bgTemplate);

                    break;
                case BE:
                    bg = new BladesEdgeArena(bgTemplate);

                    break;
                case EY:
                    bg = new BgEyeofStorm(bgTemplate);

                    break;
                case RL:
                    bg = new RuinsofLordaeronArena(bgTemplate);

                    break;
                case SA:
                    bg = new BgStrandOfAncients(bgTemplate);

                    break;
                case DS:
                    bg = new DalaranSewersArena(bgTemplate);

                    break;
                case RV:
                    bg = new RingofValorArena(bgTemplate);

                    break;
                //case BattlegroundTypeId.IC:
                //bg = new BattlegroundIC(bgTemplate);
                //break;
                case AA:
                    bg = new Battleground(bgTemplate);

                    break;
                case RB:
                    bg = new Battleground(bgTemplate);
                    bg.setRandom(true);

                    break;
				/*
			case BattlegroundTypeId.TP:
				bg = new BattlegroundTP(bgTemplate);
				break;
			case BattlegroundTypeId.BFG:
				bg = new BattlegroundBFG(bgTemplate);
				break;
				*/
                case RandomEpic:
                    bg = new Battleground(bgTemplate);
                    bg.setRandom(true);

                    break;
                default:
                    return false;
            }
        }

        if (!bgDataStore.containsKey(bg.getTypeID())) {
            bgDataStore.put(bg.getTypeID(), new BattlegroundData());
        }

        bgDataStore.get(bg.getTypeID()).template = bg;

        return true;
    }

    private boolean isArenaType(BattlegroundTypeId bgTypeId) {
        return bgTypeId == BattlegroundTypeId.AA || bgTypeId == BattlegroundTypeId.BE || bgTypeId == BattlegroundTypeId.NA || bgTypeId == BattlegroundTypeId.DS || bgTypeId == BattlegroundTypeId.RV || bgTypeId == BattlegroundTypeId.RL;
    }

    private void checkBattleMasters() {
        var templates = global.getObjectMgr().getCreatureTemplates();

        for (var creature : templates.entrySet()) {
            if (creature.getValue().npcflag.hasFlag((int) NPCFlags.BattleMaster.getValue()) && !mBattleMastersMap.containsKey(creature.getValue().entry)) {
                Logs.SQL.error("CreatureTemplate (Entry: {0}) has UNIT_NPC_FLAG_BATTLEMASTER but no data in `battlemaster_entry` table. Removing flag!", creature.getValue().entry);
                templates.get(creature.getKey()).npcflag &= ~(int) NPCFlags.BattleMaster.getValue();
            }
        }
    }

    private HolidayIds BGTypeToWeekendHolidayId(BattlegroundTypeId bgTypeId) {
        switch (bgTypeId) {
            case AV:
                return HolidayIds.CallToArmsAv;
            case EY:
                return HolidayIds.CallToArmsEs;
            case WS:
                return HolidayIds.CallToArmsWg;
            case SA:
                return HolidayIds.CallToArmsSa;
            case AB:
                return HolidayIds.CallToArmsAb;
            case IC:
                return HolidayIds.CallToArmsIc;
            case TP:
                return HolidayIds.CallToArmsTp;
            case BFG:
                return HolidayIds.CallToArmsBg;
            default:
                return HolidayIds.NONE;
        }
    }

    private BattlegroundTypeId getRandomBG(BattlegroundTypeId bgTypeId) {
        var bgTemplate = getBattlegroundTemplateByTypeId(bgTypeId);

        if (bgTemplate != null) {
            HashMap<BattlegroundTypeId, Float> selectionWeights = new HashMap<BattlegroundTypeId, Float>();

            for (var mapId : bgTemplate.battlemasterEntry.mapId) {
                if (mapId == -1) {
                    break;
                }

                var bg = getBattlegroundTemplateByMapId((int) mapId);

                if (bg != null) {
                    selectionWeights.put(bg.id, bg.weight);
                }
            }

            return selectionWeights.SelectRandomElementByWeight(i -> i.value).key;
        }

        return BattlegroundTypeId.NONE;
    }

    private BattlegroundTemplate getBattlegroundTemplateByTypeId(BattlegroundTypeId id) {
        return battlegroundTemplates.get(id);
    }

    private BattlegroundTemplate getBattlegroundTemplateByMapId(int mapId) {
        return battlegroundMapTemplates.get(mapId);
    }

    private final static class ScheduledQueueUpdate {
        public final int arenaMatchmakerRating;
        public final BattlegroundbracketId bracketId;
        public battlegroundQueueTypeId queueId = new battlegroundQueueTypeId();
        public ScheduledQueueUpdate() {
        }
        public ScheduledQueueUpdate(int arenaMatchmakerRating, BattlegroundQueueTypeId queueId, BattlegroundBracketId bracketId) {
            arenaMatchmakerRating = arenaMatchmakerRating;
            queueId = queueId;
            bracketId = bracketId;
        }

        public static boolean opEquals(ScheduledQueueUpdate right, ScheduledQueueUpdate left) {
            return left.arenaMatchmakerRating == right.arenaMatchmakerRating && BattlegroundQueueTypeId.opEquals(left.queueId, right.queueId) && left.bracketId == right.bracketId;
        }

        public static boolean opNotEquals(ScheduledQueueUpdate right, ScheduledQueueUpdate left) {
            return !(ScheduledQueueUpdate.opEquals(right, left));
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }

        @Override
        public int hashCode() {
            return (new integer(arenaMatchmakerRating)).hashCode() ^ queueId.hashCode() ^ bracketId.hashCode();
        }

        public ScheduledQueueUpdate clone() {
            ScheduledQueueUpdate varCopy = new ScheduledQueueUpdate();

            varCopy.arenaMatchmakerRating = this.arenaMatchmakerRating;
            varCopy.queueId = this.queueId;
            varCopy.bracketId = this.bracketId;

            return varCopy;
        }
    }
}
