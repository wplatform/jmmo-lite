package com.github.azeroth.game.achievement;


import game.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

public class AchievementGlobalMgr {
    // store achievements by referenced achievement id to speed up lookup
    private final MultiMap<Integer, AchievementRecord> achievementListByReferencedId = new MultiMap<Integer, AchievementRecord>();

    // store realm first achievements
    private final HashMap<Integer, LocalDateTime> allCompletedAchievements = new HashMap<Integer, LocalDateTime>();

    private final HashMap<Integer, AchievementReward> achievementRewards = new HashMap<Integer, AchievementReward>();

    private final HashMap<Integer, AchievementRewardLocale> achievementRewardLocales = new HashMap<Integer, AchievementRewardLocale>();

    private final HashMap<Integer, Integer> achievementScripts = new HashMap<Integer, Integer>();

    private AchievementGlobalMgr() {
    }


    public final ArrayList<AchievementRecord> getAchievementByReferencedId(int id) {
        return achievementListByReferencedId.get(id);
    }

    public final AchievementReward getAchievementReward(AchievementRecord achievement) {
        return achievementRewards.get(achievement.id);
    }

    public final AchievementRewardLocale getAchievementRewardLocale(AchievementRecord achievement) {
        return achievementRewardLocales.get(achievement.id);
    }

    public final boolean isRealmCompleted(AchievementRecord achievement) {
        var time = allCompletedAchievements.get(achievement.id);

        if (time == null) {
            return false;
        }

        if (LocalDateTime.MIN.equals(time)) {
            return false;
        }

        if (LocalDateTime.MAX.equals(time)) {
            return true;
        }

        // Allow completing the realm first kill for entire minute after first person did it
        // it may allow more than one group to achieve it (highly unlikely)
        // but apparently this is how blizz handles it as well
        if (achievement.flags.hasFlag(AchievementFlags.RealmFirstKill)) {
            return (LocalDateTime.now() - time) > durationofMinutes(1);
        }

        return true;
    }

    public final void setRealmCompleted(AchievementRecord achievement) {
        if (isRealmCompleted(achievement)) {
            return;
        }

        allCompletedAchievements.put(achievement.id, LocalDateTime.now());
    }

    //==========================================================
    public final void loadAchievementReferenceList() {
        var oldMSTime = System.currentTimeMillis();

        if (CliDB.AchievementStorage.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 achievement references.");

            return;
        }

        int count = 0;

        for (var achievement : CliDB.AchievementStorage.values()) {
            if (achievement.SharesCriteria == 0) {
                continue;
            }

            achievementListByReferencedId.add(achievement.SharesCriteria, achievement);
            ++count;
        }

        // Once Bitten, Twice Shy (10 player) - Icecrown Citadel
        var achievement1 = CliDB.AchievementStorage.get(4539);

        if (achievement1 != null) {
            achievement1.instanceID = 631; // Correct map requirement (currently has Ulduar); 6.0.3 note - it STILL has ulduar requirement
        }

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} achievement references in {1} ms.", count, time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadAchievementScripts() {
        var oldMSTime = System.currentTimeMillis();

        achievementScripts.clear(); // need for reload case

        var result = DB.World.query("SELECT achievementId, ScriptName FROM achievement_scripts");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 achievement scripts. DB table `achievement_scripts` is empty.");

            return;
        }

        do {
            var achievementId = result.<Integer>Read(0);
            var scriptName = result.<String>Read(1);

            var achievement = CliDB.AchievementStorage.get(achievementId);

            if (achievement == null) {
                Logs.SQL.error(String.format("Table `achievement_scripts` contains non-existing achievement (ID: %1$s), skipped.", achievementId));

                continue;
            }

            achievementScripts.put(achievementId, global.getObjectMgr().getScriptId(scriptName));
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, String.format("Loaded %1$s achievement scripts in %2$s ms", achievementScripts.size(), time.GetMSTimeDiffToNow(oldMSTime)));
    }

    public final void loadCompletedAchievements() {
        var oldMSTime = System.currentTimeMillis();

        // Populate _allCompletedAchievements with all realm first achievement ids to make multithreaded access safer
        // while it will not prevent races, it will prevent crashes that happen because std::unordered_map key was added
        // instead the only potential race will happen on value associated with the key
        for (var achievement : CliDB.AchievementStorage.values()) {
            if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
                allCompletedAchievements.put(achievement.id, LocalDateTime.MIN);
            }
        }

        var result = DB.characters.query("SELECT achievement FROM character_achievement GROUP BY achievement");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 realm first completed achievements. DB table `character_achievement` is empty.");

            return;
        }

        do {
            var achievementId = result.<Integer>Read(0);
            var achievement = CliDB.AchievementStorage.get(achievementId);

            if (achievement == null) {
                // Remove non-existing achievements from all character
                Log.outError(LogFilter.achievement, "Non-existing achievement {0} data has been removed from the table `character_achievement`.", achievementId);

                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INVALID_ACHIEVMENT);
                stmt.AddValue(0, achievementId);
                DB.characters.execute(stmt);

                continue;
            } else if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
                allCompletedAchievements.put(achievementId, LocalDateTime.MAX);
            }
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} realm first completed achievements in {1} ms.", allCompletedAchievements.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadRewards() {
        var oldMSTime = System.currentTimeMillis();

        achievementRewards.clear(); // need for reload case

        //                                         0   1       2       3       4       5        6     7
        var result = DB.World.query("SELECT ID, TitleA, TitleH, itemID, sender, subject, body, MailTemplateID FROM achievement_reward");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, ">> Loaded 0 achievement rewards. DB table `achievement_reward` is empty.");

            return;
        }

        do {
            var id = result.<Integer>Read(0);
            var achievement = CliDB.AchievementStorage.get(id);

            if (achievement == null) {
                Logs.SQL.error(String.format("Table `achievement_reward` contains a wrong achievement ID (%1$s), ignored.", id));

                continue;
            }

            AchievementReward reward = new AchievementReward();
            reward.TitleId[0] = result.<Integer>Read(1);
            reward.TitleId[1] = result.<Integer>Read(2);
            reward.itemId = result.<Integer>Read(3);
            reward.senderCreatureId = result.<Integer>Read(4);
            reward.subject = result.<String>Read(5);
            reward.body = result.<String>Read(6);
            reward.mailTemplateId = result.<Integer>Read(7);

            // must be title or mail at least
            if (reward.TitleId[0] == 0 && reward.TitleId[1] == 0 && reward.senderCreatureId == 0) {
                Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) does not contain title or item reward data. Ignored.", id));

                continue;
            }

            if (achievement.faction == AchievementFaction.Any && (reward.TitleId[0] == 0 ^ reward.TitleId[1] == 0)) {
                Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) contains the title (A: %2$s H: %3$s) for only one team.", id, reward.TitleId[0], reward.TitleId[1]));
            }

            if (reward.TitleId[0] != 0) {
                var titleEntry = CliDB.CharTitlesStorage.get(reward.TitleId[0]);

                if (titleEntry == null) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) contains an invalid title ID (%2$s) in `title_A`, set to 0", id, reward.TitleId[0]));
                    reward.TitleId[0] = 0;
                }
            }

            if (reward.TitleId[1] != 0) {
                var titleEntry = CliDB.CharTitlesStorage.get(reward.TitleId[1]);

                if (titleEntry == null) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) contains an invalid title ID (%2$s) in `title_H`, set to 0", id, reward.TitleId[1]));
                    reward.TitleId[1] = 0;
                }
            }

            //check mail data before item for report including wrong item case
            if (reward.senderCreatureId != 0) {
                if (global.getObjectMgr().getCreatureTemplate(reward.senderCreatureId) == null) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) contains an invalid creature ID %2$s as sender, mail reward skipped.", id, reward.senderCreatureId));
                    reward.senderCreatureId = 0;
                }
            } else {
                if (reward.itemId != 0) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) does not have sender data, but contains an item reward. Item will not be rewarded.", id));
                }

                if (!reward.subject.isEmpty()) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) does not have sender data, but contains a mail subject.", id));
                }

                if (!reward.body.isEmpty()) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) does not have sender data, but contains mail text.", id));
                }

                if (reward.mailTemplateId != 0) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) does not have sender data, but has a mailTemplateId.", id));
                }
            }

            if (reward.mailTemplateId != 0) {
                if (!CliDB.MailTemplateStorage.containsKey(reward.mailTemplateId)) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) is using an invalid mailTemplateId (%2$s).", id, reward.mailTemplateId));
                    reward.mailTemplateId = 0;
                } else if (!reward.subject.isEmpty() || !reward.body.isEmpty()) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) is using mailTemplateId (%2$s) and mail subject/text.", id, reward.mailTemplateId));
                }
            }

            if (reward.itemId != 0) {
                if (global.getObjectMgr().getItemTemplate(reward.itemId) == null) {
                    Logs.SQL.error(String.format("Table `achievement_reward` (ID: %1$s) contains an invalid item id %2$s, reward mail will not contain the rewarded item.", id, reward.itemId));
                    reward.itemId = 0;
                }
            }

            achievementRewards.put(id, reward);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} achievement rewards in {1} ms.", achievementRewards.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }

    public final void loadRewardLocales() {
        var oldMSTime = System.currentTimeMillis();

        achievementRewardLocales.clear(); // need for reload case

        //                                         0   1       2        3
        var result = DB.World.query("SELECT ID, locale, subject, Body FROM achievement_reward_locale");

        if (result.isEmpty()) {
            Log.outInfo(LogFilter.ServerLoading, "Loaded 0 achievement reward locale strings.  DB table `achievement_reward_locale` is empty.");

            return;
        }

        do {
            var id = result.<Integer>Read(0);
            var localeName = result.<String>Read(1);

            if (!achievementRewards.containsKey(id)) {
                Logs.SQL.error(String.format("Table `achievement_reward_locale` (ID: %1$s) contains locale strings for a non-existing achievement reward.", id));

                continue;
            }

            AchievementRewardLocale data = new AchievementRewardLocale();
            var locale = localeName.<locale>ToEnum();

            if (!SharedConst.IsValidLocale(locale) || locale == locale.enUS) {
                continue;
            }

            ObjectManager.addLocaleString(result.<String>Read(2), locale, data.subject);
            ObjectManager.addLocaleString(result.<String>Read(3), locale, data.body);

            achievementRewardLocales.put(id, data);
        } while (result.NextRow());

        Log.outInfo(LogFilter.ServerLoading, "Loaded {0} achievement reward locale strings in {1} ms.", achievementRewardLocales.size(), time.GetMSTimeDiffToNow(oldMSTime));
    }


    public final int getAchievementScriptId(int achievementId) {
        return achievementScripts.get(achievementId);
    }
}
