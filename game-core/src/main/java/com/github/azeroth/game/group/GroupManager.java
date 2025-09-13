package com.github.azeroth.game.group;


import java.util.HashMap;


public class GroupManager {
    private final HashMap<Long, PlayerGroup> groupStore = new HashMap<Long, PlayerGroup>();
    private final HashMap<Integer, PlayerGroup> groupDbStore = new HashMap<Integer, PlayerGroup>();
    private long nextGroupId;
    private int nextGroupDbStoreId;

    private GroupManager() {
        nextGroupDbStoreId = 1;
        nextGroupId = 1;
    }

    public final int generateNewGroupDbStoreId() {
        var newStorageId = nextGroupDbStoreId;

        for (var i = ++nextGroupDbStoreId; i < 0xFFFFFFFF; ++i) {
            if ((i < groupDbStore.size() && groupDbStore.get(i) == null) || i >= groupDbStore.size()) {
                nextGroupDbStoreId = i;

                break;
            }
        }

        if (newStorageId == nextGroupDbStoreId) {
            Log.outError(LogFilter.Server, "Group storage ID overflow!! Can't continue, shutting down server. ");
            global.getWorldMgr().stopNow();
        }

        return newStorageId;
    }

    public final void registerGroupDbStoreId(int storageId, PlayerGroup group) {
        groupDbStore.put(storageId, group);
    }

    public final void freeGroupDbStoreId(PlayerGroup group) {
        var storageId = group.getDbStoreId();

        if (storageId < nextGroupDbStoreId) {
            nextGroupDbStoreId = storageId;
        }

        groupDbStore.put(storageId - 1, null);
    }

    public final PlayerGroup getGroupByDbStoreId(int storageId) {
        return groupDbStore.get(storageId);
    }

    public final long generateGroupId() {
        if (nextGroupId >= 0xFFFFFFFEL) {
            Log.outError(LogFilter.Server, "Group guid overflow!! Can't continue, shutting down server. ");
            global.getWorldMgr().stopNow();
        }

        return nextGroupId++;
    }

    public final PlayerGroup getGroupByGUID(ObjectGuid groupId) {
        return groupStore.get(groupId.getCounter());
    }

    public final void update(int diff) {
        for (var group : groupStore.values()) {
            group.update(diff);
        }
    }

    public final void addGroup(PlayerGroup group) {
        groupStore.put(group.getGUID().getCounter(), group);
    }

    public final void removeGroup(PlayerGroup group) {
        groupStore.remove(group.getGUID().getCounter());
    }

    public final void loadGroups() {
        {
            var oldMSTime = System.currentTimeMillis();

            // Delete all members that does not exist
            DB.characters.DirectExecute("DELETE FROM group_member WHERE memberGuid NOT IN (SELECT guid FROM character)");
            // Delete all groups whose leader does not exist
            DB.characters.DirectExecute("DELETE FROM `groups` WHERE leaderGuid NOT IN (SELECT guid FROM character)");
            // Delete all groups with less than 2 members
            DB.characters.DirectExecute("DELETE FROM `groups` WHERE guid NOT IN (SELECT guid FROM group_member GROUP BY guid HAVING COUNT(guid) > 1)");
            // Delete all rows from group_member with no group
            DB.characters.DirectExecute("DELETE FROM group_member WHERE guid NOT IN (SELECT guid FROM `groups`)");

            //                                                    0              1           2             3                 4      5          6      7         8       9
            var result = DB.characters.query("SELECT g.leaderGuid, g.lootMethod, g.looterGuid, g.lootThreshold, g.icon1, g.icon2, g.icon3, g.icon4, g.icon5, g.icon6" + ", g.icon7, g.icon8, g.groupType, g.difficulty, g.raiddifficulty, g.legacyRaidDifficulty, g.masterLooterGuid, g.guid, lfg.dungeon, lfg.state FROM `groups` g LEFT JOIN lfg_data lfg ON lfg.guid = g.guid ORDER BY g.guid ASC");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 group definitions. DB table `groups` is empty!");

                return;
            }

            int count = 0;

            do {
                PlayerGroup group = new PlayerGroup();
                group.loadGroupFromDB(result.GetFields());
                addGroup(group);

                // Get the ID used for storing the group in the database and register it in the pool.
                var storageId = group.getDbStoreId();

                registerGroupDbStoreId(storageId, group);

                // Increase the next available storage ID
                if (storageId == nextGroupDbStoreId) {
                    nextGroupDbStoreId++;
                }

                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} group definitions in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        }

        Log.outInfo(LogFilter.ServerLoading, "Loading Group members...");

        {
            var oldMSTime = System.currentTimeMillis();

            //                                                0        1           2            3       4
            var result = DB.characters.query("SELECT guid, memberGuid, memberFlags, subgroup, roles FROM group_member ORDER BY guid");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 group members. DB table `group_member` is empty!");

                return;
            }

            int count = 0;

            do {
                var group = getGroupByDbStoreId(result.<Integer>Read(0));

                if (group) {
                    group.loadMemberFromDB(result.<Integer>Read(1), result.<Byte>Read(2), result.<Byte>Read(3), LfgRoles.forValue(result.<Byte>Read(4)));
                } else {
                    Log.outError(LogFilter.Server, "GroupMgr:LoadGroups: Consistency failed, can't find group (storage id: {0})", result.<Integer>Read(0));
                }

                ++count;
            } while (result.NextRow());

            Log.outInfo(LogFilter.ServerLoading, "Loaded {0} group members in {1} ms", count, time.GetMSTimeDiffToNow(oldMSTime));
        }
    }
}
