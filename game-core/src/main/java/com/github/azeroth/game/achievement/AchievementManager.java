package com.github.azeroth.game.achievement;


import com.github.azeroth.game.entity.player.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;


public class AchievementManager extends CriteriaHandler {
    public tangible.Func1Param<Map.entry<Integer, CompletedAchievementData>, AchievementRecord> visibleAchievementCheck = value ->
    {
        var achievement = CliDB.AchievementStorage.get(value.key);

        if (achievement != null && !achievement.flags.hasFlag(AchievementFlags.hidden)) {
            return achievement;
        }

        return null;
    };

    protected HashMap<Integer, CompletedAchievementData> completedAchievements = new HashMap<Integer, CompletedAchievementData>();
    protected int achievementPoints;

    public final int getAchievementPoints() {
        return achievementPoints;
    }

    public final Collection<Integer> getCompletedAchievementIds() {
        return completedAchievements.keySet();
    }

    /**
     * called at player login. The player might have fulfilled some achievements when the achievement system wasn't working yet
     *
     * @param referencePlayer
     */
    public final void checkAllAchievementCriteria(Player referencePlayer) {
        // suppress sending packets
        for (CriteriaType i = 0; i.getValue() < CriteriaType.count.getValue(); ++i) {
            updateCriteria(i, 0, 0, 0, null, referencePlayer);
        }
    }

    public final boolean hasAchieved(int achievementId) {
        return completedAchievements.containsKey(achievementId);
    }

    @Override
    public boolean canUpdateCriteriaTree(Criteria criteria, CriteriaTree tree, Player referencePlayer) {
        var achievement = tree.achievement;

        if (achievement == null) {
            return false;
        }

        if (hasAchieved(achievement.id)) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteriaTree: (Id: {0} Type {1} Achievement {2}) Achievement already earned", criteria.id, criteria.entry.type, achievement.id);

            return false;
        }

        if (achievement.instanceID != -1 && referencePlayer.getLocation().getMapId() != achievement.instanceID) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteriaTree: (Id: {0} Type {1} Achievement {2}) Wrong map", criteria.id, criteria.entry.type, achievement.id);

            return false;
        }

        if ((achievement.faction == AchievementFaction.Horde && referencePlayer.getTeam() != Team.Horde) || (achievement.faction == AchievementFaction.Alliance && referencePlayer.getTeam() != Team.ALLIANCE)) {
            Log.outTrace(LogFilter.achievement, "CanUpdateCriteriaTree: (Id: {0} Type {1} Achievement {2}) Wrong faction", criteria.id, criteria.entry.type, achievement.id);

            return false;
        }

        // Don't update realm first achievements if the player's account isn't allowed to do so
        if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
            if (referencePlayer.getSession().hasPermission(RBACPermissions.CannotEarnRealmFirstAchievements)) {
                return false;
            }
        }

        if (achievement.covenantID != 0 && referencePlayer.getPlayerData().covenantID != achievement.covenantID) {
            Log.outTrace(LogFilter.achievement, String.format("CanUpdateCriteriaTree: (Id: %1$s Type %2$s Achievement %3$s) Wrong covenant", criteria.id, criteria.entry.type, achievement.id));

            return false;
        }

        return super.canUpdateCriteriaTree(criteria, tree, referencePlayer);
    }

    @Override
    public boolean canCompleteCriteriaTree(CriteriaTree tree) {
        var achievement = tree.achievement;

        if (achievement == null) {
            return false;
        }

        // counter can never complete
        if (achievement.flags.hasFlag(AchievementFlags.counter)) {
            return false;
        }

        if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
            // someone on this realm has already completed that achievement
            if (global.getAchievementMgr().isRealmCompleted(achievement)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public void completedCriteriaTree(CriteriaTree tree, Player referencePlayer) {
        var achievement = tree.achievement;

        if (achievement == null) {
            return;
        }

        // counter can never complete
        if (achievement.flags.hasFlag(AchievementFlags.counter)) {
            return;
        }

        // already completed and stored
        if (hasAchieved(achievement.id)) {
            return;
        }

        if (isCompletedAchievement(achievement)) {
            completedAchievement(achievement, referencePlayer);
        }
    }

    @Override
    public void afterCriteriaTreeUpdate(CriteriaTree tree, Player referencePlayer) {
        var achievement = tree.achievement;

        if (achievement == null) {
            return;
        }

        // check again the completeness for SUMM and REQ COUNT achievements,
        // as they don't depend on the completed criteria but on the sum of the progress of each individual criteria
        if (achievement.flags.hasFlag(AchievementFlags.Summ)) {
            if (isCompletedAchievement(achievement)) {
                completedAchievement(achievement, referencePlayer);
            }
        }

        var achRefList = global.getAchievementMgr().getAchievementByReferencedId(achievement.id);

        for (var refAchievement : achRefList) {
            if (isCompletedAchievement(refAchievement)) {
                completedAchievement(refAchievement, referencePlayer);
            }
        }
    }

    @Override
    public boolean requiredAchievementSatisfied(int achievementId) {
        return hasAchieved(achievementId);
    }

    public void completedAchievement(AchievementRecord entry, Player referencePlayer) {
    }

    private boolean isCompletedAchievement(AchievementRecord entry) {
        // counter can never complete
        if (entry.flags.hasFlag(AchievementFlags.counter)) {
            return false;
        }

        var tree = global.getCriteriaMgr().getCriteriaTree(entry.CriteriaTree);

        if (tree == null) {
            return false;
        }

        // For SUMM achievements, we have to count the progress of each criteria of the achievement.
        // Oddly, the target count is NOT contained in the achievement, but in each individual criteria
        if (entry.flags.hasFlag(AchievementFlags.Summ)) {
            long progress = 0;

            CriteriaManager.walkCriteriaTree(tree, criteriaTree ->
            {
                if (criteriaTree.criteria != null) {
                    var criteriaProgress = getCriteriaProgress(criteriaTree.criteria);

                    if (criteriaProgress != null) {
                        progress += (long) criteriaProgress.counter;
                    }
                }
            });

            return progress >= tree.entry.amount;
        }

        return isCompletedCriteriaTree(tree);
    }
}
