package com.github.azeroth.game.ai;


import com.github.azeroth.game.domain.misc.WaypointMoveType;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.SpellInfo;

public class SmartAI extends CreatureAI {
    private static final int SMART_ESCORT_MAX_PLAYER_DIST = 60;
    private static final int SMART_MAX_AID_DIST = SMART_ESCORT_MAX_PLAYER_DIST / 2;
    private final smartScript script = new smartScript();
    private final waypointPath path = new waypointPath();
    // Vehicle conditions
    private final boolean hasConditions;
    public int escortQuestID;
    private boolean isCharmed;
    private int followCreditType;
    private int followArrivedTimer;
    private int followCredit;
    private int followArrivedEntry;
    private ObjectGuid followGuid = ObjectGuid.EMPTY;
    private float followDist;
    private float followAngle;

    private SmartEscortState escortState = SmartEscortState.values()[0];
    private int escortNPCFlags;
    private int escortInvokerCheckTimer;
    private int currentWaypointNode;
    private boolean waypointReached;
    private int waypointPauseTimer;
    private boolean waypointPauseForced;
    private boolean repeatWaypointPath;
    private boolean _OOCReached;
    private boolean waypointPathEnded;

    private boolean run;
    private boolean evadeDisabled;
    private boolean canCombatMove;
    private int invincibilityHpLevel;

    private int despawnTime;
    private int despawnState;
    private int conditionsTimer;

    // Gossip
    private boolean gossipReturn;

    public SmartAI(Creature creature) {
        super(creature);
        escortInvokerCheckTimer = 1000;
        run = true;
        canCombatMove = true;

        hasConditions = global.getConditionMgr().hasConditionsForNotGroupedEntry(ConditionSourceType.CreatureTemplateVehicle, creature.getEntry());
    }


    public final void startPath(boolean run, int pathId, boolean repeat, Unit invoker) {
        startPath(run, pathId, repeat, invoker, 1);
    }

    public final void startPath(boolean run, int pathId, boolean repeat) {
        startPath(run, pathId, repeat, null, 1);
    }

    public final void startPath(boolean run, int pathId) {
        startPath(run, pathId, false, null, 1);
    }

    public final void startPath(boolean run) {
        startPath(run, 0, false, null, 1);
    }

    public final void startPath() {
        startPath(false, 0, false, null, 1);
    }

    public final void startPath(boolean run, int pathId, boolean repeat, Unit invoker, int nodeId) {
        if (hasEscortState(SmartEscortState.Escorting)) {
            stopPath();
        }

        setRun(run);

        if (pathId != 0) {
            if (!loadPath(pathId)) {
                return;
            }
        }

        if (path.nodes.isEmpty()) {
            return;
        }

        currentWaypointNode = nodeId;
        waypointPathEnded = false;

        repeatWaypointPath = repeat;

        // Do not use AddEscortState, removing everything from previous
        escortState = SmartEscortState.Escorting;

        if (invoker && invoker.isPlayer()) {
            escortNPCFlags = (int) me.getNpcFlags().getValue();
            me.replaceAllNpcFlags(NPCFlags.NONE);
        }

        me.getMotionMaster().movePath(path, repeatWaypointPath);
    }

    public final void pausePath(int delay, boolean forced) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            me.pauseMovement(delay, MovementSlot.Default, forced);

            if (me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {

                var(nodeId, pathId) = me.CurrentWaypointInfo;
                getScript().processEventsFor(SmartEvents.WaypointPaused, null, nodeId, pathId);
            }

            return;
        }

        if (hasEscortState(SmartEscortState.paused)) {
            Log.outError(LogFilter.Server, String.format("SmartAI.PausePath: Creature entry %1$s wanted to pause waypoint movement while already paused, ignoring.", me.getEntry()));

            return;
        }

        waypointPauseTimer = delay;

        if (forced) {
            waypointPauseForced = forced;
            setRun(run);
            me.pauseMovement();
            me.setHomePosition(me.getLocation());
        } else {
            waypointReached = false;
        }

        addEscortState(SmartEscortState.paused);
        getScript().processEventsFor(SmartEvents.WaypointPaused, null, currentWaypointNode, getScript().getPathId());
    }

    public final boolean canResumePath() {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            // The whole resume logic doesn't support this case
            return false;
        }

        return hasEscortState(SmartEscortState.paused);
    }


    public final void stopPath(int despawnTime, int quest) {
        stopPath(despawnTime, quest, false);
    }

    public final void stopPath(int despawnTime) {
        stopPath(despawnTime, 0, false);
    }

    public final void stopPath() {
        stopPath(0, 0, false);
    }

    public final void stopPath(int despawnTime, int quest, boolean fail) {
        if (!hasEscortState(SmartEscortState.Escorting)) {

            ( int nodeId, int pathId)waypointInfo = new ();

            if (me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {
                waypointInfo = me.CurrentWaypointInfo;
            }

            if (despawnState != 2) {
                setDespawnTime(despawnTime);
            }

            me.getMotionMaster().moveIdle();

            if (waypointInfo.Item1 != 0) {
                getScript().processEventsFor(SmartEvents.WaypointStopped, null, waypointInfo.Item1, waypointInfo.item2);
            }

            if (!fail) {
                if (waypointInfo.Item1 != 0) {
                    getScript().processEventsFor(SmartEvents.WaypointEnded, null, waypointInfo.Item1, waypointInfo.item2);
                }

                if (despawnState == 1) {
                    startDespawn();
                }
            }

            return;
        }

        if (quest != 0) {
            escortQuestID = quest;
        }

        if (despawnState != 2) {
            setDespawnTime(despawnTime);
        }

        me.getMotionMaster().moveIdle();

        getScript().processEventsFor(SmartEvents.WaypointStopped, null, currentWaypointNode, getScript().getPathId());

        endPath(fail);
    }


    public final void endPath() {
        endPath(false);
    }

    public final void endPath(boolean fail) {
        removeEscortState(SmartEscortState.Escorting.getValue() | SmartEscortState.paused.getValue() | SmartEscortState.Returning.getValue());
        path.nodes.clear();
        waypointPauseTimer = 0;

        if (escortNPCFlags != 0) {
            me.replaceAllNpcFlags(NPCFlags.forValue(escortNPCFlags));
            escortNPCFlags = 0;
        }

        var targets = getScript().getStoredTargetList(SharedConst.SmartEscortTargets, me);

        if (targets != null && escortQuestID != 0) {
            if (targets.size() == 1 && getScript().isPlayer(targets.get(0))) {
                var player = targets.get(0).AsPlayer;

                if (!fail && player.isAtGroupRewardDistance(me) && player.getCorpse() == null) {
                    player.groupEventHappens(escortQuestID, me);
                }

                if (fail) {
                    player.failQuest(escortQuestID);
                }

                var group = player.group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.next()) {
                        var groupGuy = groupRef.source;

                        if (!groupGuy.isInMap(player)) {
                            continue;
                        }

                        if (!fail && groupGuy.isAtGroupRewardDistance(me) && !groupGuy.getCorpse()) {
                            groupGuy.areaExploredOrEventHappens(escortQuestID);
                        } else if (fail) {
                            groupGuy.failQuest(escortQuestID);
                        }
                    }
                }
            } else {
                for (var obj : targets) {
                    if (getScript().isPlayer(obj)) {
                        var player = obj.toPlayer();

                        if (!fail && player.isAtGroupRewardDistance(me) && player.getCorpse() == null) {
                            player.areaExploredOrEventHappens(escortQuestID);
                        } else if (fail) {
                            player.failQuest(escortQuestID);
                        }
                    }
                }
            }
        }

        // End Path events should be only processed if it was SUCCESSFUL stop or stop called by SMART_ACTION_WAYPOINT_STOP
        if (fail) {
            return;
        }

        var pathid = getScript().getPathId();
        getScript().processEventsFor(SmartEvents.WaypointEnded, null, currentWaypointNode, pathid);

        if (repeatWaypointPath) {
            if (isAIControlled()) {
                startPath(run, getScript().getPathId(), repeatWaypointPath);
            }
        } else if (pathid == getScript().getPathId()) // if it's not the same pathid, our script wants to start another path; don't override it
        {
            getScript().setPathId(0);
        }

        if (despawnState == 1) {
            startDespawn();
        }
    }

    public final void resumePath() {
        getScript().processEventsFor(SmartEvents.WaypointResumed, null, currentWaypointNode, getScript().getPathId());

        removeEscortState(SmartEscortState.paused);

        waypointPauseForced = false;
        waypointReached = false;
        waypointPauseTimer = 0;

        setRun(run);
        me.resumeMovement();
    }

    @Override
    public void updateAI(int diff) {
        if (!me.isAlive()) {
            if (isEngaged()) {
                engagementOver();
            }

            return;
        }

        checkConditions(diff);

        var hasVictim = updateVictim();

        getScript().onUpdate(diff);

        updatePath(diff);
        updateFollow(diff);
        updateDespawn(diff);

        if (!isAIControlled()) {
            return;
        }

        if (!hasVictim) {
            return;
        }

        doMeleeAttackIfReady();
    }

    @Override
    public void waypointReached(int nodeId, int pathId) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            getScript().processEventsFor(SmartEvents.WaypointReached, null, nodeId, pathId);

            return;
        }

        currentWaypointNode = nodeId;

        getScript().processEventsFor(SmartEvents.WaypointReached, null, currentWaypointNode, pathId);

        if (waypointPauseTimer != 0 && !waypointPauseForced) {
            waypointReached = true;
            me.pauseMovement();
            me.setHomePosition(me.getLocation());
        } else if (hasEscortState(SmartEscortState.Escorting) && me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {
            if (currentWaypointNode == path.nodes.size()) {
                waypointPathEnded = true;
            } else {
                setRun(run);
            }
        }
    }

    @Override
    public void waypointPathEnded(int nodeId, int pathId) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            getScript().processEventsFor(SmartEvents.WaypointEnded, null, nodeId, pathId);

            return;
        }
    }

    @Override
    public void movementInform(MovementGeneratorType movementType, int id) {
        if (movementType == MovementGeneratorType.Point && id == eventId.SmartEscortLastOCCPoint) {
            me.clearUnitState(UnitState.Evade);
        }

        getScript().processEventsFor(SmartEvents.Movementinform, null, (int) movementType.getValue(), id);

        if (!hasEscortState(SmartEscortState.Escorting)) {
            return;
        }

        if (movementType != MovementGeneratorType.Point && id == eventId.SmartEscortLastOCCPoint) {
            _OOCReached = true;
        }
    }

    public final void startAttackOnOwnersInCombatWith() {
        Player owner;
        tangible.OutObject<unit> tempOut_owner = new tangible.OutObject<unit>();
        if (!me.tryGetOwner(tempOut_owner)) {
            owner = tempOut_owner.outArgValue;
            return;
        } else {
            owner = tempOut_owner.outArgValue;
        }

        var summon = me.toTempSummon();

        if (summon != null) {
            var attack = owner.getSelectedUnit();

            if (attack == null) {
                attack = owner.getAttackers().FirstOrDefault();
            }

            if (attack != null) {
                summon.attack(attack, true);
            }
        }
    }


    @Override
    public void enterEvadeMode() {
        enterEvadeMode(EvadeReason.other);
    }

    @Override
    public void enterEvadeMode(EvadeReason why) {
        if (evadeDisabled) {
            getScript().processEventsFor(SmartEvents.Evade);

            return;
        }

        if (!isAIControlled()) {
            me.attackStop();

            return;
        }

        if (!_EnterEvadeMode()) {
            return;
        }

        me.addUnitState(UnitState.Evade);

        getScript().processEventsFor(SmartEvents.Evade); // must be after _EnterEvadeMode (spells, auras, ...)

        setRun(run);

        var owner = me.getCharmerOrOwner();

        if (owner != null) {
            me.getMotionMaster().moveFollow(owner, SharedConst.PetFollowDist, SharedConst.PetFollowAngle);
            me.clearUnitState(UnitState.Evade);
        } else if (hasEscortState(SmartEscortState.Escorting)) {
            addEscortState(SmartEscortState.Returning);
            returnToLastOOCPos();
        } else {
            var target = !followGuid.isEmpty() ? global.getObjAccessor().GetUnit(me, followGuid) : null;

            if (target) {
                me.getMotionMaster().moveFollow(target, followDist, followAngle);
                // evade is not cleared in MoveFollow, so we can't keep it
                me.clearUnitState(UnitState.Evade);
            } else {
                me.getMotionMaster().moveTargetedHome();
            }
        }

        if (!me.hasUnitState(UnitState.Evade)) {
            getScript().onReset();
        }
    }

    @Override
    public void moveInLineOfSight(Unit who) {
        if (who == null) {
            return;
        }

        getScript().onMoveInLineOfSight(who);

        if (!isAIControlled()) {
            return;
        }

        if (hasEscortState(SmartEscortState.Escorting) && assistPlayerInCombatAgainst(who)) {
            return;
        }

        super.moveInLineOfSight(who);
    }

    @Override
    public void initializeAI() {
        getScript().onInitialize(me);

        despawnTime = 0;
        despawnState = 0;
        escortState = SmartEscortState.NONE;

        followGuid.clear(); //do not reset follower on reset(), we need it after combat evade
        followDist = 0;
        followAngle = 0;
        followCredit = 0;
        followArrivedTimer = 1000;
        followArrivedEntry = 0;
        followCreditType = 0;
    }

    @Override
    public void justAppeared() {
        super.justAppeared();

        if (me.isDead()) {
            return;
        }

        getScript().processEventsFor(SmartEvents.Respawn);
        getScript().onReset();
    }

    @Override
    public void justReachedHome() {
        getScript().onReset();
        getScript().processEventsFor(SmartEvents.ReachedHome);

        var formation = me.getFormation();

        if (formation == null || formation.getLeader() == me || !formation.isFormed()) {
            if (me.getMotionMaster().getCurrentMovementGeneratorType(MovementSlot.Default) != MovementGeneratorType.Waypoint) {
                if (me.getWaypointPath() != 0) {
                    me.getMotionMaster().movePath(me.getWaypointPath(), true);
                }
            }

            me.resumeMovement();
        } else if (formation.isFormed()) {
            me.getMotionMaster().moveIdle(); // wait the order of leader
        }
    }

    @Override
    public void justEngagedWith(Unit victim) {
        if (isAIControlled()) {
            me.interruptNonMeleeSpells(false); // must be before ProcessEvents
        }

        getScript().processEventsFor(SmartEvents.Aggro, victim);
    }

    @Override
    public void justDied(Unit killer) {
        if (hasEscortState(SmartEscortState.Escorting)) {
            endPath(true);
        }

        getScript().processEventsFor(SmartEvents.Death, killer);
    }

    @Override
    public void killedUnit(Unit victim) {
        getScript().processEventsFor(SmartEvents.kill, victim);
    }

    @Override
    public void justSummoned(Creature summon) {
        getScript().processEventsFor(SmartEvents.SummonedUnit, summon);
    }

    @Override
    public void summonedCreatureDies(Creature summon, Unit killer) {
        getScript().processEventsFor(SmartEvents.SummonedUnitDies, summon);
    }

    @Override
    public void attackStart(Unit who) {
        // dont allow charmed npcs to act on their own
        if (!isAIControlled()) {
            if (who != null) {
                me.attack(who, true);
            }

            return;
        }

        if (who != null && me.attack(who, true)) {
            me.getMotionMaster().clear(MovementGeneratorPriority.NORMAL);
            me.pauseMovement();

            if (canCombatMove) {
                setRun(run);
                me.getMotionMaster().moveChase(who);
            }
        }
    }

    @Override
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHit, caster.toUnit(), 0, 0, false, spellInfo, caster.toGameObject());
    }

    @Override
    public void spellHitTarget(WorldObject target, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHitTarget, target.toUnit(), 0, 0, false, spellInfo, target.toGameObject());
    }

    @Override
    public void onSpellCast(SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.OnSpellCast, null, 0, 0, false, spellInfo);
    }

    @Override
    public void onSpellFailed(SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.OnSpellFailed, null, 0, 0, false, spellInfo);
    }

    @Override
    public void onSpellStart(SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.OnSpellStart, null, 0, 0, false, spellInfo);
    }


    @Override
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType) {
        damageTaken(attacker, damage, damageType, null);
    }

    @Override
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.Damaged, attacker, damage.refArgValue.intValue());

        if (!isAIControlled()) // don't allow players to use unkillable units
        {
            return;
        }

        if (invincibilityHpLevel != 0 && (damage.refArgValue >= me.getHealth() - invincibilityHpLevel)) {
            damage.refArgValue = (int) (me.getHealth() - invincibilityHpLevel); // damage should not be nullified, because of player damage req.
        }
    }

    @Override
    public void healReceived(Unit by, double addhealth) {
        getScript().processEventsFor(SmartEvents.ReceiveHeal, by, (int) addhealth);
    }

    @Override
    public void receiveEmote(Player player, TextEmotes emoteId) {
        getScript().processEventsFor(SmartEvents.ReceiveEmote, player, (int) emoteId.getValue());
    }

    @Override
    public void isSummonedBy(WorldObject summoner) {
        getScript().processEventsFor(SmartEvents.JustSummoned, summoner.toUnit(), 0, 0, false, null, summoner.toGameObject());
    }

    @Override
    public void damageDealt(Unit victim, tangible.RefObject<Double> damage, DamageEffectType damageType) {
        getScript().processEventsFor(SmartEvents.DamagedTarget, victim, damage.refArgValue.intValue());
    }

    @Override
    public void summonedCreatureDespawn(Creature summon) {
        getScript().processEventsFor(SmartEvents.SummonDespawned, summon, summon.getEntry());
    }

    @Override
    public void corpseRemoved(long respawnDelay) {
        getScript().processEventsFor(SmartEvents.CorpseRemoved, null, (int) respawnDelay);
    }

    @Override
    public void onDespawn() {
        getScript().processEventsFor(SmartEvents.OnDespawn);
    }

    @Override
    public void passengerBoarded(Unit passenger, byte seatId, boolean apply) {
        getScript().processEventsFor(apply ? SmartEvents.PassengerBoarded : SmartEvents.PassengerRemoved, passenger, (int) seatId, 0, apply);
    }

    @Override
    public void onCharmed(boolean isNew) {
        var charmed = me.isCharmed();

        if (charmed) // do this before we change charmed state, as charmed state might prevent these things from processing
        {
            if (hasEscortState(SmartEscortState.Escorting.getValue() | SmartEscortState.paused.getValue() | SmartEscortState.Returning.getValue())) {
                endPath(true);
            }
        }

        isCharmed = charmed;

        if (charmed && !me.isPossessed() && !me.isVehicle()) {
            me.getMotionMaster().moveFollow(me.getCharmer(), SharedConst.PetFollowDist, me.getFollowAngle());
        }

        if (!charmed && !me.isInEvadeMode()) {
            if (repeatWaypointPath) {
                startPath(run, getScript().getPathId(), true);
            } else {
                me.setWalk(!run);
            }

            if (!me.getLastCharmerGuid().isEmpty()) {
                if (!me.hasReactState(ReactStates.Passive)) {
                    var lastCharmer = global.getObjAccessor().GetUnit(me, me.getLastCharmerGuid());

                    if (lastCharmer != null) {
                        me.engageWithTarget(lastCharmer);
                    }
                }

                me.getLastCharmerGuid().clear();

                if (!me.isInCombat()) {
                    enterEvadeMode(EvadeReason.NoHostiles);
                }
            }
        }

        getScript().processEventsFor(SmartEvents.Charmed, null, 0, 0, charmed);

        if (!getScript().hasAnyEventWithFlag(SmartEventFlags.WhileCharmed)) // we can change AI if there are no events with this flag
        {
            super.onCharmed(isNew);
        }
    }

    @Override
    public void doAction(int param) {
        getScript().processEventsFor(SmartEvents.ActionDone, null, (int) param);
    }

    @Override
    public int getData(int id) {
        return 0;
    }

    @Override
    public void setData(int id, int value) {
        setData(id, value, null);
    }

    public final void setData(int id, int value, Unit invoker) {
        getScript().processEventsFor(SmartEvents.DataSet, invoker, id, value);
    }

    @Override
    public void setGUID(ObjectGuid guid, int id) {
    }

    @Override
    public ObjectGuid getGUID(int id) {
        return ObjectGuid.Empty;
    }

    public final void setRun(boolean run) {
        me.setWalk(!run);
        run = run;

        for (var node : path.nodes) {
            node.moveType = run ? WaypointMoveType.Run : WaypointMoveType.Walk;
        }
    }


    public final void setDisableGravity() {
        setDisableGravity(true);
    }

    public final void setDisableGravity(boolean disable) {
        me.setDisableGravity(disable);
    }

    public final void setEvadeDisabled(boolean disable) {
        evadeDisabled = disable;
    }

    @Override
    public boolean onGossipHello(Player player) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipHello, player);

        return gossipReturn;
    }

    @Override
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipSelect, player, menuId, gossipListId);

        return gossipReturn;
    }

    @Override
    public boolean onGossipSelectCode(Player player, int menuId, int gossipListId, String code) {
        return false;
    }

    @Override
    public void onQuestAccept(Player player, Quest quest) {
        getScript().processEventsFor(SmartEvents.AcceptedQuest, player, quest.id);
    }

    @Override
    public void onQuestReward(Player player, Quest quest, LootItemType type, int opt) {
        getScript().processEventsFor(SmartEvents.RewardQuest, player, quest.id, opt);
    }


    public final void setCombatMove(boolean on) {
        setCombatMove(on, false);
    }

    public final void setCombatMove(boolean on, boolean stopMoving) {
        if (canCombatMove == on) {
            return;
        }

        canCombatMove = on;

        if (!isAIControlled()) {
            return;
        }

        if (me.isEngaged()) {
            if (on) {
                if (!me.hasReactState(ReactStates.Passive) && me.getVictim() && !me.getMotionMaster().hasMovementGenerator(movement ->
                {
                    return movement.getMovementGeneratorType() == MovementGeneratorType.chase && movement.mode == MovementGeneratorMode.Default && movement.priority == MovementGeneratorPriority.NORMAL;
                })) {
                    setRun(run);
                    me.getMotionMaster().moveChase(me.getVictim());
                }
            } else {
                var movement = me.getMotionMaster().getMovementGenerator(a -> a.getMovementGeneratorType() == MovementGeneratorType.chase && a.mode == MovementGeneratorMode.Default && a.priority == MovementGeneratorPriority.NORMAL);

                if (movement != null) {
                    me.getMotionMaster().remove(movement);

                    if (stopMoving) {
                        me.stopMoving();
                    }
                }
            }
        }
    }

    public final void setFollow(Unit target, float dist, float angle, int credit, int end, int creditType) {
        if (target == null) {
            stopFollow(false);

            return;
        }

        followGuid = target.getGUID();
        followDist = dist;
        followAngle = angle;
        followArrivedTimer = 1000;
        followCredit = credit;
        followArrivedEntry = end;
        followCreditType = creditType;
        setRun(run);
        me.getMotionMaster().moveFollow(target, followDist, followAngle);
    }

    public final void stopFollow(boolean complete) {
        followGuid.clear();
        followDist = 0;
        followAngle = 0;
        followCredit = 0;
        followArrivedTimer = 1000;
        followArrivedEntry = 0;
        followCreditType = 0;
        me.getMotionMaster().clear();
        me.stopMoving();
        me.getMotionMaster().moveIdle();

        if (!complete) {
            return;
        }

        var player = global.getObjAccessor().getPlayer(me, followGuid);

        if (player != null) {
            if (followCreditType == 0) {
                player.rewardPlayerAndGroupAtEvent(followCredit, me);
            } else {
                player.groupEventHappens(followCredit, me);
            }
        }

        setDespawnTime(5000);
        startDespawn();
        getScript().processEventsFor(SmartEvents.FollowCompleted, player);
    }


    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        setTimedActionList(e, entry, invoker, 0);
    }

    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker, int startFromEventId) {
        getScript().setTimedActionList(e, entry, invoker, startFromEventId);
    }

    @Override
    public void onGameEvent(boolean start, short eventId) {
        getScript().processEventsFor(start ? SmartEvents.GameEventStart : SmartEvents.GameEventEnd, null, eventId);
    }

    @Override
    public void onSpellClick(Unit clicker, tangible.RefObject<Boolean> spellClickHandled) {
        if (!spellClickHandled.refArgValue) {
            return;
        }

        getScript().processEventsFor(SmartEvents.OnSpellclick, clicker);
    }

    @Override
    public void reset() {
        if (!hasEscortState(SmartEscortState.Escorting)) //dont mess up escort movement after combat
        {
            setRun(run);
        }

        getScript().onReset();
    }

    public final boolean hasEscortState(SmartEscortState escortState) {
        return (escortState.getValue() & escortState.getValue()) != 0;
    }

    public final void addEscortState(SmartEscortState escortState) {
        escortState = SmartEscortState.forValue(escortState.getValue() | escortState.getValue());
    }

    public final void removeEscortState(SmartEscortState escortState) {
        escortState = SmartEscortState.forValue(escortState.getValue() & ~escortState.getValue());
    }

    public final boolean canCombatMove() {
        return canCombatMove;
    }

    public final SmartScript getScript() {
        return script;
    }

    public final void setInvincibilityHpLevel(int level) {
        invincibilityHpLevel = level;
    }


    public final void setDespawnTime(int t) {
        setDespawnTime(t, 0);
    }

    public final void setDespawnTime(int t, int r) {
        despawnTime = t;
        despawnState = t != 0 ? 1 : 0;
    }

    public final void startDespawn() {
        despawnState = 2;
    }

    public final void setWPPauseTimer(int time) {
        waypointPauseTimer = time;
    }

    public final void setGossipReturn(boolean val) {
        gossipReturn = val;
    }

    private boolean isAIControlled() {
        return !isCharmed;
    }

    private boolean loadPath(int entry) {
        if (hasEscortState(SmartEscortState.Escorting)) {
            return false;
        }

        var path = global.getSmartAIMgr().getPath(entry);

        if (path == null || path.nodes.isEmpty()) {
            getScript().setPathId(0);

            return false;
        }

        path.id = path.id;
        path.nodes.addAll(path.nodes);

        for (var waypoint : path.nodes) {
            waypoint.x = MapDefine.normalizeMapCoord(waypoint.x);
            waypoint.y = MapDefine.normalizeMapCoord(waypoint.y);
            waypoint.moveType = _run ? WaypointMoveType.Run : WaypointMoveType.Walk;
        }

        getScript().setPathId(entry);

        return true;
    }

    private void returnToLastOOCPos() {
        if (!isAIControlled()) {
            return;
        }

        me.setWalk(false);
        me.getMotionMaster().movePoint(eventId.SmartEscortLastOCCPoint, me.getHomePosition());
    }

    private boolean isEscortInvokerInRange() {
        var targets = getScript().getStoredTargetList(SharedConst.SmartEscortTargets, me);

        if (targets != null) {
            float checkDist = me.getInstanceScript() != null ? SMART_ESCORT_MAX_PLAYER_DIST * 2 : SMART_ESCORT_MAX_PLAYER_DIST;

            if (targets.size() == 1 && getScript().isPlayer(targets.get(0))) {
                var player = targets.get(0).AsPlayer;

                if (me.getDistance(player) <= checkDist) {
                    return true;
                }

                var group = player.group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.next()) {
                        var groupGuy = groupRef.source;

                        if (groupGuy.isInMap(player) && me.getDistance(groupGuy) <= checkDist) {
                            return true;
                        }
                    }
                }
            } else {
                for (var obj : targets) {
                    if (getScript().isPlayer(obj)) {
                        if (me.getDistance(obj.toPlayer()) <= checkDist) {
                            return true;
                        }
                    }
                }
            }

            // no valid target found
            return false;
        }

        // no player invoker was stored, just ignore range check
        return true;
    }

    private boolean assistPlayerInCombatAgainst(Unit who) {
        if (me.hasReactState(ReactStates.Passive) || !isAIControlled()) {
            return false;
        }

        if (who == null || who.getVictim() == null) {
            return false;
        }

        //experimental (unknown) flag not present
        if (!me.getCreatureTemplate().typeFlags.hasFlag(CreatureTypeFlags.CanAssist)) {
            return false;
        }

        //not a player
        if (who.getVictim().getCharmerOrOwnerPlayerOrPlayerItself() == null) {
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
        if (who.isCreature() && who.toCreature().isInEvadeMode()) {
            return false;
        }

        if (!me.isValidAssistTarget(who.getVictim())) {
            return false;
        }

        //too far away and no free sight
        if (me.isWithinDistInMap(who, SMART_MAX_AID_DIST) && me.isWithinLOSInMap(who)) {
            me.engageWithTarget(who);

            return true;
        }

        return false;
    }

    private void checkConditions(int diff) {
        if (!hasConditions) {
            return;
        }

        if (conditionsTimer <= diff) {
            var vehicleKit = me.getVehicleKit();

            if (vehicleKit != null) {
                for (var pair : vehicleKit.Seats.entrySet()) {
                    var passenger = global.getObjAccessor().GetUnit(me, pair.getValue().passenger.guid);

                    if (passenger != null) {
                        var player = passenger.toPlayer();

                        if (player != null) {
                            if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.CreatureTemplateVehicle, me.getEntry(), player, me)) {
                                player.exitVehicle();

                                return; // check other pessanger in next tick
                            }
                        }
                    }
                }
            }

            conditionsTimer = 1000;
        } else {
            _conditionsTimer -= diff;
        }
    }

    private void updatePath(int diff) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            return;
        }

        if (escortInvokerCheckTimer < diff) {
            if (!isEscortInvokerInRange()) {
                stopPath(0, escortQuestID, true);

                // allow to properly hook out of range despawn action, which in most cases should perform the same operation as dying
                getScript().processEventsFor(SmartEvents.Death, me);
                me.despawnOrUnsummon();

                return;
            }

            escortInvokerCheckTimer = 1000;
        } else {
            _escortInvokerCheckTimer -= diff;
        }

        // handle pause
        if (hasEscortState(SmartEscortState.paused) && (waypointReached || waypointPauseForced)) {
            // Resume only if there was a pause timer set
            if (waypointPauseTimer != 0 && !me.isInCombat() && !hasEscortState(SmartEscortState.Returning)) {
                if (waypointPauseTimer <= diff) {
                    resumePath();
                } else {
                    _waypointPauseTimer -= diff;
                }
            }
        } else if (waypointPathEnded) // end path
        {
            waypointPathEnded = false;
            stopPath();

            return;
        }

        if (hasEscortState(SmartEscortState.Returning)) {
            if (_OOCReached) //reached OOC WP
            {
                _OOCReached = false;
                removeEscortState(SmartEscortState.Returning);

                if (!hasEscortState(SmartEscortState.paused)) {
                    resumePath();
                }
            }
        }
    }

    private void updateFollow(int diff) {
        if (followGuid.isEmpty()) {
            if (followArrivedTimer < diff) {
                if (me.findNearestCreature(followArrivedEntry, SharedConst.InteractionDistance, true)) {
                    stopFollow(true);

                    return;
                }

                followArrivedTimer = 1000;
            } else {
                _followArrivedTimer -= diff;
            }
        }
    }

    private void updateDespawn(int diff) {
        if (despawnState <= 1 || despawnState > 3) {
            return;
        }

        if (despawnTime < diff) {
            if (despawnState == 2) {
                me.setVisible(false);
                despawnTime = 5000;
                despawnState++;
            } else {
                me.despawnOrUnsummon();
            }
        } else {
            _despawnTime -= diff;
        }
    }
}
