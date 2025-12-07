package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class FollowerAI extends ScriptedAI {
    private ObjectGuid leaderGUID = new ObjectGuid();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _updateFollowTimer;
    private int updateFollowTimer;
    private FollowState followState = FollowState.values()[0];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: uint _questForFollow;
    private int questForFollow;

    public FollowerAI(Creature creature) {
        super(creature);
        updateFollowTimer = 2500;
        followState = FollowState.None;
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
                for (var groupRef = group.getFirstMember(); groupRef != null; groupRef = groupRef.Next()) {
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
            if (hasFollowState(FollowState.Paused)) {
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public override void UpdateAI(uint uiDiff)
    @Override
    public void updateAI(int uiDiff) {
        if (hasFollowState(FollowState.Inprogress) && !me.isEngaged()) {
            if (updateFollowTimer <= uiDiff) {
                if (hasFollowState(FollowState.Complete) && !hasFollowState(FollowState.PostEvent)) {
                    Log.outDebug(LogFilter.ScriptsAi, String.format("FollowerAI::UpdateAI: is set completed, despawns. (%1$s)", me.getGUID().clone()));
                    me.despawnOrUnsummon();

                    return;
                }

                var maxRangeExceeded = true;
                var questAbandoned = (questForFollow != 0);

                var player = getLeaderForFollower();

                if (player) {
                    var group = player.getGroup();

                    if (group) {
                        for (var groupRef = group.getFirstMember(); groupRef != null && (maxRangeExceeded || questAbandoned); groupRef = groupRef.Next()) {
                            var member = groupRef.getSource();

                            if (member == null) {
                                continue;
                            }

                            if (maxRangeExceeded && me.isWithinDistInMap(member, 100.0f)) {
                                maxRangeExceeded = false;
                            }

                            if (questAbandoned) {
                                var status = member.getQuestStatus(questForFollow);

                                if ((status == QuestStatus.Complete) || (status == QuestStatus.Incomplete)) {
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

                            if ((status == QuestStatus.Complete) || (status == QuestStatus.Incomplete)) {
                                questAbandoned = false;
                            }
                        }
                    }
                }

                if (maxRangeExceeded || questAbandoned) {
                    Log.outDebug(LogFilter.ScriptsAi, String.format("FollowerAI::UpdateAI: failed because player/group was to far away or not found (%1$s)", me.getGUID().clone()));
                    me.despawnOrUnsummon();

                    return;
                }

                updateFollowTimer = 1000;
            } else {
                updateFollowTimer -= uiDiff;
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void StartFollow(Player player, uint factionForFollower = 0, Quest quest = null)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final void startFollow(Player player, int factionForFollower, Quest quest) {
        var cdata = me.getCreatureData();

        if (cdata != null) {
            if (WorldConfig.getBoolValue(WorldCfg.RespawnDynamicEscortNpc) && cdata.spawnGroupData.flags.HasFlag(SpawnGroupFlags.EscortQuestNpc)) {
                me.saveRespawnTime(me.respawnDelay);
            }
        }

        if (me.isEngaged()) {
            Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::StartFollow: attempt to StartFollow while in combat. (%1$s)", me.getGUID().clone()));

            return;
        }

        if (hasFollowState(FollowState.Inprogress)) {
            Log.outError(LogFilter.Scenario, String.format("FollowerAI::StartFollow: attempt to StartFollow while already following. (%1$s)", me.getGUID().clone()));

            return;
        }

        //set variables
        leaderGUID = player.getGUID().clone();

        if (factionForFollower != 0) {
            me.setFaction(factionForFollower);
        }

        questForFollow = quest.id;

        me.getMotionMaster().clear(MovementGeneratorPriority.Normal);
        me.pauseMovement();

        me.replaceAllNpcFlags(NPCFlags.None);
        me.replaceAllNpcFlags2(NPCFlags2.None);

        addFollowState(FollowState.Inprogress);

        me.getMotionMaster().moveFollow(player, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);

        Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::StartFollow: start follow %1$s - %2$s (%3$s)", player.getName(), leaderGUID.clone(), me.getGUID().clone()));
    }

    public final void setFollowPaused(boolean paused) {
        if (!hasFollowState(FollowState.Inprogress) || hasFollowState(FollowState.Complete)) {
            return;
        }

        if (paused) {
            addFollowState(FollowState.Paused);

            if (me.hasUnitState(UnitState.Follow)) {
                me.getMotionMaster().remove(MovementGeneratorType.Follow);
            }
        } else {
            removeFollowState(FollowState.Paused);

            var leader = getLeaderForFollower();

            if (leader != null) {
                me.getMotionMaster().moveFollow(leader, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
            }
        }
    }


    public final void setFollowComplete() {
        setFollowComplete(false);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void SetFollowComplete(bool withEndEvent = false)
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: void UpdateFollowerAI(uint diff)
    private void updateFollowerAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    private Player getLeaderForFollower() {
        var player = Global.getObjAccessor().GetPlayer(me, leaderGUID.clone());

        if (player) {
            if (player.IsAlive) {
                return player;
            } else {
                var group = player.Group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.Next()) {
                        var member = groupRef.Source;

                        if (member && me.isWithinDistInMap(member, 100.0f) && member.IsAlive) {
                            Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::GetLeaderForFollower: GetLeader changed and returned new leader. (%1$s)", me.getGUID().clone()));
                            leaderGUID = member.GUID;

                            return member;
                        }
                    }
                }
            }
        }

        Log.outDebug(LogFilter.Scripts, String.format("FollowerAI::GetLeaderForFollower: GetLeader can not find suitable leader. (%1$s)", me.getGUID().clone()));

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
        if (!me.getTemplate().typeFlags.HasAnyFlag(CreatureTypeFlags.CanAssist)) {
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
        if (who.getTypeId() == TypeId.Unit && who.getAsCreature().isInEvadeMode()) {
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
        followState = game.ai.FollowState.forValue(followState.getValue() | uiFollowState.getValue());
    }

    private void removeFollowState(FollowState uiFollowState) {
        followState = game.ai.FollowState.forValue(followState.getValue() & ~uiFollowState.getValue());
    }
}