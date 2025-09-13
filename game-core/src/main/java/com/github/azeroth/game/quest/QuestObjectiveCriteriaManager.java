package com.github.azeroth.game.quest;


import com.github.azeroth.game.achievement.*;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.domain.quest.QuestStatus;
import com.github.azeroth.game.domain.quest.QuestObjective;

import java.util.ArrayList;

class QuestObjectiveCriteriaManager extends CriteriaHandler {
    private final Player owner;
    private final ArrayList<Integer> completedObjectives = new ArrayList<>();

    public QuestObjectiveCriteriaManager(Player owner) {
        owner = owner;
    }

    public static void deleteFromDB(ObjectGuid guid) {
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA);
        stmt.AddValue(0, guid.getCounter());
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA_PROGRESS);
        stmt.AddValue(0, guid.getCounter());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }

    public final void checkAllQuestObjectiveCriteria(Player referencePlayer) {
        // suppress sending packets
        for (CriteriaType i = 0; i.getValue() < CriteriaType.count.getValue(); ++i) {
            updateCriteria(i, 0, 0, 0, null, referencePlayer);
        }
    }

    @Override
    public void reset() {
        for (var pair : criteriaProgress.entrySet()) {
            sendCriteriaProgressRemoved(pair.getKey());
        }

        criteriaProgress.clear();

        deleteFromDB(owner.getGUID());

        // re-fill data
        checkAllQuestObjectiveCriteria(owner);
    }

    public final void loadFromDB(SQLResult objectiveResult, SQLResult criteriaResult) {
        if (!objectiveResult.isEmpty()) {
            do {
                var objectiveId = objectiveResult.<Integer>Read(0);

                var objective = global.getObjectMgr().getQuestObjective(objectiveId);

                if (objective == null) {
                    continue;
                }

                completedObjectives.add(objectiveId);
            } while (objectiveResult.NextRow());
        }

        if (!criteriaResult.isEmpty()) {
            var now = gameTime.GetGameTime();

            do {
                var criteriaId = criteriaResult.<Integer>Read(0);
                var counter = criteriaResult.<Long>Read(1);
                var date = criteriaResult.<Long>Read(2);

                var criteria = global.getCriteriaMgr().getCriteria(criteriaId);

                if (criteria == null) {
                    // Removing non-existing criteria data for all character
                    Log.outError(LogFilter.player, String.format("Non-existing quest objective criteria %1$s data has been removed from the table `character_queststatus_objectives_criteria_progress`.", criteriaId));

                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INVALID_QUEST_PROGRESS_CRITERIA);
                    stmt.AddValue(0, criteriaId);
                    DB.characters.execute(stmt);

                    continue;
                }

                if (criteria.entry.startTimer != 0 && date + criteria.entry.startTimer < now) {
                    continue;
                }

                CriteriaProgress progress = new criteriaProgress();
                progress.counter = counter;
                progress.date = date;
                progress.changed = false;

                criteriaProgress.put(criteriaId, progress);
            } while (criteriaResult.NextRow());
        }
    }

    public final void saveToDB(SQLTransaction trans) {
        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA);
        stmt.AddValue(0, owner.getGUID().getCounter());
        trans.append(stmt);

        if (!completedObjectives.isEmpty()) {
            for (var completedObjectiveId : completedObjectives) {
                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA);
                stmt.AddValue(0, owner.getGUID().getCounter());
                stmt.AddValue(1, completedObjectiveId);
                trans.append(stmt);
            }
        }

        if (!criteriaProgress.isEmpty()) {
            for (var pair : criteriaProgress.entrySet()) {
                if (!pair.getValue().changed) {
                    continue;
                }

                stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA_PROGRESS_BY_CRITERIA);
                stmt.AddValue(0, owner.getGUID().getCounter());
                stmt.AddValue(1, pair.getKey());
                trans.append(stmt);

                if (pair.getValue().counter != 0) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_QUESTSTATUS_OBJECTIVES_CRITERIA_PROGRESS);
                    stmt.AddValue(0, owner.getGUID().getCounter());
                    stmt.AddValue(1, pair.getKey());
                    stmt.AddValue(2, pair.getValue().counter);
                    stmt.AddValue(3, pair.getValue().date);
                    trans.append(stmt);
                }

                pair.getValue().changed = false;
            }
        }
    }

    public final void resetCriteria(CriteriaFailEvent failEvent, int failAsset, boolean evenIfCriteriaComplete) {
        Log.outDebug(LogFilter.player, String.format("QuestObjectiveCriteriaMgr.resetCriteria(%1$s, %2$s, %3$s)", failEvent, failAsset, evenIfCriteriaComplete));

        // disable for gamemasters with GM-mode enabled
        if (owner.isGameMaster()) {
            return;
        }

        var playerCriteriaList = global.getCriteriaMgr().getCriteriaByFailEvent(failEvent, (int) failAsset);

        for (var playerCriteria : playerCriteriaList) {
            var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(playerCriteria.id);
            var allComplete = true;

            for (var tree : trees) {
                // don't update already completed criteria if not forced
                if (!(isCompletedCriteriaTree(tree) && !evenIfCriteriaComplete)) {
                    allComplete = false;

                    break;
                }
            }

            if (allComplete) {
                continue;
            }

            removeCriteriaProgress(playerCriteria);
        }
    }

    public final void resetCriteriaTree(int criteriaTreeId) {
        var tree = global.getCriteriaMgr().getCriteriaTree(criteriaTreeId);

        if (tree == null) {
            return;
        }

        CriteriaManager.walkCriteriaTree(tree, criteriaTree ->
        {
            removeCriteriaProgress(criteriaTree.criteria);
        });
    }

    @Override
    public void sendAllData(Player receiver) {
        for (var pair : criteriaProgress.entrySet()) {
            CriteriaUpdate criteriaUpdate = new CriteriaUpdate();

            criteriaUpdate.criteriaID = pair.getKey();
            criteriaUpdate.quantity = pair.getValue().counter;
            criteriaUpdate.playerGUID = owner.getGUID();
            criteriaUpdate.flags = 0;

            criteriaUpdate.currentTime = pair.getValue().date;
            criteriaUpdate.creationTime = 0;

            sendPacket(criteriaUpdate);
        }
    }

    public final boolean hasCompletedObjective(QuestObjective questObjective) {
        return completedObjectives.contains(questObjective.id);
    }

    @Override
    public void sendCriteriaUpdate(Criteria criteria, CriteriaProgress progress, Duration timeElapsed, boolean timedCompleted) {
        CriteriaUpdate criteriaUpdate = new CriteriaUpdate();

        criteriaUpdate.criteriaID = criteria.id;
        criteriaUpdate.quantity = progress.counter;
        criteriaUpdate.playerGUID = owner.getGUID();
        criteriaUpdate.flags = 0;

        if (criteria.entry.startTimer != 0) {
            criteriaUpdate.flags = timedCompleted ? 1 : 0; // 1 is for keeping the counter at 0 in client
        }

        criteriaUpdate.currentTime = progress.date;
        criteriaUpdate.elapsedTime = (int) timeElapsed.TotalSeconds;
        criteriaUpdate.creationTime = 0;

        sendPacket(criteriaUpdate);
    }

    @Override
    public void sendCriteriaProgressRemoved(int criteriaId) {
        CriteriaDeleted criteriaDeleted = new CriteriaDeleted();
        criteriaDeleted.criteriaID = criteriaId;
        sendPacket(criteriaDeleted);
    }

    @Override
    public boolean canUpdateCriteriaTree(Criteria criteria, CriteriaTree tree, Player referencePlayer) {
        var objective = tree.questObjective;

        if (objective == null) {
            return false;
        }

        if (hasCompletedObjective(objective)) {
            Log.outTrace(LogFilter.player, String.format("QuestObjectiveCriteriaMgr.CanUpdateCriteriaTree: (Id: %1$s Type %2$s Quest Objective %3$s) Objective already completed", criteria.id, criteria.entry.type, objective.id));

            return false;
        }

        if (owner.getQuestStatus(objective.questID) != QuestStatus.INCOMPLETE) {
            Log.outTrace(LogFilter.achievement, String.format("QuestObjectiveCriteriaMgr.CanUpdateCriteriaTree: (Id: %1$s Type %2$s Quest Objective %3$s) Not on quest", criteria.id, criteria.entry.type, objective.id));

            return false;
        }

        var quest = global.getObjectMgr().getQuestTemplate(objective.questID);

        if (owner.getGroup() && owner.getGroup().isRaidGroup() && !quest.isAllowedInRaid(referencePlayer.getMap().getDifficultyID())) {
            Log.outTrace(LogFilter.achievement, String.format("QuestObjectiveCriteriaMgr.CanUpdateCriteriaTree: (Id: %1$s Type %2$s Quest Objective %3$s) Quest cannot be completed in raid group", criteria.id, criteria.entry.type, objective.id));

            return false;
        }

        var slot = owner.findQuestSlot(objective.questID);

        if (slot >= SharedConst.MaxQuestLogSize || !owner.isQuestObjectiveCompletable(slot, quest, objective)) {
            Log.outTrace(LogFilter.achievement, String.format("QuestObjectiveCriteriaMgr.CanUpdateCriteriaTree: (Id: %1$s Type %2$s Quest Objective %3$s) Objective not completable", criteria.id, criteria.entry.type, objective.id));

            return false;
        }

        return super.canUpdateCriteriaTree(criteria, tree, referencePlayer);
    }

    @Override
    public boolean canCompleteCriteriaTree(CriteriaTree tree) {
        var objective = tree.questObjective;

        if (objective == null) {
            return false;
        }

        return super.canCompleteCriteriaTree(tree);
    }

    @Override
    public void completedCriteriaTree(CriteriaTree tree, Player referencePlayer) {
        var objective = tree.questObjective;

        if (objective == null) {
            return;
        }

        completedObjective(objective, referencePlayer);
    }

    @Override
    public void sendPacket(ServerPacket data) {
        owner.sendPacket(data);
    }

    @Override
    public String getOwnerInfo() {
        return String.format("%1$s %2$s", owner.getGUID(), owner.getName());
    }

    @Override
    public ArrayList<criteria> getCriteriaByType(CriteriaType type, int asset) {
        return global.getCriteriaMgr().getQuestObjectiveCriteriaByType(type);
    }

    private void completedObjective(QuestObjective questObjective, Player referencePlayer) {
        if (hasCompletedObjective(questObjective)) {
            return;
        }

        owner.killCreditCriteriaTreeObjective(questObjective);

        Log.outInfo(LogFilter.player, String.format("QuestObjectiveCriteriaMgr.completedObjective(%1$s). %2$s", questObjective.id, getOwnerInfo()));

        completedObjectives.add(questObjective.id);
    }
}
