package com.github.azeroth.game.achievement;


import com.github.azeroth.game.chat.BroadcastTextBuilder;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.mail.MailDraft;
import com.github.azeroth.game.mail.MailSender;
import com.github.azeroth.game.map.LocalizedDo;
import com.github.azeroth.game.map.PlayerDistWorker;
import com.github.azeroth.game.map.grid.Cell;
import com.github.azeroth.game.listener.interfaces.iachievement.IAchievementOnCompleted;
import game.ObjectManager;
import game.WorldConfig;

import java.util.ArrayList;
import java.util.locale;


public class PlayerAchievementMgr extends AchievementManager {
    private final Player owner;

    public PlayerAchievementMgr(Player owner) {
        owner = owner;
    }

    public static void deleteFromDB(ObjectGuid guid) {
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_ACHIEVEMENT);
        stmt.AddValue(0, guid.getCounter());
        DB.characters.execute(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_ACHIEVEMENT_PROGRESS);
        stmt.AddValue(0, guid.getCounter());
        DB.characters.execute(stmt);

        DB.characters.CommitTransaction(trans);
    }

    @Override
    public void reset() {
        super.reset();

        for (var iter : completedAchievements.entrySet()) {
            AchievementDeleted achievementDeleted = new AchievementDeleted();
            achievementDeleted.achievementID = iter.getKey();
            sendPacket(achievementDeleted);
        }

        completedAchievements.clear();
        achievementPoints = 0;
        deleteFromDB(owner.getGUID());

        // re-fill data
        checkAllAchievementCriteria(owner);
    }

    public final void loadFromDB(SQLResult achievementResult, SQLResult criteriaResult) {
        if (!achievementResult.isEmpty()) {
            do {
                var achievementid = achievementResult.<Integer>Read(0);

                // must not happen: cleanup at server startup in sAchievementMgr.loadCompletedAchievements()
                var achievement = CliDB.AchievementStorage.get(achievementid);

                if (achievement == null) {
                    continue;
                }

                CompletedAchievementData ca = new CompletedAchievementData();
                ca.date = achievementResult.<Long>Read(1);
                ca.changed = false;

                achievementPoints += achievement.points;

                // title achievement rewards are retroactive
                var reward = global.getAchievementMgr().getAchievementReward(achievement);

                if (reward != null) {
                    var titleId = reward.TitleId[Player.teamForRace(owner.getRace()) == Team.ALLIANCE ? 0 : 1];

                    if (titleId != 0) {
                        var titleEntry = CliDB.CharTitlesStorage.get(titleId);

                        if (titleEntry != null) {
                            owner.setTitle(titleEntry);
                        }
                    }
                }

                completedAchievements.put(achievementid, ca);
            } while (achievementResult.NextRow());
        }

        if (!criteriaResult.isEmpty()) {
            var now = GameTime.getGameTime();

            do {
                var id = criteriaResult.<Integer>Read(0);
                var counter = criteriaResult.<Long>Read(1);
                var date = criteriaResult.<Long>Read(2);

                var criteria = global.getCriteriaMgr().getCriteria(id);

                if (criteria == null) {
                    // Removing non-existing criteria data for all character
                    Log.outError(LogFilter.achievement, "Non-existing achievement criteria {0} data removed from table `character_achievement_progress`.", id);

                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INVALID_ACHIEV_PROGRESS_CRITERIA);
                    stmt.AddValue(0, id);
                    DB.characters.execute(stmt);

                    continue;
                }

                if (criteria.entry.startTimer != 0 && (date + criteria.entry.startTimer) < now) {
                    continue;
                }

                CriteriaProgress progress = new criteriaProgress();
                progress.counter = counter;
                progress.date = date;
                progress.playerGUID = owner.getGUID();
                progress.changed = false;

                criteriaProgress.put(id, progress);
            } while (criteriaResult.NextRow());
        }
    }

    public final void saveToDB(SQLTransaction trans) {
        if (!completedAchievements.isEmpty()) {
            for (var pair : completedAchievements.entrySet()) {
                if (!pair.getValue().changed) {
                    continue;
                }

                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_ACHIEVEMENT_BY_ACHIEVEMENT);
                stmt.AddValue(0, pair.getKey());
                stmt.AddValue(1, owner.getGUID().getCounter());
                trans.append(stmt);

                stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_ACHIEVEMENT);
                stmt.AddValue(0, owner.getGUID().getCounter());
                stmt.AddValue(1, pair.getKey());
                stmt.AddValue(2, pair.getValue().date);
                trans.append(stmt);

                pair.getValue().changed = false;
            }
        }

        if (!criteriaProgress.isEmpty()) {
            for (var pair : criteriaProgress.entrySet()) {
                if (!pair.getValue().changed) {
                    continue;
                }

                var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_CHAR_ACHIEVEMENT_PROGRESS_BY_CRITERIA);
                stmt.AddValue(0, owner.getGUID().getCounter());
                stmt.AddValue(1, pair.getKey());
                trans.append(stmt);

                if (pair.getValue().counter != 0) {
                    stmt = DB.characters.GetPreparedStatement(CharStatements.INS_CHAR_ACHIEVEMENT_PROGRESS);
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
        Log.outDebug(LogFilter.achievement, String.format("ResetAchievementCriteria(%1$s, %2$s, %3$s)", failEvent, failAsset, evenIfCriteriaComplete));

        // Disable for GameMasters with GM-mode enabled or for players that don't have the related RBAC permission
        if (owner.isGameMaster() || owner.getSession().hasPermission(RBACPermissions.CannotEarnAchievements)) {
            return;
        }

        var achievementCriteriaList = global.getCriteriaMgr().getCriteriaByFailEvent(failEvent, (int) failAsset);

        if (!achievementCriteriaList.isEmpty()) {
            for (var achievementCriteria : achievementCriteriaList) {
                var trees = global.getCriteriaMgr().getCriteriaTreesByCriteria(achievementCriteria.id);
                var allComplete = true;

                for (var tree : trees) {
                    // don't update already completed criteria if not forced or achievement already complete
                    if (!(isCompletedCriteriaTree(tree) && !evenIfCriteriaComplete) || !hasAchieved(tree.achievement.id)) {
                        allComplete = false;

                        break;
                    }
                }

                if (allComplete) {
                    continue;
                }

                removeCriteriaProgress(achievementCriteria);
            }
        }
    }

    @Override
    public void sendAllData(Player receiver) {
        AllAccountCriteria allAccountCriteria = new AllAccountCriteria();
        AllAchievementData achievementData = new AllAchievementData();

        for (var pair : completedAchievements.entrySet()) {
            var achievement = visibleAchievementCheck.invoke(pair);

            if (achievement == null) {
                continue;
            }

            EarnedAchievement earned = new EarnedAchievement();
            earned.id = pair.getKey();
            earned.date = pair.getValue().date;

            if (!achievement.flags.hasFlag(AchievementFlags.Account)) {
                earned.owner = owner.getGUID();
                earned.virtualRealmAddress = earned.nativeRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
            }

            achievementData.data.earned.add(earned);
        }

        for (var pair : criteriaProgress.entrySet()) {
            var criteria = global.getCriteriaMgr().getCriteria(pair.getKey());

            CriteriaProgressPkt progress = new criteriaProgressPkt();
            progress.id = pair.getKey();
            progress.quantity = pair.getValue().counter;
            progress.player = pair.getValue().playerGUID;
            progress.flags = 0;
            progress.date = pair.getValue().date;
            progress.timeFromStart = 0;
            progress.timeFromCreate = 0;
            achievementData.data.progress.add(progress);

            if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.Account)) {
                CriteriaProgressPkt accountProgress = new criteriaProgressPkt();
                accountProgress.id = pair.getKey();
                accountProgress.quantity = pair.getValue().counter;
                accountProgress.player = owner.getSession().getBattlenetAccountGUID();
                accountProgress.flags = 0;
                accountProgress.date = pair.getValue().date;
                accountProgress.timeFromStart = 0;
                accountProgress.timeFromCreate = 0;
                allAccountCriteria.progress.add(accountProgress);
            }
        }

        if (!allAccountCriteria.progress.isEmpty()) {
            sendPacket(allAccountCriteria);
        }

        sendPacket(achievementData);
    }

    public final void sendAchievementInfo(Player receiver) {
        RespondInspectAchievements inspectedAchievements = new RespondInspectAchievements();
        inspectedAchievements.player = owner.getGUID();

        for (var pair : completedAchievements.entrySet()) {
            var achievement = visibleAchievementCheck.invoke(pair);

            if (achievement == null) {
                continue;
            }

            EarnedAchievement earned = new EarnedAchievement();
            earned.id = pair.getKey();
            earned.date = pair.getValue().date;

            if (!achievement.flags.hasFlag(AchievementFlags.Account)) {
                earned.owner = owner.getGUID();
                earned.virtualRealmAddress = earned.nativeRealmAddress = global.getWorldMgr().getVirtualRealmAddress();
            }

            inspectedAchievements.data.earned.add(earned);
        }

        for (var pair : criteriaProgress.entrySet()) {
            CriteriaProgressPkt progress = new criteriaProgressPkt();
            progress.id = pair.getKey();
            progress.quantity = pair.getValue().counter;
            progress.player = pair.getValue().playerGUID;
            progress.flags = 0;
            progress.date = pair.getValue().date;
            progress.timeFromStart = 0;
            progress.timeFromCreate = 0;
            inspectedAchievements.data.progress.add(progress);
        }

        receiver.sendPacket(inspectedAchievements);
    }

    @Override
    public void completedAchievement(AchievementRecord achievement, Player referencePlayer) {
        // Disable for GameMasters with GM-mode enabled or for players that don't have the related RBAC permission
        if (owner.isGameMaster() || owner.getSession().hasPermission(RBACPermissions.CannotEarnAchievements)) {
            return;
        }

        if ((achievement.faction == AchievementFaction.Horde && referencePlayer.getTeam() != Team.Horde) || (achievement.faction == AchievementFaction.Alliance && referencePlayer.getTeam() != Team.ALLIANCE)) {
            return;
        }

        if (achievement.flags.hasFlag(AchievementFlags.counter) || hasAchieved(achievement.id)) {
            return;
        }

        if (achievement.flags.hasFlag(AchievementFlags.ShowInGuildNews)) {
            var guild = referencePlayer.getGuild();

            if (guild) {
                guild.addGuildNews(GuildNews.PlayerAchievement, referencePlayer.getGUID(), (int) (achievement.flags.getValue() & AchievementFlags.ShowInGuildHeader.getValue().getValue()), achievement.id);
            }
        }

        if (!owner.getSession().getPlayerLoading()) {
            sendAchievementEarned(achievement);
        }

        Log.outDebug(LogFilter.achievement, "PlayerAchievementMgr.completedAchievement({0}). {1}", achievement.id, getOwnerInfo());

        CompletedAchievementData ca = new CompletedAchievementData();
        ca.date = GameTime.getGameTime();
        ca.changed = true;
        completedAchievements.put(achievement.id, ca);

        if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
            global.getAchievementMgr().setRealmCompleted(achievement);
        }

        if (!achievement.flags.hasFlag(AchievementFlags.TrackingFlag)) {
            achievementPoints += achievement.points;
        }

        updateCriteria(CriteriaType.EarnAchievement, achievement.id, 0, 0, null, referencePlayer);
        updateCriteria(CriteriaType.EarnAchievementPoints, achievement.points, 0, 0, null, referencePlayer);

        global.getScriptMgr().<IAchievementOnCompleted>RunScript(p -> p.OnCompleted(referencePlayer, achievement), global.getAchievementMgr().getAchievementScriptId(achievement.id));
        // reward items and titles if any
        var reward = global.getAchievementMgr().getAchievementReward(achievement);

        // no rewards
        if (reward == null) {
            return;
        }

        // titles
        //! Currently there's only one achievement that deals with gender-specific titles.
        //! Since no common attributes were found, (not even in titleRewardFlags field)
        //! we explicitly check by ID. Maybe in the future we could move the achievement_reward
        //! condition fields to the condition system.
        var titleId = reward.TitleId[achievement.id == 1793 ? owner.getNativeGender().getValue() : (owner.getTeam() == Team.ALLIANCE ? 0 : 1)];

        if (titleId != 0) {
            var titleEntry = CliDB.CharTitlesStorage.get(titleId);

            if (titleEntry != null) {
                owner.setTitle(titleEntry);
            }
        }

        // mail
        if (reward.senderCreatureId != 0) {
            MailDraft draft = new MailDraft(reward.mailTemplateId);

            if (reward.mailTemplateId == 0) {
                // subject and text
                var subject = reward.subject;
                var text = reward.body;

                var localeConstant = owner.getSession().getSessionDbLocaleIndex();

                if (localeConstant != locale.enUS) {
                    var loc = global.getAchievementMgr().getAchievementRewardLocale(achievement);

                    if (loc != null) {
                        tangible.RefObject<String> tempRef_subject = new tangible.RefObject<String>(subject);
                        ObjectManager.getLocaleString(loc.subject, localeConstant, tempRef_subject);
                        subject = tempRef_subject.refArgValue;
                        tangible.RefObject<String> tempRef_text = new tangible.RefObject<String>(text);
                        ObjectManager.getLocaleString(loc.body, localeConstant, tempRef_text);
                        text = tempRef_text.refArgValue;
                    }
                }

                draft = new MailDraft(subject, text);
            }

            SQLTransaction trans = new SQLTransaction();

            var item = reward.itemId != 0 ? item.createItem(reward.itemId, 1, itemContext.NONE, owner) : null;

            if (item) {
                // save new item before send
                item.saveToDB(trans); // save for prevent lost at next mail load, if send fail then item will deleted

                // item
                draft.addItem(item);
            }

            draft.sendMailTo(trans, owner, new MailSender(MailMessageType.CREATURE, reward.senderCreatureId));
            DB.characters.CommitTransaction(trans);
        }
    }

    public final boolean modifierTreeSatisfied(int modifierTreeId) {
        var modifierTree = global.getCriteriaMgr().getModifierTree(modifierTreeId);

        if (modifierTree != null) {
            return modifierTreeSatisfied(modifierTree, 0, 0, null, owner);
        }

        return false;
    }

    @Override
    public void sendCriteriaUpdate(Criteria criteria, CriteriaProgress progress, Duration timeElapsed, boolean timedCompleted) {
        if (criteria.flagsCu.hasFlag(CriteriaFlagsCu.Account)) {
            AccountCriteriaUpdate criteriaUpdate = new AccountCriteriaUpdate();
            criteriaUpdate.progress.id = criteria.id;
            criteriaUpdate.progress.quantity = progress.counter;
            criteriaUpdate.progress.player = owner.getSession().getBattlenetAccountGUID();
            criteriaUpdate.progress.flags = 0;

            if (criteria.entry.startTimer != 0) {
                criteriaUpdate.progress.flags = timedCompleted ? 1 : 0; // 1 is for keeping the counter at 0 in client
            }

            criteriaUpdate.progress.date = progress.date;
            criteriaUpdate.progress.timeFromStart = (int) timeElapsed.TotalSeconds;
            criteriaUpdate.progress.timeFromCreate = 0;
            sendPacket(criteriaUpdate);
        } else {
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
    }

    @Override
    public void sendCriteriaProgressRemoved(int criteriaId) {
        CriteriaDeleted criteriaDeleted = new CriteriaDeleted();
        criteriaDeleted.criteriaID = criteriaId;
        sendPacket(criteriaDeleted);
    }

    @Override
    public void sendPacket(ServerPacket data) {
        owner.sendPacket(data);
    }

    @Override
    public ArrayList<criteria> getCriteriaByType(CriteriaType type, int asset) {
        return global.getCriteriaMgr().getPlayerCriteriaByType(type, asset);
    }

    @Override
    public String getOwnerInfo() {
        return String.format("%1$s %2$s", owner.getGUID(), owner.getName());
    }

    private void sendAchievementEarned(AchievementRecord achievement) {
        // Don't send for achievements with ACHIEVEMENT_FLAG_HIDDEN
        if (achievement.flags.hasFlag(AchievementFlags.hidden)) {
            return;
        }

        Log.outDebug(LogFilter.achievement, "PlayerAchievementMgr.sendAchievementEarned({0})", achievement.id);

        if (!achievement.flags.hasFlag(AchievementFlags.TrackingFlag)) {
            var guild = global.getGuildMgr().getGuildById(owner.getGuildId());

            if (guild) {
                BroadcastTextBuilder say_builder = new BroadcastTextBuilder(owner, ChatMsg.guildAchievement, (int) BroadcastTextIds.AchivementEarned.getValue(), owner.getNativeGender(), owner, achievement.id);
                var say_do = new LocalizedDo(say_builder);
                guild.broadcastWorker(say_do, owner);
            }

            if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
                // broadcast realm first reached
                BroadcastAchievement serverFirstAchievement = new BroadcastAchievement();
                serverFirstAchievement.name = owner.getName();
                serverFirstAchievement.playerGUID = owner.getGUID();
                serverFirstAchievement.achievementID = achievement.id;
                global.getWorldMgr().sendGlobalMessage(serverFirstAchievement);
            }
            // if player is in world he can tell his friends about new achievement
            else if (owner.isInWorld()) {
                BroadcastTextBuilder builder = new BroadcastTextBuilder(owner, ChatMsg.achievement, (int) BroadcastTextIds.AchivementEarned.getValue(), owner.getNativeGender(), owner, achievement.id);
                var localizer = new LocalizedDo(builder);
                var _worker = new PlayerDistWorker(owner, WorldConfig.getFloatValue(WorldCfg.ListenRangeSay), localizer, gridType.World);
                Cell.visitGrid(owner, _worker, WorldConfig.getFloatValue(WorldCfg.ListenRangeSay));
            }
        }

        AchievementEarned achievementEarned = new AchievementEarned();
        achievementEarned.sender = owner.getGUID();
        achievementEarned.earner = owner.getGUID();
        achievementEarned.earnerNativeRealm = achievementEarned.earnerVirtualRealm = global.getWorldMgr().getVirtualRealmAddress();
        achievementEarned.achievementID = achievement.id;
        achievementEarned.time = GameTime.getGameTime();

        if (!achievement.flags.hasFlag(AchievementFlags.TrackingFlag)) {
            owner.sendMessageToSetInRange(achievementEarned, WorldConfig.getFloatValue(WorldCfg.ListenRangeSay), true);
        } else {
            owner.sendPacket(achievementEarned);
        }
    }
}
