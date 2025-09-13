package com.github.azeroth.game.ai;


import com.github.azeroth.game.domain.misc.WaypointMoveType;
import com.github.azeroth.game.domain.misc.WaypointNode;
import com.github.azeroth.game.domain.misc.WaypointPath;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;

public class EscortAI extends ScriptedAI {
    private final WaypointPath path;

    private ObjectGuid playerGUID = ObjectGuid.EMPTY;
    private duration pauseTimer = new duration();
    private int playerCheckTimer;
    private EscortState escortState = EscortState.values()[0];
    private float maxPlayerDistance;

    private Quest escortQuest; //generally passed in start() when regular escort script.

    private boolean activeAttacker; // obsolete, determined by faction.
    private boolean running; // all creatures are walking by default (has flag MOVEMENTFLAG_WALK)
    private boolean instantRespawn; // if creature should respawn instantly after escort over (if not, database respawntime are used)
    private boolean returnToStart; // if creature can walk same path (loop) without despawn. Not for regular escort quests.
    private boolean despawnAtEnd;
    private boolean despawnAtFar;
    private boolean manualPath;
    private boolean hasImmuneToNPCFlags;
    private boolean started;
    private boolean ended;
    private boolean resume;

    public EscortAI(Creature creature) {
        super(creature);
        pauseTimer = duration.FromSeconds(2.5);
        playerCheckTimer = 1000;
        maxPlayerDistance = 100;
        activeAttacker = true;
        despawnAtEnd = true;
        despawnAtFar = true;

        path = new waypointPath();
    }

    public final Player getPlayerForEscort() {
        return global.getObjAccessor().getPlayer(me, playerGUID);
    }

    @Override
    public void moveInLineOfSight(Unit who) {
        if (who == null) {
            return;
        }

        if (hasEscortState(EscortState.Escorting) && assistPlayerInCombatAgainst(who)) {
            return;
        }

        super.moveInLineOfSight(who);
    }

    @Override
    public void justDied(Unit killer) {
        if (!hasEscortState(EscortState.Escorting) || playerGUID.isEmpty() || escortQuest == null) {
            return;
        }

        var player = getPlayerForEscort();

        if (player) {
            var group = player.getGroup();

            if (group) {
                for (var groupRef = group.getFirstMember(); groupRef != null; groupRef = groupRef.next()) {
                    var member = groupRef.getSource();

                    if (member) {
                        if (member.isInMap(player)) {
                            member.failQuest(escortQuest.id);
                        }
                    }
                }
            } else {
                player.failQuest(escortQuest.id);
            }
        }
    }

    @Override
    public void initializeAI() {
        escortState = EscortState.NONE;

        if (!isCombatMovementAllowed()) {
            setCombatMovement(true);
        }

        //add a small delay before going to first waypoint, normal in near all cases
        pauseTimer = duration.FromSeconds(2);

        if (me.getFaction() != me.getCreatureTemplate().faction) {
            me.restoreFaction();
        }

        reset();
    }


    @Override
    public void enterEvadeMode() {
        enterEvadeMode(EvadeReason.other);
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
        me.removeAllAuras();
        me.combatStop(true);
        me.setTappedBy(null);

        engagementOver();

        if (hasEscortState(EscortState.Escorting)) {
            addEscortState(EscortState.Returning);
            returnToLastPoint();
            Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI.EnterEvadeMode has left combat and is now returning to last point %1$s", me.getGUID()));
        } else {
            me.getMotionMaster().moveTargetedHome();

            if (hasImmuneToNPCFlags) {
                me.setImmuneToNPC(true);
            }

            reset();
        }
    }

    @Override
    public void updateAI(int diff) {
        //Waypoint Updating
        if (hasEscortState(EscortState.Escorting) && !me.isEngaged() && !hasEscortState(EscortState.Returning)) {
            if (pauseTimer.TotalMilliseconds <= diff) {
                if (!hasEscortState(EscortState.paused)) {
                    pauseTimer = duration.Zero;

                    if (ended) {
                        ended = false;
                        me.getMotionMaster().moveIdle();

                        if (despawnAtEnd) {
                            Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::UpdateAI: reached end of waypoints, despawning at end (%1$s)", me.getGUID()));

                            if (returnToStart) {
                                var respawnPosition = me.getRespawnPosition();
                                me.getMotionMaster().movePoint(EscortPointIds.home, respawnPosition);
                                Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::UpdateAI: returning to spawn location: %1$s (%2$s)", respawnPosition, me.getGUID()));
                            } else if (instantRespawn) {
                                me.respawn();
                            } else {
                                me.despawnOrUnsummon();
                            }
                        }

                        Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::UpdateAI: reached end of waypoints (%1$s)", me.getGUID()));
                        removeEscortState(EscortState.Escorting);

                        return;
                    }

                    if (!started) {
                        started = true;
                        me.getMotionMaster().movePath(path, false);
                    } else if (resume) {
                        resume = false;
                        var movementGenerator = me.getMotionMaster().getCurrentMovementGenerator(MovementSlot.Default);

                        if (movementGenerator != null) {
                            movementGenerator.resume(0);
                        }
                    }
                }
            } else {
                _pauseTimer -= duration.ofSeconds(diff);
            }
        }


        //Check if player or any member of his group is within range
        if (despawnAtFar && hasEscortState(EscortState.Escorting) && !playerGUID.isEmpty() && !me.isEngaged() && !hasEscortState(EscortState.Returning)) {
            if (playerCheckTimer <= diff) {
                if (!isPlayerOrGroupInRange()) {
                    Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::UpdateAI: failed because player/group was to far away or not found (%1$s)", me.getGUID()));

                    var isEscort = false;
                    var creatureData = me.getCreatureData();

                    if (creatureData != null) {
                        isEscort = (WorldConfig.getBoolValue(WorldCfg.RespawnDynamicEscortNpc) && creatureData.getSpawnGroupData().getFlags().hasFlag(SpawnGroupFlags.EscortQuestNpc));
                    }

                    if (instantRespawn) {
                        if (!isEscort) {
                            me.despawnOrUnsummon(duration.Zero, duration.FromSeconds(1));
                        } else {
                            me.getMap().respawn(SpawnObjectType.CREATURE, me.getSpawnId());
                        }
                    } else {
                        me.despawnOrUnsummon();
                    }

                    return;
                }

                playerCheckTimer = 1000;
            } else {
                _playerCheckTimer -= diff;
            }
        }

        updateEscortAI(diff);
    }

    public void updateEscortAI(int diff) {
        if (!updateVictim()) {
            return;
        }

        doMeleeAttackIfReady();
    }

    @Override
    public void movementInform(MovementGeneratorType moveType, int id) {
        // no action allowed if there is no escort
        if (!hasEscortState(EscortState.Escorting)) {
            return;
        }

        //Combat start position reached, continue waypoint movement
        if (moveType == MovementGeneratorType.Point) {
            if (duration.opEquals(pauseTimer, duration.Zero)) {
                pauseTimer = duration.FromSeconds(2);
            }

            if (id == EscortPointIds.lastPoint) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::MovementInform has returned to original position before combat (%1$s)", me.getGUID()));

                me.setWalk(!running);
                removeEscortState(EscortState.Returning);
            } else if (id == EscortPointIds.home) {
                Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::MovementInform: returned to home location and restarting waypoint path (%1$s)", me.getGUID()));
                started = false;
            }
        } else if (moveType == MovementGeneratorType.Waypoint) {
            var waypoint = path.nodes.get((int) id);

            Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::MovementInform: waypoint node %1$s reached (%2$s)", waypoint.id, me.getGUID()));

            // last point
            if (id == path.nodes.size() - 1) {
                started = false;
                ended = true;
                pauseTimer = duration.FromSeconds(1);
            }
        }
    }

    public final void addWaypoint(int id, float x, float y, float z, float orientation, Duration waitTime) {
        x = MapDefine.normalizeMapCoord(x);
        y = MapDefine.normalizeMapCoord(y);

        WaypointNode waypoint = new WaypointNode();
        waypoint.id = id;
        waypoint.x = x;
        waypoint.y = y;
        waypoint.z = z;
        waypoint.orientation = orientation;
        waypoint.moveType = _running ? WaypointMoveType.Run : WaypointMoveType.Walk;
        waypoint.delay = (int) waitTime.TotalMilliseconds;
        waypoint.eventId = 0;
        waypoint.eventChance = 100;
        path.nodes.add(waypoint);

        manualPath = true;
    }


    public final void setRun() {
        setRun(true);
    }

    public final void setRun(boolean on) {
        if (on == running) {
            return;
        }

        for (var node : path.nodes) {
            node.moveType = on ? WaypointMoveType.Run : WaypointMoveType.Walk;
        }

        me.setWalk(!on);

        running = on;
    }

    /**
     * todo get rid of this many variables passed in function.
     */

    public final void start(boolean isActiveAttacker, boolean run, ObjectGuid playerGUID, Quest quest, boolean instantRespawn, boolean canLoopPath) {
        start(isActiveAttacker, run, playerGUID, quest, instantRespawn, canLoopPath, true);
    }

    public final void start(boolean isActiveAttacker, boolean run, ObjectGuid playerGUID, Quest quest, boolean instantRespawn) {
        start(isActiveAttacker, run, playerGUID, quest, instantRespawn, false, true);
    }

    public final void start(boolean isActiveAttacker, boolean run, ObjectGuid playerGUID, Quest quest) {
        start(isActiveAttacker, run, playerGUID, quest, false, false, true);
    }

    public final void start(boolean isActiveAttacker, boolean run, ObjectGuid playerGUID) {
        start(isActiveAttacker, run, playerGUID, null, false, false, true);
    }

    public final void start(boolean isActiveAttacker, boolean run) {
        start(isActiveAttacker, run, null, null, false, false, true);
    }

    public final void start(boolean isActiveAttacker) {
        start(isActiveAttacker, false, null, null, false, false, true);
    }

    public final void start() {
        start(true, false, null, null, false, false, true);
    }

    public final void start(boolean isActiveAttacker, boolean run, ObjectGuid playerGUID, Quest quest, boolean instantRespawn, boolean canLoopPath, boolean resetWaypoints) {
        // Queue respawn from the point it starts
        var cdata = me.getCreatureData();

        if (cdata != null) {
            if (WorldConfig.getBoolValue(WorldCfg.RespawnDynamicEscortNpc) && cdata.getSpawnGroupData().getFlags().hasFlag(SpawnGroupFlags.EscortQuestNpc)) {
                me.saveRespawnTime(me.getRespawnDelay());
            }
        }

        if (me.isEngaged()) {
            Log.outError(LogFilter.ScriptsAi, String.format("EscortAI::Start: (script: %1$s attempts to Start while in combat (%2$s)", me.getScriptName(), me.getGUID()));

            return;
        }

        if (hasEscortState(EscortState.Escorting)) {
            Log.outError(LogFilter.ScriptsAi, String.format("EscortAI::Start: (script: %1$s attempts to Start while already escorting (%2$s)", me.getScriptName(), me.getGUID()));

            return;
        }

        running = run;

        if (!manualPath && resetWaypoints) {
            fillPointMovementListForCreature();
        }

        if (path.nodes.isEmpty()) {
            Log.outError(LogFilter.ScriptsAi, String.format("EscortAI::Start: (script: %1$s starts with 0 waypoints (possible missing entry in script_waypoint. Quest: %2$s (%3$s)", me.getScriptName(), (quest != null ? quest.Id : 0), me.getGUID()));

            return;
        }

        // set variables
        activeAttacker = isActiveAttacker;
        playerGUID = playerGUID;
        escortQuest = quest;
        instantRespawn = instantRespawn;
        returnToStart = canLoopPath;

        if (returnToStart && instantRespawn) {
            Log.outError(LogFilter.ScriptsAi, String.format("EscortAI::Start: (script: %1$s is set to return home after waypoint end and instant respawn at waypoint end. Creature will never despawn (%2$s)", me.getScriptName(), me.getGUID()));
        }

        me.getMotionMaster().moveIdle();
        me.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);

        //disable npcflags
        me.replaceAllNpcFlags(NPCFlags.NONE);
        me.replaceAllNpcFlags2(NPCFlags2.NONE);

        if (me.isImmuneToNPC()) {
            hasImmuneToNPCFlags = true;
            me.setImmuneToNPC(false);
        }

        Log.outDebug(LogFilter.ScriptsAi, String.format("EscortAI::Start: (script: %1$s, started with %2$s waypoints. ActiveAttacker = %3$s, run = %4$s, player = %5$s (%6$s)", me.getScriptName(), path.nodes.size(), activeAttacker, running, playerGUID, me.getGUID()));

        // set initial speed
        me.setWalk(!running);

        started = false;
        addEscortState(EscortState.Escorting);
    }

    public final void setEscortPaused(boolean on) {
        if (!hasEscortState(EscortState.Escorting)) {
            return;
        }

        if (on) {
            addEscortState(EscortState.paused);
            var movementGenerator = me.getMotionMaster().getCurrentMovementGenerator(MovementSlot.Default);

            if (movementGenerator != null) {
                movementGenerator.pause(0);
            }
        } else {
            removeEscortState(EscortState.paused);
            resume = true;
        }
    }

    public final void setPauseTimer(Duration timer) {
        pauseTimer = timer;
    }

    public final boolean hasEscortState(EscortState escortState) {
        return (escortState.getValue() & escortState.getValue()) != 0;
    }

    @Override
    public boolean isEscorted() {
        return !playerGUID.isEmpty();
    }

    public final void setDespawnAtEnd(boolean despawn) {
        despawnAtEnd = despawn;
    }

    public final void setDespawnAtFar(boolean despawn) {
        despawnAtFar = despawn;
    }

    public final boolean isActiveAttacker() {
        return activeAttacker;
    } // used in EnterEvadeMode override

    public final void setActiveAttacker(boolean attack) {
        activeAttacker = attack;
    }

    //see followerAI
    private boolean assistPlayerInCombatAgainst(Unit who) {
        if (!who || !who.getVictim()) {
            return false;
        }

        if (me.hasReactState(ReactStates.Passive)) {
            return false;
        }

        //experimental (unknown) flag not present
        if (!me.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlags.CanAssist)) {
            return false;
        }

        //not a player
        if (!who.getVictim().getCharmerOrOwnerPlayerOrPlayerItself()) {
            return false;
        }

        //never attack friendly
        if (me.isValidAssistTarget(who.getVictim())) {
            return false;
        }

        //too far away and no free sight?
        if (me.isWithinDistInMap(who, getMaxPlayerDistance()) && me.isWithinLOSInMap(who)) {
            me.engageWithTarget(who);

            return true;
        }

        return false;
    }

    private void returnToLastPoint() {
        me.getMotionMaster().movePoint(0xFFFFFF, me.getHomePosition());
    }

    private boolean isPlayerOrGroupInRange() {
        var player = getPlayerForEscort();

        if (player) {
            var group = player.getGroup();

            if (group) {
                for (var groupRef = group.getFirstMember(); groupRef != null; groupRef = groupRef.next()) {
                    var member = groupRef.getSource();

                    if (member) {
                        if (me.isWithinDistInMap(member, getMaxPlayerDistance())) {
                            return true;
                        }
                    }
                }
            } else if (me.isWithinDistInMap(player, getMaxPlayerDistance())) {
                return true;
            }
        }

        return false;
    }

    private void fillPointMovementListForCreature() {
        var path = global.getWaypointMgr().getPath(me.getEntry());

        if (path == null) {
            return;
        }

        for (var value : path.nodes) {
            var node = value;
            node.x = MapDefine.normalizeMapCoord(node.x);
            node.y = MapDefine.normalizeMapCoord(node.y);
            node.moveType = _running ? WaypointMoveType.Run : WaypointMoveType.Walk;

            path.nodes.add(node);
        }
    }

    private float getMaxPlayerDistance() {
        return maxPlayerDistance;
    }

    private void setMaxPlayerDistance(float newMax) {
        maxPlayerDistance = newMax;
    }

    private ObjectGuid getEventStarterGUID() {
        return playerGUID;
    }

    private void addEscortState(EscortState escortState) {
        escortState = EscortState.forValue(escortState.getValue() | escortState.getValue());
    }

    private void removeEscortState(EscortState escortState) {
        escortState = EscortState.forValue(escortState.getValue() & ~escortState.getValue());
    }
}
