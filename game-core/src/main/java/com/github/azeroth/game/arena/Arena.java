package com.github.azeroth.game.arena;


import com.github.azeroth.game.battleground.Battleground;
import com.github.azeroth.game.battleground.BattlegroundTemplate;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;


public class Arena extends Battleground {
    public ArenaTeamScore[] arenaTeamScores = new ArenaTeamScore[SharedConst.PvpTeamsCount];
    protected taskScheduler taskScheduler = new taskScheduler();

    public Arena(BattlegroundTemplate battlegroundTemplate) {
        super(battlegroundTemplate);
        StartDelayTimes[BattlegroundConst.EventIdFirst] = BattlegroundStartTimeIntervals.Delay1m;
        StartDelayTimes[BattlegroundConst.EventIdSecond] = BattlegroundStartTimeIntervals.Delay30s;
        StartDelayTimes[BattlegroundConst.EventIdThird] = BattlegroundStartTimeIntervals.Delay15s;
        StartDelayTimes[BattlegroundConst.EventIdFourth] = BattlegroundStartTimeIntervals.NONE;

        StartMessageIds[BattlegroundConst.EventIdFirst] = ArenaBroadcastTexts.OneMinute;
        StartMessageIds[BattlegroundConst.EventIdSecond] = ArenaBroadcastTexts.ThirtySeconds;
        StartMessageIds[BattlegroundConst.EventIdThird] = ArenaBroadcastTexts.FifteenSeconds;
        StartMessageIds[BattlegroundConst.EventIdFourth] = ArenaBroadcastTexts.HasBegun;
    }

    @Override
    public void addPlayer(Player player) {
        var isInBattleground = isPlayerInBattleground(player.getGUID());
        super.addPlayer(player);

        if (!isInBattleground) {
            playerScores.put(player.getGUID(), new ArenaScore(player.getGUID(), player.getBgTeam()));
        }

        if (player.getBgTeam() == Team.ALLIANCE) // gold
        {
            if (player.getEffectiveTeam() == Team.Horde) {
                player.castSpell(player, ArenaSpellIds.HordeGoldFlag, true);
            } else {
                player.castSpell(player, ArenaSpellIds.AllianceGoldFlag, true);
            }
        } else // green
        {
            if (player.getEffectiveTeam() == Team.Horde) {
                player.castSpell(player, ArenaSpellIds.HordeGreenFlag, true);
            } else {
                player.castSpell(player, ArenaSpellIds.AllianceGreenFlag, true);
            }
        }

        updateArenaWorldState();
    }

    @Override
    public void removePlayer(Player player, ObjectGuid guid, Team team) {
        if (getStatus() == BattlegroundStatus.WaitLeave) {
            return;
        }

        updateArenaWorldState();
        checkWinConditions();
    }

    @Override
    public void handleKillPlayer(Player victim, Player killer) {
        if (getStatus() != BattlegroundStatus.inProgress) {
            return;
        }

        super.handleKillPlayer(victim, killer);

        updateArenaWorldState();
        checkWinConditions();
    }

    @Override
    public void buildPvPLogDataPacket(tangible.OutObject<PVPMatchStatistics> pvpLogData) {
        super.buildPvPLogDataPacket(pvpLogData);

        if (isRated()) {
            pvpLogData.outArgValue.ratings = new PVPMatchStatistics.RatingData();

            for (byte i = 0; i < SharedConst.PvpTeamsCount; ++i) {
                pvpLogData.outArgValue.ratings.Postmatch[i] = _arenaTeamScores[i].postMatchRating;
                pvpLogData.outArgValue.ratings.Prematch[i] = _arenaTeamScores[i].preMatchRating;
                pvpLogData.outArgValue.ratings.PrematchMMR[i] = _arenaTeamScores[i].preMatchMMR;
            }
        }
    }

    @Override
    public void removePlayerAtLeave(ObjectGuid guid, boolean transport, boolean SendPacket) {
        if (isRated() && getStatus() == BattlegroundStatus.inProgress) {
            var bgPlayer = getPlayers().get(guid);

            if (bgPlayer != null) // check if the player was a participant of the match, or only entered through gm command (appear)
            {
                // if the player was a match participant, calculate rating

                var winnerArenaTeam = global.getArenaTeamMgr().getArenaTeamById(getArenaTeamIdForTeam(getOtherTeam(bgPlayer.team)));
                var loserArenaTeam = global.getArenaTeamMgr().getArenaTeamById(getArenaTeamIdForTeam(bgPlayer.team));

                // left a rated match while the encounter was in progress, consider as loser
                if (winnerArenaTeam != null && loserArenaTeam != null && winnerArenaTeam != loserArenaTeam) {
                    var player = _GetPlayer(guid, bgPlayer.offlineRemoveTime != 0, "Arena.RemovePlayerAtLeave");

                    if (player) {
                        loserArenaTeam.memberLost(player, getArenaMatchmakerRating(getOtherTeam(bgPlayer.team)));
                    } else {
                        loserArenaTeam.offlineMemberLost(guid, getArenaMatchmakerRating(getOtherTeam(bgPlayer.team)));
                    }
                }
            }
        }

        // remove player
        super.removePlayerAtLeave(guid, transport, SendPacket);
    }

    @Override
    public void checkWinConditions() {
        if (getAlivePlayersCountByTeam(Team.ALLIANCE) == 0 && getPlayersCountByTeam(Team.Horde) != 0) {
            endBattleground(Team.Horde);
        } else if (getPlayersCountByTeam(Team.ALLIANCE) != 0 && getAlivePlayersCountByTeam(Team.Horde) == 0) {
            endBattleground(Team.ALLIANCE);
        }
    }

    @Override
    public void endBattleground(Team winner) {
        // arena rating calculation
        if (isRated()) {
            int loserTeamRating;
            int loserMatchmakerRating;
            var loserChange = 0;
            var loserMatchmakerChange = 0;
            int winnerTeamRating;
            int winnerMatchmakerRating;
            var winnerChange = 0;
            var winnerMatchmakerChange = 0;
            var guildAwarded = false;

            // In case of arena draw, follow this logic:
            // winnerArenaTeam => ALLIANCE, loserArenaTeam => HORDE
            var winnerArenaTeam = global.getArenaTeamMgr().getArenaTeamById(getArenaTeamIdForTeam(winner == 0 ? Team.ALLIANCE : winner));
            var loserArenaTeam = global.getArenaTeamMgr().getArenaTeamById(getArenaTeamIdForTeam(winner == 0 ? Team.Horde : getOtherTeam(winner)));

            if (winnerArenaTeam != null && loserArenaTeam != null && winnerArenaTeam != loserArenaTeam) {
                // In case of arena draw, follow this logic:
                // winnerMatchmakerRating => ALLIANCE, loserMatchmakerRating => HORDE
                loserTeamRating = loserArenaTeam.getRating();
                loserMatchmakerRating = getArenaMatchmakerRating(winner == 0 ? Team.Horde : getOtherTeam(winner));
                winnerTeamRating = winnerArenaTeam.getRating();
                winnerMatchmakerRating = getArenaMatchmakerRating(winner == 0 ? Team.ALLIANCE : winner);

                if (winner != 0) {
                    tangible.RefObject<Integer> tempRef_winnerChange = new tangible.RefObject<Integer>(winnerChange);
                    winnerMatchmakerChange = winnerArenaTeam.wonAgainst(winnerMatchmakerRating, loserMatchmakerRating, tempRef_winnerChange);
                    winnerChange = tempRef_winnerChange.refArgValue;
                    tangible.RefObject<Integer> tempRef_loserChange = new tangible.RefObject<Integer>(loserChange);
                    loserMatchmakerChange = loserArenaTeam.lostAgainst(loserMatchmakerRating, winnerMatchmakerRating, tempRef_loserChange);
                    loserChange = tempRef_loserChange.refArgValue;

                    Log.outDebug(LogFilter.Arena, "match Type: {0} --- Winner: old rating: {1}, rating gain: {2}, old MMR: {3}, MMR gain: {4} --- Loser: old rating: {5}, " + "rating loss: {6}, old MMR: {7}, MMR loss: {8} ---", getArenaType(), winnerTeamRating, winnerChange, winnerMatchmakerRating, winnerMatchmakerChange, loserTeamRating, loserChange, loserMatchmakerRating, loserMatchmakerChange);

                    setArenaMatchmakerRating(winner, (int) (winnerMatchmakerRating + winnerMatchmakerChange));
                    setArenaMatchmakerRating(getOtherTeam(winner), (int) (loserMatchmakerRating + loserMatchmakerChange));

                    // bg team that the client expects is different to TeamId
                    // alliance 1, horde 0
                    var winnerTeam = (byte) (winner == Team.ALLIANCE ? PvPTeamId.Alliance : PvPTeamId.Horde);
                    var loserTeam = (byte) (winner == Team.ALLIANCE ? PvPTeamId.Horde : PvPTeamId.Alliance);

                    _arenaTeamScores[winnerTeam].assign(winnerTeamRating, (int) (winnerTeamRating + winnerChange), winnerMatchmakerRating, getArenaMatchmakerRating(winner));
                    _arenaTeamScores[loserTeam].assign(loserTeamRating, (int) (loserTeamRating + loserChange), loserMatchmakerRating, getArenaMatchmakerRating(getOtherTeam(winner)));

                    Log.outDebug(LogFilter.Arena, "Arena match Type: {0} for Team1Id: {1} - Team2Id: {2} ended. WinnerTeamId: {3}. Winner rating: +{4}, Loser rating: {5}", getArenaType(), getArenaTeamIdByIndex(TeamId.ALLIANCE), getArenaTeamIdByIndex(TeamId.HORDE), winnerArenaTeam.getId(), winnerChange, loserChange);

                    if (WorldConfig.getBoolValue(WorldCfg.ArenaLogExtendedInfo)) {
                        for (var score : playerScores.entrySet()) {
                            var player = global.getObjAccessor().findPlayer(score.getKey());

                            if (player) {
                                Log.outDebug(LogFilter.Arena, "Statistics match Type: {0} for {1} (GUID: {2}, Team: {3}, IP: {4}): {5}", getArenaType(), player.getName(), score.getKey(), player.getArenaTeamId((byte) (getArenaType() == ArenaTypes.Team5v5 ? 2 : (getArenaType() == ArenaTypes.Team3v3 ? 1 : 0))), player.getSession().getRemoteAddress(), score.getValue().toString());
                            }
                        }
                    }
                }
                // Deduct 16 points from each teams arena-rating if there are no winners after 45+2 minutes
                else {
                    _arenaTeamScores[PvPTeamId.Alliance.getValue()].assign(winnerTeamRating, (int) (winnerTeamRating + SharedConst.ArenaTimeLimitPointsLoss), winnerMatchmakerRating, getArenaMatchmakerRating(Team.ALLIANCE));
                    _arenaTeamScores[PvPTeamId.Horde.getValue()].assign(loserTeamRating, (int) (loserTeamRating + SharedConst.ArenaTimeLimitPointsLoss), loserMatchmakerRating, getArenaMatchmakerRating(Team.Horde));

                    winnerArenaTeam.finishGame(SharedConst.ArenaTimeLimitPointsLoss);
                    loserArenaTeam.finishGame(SharedConst.ArenaTimeLimitPointsLoss);
                }

                var aliveWinners = getAlivePlayersCountByTeam(winner);

                for (var pair : getPlayers().entrySet()) {
                    var team = pair.getValue().team;

                    if (pair.getValue().offlineRemoveTime != 0) {
                        // if rated arena match - make member lost!
                        if (team == winner) {
                            winnerArenaTeam.offlineMemberLost(pair.getKey(), loserMatchmakerRating, winnerMatchmakerChange);
                        } else {
                            if (winner == 0) {
                                winnerArenaTeam.offlineMemberLost(pair.getKey(), loserMatchmakerRating, winnerMatchmakerChange);
                            }

                            loserArenaTeam.offlineMemberLost(pair.getKey(), winnerMatchmakerRating, loserMatchmakerChange);
                        }

                        continue;
                    }

                    var player = _GetPlayer(pair.getKey(), pair.getValue().offlineRemoveTime != 0, "Arena.EndBattleground");

                    if (!player) {
                        continue;
                    }

                    // per player calculation
                    if (team == winner) {
                        // update achievement BEFORE personal rating update
                        var rating = player.getArenaPersonalRating(winnerArenaTeam.getSlot());
                        player.updateCriteria(CriteriaType.WinAnyRankedArena, rating != 0 ? rating : 1);
                        player.updateCriteria(CriteriaType.WinArena, getMapId());

                        // Last standing - Rated 5v5 arena & be solely alive player
                        if (getArenaType() == ArenaTypes.Team5v5 && aliveWinners == 1 && player.isAlive()) {
                            player.castSpell(player, ArenaSpellIds.LastManStanding, true);
                        }

                        if (!guildAwarded) {
                            guildAwarded = true;
                            long guildId = getBgMap().getOwnerGuildId(player.getBgTeam());

                            if (guildId != 0) {
                                var guild = global.getGuildMgr().getGuildById(guildId);

                                if (guild) {
                                    guild.updateCriteria(CriteriaType.WinAnyRankedArena, Math.max(winnerArenaTeam.getRating(), 1), 0, 0, null, player);
                                }
                            }
                        }

                        winnerArenaTeam.memberWon(player, loserMatchmakerRating, winnerMatchmakerChange);
                    } else {
                        if (winner == 0) {
                            winnerArenaTeam.memberLost(player, loserMatchmakerRating, winnerMatchmakerChange);
                        }

                        loserArenaTeam.memberLost(player, winnerMatchmakerRating, loserMatchmakerChange);

                        // Arena lost => reset the win_rated_arena having the "no_lose" condition
                        player.resetCriteria(CriteriaFailEvent.LoseRankedArenaMatchWithTeamSize, 0);
                    }
                }

                // save the stat changes
                winnerArenaTeam.saveToDB();
                loserArenaTeam.saveToDB();
                // send updated arena team stats to players
                // this way all arena team members will get notified, not only the ones who participated in this match
                winnerArenaTeam.notifyStatsChanged();
                loserArenaTeam.notifyStatsChanged();
            }
        }

        // end Battleground
        super.endBattleground(winner);
    }

    private void updateArenaWorldState() {
        updateWorldState(ArenaWorldStates.ALIVEPLAYERSGREEN, (int) getAlivePlayersCountByTeam(Team.Horde));
        updateWorldState(ArenaWorldStates.ALIVEPLAYERSGOLD, (int) getAlivePlayersCountByTeam(Team.ALLIANCE));
    }
}
