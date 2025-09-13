package com.github.azeroth.game.dungeonfinding;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class LFGQueue {
    // Queue
    private final HashMap<ObjectGuid, LfgQueueData> queueDataStore = new HashMap<ObjectGuid, LfgQueueData>();
    private final HashMap<String, LfgCompatibilityData> compatibleMapStore = new HashMap<String, LfgCompatibilityData>();
    private final HashMap<Integer, LfgWaitTime> waitTimesAvgStore = new HashMap<Integer, LfgWaitTime>();
    private final HashMap<Integer, LfgWaitTime> waitTimesTankStore = new HashMap<Integer, LfgWaitTime>();
    private final HashMap<Integer, LfgWaitTime> waitTimesHealerStore = new HashMap<Integer, LfgWaitTime>();
    private final HashMap<Integer, LfgWaitTime> waitTimesDpsStore = new HashMap<Integer, LfgWaitTime>();
    private final ArrayList<ObjectGuid> currentQueueStore = new ArrayList<>();
    private final ArrayList<ObjectGuid> newToQueueStore = new ArrayList<>();

    public static String concatenateGuids(ArrayList<ObjectGuid> guids) {
        if (guids.isEmpty()) {
            return "";
        }

        // need the guids in order to avoid duplicates
        StringBuilder val = new StringBuilder();
        collections.sort(guids);
        var it = guids.get(0);
        val.append(it);

        for (var guid : guids) {
            if (guid == it) {
                continue;
            }

            val.append(String.format("|%1$s", guid));
        }

        return val.toString();
    }

    public static String getRolesString(LfgRoles roles) {
        StringBuilder rolesstr = new StringBuilder();

        if (roles.hasFlag(LfgRoles.Tank)) {
            rolesstr.append("Tank");
        }

        if (roles.hasFlag(LfgRoles.healer)) {
            if (rolesstr.capacity() != 0) {
                rolesstr.append(", ");
            }

            rolesstr.append("Healer");
        }

        if (roles.hasFlag(LfgRoles.damage)) {
            if (rolesstr.capacity() != 0) {
                rolesstr.append(", ");
            }

            rolesstr.append("Damage");
        }

        if (roles.hasFlag(LfgRoles.leader)) {
            if (rolesstr.capacity() != 0) {
                rolesstr.append(", ");
            }

            rolesstr.append("Leader");
        }

        if (rolesstr.capacity() == 0) {
            rolesstr.append("None");
        }

        return rolesstr.toString();
    }

    public static String concatenateDungeons(ArrayList<Integer> dungeons) {
        var str = "";

        if (!dungeons.isEmpty()) {
            for (var it : dungeons) {
                if (!StringUtil.isEmpty(str)) {
                    str += ", ";
                }

                str += it;
            }
        }

        return str;
    }


    public final void addToQueue(ObjectGuid guid) {
        addToQueue(guid, false);
    }

    public final void addToQueue(ObjectGuid guid, boolean reAdd) {
        if (!queueDataStore.containsKey(guid)) {
            Log.outError(LogFilter.Lfg, "AddToQueue: Queue data not found for [{0}]", guid);

            return;
        }

        if (reAdd) {
            addToFrontCurrentQueue(guid);
        } else {
            addToNewQueue(guid);
        }
    }

    public final void removeFromQueue(ObjectGuid guid) {
        removeFromNewQueue(guid);
        removeFromCurrentQueue(guid);
        removeFromCompatibles(guid);

        var sguid = guid.toString();

        var itDelete = queueDataStore.LastOrDefault().key;

        for (var key : queueDataStore.keySet().ToList()) {
            var data = queueDataStore.get(key);

            if (key != guid) {
                if (data.bestCompatible.contains(sguid)) {
                    data.bestCompatible = "";
                    findBestCompatibleInQueue(key, data);
                }
            } else {
                itDelete = key;
            }
        }

        if (!itDelete.IsEmpty) {
            queueDataStore.remove(itDelete);
        }
    }

    public final void addToNewQueue(ObjectGuid guid) {
        newToQueueStore.add(guid);
    }

    public final void removeFromNewQueue(ObjectGuid guid) {
        newToQueueStore.remove(guid);
    }

    public final void addToCurrentQueue(ObjectGuid guid) {
        currentQueueStore.add(guid);
    }

    public final void removeFromCurrentQueue(ObjectGuid guid) {
        currentQueueStore.remove(guid);
    }

    public final void addQueueData(ObjectGuid guid, long joinTime, ArrayList<Integer> dungeons, HashMap<ObjectGuid, LfgRoles> rolesMap) {
        queueDataStore.put(guid, new LfgQueueData(joinTime, dungeons, rolesMap));
        addToQueue(guid);
    }

    public final void removeQueueData(ObjectGuid guid) {
        queueDataStore.remove(guid);
    }

    public final void updateWaitTimeAvg(int waitTime, int dungeonId) {
        var wt = waitTimesAvgStore.get(dungeonId);
        var old_number = wt.number++;
        wt.time = (int) ((wt.time * old_number + waitTime) / wt.number);
    }

    public final void updateWaitTimeTank(int waitTime, int dungeonId) {
        var wt = waitTimesTankStore.get(dungeonId);
        var old_number = wt.number++;
        wt.time = (int) ((wt.time * old_number + waitTime) / wt.number);
    }

    public final void updateWaitTimeHealer(int waitTime, int dungeonId) {
        var wt = waitTimesHealerStore.get(dungeonId);
        var old_number = wt.number++;
        wt.time = (int) ((wt.time * old_number + waitTime) / wt.number);
    }

    public final void updateWaitTimeDps(int waitTime, int dungeonId) {
        var wt = waitTimesDpsStore.get(dungeonId);
        var old_number = wt.number++;
        wt.time = (int) ((wt.time * old_number + waitTime) / wt.number);
    }

    public final byte findGroups() {
        byte proposals = 0;
        ArrayList<ObjectGuid> firstNew = new ArrayList<>();

        while (!newToQueueStore.isEmpty()) {
            var frontguid = newToQueueStore.get(0);
            Log.outDebug(LogFilter.Lfg, "FindGroups: checking [{0}] newToQueue({1}), currentQueue({2})", frontguid, newToQueueStore.size(), currentQueueStore.size());
            firstNew.clear();
            firstNew.add(frontguid);
            removeFromNewQueue(frontguid);

            ArrayList<ObjectGuid> temporalList = new ArrayList<ObjectGuid>(currentQueueStore);
            var compatibles = findNewGroups(firstNew, temporalList);

            if (compatibles == LfgCompatibility.Match) {
                ++proposals;
            } else {
                addToCurrentQueue(frontguid); // Lfg group not found, add this group to the queue.
            }
        }

        return proposals;
    }

    public final void updateQueueTimers(byte queueId, long currTime) {
        Log.outDebug(LogFilter.Lfg, "Updating queue timers...");

        for (var itQueue : queueDataStore.entrySet()) {
            var queueinfo = itQueue.getValue();
            var dungeonId = queueinfo.dungeons.FirstOrDefault();
            var queuedTime = (int) (currTime - queueinfo.joinTime);
            var role = LfgRoles.NONE;
            var waitTime = -1;

            if (!waitTimesTankStore.containsKey(dungeonId)) {
                waitTimesTankStore.put(dungeonId, new LfgWaitTime());
            }

            if (!waitTimesHealerStore.containsKey(dungeonId)) {
                waitTimesHealerStore.put(dungeonId, new LfgWaitTime());
            }

            if (!waitTimesDpsStore.containsKey(dungeonId)) {
                waitTimesDpsStore.put(dungeonId, new LfgWaitTime());
            }

            if (!waitTimesAvgStore.containsKey(dungeonId)) {
                waitTimesAvgStore.put(dungeonId, new LfgWaitTime());
            }

            var wtTank = waitTimesTankStore.get(dungeonId).time;
            var wtHealer = waitTimesHealerStore.get(dungeonId).time;
            var wtDps = waitTimesDpsStore.get(dungeonId).time;
            var wtAvg = waitTimesAvgStore.get(dungeonId).time;

            for (var itPlayer : queueinfo.roles) {
                role = LfgRoles.forValue(role.getValue() | itPlayer.value.getValue());
            }

            role = LfgRoles.forValue(role.getValue() & ~LfgRoles.leader.getValue());

            switch (role) {
                case None: // Should not happen - just in case
                    waitTime = -1;

                    break;
                case Tank:
                    waitTime = wtTank;

                    break;
                case Healer:
                    waitTime = wtHealer;

                    break;
                case Damage:
                    waitTime = wtDps;

                    break;
                default:
                    waitTime = wtAvg;

                    break;
            }

            if (StringUtil.isEmpty(queueinfo.bestCompatible)) {
                findBestCompatibleInQueue(itQueue.getKey(), itQueue.getValue());
            }

            LfgQueueStatusData queueData = new LfgQueueStatusData(queueId, dungeonId, waitTime, wtAvg, wtTank, wtHealer, wtDps, queuedTime, queueinfo.tanks, queueinfo.healers, queueinfo.dps);

            for (var itPlayer : queueinfo.roles) {
                var pguid = itPlayer.key;
                global.getLFGMgr().sendLfgQueueStatus(pguid, queueData);
            }
        }
    }

    public final long getJoinTime(ObjectGuid guid) {
        var queueData = queueDataStore.get(guid);

        if (queueData != null) {
            return queueData.joinTime;
        }

        return 0;
    }

    public final String dumpQueueInfo() {
        int players = 0;
        int groups = 0;
        int playersInGroup = 0;

        for (byte i = 0; i < 2; ++i) {
            var queue = i != 0 ? newToQueueStore : currentQueueStore;

            for (var guid : queue) {
                if (guid.isParty()) {
                    groups++;
                    playersInGroup += global.getLFGMgr().getPlayerCount(guid);
                } else {
                    players++;
                }
            }
        }

        return String.format("Queued Players: %1$s (in group: %2$s) Groups: %3$s\n", players, playersInGroup, groups);
    }


    public final String dumpCompatibleInfo() {
        return dumpCompatibleInfo(false);
    }

    public final String dumpCompatibleInfo(boolean full) {
        var str = "Compatible Map size: " + compatibleMapStore.size() + "\n";

        if (full) {
            for (var pair : compatibleMapStore.entrySet()) {
                str += "(" + pair.getKey() + "): " + getCompatibleString(pair.getValue().compatibility) + "\n";
            }
        }

        return str;
    }

    public final void updateBestCompatibleInQueue(ObjectGuid guid, LfgQueueData queueData, String key, HashMap<ObjectGuid, LfgRoles> roles) {
        var storedSize = (byte) (StringUtil.isEmpty(queueData.bestCompatible) ? 0 : queueData.bestCompatible.count(p -> p == '|') + 1);

        var size = (byte) (key.count(p -> p == '|') + 1);

        if (size <= storedSize) {
            return;
        }

        Log.outDebug(LogFilter.Lfg, "UpdateBestCompatibleInQueue: changed ({0}) to ({1}) as best compatible group for {2}", queueData.bestCompatible, key, guid);

        queueData.bestCompatible = key;
        queueData.tanks = SharedConst.LFGTanksNeeded;
        queueData.healers = SharedConst.LFGHealersNeeded;
        queueData.dps = SharedConst.LFGDPSNeeded;

        for (var it : roles.entrySet()) {
            var role = it.getValue();

            if (role.hasFlag(LfgRoles.Tank)) {
                --queueData.tanks;
            } else if (role.hasFlag(LfgRoles.healer)) {
                --queueData.healers;
            } else {
                --queueData.dps;
            }
        }
    }

    private String getCompatibleString(LfgCompatibility compatibles) {
        switch (compatibles) {
            case Pending:
                return "Pending";
            case BadStates:
                return "Compatibles (Bad States)";
            case Match:
                return "Match";
            case WithLessPlayers:
                return "Compatibles (Not enough players)";
            case HasIgnores:
                return "Has ignores";
            case MultipleLfgGroups:
                return "Multiple Lfg Groups";
            case NoDungeons:
                return "Incompatible dungeons";
            case NoRoles:
                return "Incompatible roles";
            case TooMuchPlayers:
                return "Too much players";
            case WrongGroupSize:
                return "Wrong group size";
            default:
                return "Unknown";
        }
    }

    private void addToFrontCurrentQueue(ObjectGuid guid) {
        currentQueueStore.add(0, guid);
    }

    private void removeFromCompatibles(ObjectGuid guid) {
        var strGuid = guid.toString();

        Log.outDebug(LogFilter.Lfg, "RemoveFromCompatibles: Removing [{0}]", guid);

        for (var itNext : compatibleMapStore.ToList()) {
            if (itNext.key.contains(strGuid)) {
                compatibleMapStore.remove(itNext.key);
            }
        }
    }

    private void setCompatibles(String key, LfgCompatibility compatibles) {
        if (!compatibleMapStore.containsKey(key)) {
            compatibleMapStore.put(key, new LfgCompatibilityData());
        }

        compatibleMapStore.get(key).compatibility = compatibles;
    }

    private void setCompatibilityData(String key, LfgCompatibilityData data) {
        compatibleMapStore.put(key, data);
    }

    private LfgCompatibility getCompatibles(String key) {
        var compatibilityData = compatibleMapStore.get(key);

        if (compatibilityData != null) {
            return compatibilityData.compatibility;
        }

        return LfgCompatibility.Pending;
    }

    private LfgCompatibilityData getCompatibilityData(String key) {
        var compatibilityData = compatibleMapStore.get(key);

        if (compatibilityData != null) {
            return compatibilityData;
        }

        return null;
    }

    private LfgCompatibility findNewGroups(ArrayList<ObjectGuid> check, ArrayList<ObjectGuid> all) {
        var strGuids = concatenateGuids(check);
        var compatibles = getCompatibles(strGuids);

        Log.outDebug(LogFilter.Lfg, "FindNewGroup: ({0}): {1} - all({2})", strGuids, getCompatibleString(compatibles), concatenateGuids(all));

        if (compatibles == LfgCompatibility.Pending) // Not previously cached, calculate
        {
            compatibles = checkCompatibility(check);
        }

        if (compatibles == LfgCompatibility.BadStates && global.getLFGMgr().allQueued(check)) {
            Log.outDebug(LogFilter.Lfg, "FindNewGroup: ({0}) compatibles (cached) changed from bad states to match", strGuids);
            setCompatibles(strGuids, LfgCompatibility.Match);

            return LfgCompatibility.Match;
        }

        if (compatibles != LfgCompatibility.WithLessPlayers) {
            return compatibles;
        }

        // Try to match with queued groups
        while (!all.isEmpty()) {
            check.add(all.get(0));
            all.remove(0);
            var subcompatibility = findNewGroups(check, all);

            if (subcompatibility == LfgCompatibility.Match) {
                return LfgCompatibility.Match;
            }

            check.remove(check.size() - 1);
        }

        return compatibles;
    }

    private LfgCompatibility checkCompatibility(ArrayList<ObjectGuid> check) {
        var strGuids = concatenateGuids(check);
        LfgProposal proposal = new LfgProposal();
        ArrayList<Integer> proposalDungeons;
        HashMap<ObjectGuid, ObjectGuid> proposalGroups = new HashMap<ObjectGuid, ObjectGuid>();
        HashMap<ObjectGuid, LfgRoles> proposalRoles = new HashMap<ObjectGuid, LfgRoles>();

        // Check for correct size
        if (check.size() > MapDefine.MaxGroupSize || check.isEmpty()) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}): Size wrong - Not compatibles", strGuids);

            return LfgCompatibility.WrongGroupSize;
        }

        // Check all-but-new compatiblitity
        if (check.size() > 2) {
            var frontGuid = check.get(0);
            check.remove(0);

            // Check all-but-new compatibilities (New, A, B, C, D) -. check(A, B, C, D)
            var child_compatibles = checkCompatibility(check);

            if (child_compatibles.getValue() < LfgCompatibility.WithLessPlayers.getValue()) // Group not compatible
            {
                Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) child {1} not compatibles", strGuids, concatenateGuids(check));
                setCompatibles(strGuids, child_compatibles);

                return child_compatibles;
            }

            check.add(0, frontGuid);
        }

        // Check if more than one LFG group and number of players joining
        byte numPlayers = 0;
        byte numLfgGroups = 0;

        for (var guid : check) {
            if (!(numLfgGroups < 2) && !(numPlayers <= MapDefine.MaxGroupSize)) {
                break;
            }

            var itQueue = queueDataStore.get(guid);

            if (itQueue == null) {
                Log.outError(LogFilter.Lfg, "CheckCompatibility: [{0}] is not queued but listed as queued!", guid);
                removeFromQueue(guid);

                return LfgCompatibility.Pending;
            }

            // Store group so we don't need to call Mgr to get it later (if it's player group will be 0 otherwise would have joined as group)
            for (var it2 : itQueue.roles) {
                proposalGroups.put(it2.key, guid.isPlayer() ? guid : ObjectGuid.Empty);
            }

            numPlayers += (byte) itQueue.roles.count;

            if (global.getLFGMgr().isLfgGroup(guid)) {
                if (numLfgGroups == 0) {
                    proposal.group = guid;
                }

                ++numLfgGroups;
            }
        }

        // Group with less that MAXGROUPSIZE members always compatible
        if (check.size() == 1 && numPlayers != MapDefine.MaxGroupSize) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) sigle group. Compatibles", strGuids);
            var guid = check.get(0);
            var itQueue = queueDataStore.get(guid);

            LfgCompatibilityData data = new LfgCompatibilityData(LfgCompatibility.WithLessPlayers);
            data.roles = itQueue.roles;
            global.getLFGMgr().checkGroupRoles(data.roles);

            updateBestCompatibleInQueue(guid, itQueue, strGuids, data.roles);
            setCompatibilityData(strGuids, data);

            return LfgCompatibility.WithLessPlayers;
        }

        if (numLfgGroups > 1) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) More than one Lfggroup ({1})", strGuids, numLfgGroups);
            setCompatibles(strGuids, LfgCompatibility.MultipleLfgGroups);

            return LfgCompatibility.MultipleLfgGroups;
        }

        if (numPlayers > MapDefine.MaxGroupSize) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) Too much players ({1})", strGuids, numPlayers);
            setCompatibles(strGuids, LfgCompatibility.TooMuchPlayers);

            return LfgCompatibility.TooMuchPlayers;
        }

        // If it's single group no need to check for duplicate players, ignores, bad roles or bad dungeons as it's been checked before joining
        if (check.size() > 1) {
            for (var it : check) {
                var roles = queueDataStore.get(it).roles;

                for (var rolePair : roles.entrySet()) {
                    Map.entry<ObjectGuid, LfgRoles> itPlayer = new KeyValuePair<ObjectGuid, LfgRoles>();

                    for (var _player : proposalRoles.entrySet()) {
                        itPlayer = player;

                        if (rolePair.getKey() == itPlayer.getKey()) {
                            Log.outError(LogFilter.Lfg, "CheckCompatibility: ERROR! Player multiple times in queue! [{0}]", rolePair.getKey());
                        } else if (global.getLFGMgr().hasIgnore(rolePair.getKey(), itPlayer.getKey())) {
                            break;
                        }
                    }

                    if (itPlayer.getKey() == proposalRoles.LastOrDefault().key) {
                        proposalRoles.put(rolePair.getKey(), rolePair.getValue());
                    }
                }
            }

            var playersize = (byte) (numPlayers - proposalRoles.size());

            if (playersize != 0) {
                Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) not compatible, {1} players are ignoring each other", strGuids, playersize);
                setCompatibles(strGuids, LfgCompatibility.HasIgnores);

                return LfgCompatibility.HasIgnores;
            }

            StringBuilder o;
            var debugRoles = proposalRoles;

            if (!global.getLFGMgr().checkGroupRoles(proposalRoles)) {
                o = new StringBuilder();

                for (var it : debugRoles.entrySet()) {
                    o.append(String.format(", %1$s: %2$s", it.getKey(), getRolesString(it.getValue())));
                }

                Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) Roles not compatible{1}", strGuids, o.toString());
                setCompatibles(strGuids, LfgCompatibility.NoRoles);

                return LfgCompatibility.NoRoles;
            }

            var itguid = check.get(0);
            proposalDungeons = queueDataStore.get(itguid).dungeons;
            o = new StringBuilder();
            o.append(String.format(", %1$s: (%2$s)", itguid, global.getLFGMgr().concatenateDungeons(proposalDungeons)));

            for (var guid : check) {
                if (guid == itguid) {
                    continue;
                }

                var dungeons = queueDataStore.get(itguid).dungeons;
                o.append(String.format(", %1$s: (%2$s)", guid, global.getLFGMgr().concatenateDungeons(dungeons)));
                var temporal = proposalDungeons.Intersect(dungeons).ToList();
                proposalDungeons = temporal;
            }

            if (proposalDungeons.isEmpty()) {
                Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) No compatible dungeons{1}", strGuids, o.toString());
                setCompatibles(strGuids, LfgCompatibility.NoDungeons);

                return LfgCompatibility.NoDungeons;
            }
        } else {
            var gguid = check.get(0);
            var queue = queueDataStore.get(gguid);
            proposalDungeons = queue.dungeons;
            proposalRoles = queue.roles;
            global.getLFGMgr().checkGroupRoles(proposalRoles); // assing new roles
        }

        // Enough players?
        if (numPlayers != MapDefine.MaxGroupSize) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) Compatibles but not enough players({1})", strGuids, numPlayers);
            LfgCompatibilityData data = new LfgCompatibilityData(LfgCompatibility.WithLessPlayers);
            data.roles = proposalRoles;

            for (var guid : check) {
                var queueData = queueDataStore.get(guid);
                updateBestCompatibleInQueue(guid, queueData, strGuids, data.roles);
            }

            setCompatibilityData(strGuids, data);

            return LfgCompatibility.WithLessPlayers;
        }

        var guid = check.get(0);
        proposal.queues = check;
        proposal.isNew = numLfgGroups != 1 || global.getLFGMgr().getOldState(guid) != LfgState.Dungeon;

        if (!global.getLFGMgr().allQueued(check)) {
            Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) Group MATCH but can't create proposal!", strGuids);
            setCompatibles(strGuids, LfgCompatibility.BadStates);

            return LfgCompatibility.BadStates;
        }

        // Create a new proposal
        proposal.cancelTime = gameTime.GetGameTime() + SharedConst.LFGTimeProposal;
        proposal.state = LfgProposalState.Initiating;
        proposal.leader = ObjectGuid.Empty;
        proposal.dungeonId = proposalDungeons.SelectRandom();

        var leader = false;

        for (var rolePair : proposalRoles.entrySet()) {
            // Assing new leader
            if (rolePair.getValue().hasFlag(LfgRoles.leader)) {
                if (!leader || proposal.leader.isEmpty() || (boolean) RandomUtil.IRand(0, 1)) {
                    proposal.leader = rolePair.getKey();
                }

                leader = true;
            } else if (!leader && (proposal.leader.isEmpty() || (boolean) RandomUtil.IRand(0, 1))) {
                proposal.leader = rolePair.getKey();
            }

            // Assing player data and roles
            LfgProposalPlayer data = new LfgProposalPlayer();
            data.role = rolePair.getValue();
            data.group = proposalGroups.get(rolePair.getKey());

            if (!proposal.isNew && !data.group.isEmpty() && Objects.equals(data.group, proposal.group)) // Player from existing group, autoaccept
            {
                data.accept = LfgAnswer.Agree;
            }

            proposal.players.put(rolePair.getKey(), data);
        }

        // Mark proposal members as not queued (but not remove queue data)
        for (var guid : proposal.queues) {
            removeFromNewQueue(guid);
            removeFromCurrentQueue(guid);
        }

        global.getLFGMgr().addProposal(proposal);

        Log.outDebug(LogFilter.Lfg, "CheckCompatibility: ({0}) MATCH! Group formed", strGuids);
        setCompatibles(strGuids, LfgCompatibility.Match);

        return LfgCompatibility.Match;
    }

    private void findBestCompatibleInQueue(ObjectGuid guid, LfgQueueData data) {
        Log.outDebug(LogFilter.Lfg, "FindBestCompatibleInQueue: {0}", guid);

        for (var pair : compatibleMapStore.entrySet()) {
            if (pair.getValue().compatibility == LfgCompatibility.WithLessPlayers && pair.getKey().contains(guid.toString())) {
                updateBestCompatibleInQueue(guid, data, pair.getKey(), pair.getValue().roles);
            }
        }
    }
}
