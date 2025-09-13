package com.github.azeroth.game.quest;


import com.github.azeroth.dbc.DbcObjectManager;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.packet.quest.QueryQuestInfoResponse;
import com.github.azeroth.game.networking.packet.quest.QuestRewards;
import com.github.azeroth.game.domain.quest.QuestTagType;
import com.github.azeroth.game.domain.quest.*;
import com.github.azeroth.game.world.setting.WorldSetting;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

import static java.util.logging.Logger.global;

@RequiredArgsConstructor
public class Quest {
    private final QuestTemplate questTemplate;

    private final WorldSetting worldSetting;
    private final DbcObjectManager dbcObjectManager;


    public int XPValue(Player player, int contentTuningId, int xpDifficulty, float xpMultiplier) {
        return XPValue(player, contentTuningId, xpDifficulty, xpMultiplier, -1);
    }

    public int XPValue(Player player, int contentTuningId, int xpDifficulty) {
        return XPValue(player, contentTuningId, xpDifficulty, 1.0f, -1);
    }

    public int XPValue(Player player, int contentTuningId, int xpDifficulty, float xpMultiplier, int expansion) {
        if (player != null) {
            var questLevel = (int) player.getQuestLevel(contentTuningId);
            var questXp = dbcObjectManager.questXp(questLevel);

            if (questXp == null || xpDifficulty >= 10) {
                return 0;
            }

            var diffFactor = 2 * (questLevel - player.getLevel()) + 12;

            if (diffFactor < 1) {
                diffFactor = 1;
            } else if (diffFactor > 10) {
                diffFactor = 10;
            }

            var xp = (int) (diffFactor * questXp.Difficulty[xpDifficulty] * xpMultiplier / 10);

            if (player.getLevel() >= global.getObjectMgr().getMaxLevelForExpansion(expansion.forValue(WorldConfig.getIntValue(WorldCfg.expansion)) - 1) && player.getSession().getExpansion() == expansion.forValue(WorldConfig.getIntValue(WorldCfg.expansion)) && expansion >= 0 && expansion < (int) WorldConfig.getIntValue(WorldCfg.expansion)) {
                xp = (int) (xp / 9.0f);
            }

            xp = roundXPValue(xp);

            if (WorldConfig.getUIntValue(WorldCfg.MinQuestScaledXpRatio) != 0) {
                var minScaledXP = roundXPValue((int) (questXp.Difficulty[xpDifficulty] * xpMultiplier)) * WorldConfig.getUIntValue(WorldCfg.MinQuestScaledXpRatio) / 100;
                xp = Math.max(minScaledXP, xp);
            }

            return xp;
        }

        return 0;
    }

    public static boolean isTakingQuestEnabled(int questId) {
        if (!global.getQuestPoolMgr().isQuestActive(questId)) {
            return false;
        }

        return true;
    }

    public static int roundXPValue(int xp) {
        if (xp <= 100) {
            return 5 * ((xp + 2) / 5);
        } else if (xp <= 500) {
            return 10 * ((xp + 5) / 10);
        } else if (xp <= 1000) {
            return 25 * ((xp + 12) / 25);
        } else {
            return 50 * ((xp + 25) / 50);
        }
    }

    public int getMaxMoneyValue() {
        int value = 0;
        var questLevels = global.getDB2Mgr().GetContentTuningData(contentTuningId, 0);

        if (questLevels != null) {
            var money = CliDB.QuestMoneyRewardStorage.get(questLevels.getValue().maxLevel);

            if (money != null) {
                value = (int) (money.Difficulty[RewardMoneyDifficulty] * rewardMoneyMultiplier);
            }
        }

        return value;
    }

    public int getMaxMoneyReward() {
        return (int) (getMaxMoneyValue() * WorldConfig.getFloatValue(WorldCfg.RateMoneyQuest));
    }

    public QuestTagType getQuestTag() {
        var questInfo = CliDB.QuestInfoStorage.get(questInfoID);

        if (questInfo != null) {
            return QuestTagType.forValue(questInfo.type);
        }

        return null;
    }

    public boolean isAutoAccept() {
        return !WorldConfig.getBoolValue(WorldCfg.QuestIgnoreAutoAccept) && hasFlag(QuestFlag.autoAccept);
    }

    public boolean isAutoComplete() {
        return !WorldConfig.getBoolValue(WorldCfg.QuestIgnoreAutoComplete) && type == questType.AutoComplete;
    }

    public boolean isAutoPush() {
        return questTemplate.flagsEx.hasFlag(QuestFlagEx.AUTO_PUSH);
    }

    public boolean isWorldQuest() {
        return questTemplate.flagsEx.hasFlag(QuestFlagEx.IS_WORLD_QUEST);
    }

    // Possibly deprecated flag
    public boolean isUnavailable() {
        return hasFlag(QuestFlag.Unavailable);
        return questTemplate.flags.hasFlag(QuestFlag.UN);
    }

    // table data accessors:
    public boolean isRepeatable() {
        return getSpecialFlags().hasFlag(QuestSpecialFlag.REPEATABLE);
    }

    public boolean isDaily() {
        return getFlags().hasFlag(QuestFlag.Daily);
    }

    public boolean isWeekly() {
        return getFlags().hasFlag(QuestFlag.Weekly);
    }

    public boolean isMonthly() {
        return getSpecialFlags().hasFlag(QuestSpecialFlag.Monthly);
    }

    public boolean isSeasonal() {
        return (questSortID == -QuestSort.Seasonal.getValue() || questSortID == -QuestSort.Special.getValue() || questSortID == -QuestSort.LunarFestival.getValue() || questSortID == -QuestSort.Midsummer.getValue() || questSortID == -QuestSort.Brewfest.getValue() || questSortID == -QuestSort.LoveIsInTheAir.getValue() || questSortID == -QuestSort.Noblegarden.getValue()) && !isRepeatable();
    }

    public boolean isDailyOrWeekly() {
        return getFlags().hasFlag(QuestFlag.Daily.getValue() | QuestFlag.Weekly.getValue());
    }

    public boolean isDFQuest() {
        return getSpecialFlags().hasFlag(QuestSpecialFlag.DfQuest);
    }

    public int XPValue(Player player) {
        return XPValue(player, contentTuningId, rewardXPDifficulty, rewardXPMultiplier, expansion);
    }

    public int moneyValue(Player player) {
        var money = CliDB.QuestMoneyRewardStorage.get(player.getQuestLevel(this));

        if (money != null) {
            return (int) (money.Difficulty[RewardMoneyDifficulty] * rewardMoneyMultiplier);
        } else {
            return 0;
        }
    }

    public void buildQuestRewards(QuestRewards rewards, Player player) {

    }

    public int getRewMoneyMaxLevel() {
        // If Quest has flag to not give money on max level, it's 0
        if (hasFlag(QuestFlag.NoMoneyFromXp)) {
            return 0;
        }

        // Else, return the rewarded copper sum modified by the rate
        return (int) (RewardBonusMoney * WorldConfig.getFloatValue(WorldCfg.RateMoneyMaxLevelQuest));
    }

    public boolean isRaidQuest(Difficulty difficulty) {
        switch (QuestInfos.forValue(questInfoID)) {
            case Raid:
                return true;
            case Raid10:
                return difficulty == Difficulty.Raid10N || difficulty == Difficulty.Raid10HC;
            case Raid25:
                return difficulty == Difficulty.Raid25N || difficulty == Difficulty.Raid25HC;
            default:
                break;
        }

        if (getFlags().hasFlag(QuestFlag.raid)) {
            return true;
        }

        return false;
    }

    public boolean isAllowedInRaid(Difficulty difficulty) {
        if (isRaidQuest(difficulty)) {
            return true;
        }

        return WorldConfig.getBoolValue(WorldCfg.QuestIgnoreRaid);
    }

    public int calculateHonorGain(int level) {
        int honor = 0;

        return honor;
    }

    public boolean canIncreaseRewardedQuestCounters() {
        // Dungeon Finder/Daily/Repeatable (if not weekly, monthly or seasonal) quests are never considered rewarded serverside.
        // This affects counters and client requests for completed quests.
        return (!isDFQuest() && !isDaily() && (!isRepeatable() || isWeekly() || isMonthly() || isSeasonal()));
    }

    public void initializeQueryData() {
        for (var loc = locale.enUS; loc.getValue() < locale.Total.getValue(); ++loc) {
            response[loc.getValue()] = buildQueryData(loc, null);
        }
    }

    public QueryQuestInfoResponse buildQueryData(Locale loc, Player player) {
        QueryQuestInfoResponse response = new QueryQuestInfoResponse();


        response.write();

        return response;
    }


    public boolean hasFlag(QuestFlag questFlag) {
        return questTemplate.flags.hasFlag(questFlag);
    }

    public boolean hasFlagEx(QuestFlagEx flag) {
        return (questTemplate.flagsEx.hasFlag(flag));
    }

    public boolean hasSpecialFlag(QuestSpecialFlag flag) {
        return questTemplate.specialFlags.hasFlag(flag);
    }

    public void setSpecialFlag(QuestSpecialFlag flag) {
        questTemplate.specialFlags.addFlag(flag);
    }

    public boolean hasQuestObjectiveType(QuestObjectiveType type) {
        return questTemplate.usedQuestObjectiveTypes.get(type.ordinal());
    }

}
