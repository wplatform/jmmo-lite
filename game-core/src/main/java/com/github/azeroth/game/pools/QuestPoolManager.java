package com.github.azeroth.game.pools;


import java.util.ArrayList;
import java.util.HashMap;

public class QuestPoolManager {
    private final ArrayList<QuestPool> dailyPools = new ArrayList<>();
    private final ArrayList<QuestPool> weeklyPools = new ArrayList<>();
    private final ArrayList<QuestPool> monthlyPools = new ArrayList<>();
    private final HashMap<Integer, QuestPool> poolLookup = new HashMap<Integer, QuestPool>(); // questId -> pool

    private QuestPoolManager() {
    }

    public static void regeneratePool(QuestPool pool) {
        var n = pool.members.Count - 1;
        pool.activeQuests.clear();

        for (int i = 0; i < pool.numActive; ++i) {
            var j = RandomUtil.URand(i, n);

            if (i != j) {
                var leftList = pool.members.get(i);
                pool.members.set(i, pool.members.get(j));
                pool.members.set(j, leftList);
            }

            for (var quest : pool.members.get(i)) {
                pool.activeQuests.add(quest);
            }
        }
    }

    public static void saveToDB(QuestPool pool, SQLTransaction trans) {
        var delStmt = DB.characters.GetPreparedStatement(CharStatements.DEL_POOL_QUEST_SAVE);
        delStmt.AddValue(0, pool.poolId);
        trans.append(delStmt);

        for (var questId : pool.activeQuests) {
            var insStmt = DB.characters.GetPreparedStatement(CharStatements.INS_POOL_QUEST_SAVE);
            insStmt.AddValue(0, pool.poolId);
            insStmt.AddValue(1, questId);
            trans.append(insStmt);
        }
    }

    public final void loadFromDB() {
        var oldMSTime = System.currentTimeMillis();
        HashMap<Integer, Tuple<ArrayList<QuestPool>, Integer>> lookup = new HashMap<Integer, Tuple<ArrayList<QuestPool>, Integer>>(); // poolId -> (list, index)

        poolLookup.clear();
        dailyPools.clear();
        weeklyPools.clear();
        monthlyPools.clear();

        {
            // load template data from world DB
            var result = DB.World.query("SELECT qpm.questId, qpm.poolId, qpm.poolIndex, qpt.numActive FROM quest_pool_members qpm LEFT JOIN quest_pool_template qpt ON qpm.poolId = qpt.poolId");

            if (result.isEmpty()) {
                Log.outInfo(LogFilter.ServerLoading, "Loaded 0 quest pools. DB table `quest_pool_members` is empty.");

                return;
            }

            do {
                if (result.IsNull(2)) {
                    Logs.SQL.error(String.format("Table `quest_pool_members` contains reference to non-existing pool %1$s. Skipped.", result.<Integer>Read(1)));

                    continue;
                }

                var questId = result.<Integer>Read(0);
                var poolId = result.<Integer>Read(1);
                var poolIndex = result.<Integer>Read(2);
                var numActive = result.<Integer>Read(3);

                var quest = global.getObjectMgr().getQuestTemplate(questId);

                if (quest == null) {
                    Logs.SQL.error("Table `quest_pool_members` contains reference to non-existing quest %u. Skipped.", questId);

                    continue;
                }

                if (!quest.isDailyOrWeekly() && !quest.isMonthly()) {
                    Logs.SQL.error("Table `quest_pool_members` contains reference to quest %u, which is neither daily, weekly nor monthly. Skipped.", questId);

                    continue;
                }

                if (!lookup.containsKey(poolId)) {
                    var poolList = quest.isDaily() ? _dailyPools : quest.isWeekly() ? _weeklyPools : monthlyPools;

                    QuestPool tempVar = new QuestPool();
                    tempVar.poolId = poolId;
                    tempVar.numActive = numActive;
                    poolList.add(tempVar);

                    lookup.put(poolId, new Tuple<ArrayList<QuestPool>, Integer>(poolList, poolList.Count - 1));
                }

                var pair = lookup.get(poolId);

                var members = pair.Item1[pair.Item2].members;
                members.add(poolIndex, questId);
            } while (result.NextRow());
        }

        {
            // load saved spawns from character DB
            var result = DB.characters.query("SELECT pool_id, quest_id FROM pool_quest_save");

            if (!result.isEmpty()) {
                ArrayList<Integer> unknownPoolIds = new ArrayList<>();

                do {
                    var poolId = result.<Integer>Read(0);
                    var questId = result.<Integer>Read(1);

                    var it = lookup.get(poolId);

                    if (it == null || it.Item1 == null) {
                        Logs.SQL.error("Table `pool_quest_save` contains reference to non-existant quest pool %u. Deleted.", poolId);
                        unknownPoolIds.add(poolId);

                        continue;
                    }

                    it.Item1[it.Item2].activeQuests.add(questId);
                } while (result.NextRow());

                var trans0 = new SQLTransaction();

                for (var poolId : unknownPoolIds) {
                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_POOL_QUEST_SAVE);
                    stmt.AddValue(0, poolId);
                    trans0.append(stmt);
                }

                DB.characters.CommitTransaction(trans0);
            }
        }

        // post-processing and sanity checks
        var trans = new SQLTransaction();

        for (var pair : lookup.entrySet()) {
            if (pair.getValue().Item1 == null) {
                continue;
            }

            var pool = pair.getValue().Item1[pair.getValue().Item2];

            if (pool.members.count < pool.numActive) {
                Logs.SQL.error(String.format("Table `quest_pool_template` contains quest pool %1$s requesting %2$s spawns, but only has %3$s members. Requested spawns reduced.", pool.poolId, pool.numActive, pool.members.count));
                pool.numActive = (int) pool.members.count;
            }

            var doRegenerate = pool.activeQuests.isEmpty();

            if (!doRegenerate) {
                ArrayList<Integer> accountedFor = new ArrayList<>();
                int activeCount = 0;

                for (var i = (int) pool.members.count; (i--) != 0; ) {
                    var member = pool.Members[i];

                    if (member.isEmpty()) {
                        Logs.SQL.error(String.format("Table `quest_pool_members` contains no entries at index %1$s for quest pool %2$s. Index removed.", i, pool.poolId));
                        pool.members.remove(i);

                        continue;
                    }

                    // check if the first member is active
                    var status = pool.activeQuests.contains(member[0]);

                    // temporarily remove any spawns that are accounted for
                    if (status) {
                        accountedFor.add(member[0]);
                        pool.activeQuests.remove(member[0]);
                    }

                    // now check if all other members also have the same status, and warn if not
                    for (var id : member) {
                        var otherStatus = pool.activeQuests.contains(id);

                        if (status != otherStatus) {
                            Log.outWarn(LogFilter.Sql, String.format("Table `pool_quest_save` %1$s quest %2$s (in pool %3$s, index %4$s) saved, but its index is%5$s ", (status ? "does not have" : "has"), id, pool.poolId, i, (status ? "" : " not")) + String.format("active (because quest %1$s is%2$s in the table). Set quest %3$s to %4$sactive.", member[0], (status ? "" : " not"), id, (status ? "" : "in")));
                        }

                        if (otherStatus) {
                            pool.activeQuests.remove(id);
                        }

                        if (status) {
                            accountedFor.add(id);
                        }
                    }

                    if (status) {
                        ++activeCount;
                    }
                }

                // warn for any remaining active spawns (not part of the pool)
                for (var quest : pool.activeQuests) {
                    Log.outWarn(LogFilter.Sql, String.format("Table `pool_quest_save` has saved quest %1$s for pool %2$s, but that quest is not part of the pool. Skipped.", quest, pool.poolId));
                }

                // only the previously-found spawns should actually be active
                pool.activeQuests = accountedFor;

                if (activeCount != pool.numActive) {
                    doRegenerate = true;
                    Logs.SQL.error(String.format("Table `pool_quest_save` has %1$s active members saved for pool %2$s, which requests %3$s active members. Pool spawns re-generated.", activeCount, pool.poolId, pool.numActive));
                }
            }

            if (doRegenerate) {
                regeneratePool(pool);
                saveToDB(pool, trans);
            }

            for (var memberKey : pool.members.keySet()) {
                for (var quest : pool.Members[memberKey]) {
                    if (poolLookup.containsKey(quest)) {
                        Logs.SQL.error(String.format("Table `quest_pool_members` lists quest %1$s as member of pool %2$s, but it is already a member of pool %3$s. Skipped.", quest, pool.poolId, poolLookup.get(quest).poolId));

                        continue;
                    }

                    poolLookup.put(quest, pool);
                }
            }
        }

        DB.characters.CommitTransaction(trans);

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s daily, %2$s weekly and %3$s monthly quest pools in %4$s ms", dailyPools.size(), weeklyPools.size(), monthlyPools.size(), time.GetMSTimeDiffToNow(oldMSTime)));
    }

    // the storage structure ends up making this kind of inefficient
    // we don't use it in practice (only in debug commands), so that's fine
    public final QuestPool findQuestPool(int poolId) {

//		bool lambda(QuestPool p)
//			{
//				return p.poolId == poolId;
//			}

        ;

        var questPool = tangible.ListHelper.find(dailyPools, lambda);

        if (questPool != null) {
            return questPool;
        }

        questPool = tangible.ListHelper.find(weeklyPools, lambda);

        if (questPool != null) {
            return questPool;
        }

        questPool = tangible.ListHelper.find(monthlyPools, lambda);

        if (questPool != null) {
            return questPool;
        }

        return null;
    }

    public final boolean isQuestActive(int questId) {
        var it = poolLookup.get(questId);

        if (it == null) // not pooled
        {
            return true;
        }

        return it.activeQuests.contains(questId);
    }

    public final void changeDailyQuests() {
        regenerate(dailyPools);
    }

    public final void changeWeeklyQuests() {
        regenerate(weeklyPools);
    }

    public final void changeMonthlyQuests() {
        regenerate(monthlyPools);
    }

    public final boolean isQuestPooled(int questId) {
        return poolLookup.containsKey(questId);
    }

    private void regenerate(ArrayList<QuestPool> pools) {
        var trans = new SQLTransaction();

        for (var pool : pools) {
            regeneratePool(pool);
            saveToDB(pool, trans);
        }

        DB.characters.CommitTransaction(trans);
    }
}
