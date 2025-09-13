package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;

class FollowerAI extends ScriptedAI {
    private ObjectGuid leaderGUID = ObjectGuid.EMPTY;
    private int updateFollowTimer;
    private FollowState followState = FollowState.values()[0];
    private int questForFollow;

    public FollowerAI(Creature creature) {
        super(creature);
        updateFollowTimer = 2500;
        followState = FollowState.NONE;
    }

    @Override
    public void moveInLineOfSight(Unit who) {
        if (hasFollowState(FollowState.Inprogress) && !shouldAssistPlayerInCombatAgainst(who)) {
            return;
        }

        super.moveInLineOfSight(who);
    }

    @Override
    public void justDied(Unit killer) {
        if (!hasFollowState(FollowState.Inprogress) || leaderGUID.isEmpty() || questForFollow == 0) {
            return;
        }

        // @todo need a better check for quests with time limit.
        var player = getLeaderForFollower();

        if (player) {
            var group = player.getGroup();

            if (group) {
                for (var groupRef = group.getFirstMember(); groupRef != null; groupRef = groupRef.next()) {
                    var member = groupRef.getSource();

                    if (member) {
                        if (member.isInMap(player)) {
                            member.failQuest(questForFollow);
                        }
                    }
                }
            } else {
                player.failQuest(questForFollow);
            }
        }
    }

    @Override
    public void justReachedHome() {
        if (!hasFollowState(FollowState.Inprogress)) {
            return;
        }

        var player = getLeaderForFollower();

        if (player != null) {
            if (hasFollowState(FollowState.paused)) {
                return;
            }

            me.getMotionMaster().moveFollow(player, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
        } else {
            me.despawnOrUnsummon();
        }
    }

    @Override
    public void ownerAttackedBy(Unit attacker) {
        if (!me.hasReactState(ReactStates.Passive) && shouldAssistPlayerInCombatAgainst(attacker)) {
            me.engageWithTarget(attacker);
        }
    }

    @Override
    public void updateAI(int uiDiff) {
        if (hasFollowState(FollowState.Inprogress) && !me.isEngaged()) {
            if (updateFollowTimer <= uiDiff) {
                if (hasFollowState(FollowState.Complete) && !hasFollowState(FollowState.PostEvent)) {
                    Log.outDebug(LogFilter.ScriptsAi, String.format("FollowerAI::UpdateAI: is set completed, despawns. (%1$s)", me.getGUID()));
                    me.despawnOrUnsummon();

                    return;
                }

                var maxRangeExceeded = true;
                var questAbandoned = (questForFollow != 0);

                var player = getLeaderForFollower();

                if (player) {
                    var group = player.getGroup();

                    if (group) {
                        for (var groupRef = group.getFirstMember(); groupRef != null && (maxRangeExceeded || questAbandoned); groupRef = groupRef.next()) {
                            var member = groupRef.getSource();

                            if (member == null) {
                                continue;
                            }

                            if (maxRangeExceeded && me.isWithinDistInMap(member, 100.0f)) {
                                maxRangeExceeded = false;
                            }

                            if (questAbandoned) {
                                var status = member.getQuestStatus(questForFollow);

                                if ((status == QuestStatus.Complete) || (status == QuestStatus.INCOMPLETE)) {
                                    questAbandoned = false;
                                }
                            }
                        }
                    } else {
                        if (me.isWithinDistInMap(player, 100.0f)) {
                            maxRangeExceeded = false;
                        }

                        if (questAbandoned) {
                            var status = player.getQuestStatus(questForFollow);

                            if ((status == QuestStatus.Complete) || (status == QuestStatus.INCOMPLETE)) {
                                questAbandoned = false;
                            }
                        }
                    }
                }

                if (maxRangeExceeded || questAbandoned) {
                    Log.outDebug(LogFilter.ScriptsAi, String.format("FollowerAI::UpdateAI: failed because player/group was to far away or not found (%1$s)", me.getGUID()));
                    me.despawnOrUnsummon();

                    return;
                }

                updateFollowTimer = 1000;
            } else {
                _updateFollowTimer -= uiDiff;
            }
        }

        updateFollowerAI(uiDiff);
    }


    public final void startFollow(Player player, int factionForFollower) {
        startFollow(player, factionForFollower, null);
    }

    public final void startFollow(Player player) {
        startFollow(player, 0, null);
    }

    public final void startFollow(Player player, int factionForFollower, Quest quest) {
        var cdata = me.getCreatureData();

        if (cdata != null) {
            if (WorldConfig.getBoolValue(WorldCfg.RespawnDynamicEscortNpc) && cdata.getSpawnGroupData().getFlags().hasFlag(SpawnGroupFlags.EscortQuestNpc)) {
                me.saveRespawnTime(me.getRespawnDelay());
            }
        }

        if (me.isEngaged()) {
            Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::StartFollow: attempt to StartFollow while in combat. (%1$s)", me.getGUID()));

            return;
        }

        if (hasFollowState(FollowState.Inprogress)) {
            Log.outError(LogFilter.Scenario, String.format("FollowerAI::StartFollow: attempt to StartFollow while already following. (%1$s)", me.getGUID()));

            return;
        }

        //set variables
        leaderGUID = player.getGUID();

        if (factionForFollower != 0) {
            me.setFaction(factionForFollower);
        }

        questForFollow = quest.id;

        me.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);
        me.pauseMovement();

        me.replaceAllNpcFlags(NPCFlags.NONE);
        me.replaceAllNpcFlags2(NPCFlags2.NONE);

        addFollowState(FollowState.Inprogress);

        me.getMotionMaster().moveFollow(player, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);

        Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::StartFollow: start follow %1$s - %2$s (%3$s)", player.getName(), leaderGUID, me.getGUID()));
    }

    public final void setFollowPaused(boolean paused) {
        if (!hasFollowState(FollowState.Inprogress) || hasFollowState(FollowState.Complete)) {
            return;
        }

        if (paused) {
            addFollowState(FollowState.paused);

            if (me.hasUnitState(UnitState.Follow)) {
                me.getMotionMaster().remove(MovementGeneratorType.Follow);
            }
        } else {
            removeFollowState(FollowState.paused);

            var leader = getLeaderForFollower();

            if (leader != null) {
                me.getMotionMaster().moveFollow(leader, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
            }
        }
    }


    public final void setFollowComplete() {
        setFollowComplete(false);
    }

    public final void setFollowComplete(boolean withEndEvent) {
        if (me.hasUnitState(UnitState.Follow)) {
            me.getMotionMaster().remove(MovementGeneratorType.Follow);
        }

        if (withEndEvent) {
            addFollowState(FollowState.PostEvent);
        } else {
            if (hasFollowState(FollowState.PostEvent)) {
                removeFollowState(FollowState.PostEvent);
            }
        }

        addFollowState(FollowState.Complete);
    }

    @Override
    public boolean isEscorted() {
        return hasFollowState(FollowState.Inprogress);
    }

    private void updateFollowerAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    private Player getLeaderForFollower() {
        var player = global.getObjAccessor().getPlayer(me, leaderGUID);

        if (player) {
            if (player.isAlive) {
                return player;
            } else {
                var group = player.group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.next()) {
                        var member = groupRef.source;

                        if (member && me.isWithinDistInMap(member, 100.0f) && member.isAlive) {
                            Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::GetLeaderForFollower: GetLeader changed and returned new leader. (%1$s)", me.getGUID()));
                            leaderGUID = member.GUID;

                            return member;
                        }
                    }
                }
            }
        }

        Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::GetLeaderForFollower: GetLeader can not find suitable leader. (%1$s)", me.getGUID()));

        return null;
    }

    //This part provides assistance to a player that are attacked by who, even if out of normal aggro range
    //It will cause me to attack who that are attacking _any_ player (which has been confirmed may happen also on offi)
    //The flag (type_flag) is unconfirmed, but used here for further research and is a good candidate.
    private boolean shouldAssistPlayerInCombatAgainst(Unit who) {
        if (!who || !who.getVictim()) {
            return false;
        }

        //experimental (unknown) flag not present
        if (!me.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlags.CanAssist)) {
            return false;
        }

        if (!who.isInAccessiblePlaceFor(me)) {
            return false;
        }

        if (!canAIAttack(who)) {
            return false;
        }

        // we cannot attack in evade mode
        if (me.isInEvadeMode()) {
            return false;
        }

        // or if enemy is in evade mode
        if (who.getObjectTypeId() == TypeId.UNIT && who.toCreature().isInEvadeMode()) {
            return false;
        }

        //never attack friendly
        if (me.isFriendlyTo(who)) {
            return false;
        }

        //too far away and no free sight?
        if (!me.isWithinDistInMap(who, 100.0f) || !me.isWithinLOSInMap(who)) {
            return false;
        }

        return true;
    }

    private boolean hasFollowState(FollowState uiFollowState) {
        return (followState.getValue() & uiFollowState.getValue()) != 0;
    }

    private void addFollowState(FollowState uiFollowState) {
        followState = FollowState.forValue(followState.getValue() | uiFollowState.getValue());
    }

    private void removeFollowState(FollowState uiFollowState) {
        followState = FollowState.forValue(followState.getValue() & ~uiFollowState.getValue());
    }
}
