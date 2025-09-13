package com.github.azeroth.game.entity.creature;


import com.github.azeroth.common.Logs;
import com.github.azeroth.game.domain.creature.FormationInfo;
import com.github.azeroth.game.domain.creature.GroupAIFlag;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.ZoneScript;
import com.github.azeroth.game.movement.enums.MovementGeneratorType;
import com.github.azeroth.game.movement.enums.MovementSlot;

import java.util.HashMap;


public class CreatureGroup {

    private final HashMap<Creature, FormationInfo> members = new HashMap<>();
    private final long leaderSpawnId;
    private final boolean formed;
    private Creature leader;
    private boolean engaging;


    public CreatureGroup(long leaderSpawnId) {
        this.leaderSpawnId = leaderSpawnId;
        this.formed = false;
    }

    public final Creature getLeader() {
        return leader;
    }

    public final long getLeaderSpawnId() {
        return leaderSpawnId;
    }

    public final boolean isEmpty() {
        return members.isEmpty();
    }

    public final boolean isFormed() {
        return formed;
    }

    public final void addMember(Creature member) {
        Logs.UNIT.debug("CreatureGroup::AddMember: Adding unit {}.", member.getGUID());

        //Check if it is a leader
        if (member.getSpawnId() == leaderSpawnId) {
            Logs.UNIT.debug("Unit {} is formation leader. Adding group.", member.getGUID());
            leader = member;
        }

        // formation must be registered at this point
        var formationInfo = member.getWorldContext().getObjectManager().getFormationInfo(member.getSpawnId());
        members.put(member, formationInfo);
        member.setFormation(this);
    }

    public final void removeMember(Creature member) {
        if (leader == member) {
            leader = null;
        }

        members.remove(member);
        member.setFormation(null);
    }

    public final void memberEngagingTarget(Creature member, Unit target) {
        // used to prevent recursive calls
        if (engaging) {
            return;
        }

        var formationInfo = member.getWorldContext().getObjectManager().getFormationInfo(member.getSpawnId());

        if (formationInfo.groupAI.getFlag() == 0) {
            return;
        }


        if (member == leader) {
            if (!formationInfo.groupAI.hasFlag(GroupAIFlag.MEMBERS_ASSIST_LEADER)) {
                return;
            }
        } else if (!formationInfo.groupAI.hasFlag(GroupAIFlag.LEADER_ASSISTS_MEMBER)) {
            return;
        }

        engaging = true;

        for (var pair : members.entrySet()) {
            var other = pair.getKey();

            // Skip self
            if (other == member) {
                continue;
            }

            if (!other.isAlive()) {
                continue;
            }

            if ((other != leader && formationInfo.groupAI.hasFlag(GroupAIFlag.MEMBERS_ASSIST_LEADER)
                    || (other == leader && formationInfo.groupAI.hasFlag(GroupAIFlag.LEADER_ASSISTS_MEMBER)))
                    && other.isValidAttackTarget(target)) {
                other.engageWithTarget(target);
            }
        }

        engaging = false;
    }

    public final void formationReset(boolean dismiss) {
        for (var creature : members.keySet()) {
            if (creature != leader && creature.isAlive()) {

                if (dismiss)
                    creature.getMotionMaster().remove(MovementGeneratorType.FORMATION, MovementSlot.DEFAULT);
                else
                    creature.getMotionMaster().moveIdle();

                Logs.UNIT.debug("CreatureGroup::FormationReset: Set {} movement for member {}", dismiss ? "default" : "idle", creature.getGUID());

            }
        }

    }

    public final void leaderStartedMoving() {
        if (leader == null) {
            return;
        }

        for (var pair : members.entrySet()) {
            var member = pair.getKey();

            if (member == leader || !member.isAlive() || member.isEngaged() || !pair.getValue().groupAI.hasFlag(GroupAIFlag.IDLE_IN_FORMATION)) {
                continue;
            }

            var angle = pair.getValue().followAngle + (float) Math.PI; // for some reason, someone thought it was a great idea to invert relativ angles...
            var dist = pair.getValue().followDist;

            if (!member.hasUnitState(UnitState.FOLLOW_FORMATION)) {
                member.getMotionMaster().moveFormation(leader, dist, angle, pair.getValue().leaderWaypointIds[0], pair.getValue().leaderWaypointIds[1]);
            }
        }
    }

    public final boolean canLeaderStartMoving() {
        for (var pair : members.entrySet()) {
            if (pair.getKey() != leader && pair.getKey().isAlive()) {
                if (pair.getKey().isEngaged() || pair.getKey().isReturningHome()) {
                    return false;
                }
            }
        }

        return true;
    }

    public final boolean isLeader(Creature creature) {
        return leader == creature;
    }

    public final boolean hasMember(Creature member) {
        return members.containsKey(member);
    }

    public final boolean hasAliveMembers() {
        return members.keySet().stream().anyMatch(Unit::isAlive);
    }


    public static void removeCreatureFromGroup(CreatureGroup group, Creature member) {
        Logs.UNIT.debug("Deleting member GUID: {} from group {}", group.getLeaderSpawnId(), member.getSpawnId());
        group.removeMember(member);

        // If removed member was alive we need to check if we have any other alive members
        // if not - fire OnCreatureGroupDepleted
        ZoneScript zoneScript = member.getZoneScript();
        if (zoneScript != null) {
            if (member.isAlive() && !group.hasAliveMembers())
                zoneScript.onCreatureGroupDepleted(group);
        }

        if (group.isEmpty()) {
            var map = member.getMap();

            Logs.UNIT.debug("Deleting group with InstanceID {}", member.getInstanceId());
            map.getCreatureGroupHolder().remove(group.getLeaderSpawnId());
        }
    }


    public static void addCreatureToGroup(long leaderSpawnId, Creature creature) {
        var map = creature.getMap();

        var creatureGroup = map.getCreatureGroupHolder().get(leaderSpawnId);

        if (creatureGroup != null) {
            //Add member to an existing group
            Logs.UNIT.debug("Group found: {}, inserting creature {}, Group InstanceID {}", leaderSpawnId, creature.getGUID().toString(), creature.getInstanceId());

            // With dynamic spawn the creature may have just respawned
            // we need to find previous instance of creature and delete it from the formation, as it'll be invalidated
            var bounds = map.getCreatureBySpawnIdStore().get(creature.getSpawnId());

            for (var other : bounds) {
                if (other == creature) {
                    continue;
                }

                if (creatureGroup.hasMember(other)) {
                    creatureGroup.removeMember(other);
                }
            }

            creatureGroup.addMember(creature);
        } else {
            //Create new group
            Logs.UNIT.debug("Group not found: {}. Creating new group.", leaderSpawnId);
            CreatureGroup group = new CreatureGroup(leaderSpawnId);
            map.getCreatureGroupHolder().put(leaderSpawnId, group);
            group.addMember(creature);
        }
    }



}
