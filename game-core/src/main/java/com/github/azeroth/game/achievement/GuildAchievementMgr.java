package com.github.azeroth.game.achievement;


import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.listener.interfaces.iachievement.IAchievementOnCompleted;

import java.util.ArrayList;

public class GuildAchievementMgr extends AchievementManager {
    private final Guild owner;

    public GuildAchievementMgr(Guild owner) {
        owner = owner;
    }

    public static void deleteFromDB(ObjectGuid guid) {
        SQLTransaction trans = new SQLTransaction();

        var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GUILD_ACHIEVEMENTS);
        stmt.AddValue(0, guid.getCounter());
        trans.append(stmt);

        stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_ALL_GUILD_ACHIEVEMENT_CRITERIA);
        stmt.AddValue(0, guid.getCounter());
        trans.append(stmt);

        DB.characters.CommitTransaction(trans);
    }

    @Override
    public void reset() {
        super.reset();

        var guid = owner.getGUID();

        for (var iter : completedAchievements.entrySet()) {
            GuildAchievementDeleted guildAchievementDeleted = new GuildAchievementDeleted();
            guildAchievementDeleted.achievementID = iter.getKey();
            guildAchievementDeleted.guildGUID = guid;
            guildAchievementDeleted.timeDeleted = GameTime.getGameTime();
            sendPacket(guildAchievementDeleted);
        }

        achievementPoints = 0;
        completedAchievements.clear();
        deleteFromDB(guid);
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

                TValue ca;
                if (completedAchievements.containsKey(achievementid) && (ca = completedAchievements.get(achievementid)) == ca) {
                    ca.date = achievementResult.<Long>Read(1);
                    var guids = new LocalizedString();

                    if (!guids.isEmpty()) {
                        for (var i = 0; i < guids.length; ++i) {
                            long guid;
                            tangible.OutObject<Long> tempOut_guid = new tangible.OutObject<Long>();
                            if (tangible.TryParseHelper.tryParseLong(guids.get(i), tempOut_guid)) {
                                guid = tempOut_guid.outArgValue;
                                ca.completingPlayers.add(ObjectGuid.create(HighGuid.Player, guid));
                            } else {
                                guid = tempOut_guid.outArgValue;
                            }
                        }
                    }

                    ca.changed = false;

                    achievementPoints += achievement.points;
                }
            } while (achievementResult.NextRow());
        }

        if (!criteriaResult.isEmpty()) {
            var now = GameTime.getGameTime();

            do {
                var id = criteriaResult.<Integer>Read(0);
                var counter = criteriaResult.<Long>Read(1);
                var date = criteriaResult.<Long>Read(2);
                var guidLow = criteriaResult.<Long>Read(3);

                var criteria = global.getCriteriaMgr().getCriteria(id);

                if (criteria == null) {
                    // we will remove not existed criteria for all guilds
                    Log.outError(LogFilter.achievement, "Non-existing achievement criteria {0} data removed from table `guild_achievement_progress`.", id);

                    var stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_INVALID_ACHIEV_PROGRESS_CRITERIA_GUILD);
                    stmt.AddValue(0, id);
                    DB.characters.execute(stmt);

                    continue;
                }

                if (criteria.entry.startTimer != 0 && date + criteria.entry.startTimer < now) {
                    continue;
                }

                CriteriaProgress progress = new criteriaProgress();
                progress.counter = counter;
                progress.date = date;
                progress.playerGUID = ObjectGuid.create(HighGuid.Player, guidLow);
                progress.changed = false;

                criteriaProgress.put(id, progress);
            } while (criteriaResult.NextRow());
        }
    }

    public final void saveToDB(SQLTransaction trans) {
        PreparedStatement stmt;
        StringBuilder guidstr = new StringBuilder();

        for (var pair : completedAchievements.entrySet()) {
            if (!pair.getValue().changed) {
                continue;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_ACHIEVEMENT);
            stmt.AddValue(0, owner.getId());
            stmt.AddValue(1, pair.getKey());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_ACHIEVEMENT);
            stmt.AddValue(0, owner.getId());
            stmt.AddValue(1, pair.getKey());
            stmt.AddValue(2, pair.getValue().date);

            for (var guid : pair.getValue().completingPlayers) {
                guidstr.append(String.format("%1$s,", guid.counter));
            }

            stmt.AddValue(3, guidstr.toString());
            trans.append(stmt);

            guidstr.setLength(0);
        }

        for (var pair : criteriaProgress.entrySet()) {
            if (!pair.getValue().changed) {
                continue;
            }

            stmt = DB.characters.GetPreparedStatement(CharStatements.DEL_GUILD_ACHIEVEMENT_CRITERIA);
            stmt.AddValue(0, owner.getId());
            stmt.AddValue(1, pair.getKey());
            trans.append(stmt);

            stmt = DB.characters.GetPreparedStatement(CharStatements.INS_GUILD_ACHIEVEMENT_CRITERIA);
            stmt.AddValue(0, owner.getId());
            stmt.AddValue(1, pair.getKey());
            stmt.AddValue(2, pair.getValue().counter);
            stmt.AddValue(3, pair.getValue().date);
            stmt.AddValue(4, pair.getValue().playerGUID.counter);
            trans.append(stmt);
        }
    }

    @Override
    public void sendAllData(Player receiver) {
        AllGuildAchievements allGuildAchievements = new AllGuildAchievements();

        for (var pair : completedAchievements.entrySet()) {
            var achievement = visibleAchievementCheck.invoke(pair);

            if (achievement == null) {
                continue;
            }

            EarnedAchievement earned = new EarnedAchievement();
            earned.id = pair.getKey();
            earned.date = pair.getValue().date;
            allGuildAchievements.earned.add(earned);
        }

        receiver.sendPacket(allGuildAchievements);
    }


    public final void sendAchievementInfo(Player receiver) {
        sendAchievementInfo(receiver, 0);
    }

    public final void sendAchievementInfo(Player receiver, int achievementId) {
        GuildCriteriaUpdate guildCriteriaUpdate = new GuildCriteriaUpdate();
        var achievement = CliDB.AchievementStorage.get(achievementId);

        if (achievement != null) {
            var tree = global.getCriteriaMgr().getCriteriaTree(achievement.CriteriaTree);

            if (tree != null) {
                CriteriaManager.walkCriteriaTree(tree, node ->
                {
                    if (node.criteria != null) {
                        var progress = criteriaProgress.get(node.criteria.id);

                        if (progress != null) {
                            GuildCriteriaProgress guildCriteriaProgress = new GuildCriteriaProgress();
                            guildCriteriaProgress.criteriaID = node.criteria.id;
                            guildCriteriaProgress.dateCreated = 0;
                            guildCriteriaProgress.dateStarted = 0;
                            guildCriteriaProgress.dateUpdated = progress.date;
                            guildCriteriaProgress.quantity = progress.counter;
                            guildCriteriaProgress.playerGUID = progress.playerGUID;
                            guildCriteriaProgress.flags = 0;

                            guildCriteriaUpdate.progress.add(guildCriteriaProgress);
                        }
                    }
                });
            }
        }

        receiver.sendPacket(guildCriteriaUpdate);
    }


    public final void sendAllTrackedCriterias(Player receiver, ArrayList<Integer> trackedCriterias) {
        GuildCriteriaUpdate guildCriteriaUpdate = new GuildCriteriaUpdate();

        for (var criteriaId : trackedCriterias) {
            var progress = criteriaProgress.get(criteriaId);

            if (progress == null) {
                continue;
            }

            GuildCriteriaProgress guildCriteriaProgress = new GuildCriteriaProgress();
            guildCriteriaProgress.criteriaID = criteriaId;
            guildCriteriaProgress.dateCreated = 0;
            guildCriteriaProgress.dateStarted = 0;
            guildCriteriaProgress.dateUpdated = progress.date;
            guildCriteriaProgress.quantity = progress.counter;
            guildCriteriaProgress.playerGUID = progress.playerGUID;
            guildCriteriaProgress.flags = 0;

            guildCriteriaUpdate.progress.add(guildCriteriaProgress);
        }

        receiver.sendPacket(guildCriteriaUpdate);
    }


    public final void sendAchievementMembers(Player receiver, int achievementId) {
        var achievementData = completedAchievements.get(achievementId);

        if (achievementData != null) {
            GuildAchievementMembers guildAchievementMembers = new GuildAchievementMembers();
            guildAchievementMembers.guildGUID = owner.getGUID();
            guildAchievementMembers.achievementID = achievementId;

            for (var guid : achievementData.completingPlayers) {
                guildAchievementMembers.member.add(guid);
            }

            receiver.sendPacket(guildAchievementMembers);
        }
    }

    @Override
    public void completedAchievement(AchievementRecord achievement, Player referencePlayer) {
        Log.outDebug(LogFilter.achievement, "CompletedAchievement({0})", achievement.id);

        if (achievement.flags.hasFlag(AchievementFlags.counter) || hasAchieved(achievement.id)) {
            return;
        }

        if (achievement.flags.hasFlag(AchievementFlags.ShowInGuildNews)) {
            var guild = referencePlayer.getGuild();

            if (guild) {
                guild.addGuildNews(GuildNews.achievement, ObjectGuid.Empty, (int) (achievement.flags.getValue() & AchievementFlags.ShowInGuildHeader.getValue().getValue()), achievement.id);
            }
        }

        sendAchievementEarned(achievement);
        CompletedAchievementData ca = new CompletedAchievementData();
        ca.date = GameTime.getGameTime();
        ca.changed = true;

        if (achievement.flags.hasFlag(AchievementFlags.ShowGuildMembers)) {
            if (referencePlayer.getGuildId() == owner.getId()) {
                ca.completingPlayers.add(referencePlayer.getGUID());
            }

            var group = referencePlayer.getGroup();

            if (group) {
                for (var refe = group.getFirstMember(); refe != null; refe = refe.next()) {
                    var groupMember = refe.getSource();

                    if (groupMember) {
                        if (groupMember.getGuildId() == owner.getId()) {
                            ca.completingPlayers.add(groupMember.getGUID());
                        }
                    }
                }
            }
        }

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
    }

    @Override
    public void sendCriteriaUpdate(Criteria entry, CriteriaProgress progress, Duration timeElapsed, boolean timedCompleted) {
        GuildCriteriaUpdate guildCriteriaUpdate = new GuildCriteriaUpdate();

        GuildCriteriaProgress guildCriteriaProgress = new GuildCriteriaProgress();
        guildCriteriaProgress.criteriaID = entry.id;
        guildCriteriaProgress.dateCreated = 0;
        guildCriteriaProgress.dateStarted = 0;
        guildCriteriaProgress.dateUpdated = progress.date;
        guildCriteriaProgress.quantity = progress.counter;
        guildCriteriaProgress.playerGUID = progress.playerGUID;
        guildCriteriaProgress.flags = 0;

        guildCriteriaUpdate.progress.add(guildCriteriaProgress);

        owner.broadcastPacketIfTrackingAchievement(guildCriteriaUpdate, entry.id);
    }


    @Override
    public void sendCriteriaProgressRemoved(int criteriaId) {
        GuildCriteriaDeleted guildCriteriaDeleted = new GuildCriteriaDeleted();
        guildCriteriaDeleted.guildGUID = owner.getGUID();
        guildCriteriaDeleted.criteriaID = criteriaId;
        sendPacket(guildCriteriaDeleted);
    }

    @Override
    public void sendPacket(ServerPacket data) {
        owner.broadcastPacket(data);
    }


    @Override
    public ArrayList<criteria> getCriteriaByType(CriteriaType type, int asset) {
        return global.getCriteriaMgr().getGuildCriteriaByType(type);
    }

    @Override
    public String getOwnerInfo() {
        return String.format("Guild ID %1$s %2$s", owner.getId(), owner.getName());
    }

    private void sendAchievementEarned(AchievementRecord achievement) {
        if (achievement.flags.hasFlag(AchievementFlags.RealmFirstReach.getValue() | AchievementFlags.RealmFirstKill.getValue())) {
            // broadcast realm first reached
            BroadcastAchievement serverFirstAchievement = new BroadcastAchievement();
            serverFirstAchievement.name = owner.getName();
            serverFirstAchievement.playerGUID = owner.getGUID();
            serverFirstAchievement.achievementID = achievement.id;
            serverFirstAchievement.guildAchievement = true;
            global.getWorldMgr().sendGlobalMessage(serverFirstAchievement);
        }

        GuildAchievementEarned guildAchievementEarned = new GuildAchievementEarned();
        guildAchievementEarned.achievementID = achievement.id;
        guildAchievementEarned.guildGUID = owner.getGUID();
        guildAchievementEarned.timeEarned = GameTime.getGameTime();
        sendPacket(guildAchievementEarned);
    }
}
