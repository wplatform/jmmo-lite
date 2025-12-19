package com.github.azeroth.game.ai;



import game.maps.grids.*;
import game.spells.*;







public class SmartAI extends CreatureAI {

//ORIGINAL LINE: public uint EscortQuestID;
    public int escortQuestID;
    private static final int SMART_ESCORT_MAX_PLAYER_DIST = 60;
    private static final int SMART_MAX_AID_DIST = SMART_ESCORT_MAX_PLAYER_DIST / 2;
    private final SmartScript script = new SmartScript();
    private final WaypointPath path = new WaypointPath();

    // Vehicle conditions
    private final boolean hasConditions;

    private boolean isCharmed;

//ORIGINAL LINE: uint _followCreditType;
    private int followCreditType;

//ORIGINAL LINE: uint _followArrivedTimer;
    private int followArrivedTimer;

//ORIGINAL LINE: uint _followCredit;
    private int followCredit;

//ORIGINAL LINE: uint _followArrivedEntry;
    private int followArrivedEntry;
    private ObjectGuid followGuid = new ObjectGuid();
    private float followDist;
    private float followAngle;

    private SmartEscortState escortState = SmartEscortState.values()[0];

//ORIGINAL LINE: uint _escortNPCFlags;
    private int escortNPCFlags;

//ORIGINAL LINE: uint _escortInvokerCheckTimer;
    private int escortInvokerCheckTimer;

//ORIGINAL LINE: uint _currentWaypointNode;
    private int currentWaypointNode;
    private boolean waypointReached;

//ORIGINAL LINE: uint _waypointPauseTimer;
    private int waypointPauseTimer;
    private boolean waypointPauseForced;
    private boolean repeatWaypointPath;
    private boolean oOCReached;
    private boolean waypointPathEnded;

    private boolean run;
    private boolean evadeDisabled;
    private boolean canCombatMove;

//ORIGINAL LINE: uint _invincibilityHpLevel;
    private int invincibilityHpLevel;


//ORIGINAL LINE: uint _despawnTime;
    private int despawnTime;

//ORIGINAL LINE: uint _despawnState;
    private int despawnState;

//ORIGINAL LINE: uint _conditionsTimer;
    private int conditionsTimer;

    // Gossip
    private boolean gossipReturn;

    public SmartAI(Creature creature) {
        super(creature);
        escortInvokerCheckTimer = 1000;
        run = true;
        canCombatMove = true;

        hasConditions = Global.getConditionMgr().hasConditionsForNotGroupedEntry(ConditionSourceType.CreatureTemplateVehicle, creature.getEntry());
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


//ORIGINAL LINE: public void StartPath(bool run = false, uint pathId = 0, bool repeat = false, Unit invoker = null, uint nodeId = 1)

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

        if (path.nodes.Empty()) {
            return;
        }

        currentWaypointNode = nodeId;
        waypointPathEnded = false;

        repeatWaypointPath = repeat;

        // Do not use AddEscortState, removing everything from previous
        escortState = SmartEscortState.Escorting;

        if (invoker && invoker.isPlayer()) {

//ORIGINAL LINE: _escortNPCFlags = (uint)Me.NpcFlags;
            escortNPCFlags = (int)me.getNpcFlags().getValue();
            me.replaceAllNpcFlags(NPCFlags.None);
        }

        me.getMotionMaster().movePath(path, repeatWaypointPath);
    }


//ORIGINAL LINE: public void PausePath(uint delay, bool forced)
    public final void pausePath(int delay, boolean forced) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            me.pauseMovement(delay, MovementSlot.Default, forced);

            if (me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {
//C# TO JAVA CONVERTER TODO TASK: Java has no equivalent to C# deconstruction declarations:
                var(nodeId, pathId) = Me.CurrentWaypointInfo;
                getScript().processEventsFor(SmartEvents.WaypointPaused, null, nodeId, pathId);
            }

            return;
        }

        if (hasEscortState(SmartEscortState.Paused)) {
            Log.outError(LogFilter.Server, String.format("SmartAI.PausePath: Creature entry %1$s wanted to pause waypoint movement while already paused, ignoring.", me.getEntry()));

            return;
        }

        waypointPauseTimer = delay;

        if (forced) {
            waypointPauseForced = forced;
            setRun(run);
            me.pauseMovement();
            me.setHomePosition(me.location);
        } else {
            waypointReached = false;
        }

        addEscortState(SmartEscortState.Paused);
        getScript().processEventsFor(SmartEvents.WaypointPaused, null, currentWaypointNode, getScript().getPathId());
    }
    public final boolean canResumePath() {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            // The whole resume logic doesn't support this case
            return false;
        }

        return hasEscortState(SmartEscortState.Paused);
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


//ORIGINAL LINE: public void StopPath(uint despawnTime = 0, uint quest = 0, bool fail = false)

    public final void stopPath(int despawnTime, int quest, boolean fail) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
//C# TO JAVA CONVERTER TODO TASK: Tuple variables are not converted by C# to Java Converter:
            (int nodeId, int pathId) waypointInfo = new();

            if (me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {
                waypointInfo = me.CurrentWaypointInfo;
            }

            if (despawnState != 2) {
                setDespawnTime(despawnTime);
            }

            me.getMotionMaster().moveIdle();

            if (waypointInfo.Item1 != 0) {
                getScript().processEventsFor(SmartEvents.WaypointStopped, null, waypointInfo.Item1, waypointInfo.Item2);
            }

            if (!fail) {
                if (waypointInfo.Item1 != 0) {
                    getScript().processEventsFor(SmartEvents.WaypointEnded, null, waypointInfo.Item1, waypointInfo.Item2);
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


//ORIGINAL LINE: public void EndPath(bool fail = false)
    public final void endPath(boolean fail) {
        removeEscortState(SmartEscortState.Escorting.getValue() | SmartEscortState.Paused.getValue() | SmartEscortState.Returning.getValue());
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

                if (!fail && player.IsAtGroupRewardDistance(me) && player.GetCorpse() == null) {
                    player.GroupEventHappens(escortQuestID, me);
                }

                if (fail) {
                    player.FailQuest(escortQuestID);
                }

                var group = player.Group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.Next()) {
                        var groupGuy = groupRef.Source;

                        if (!groupGuy.IsInMap(player)) {
                            continue;
                        }

                        if (!fail && groupGuy.IsAtGroupRewardDistance(me) && !groupGuy.GetCorpse()) {
                            groupGuy.AreaExploredOrEventHappens(escortQuestID);
                        } else if (fail) {
                            groupGuy.FailQuest(escortQuestID);
                        }
                    }
                }
            } else {
                for (var obj : targets) {
                    if (getScript().isPlayer(obj)) {
                        var player = obj.getAsPlayer();

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
        } else if (pathid == getScript().getPathId()) { // if it's not the same pathid, our script wants to start another path; don't override it
            getScript().setPathId(0);
        }

        if (despawnState == 1) {
            startDespawn();
        }
    }

    public final void resumePath() {
        getScript().processEventsFor(SmartEvents.WaypointResumed, null, currentWaypointNode, getScript().getPathId());

        removeEscortState(SmartEscortState.Paused);

        waypointPauseForced = false;
        waypointReached = false;
        waypointPauseTimer = 0;

        setRun(run);
        me.resumeMovement();
    }


//ORIGINAL LINE: public override void UpdateAI(uint diff)
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


//ORIGINAL LINE: public override void WaypointReached(uint nodeId, uint pathId)
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
            me.setHomePosition(me.location);
        } else if (hasEscortState(SmartEscortState.Escorting) && me.getMotionMaster().getCurrentMovementGeneratorType() == MovementGeneratorType.Waypoint) {
            if (currentWaypointNode == path.nodes.size()) {
                waypointPathEnded = true;
            } else {
                setRun(run);
            }
        }
    }


//ORIGINAL LINE: public override void WaypointPathEnded(uint nodeId, uint pathId)
    @Override
    public void waypointPathEnded(int nodeId, int pathId) {
        if (!hasEscortState(SmartEscortState.Escorting)) {
            getScript().processEventsFor(SmartEvents.WaypointEnded, null, nodeId, pathId);

            return;
        }
    }


//ORIGINAL LINE: public override void MovementInform(MovementGeneratorType movementType, uint id)
    @Override
    public void movementInform(MovementGeneratorType movementType, int id) {
        if (movementType == MovementGeneratorType.Point && id == EventId.SmartEscortLastOCCPoint) {
            me.clearUnitState(UnitState.Evade);
        }


//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.Movementinform, null, (uint)movementType, id);
        getScript().processEventsFor(SmartEvents.Movementinform, null, (int)movementType.getValue(), id);

        if (!hasEscortState(SmartEscortState.Escorting)) {
            return;
        }

        if (movementType != MovementGeneratorType.Point && id == EventId.SmartEscortLastOCCPoint) {
            oOCReached = true;
        }
    }

    public final void startAttackOnOwnersInCombatWith() {
        Player owner;
        tangible.OutObject<Unit> tempOutOwner = new tangible.OutObject<Unit>();
        if (!me.tryGetOwner(tempOutOwner)) {
        owner = tempOutOwner.outArgValue;
            return;
        } else {
        owner = tempOutOwner.outArgValue;
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
        enterEvadeMode(EvadeReason.Other);
    }


//ORIGINAL LINE: public override void EnterEvadeMode(EvadeReason why = EvadeReason.Other)
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
            var target = !followGuid.isEmpty() ? Global.getObjAccessor().getUnit(me, followGuid.clone()) : null;

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
        escortState = SmartEscortState.None;

        followGuid.clear(); //do not reset follower on Reset(), we need it after combat evade
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

        var formation = me.formation;

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
        getScript().processEventsFor(SmartEvents.Kill, victim);
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
            me.getMotionMaster().clear(MovementGeneratorPriority.Normal);
            me.pauseMovement();

            if (canCombatMove) {
                setRun(run);
                me.getMotionMaster().moveChase(who);
            }
        }
    }

    @Override
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHit, caster.getAsUnit(), 0, 0, false, spellInfo, caster.getAsGameObject());
    }

    @Override
    public void spellHitTarget(WorldObject target, SpellInfo spellInfo) {
        getScript().processEventsFor(SmartEvents.SpellHitTarget, target.getAsUnit(), 0, 0, false, spellInfo, target.getAsGameObject());
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


//ORIGINAL LINE: public override void DamageTaken(Unit attacker, ref double damage, DamageEffectType damageType, SpellInfo spellInfo = null)
    @Override
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.Damaged, attacker, (uint)damage);
        getScript().processEventsFor(SmartEvents.Damaged, attacker, damage.refArgValue.intValue());

        if (!isAIControlled()) { // don't allow players to use unkillable units
            return;
        }

        if (invincibilityHpLevel != 0 && (damage.refArgValue >= me.getHealth() - invincibilityHpLevel)) {

//ORIGINAL LINE: damage = (uint)(Me.Health - _invincibilityHpLevel);
            damage.refArgValue = (int)(me.getHealth() - invincibilityHpLevel); // damage should not be nullified, because of player damage req.
        }
    }

    @Override
    public void healReceived(Unit by, double addhealth) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.ReceiveHeal, by, (uint)addhealth);
        getScript().processEventsFor(SmartEvents.ReceiveHeal, by, (int)addhealth);
    }

    @Override
    public void receiveEmote(Player player, TextEmotes emoteId) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.ReceiveEmote, player, (uint)emoteId);
        getScript().processEventsFor(SmartEvents.ReceiveEmote, player, (int)emoteId.getValue());
    }

    @Override
    public void isSummonedBy(WorldObject summoner) {
        getScript().processEventsFor(SmartEvents.JustSummoned, summoner.getAsUnit(), 0, 0, false, null, summoner.getAsGameObject());
    }

    @Override
    public void damageDealt(Unit victim, tangible.RefObject<Double> damage, DamageEffectType damageType) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.DamagedTarget, victim, (uint)damage);
        getScript().processEventsFor(SmartEvents.DamagedTarget, victim, damage.refArgValue.intValue());
    }

    @Override
    public void summonedCreatureDespawn(Creature summon) {
        getScript().processEventsFor(SmartEvents.SummonDespawned, summon, summon.getEntry());
    }

    @Override
    public void corpseRemoved(long respawnDelay) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.CorpseRemoved, null, (uint)respawnDelay);
        getScript().processEventsFor(SmartEvents.CorpseRemoved, null, (int)respawnDelay);
    }

    @Override
    public void onDespawn() {
        getScript().processEventsFor(SmartEvents.OnDespawn);
    }

    @Override
    public void passengerBoarded(Unit passenger, byte seatId, boolean apply) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(apply ? SmartEvents.PassengerBoarded : SmartEvents.PassengerRemoved, passenger, (uint)seatId, 0, apply);
        getScript().processEventsFor(apply ? SmartEvents.PassengerBoarded : SmartEvents.PassengerRemoved, passenger, (int)seatId, 0, apply);
    }

    @Override
    public void onCharmed(boolean isNew) {
        var charmed = me.isCharmed();

        if (charmed) { // do this before we change charmed state, as charmed state might prevent these things from processing
            if (hasEscortState(SmartEscortState.Escorting.getValue() | SmartEscortState.Paused.getValue() | SmartEscortState.Returning.getValue())) {
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

            if (!me.lastCharmerGuid.isEmpty()) {
                if (!me.hasReactState(ReactStates.Passive)) {
                    var lastCharmer = Global.getObjAccessor().getUnit(me, me.lastCharmerGuid.clone());

                    if (lastCharmer != null) {
                        me.engageWithTarget(lastCharmer);
                    }
                }

                me.lastCharmerGuid.clear();

                if (!me.isInCombat()) {
                    enterEvadeMode(EvadeReason.NoHostiles);
                }
            }
        }

        getScript().processEventsFor(SmartEvents.Charmed, null, 0, 0, charmed);

        if (!getScript().hasAnyEventWithFlag(SmartEventFlags.WhileCharmed)) { // we can change AI if there are no events with this flag
            super.onCharmed(isNew);
        }
    }

    @Override
    public void doAction(int param) {

//ORIGINAL LINE: GetScript().ProcessEventsFor(SmartEvents.ActionDone, null, (uint)param);
        getScript().processEventsFor(SmartEvents.ActionDone, null, (int)param);
    }


//ORIGINAL LINE: public override uint GetData(uint id)
    @Override
    public int getData(int id) {
        return 0;
    }


//ORIGINAL LINE: public override void SetData(uint id, uint value)
    @Override
    public void setData(int id, int value) {
        setData(id, value, null);
    }


//ORIGINAL LINE: public void SetData(uint id, uint value, Unit invoker)
    public final void setData(int id, int value, Unit invoker) {
        getScript().processEventsFor(SmartEvents.DataSet, invoker, id, value);
    }

    @Override
    public void setGUID(ObjectGuid guid, int id) {
    }

    @Override
    public ObjectGuid getGUID(int id) {
        return ObjectGuid.empty;
    }

    public final void setRun(boolean run) {
        me.setWalk(!run);
        this.run = run;

        for (var node : path.nodes) {
            node.moveType = run ? WaypointMoveType.Run : WaypointMoveType.Walk;
        }
    }


    public final void setDisableGravity() {
        setDisableGravity(true);
    }


//ORIGINAL LINE: public void SetDisableGravity(bool disable = true)
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


//ORIGINAL LINE: public override bool OnGossipSelect(Player player, uint menuId, uint gossipListId)
    @Override
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        gossipReturn = false;
        getScript().processEventsFor(SmartEvents.GossipSelect, player, menuId, gossipListId);

        return gossipReturn;
    }


//ORIGINAL LINE: public override bool OnGossipSelectCode(Player player, uint menuId, uint gossipListId, string code)
    @Override
    public boolean onGossipSelectCode(Player player, int menuId, int gossipListId, String code) {
        return false;
    }

    @Override
    public void onQuestAccept(Player player, Quest quest) {
        getScript().processEventsFor(SmartEvents.AcceptedQuest, player, quest.id);
    }


//ORIGINAL LINE: public override void OnQuestReward(Player player, Quest quest, LootItemType type, uint opt)
    @Override
    public void onQuestReward(Player player, Quest quest, LootItemType type, int opt) {
        getScript().processEventsFor(SmartEvents.RewardQuest, player, quest.id, opt);
    }


    public final void setCombatMove(boolean on) {
        setCombatMove(on, false);
    }


//ORIGINAL LINE: public void SetCombatMove(bool on, bool stopMoving = false)
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
                if (!me.hasReactState(ReactStates.Passive) && me.getVictim() && !me.getMotionMaster().hasMovementGenerator(movement -> {
                    return movement.GetMovementGeneratorType() == MovementGeneratorType.Chase && movement.Mode == MovementGeneratorMode.Default && movement.Priority == MovementGeneratorPriority.Normal;
                })) {
                    setRun(run);
                    me.getMotionMaster().moveChase(me.getVictim());
                }
            } else {
                var movement = me.getMotionMaster().getMovementGenerator(a -> a.GetMovementGeneratorType() == MovementGeneratorType.Chase && a.Mode == MovementGeneratorMode.Default && a.Priority == MovementGeneratorPriority.Normal);

                if (movement != null) {
                    me.getMotionMaster().remove(movement);

                    if (stopMoving) {
                        me.stopMoving();
                    }
                }
            }
        }
    }


//ORIGINAL LINE: public void SetFollow(Unit target, float dist, float angle, uint credit, uint end, uint creditType)
    public final void setFollow(Unit target, float dist, float angle, int credit, int end, int creditType) {
        if (target == null) {
            stopFollow(false);

            return;
        }

        followGuid = target.getGUID().clone();
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

        var player = Global.getObjAccessor().GetPlayer(me, followGuid.clone());

        if (player != null) {
            if (followCreditType == 0) {
                player.RewardPlayerAndGroupAtEvent(followCredit, me);
            } else {
                player.GroupEventHappens(followCredit, me);
            }
        }

        setDespawnTime(5000);
        startDespawn();
        getScript().processEventsFor(SmartEvents.FollowCompleted, player);
    }


    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker) {
        setTimedActionList(e, entry, invoker, 0);
    }


//ORIGINAL LINE: public void SetTimedActionList(SmartScriptHolder e, uint entry, Unit invoker, uint startFromEventId = 0)

    public final void setTimedActionList(SmartScriptHolder e, int entry, Unit invoker, int startFromEventId) {
        getScript().setTimedActionList(e, entry, invoker, startFromEventId);
    }


//ORIGINAL LINE: public override void OnGameEvent(bool start, ushort eventId)
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
        if (!hasEscortState(SmartEscortState.Escorting)) { //dont mess up escort movement after combat
            setRun(run);
        }

        getScript().onReset();
    }

    public final boolean hasEscortState(SmartEscortState escortState) {
        return (this.escortState.getValue() & escortState.getValue()) != 0;
    }

    public final void addEscortState(SmartEscortState escortState) {
        this.escortState = game.ai.SmartEscortState.forValue(this.escortState.getValue() | escortState.getValue());
    }

    public final void removeEscortState(SmartEscortState escortState) {
        this.escortState = game.ai.SmartEscortState.forValue(this.escortState.getValue() & ~escortState.getValue());
    }

    public final boolean canCombatMove() {
        return canCombatMove;
    }

    public final SmartScript getScript() {
        return script;
    }


//ORIGINAL LINE: public void SetInvincibilityHpLevel(uint level)
    public final void setInvincibilityHpLevel(int level) {
        invincibilityHpLevel = level;
    }


    public final void setDespawnTime(int t) {
        setDespawnTime(t, 0);
    }


//ORIGINAL LINE: public void SetDespawnTime(uint t, uint r = 0)

    public final void setDespawnTime(int t, int r) {
        despawnTime = t;
        despawnState = t != 0 ? 1 : 0;
    }

    public final void startDespawn() {
        despawnState = 2;
    }


//ORIGINAL LINE: public void SetWPPauseTimer(uint time)
    public final void setWPPauseTimer(int time) {
        waypointPauseTimer = time;
    }

    public final void setGossipReturn(boolean val) {
        gossipReturn = val;
    }

    private boolean isAIControlled() {
        return !isCharmed;
    }


//ORIGINAL LINE: bool LoadPath(uint entry)
    private boolean loadPath(int entry) {
        if (hasEscortState(SmartEscortState.Escorting)) {
            return false;
        }

        var path = Global.getSmartAIMgr().getPath(entry);

        if (path == null || path.nodes.Empty()) {
            getScript().setPathId(0);

            return false;
        }

        this.path.id = path.id;
        this.path.nodes.addAll(path.nodes);

        for (var waypoint : this.path.nodes) {
            waypoint.x = GridDefines.normalizeMapCoord(waypoint.x);
            waypoint.y = GridDefines.normalizeMapCoord(waypoint.y);
            waypoint.moveType = run ? WaypointMoveType.Run : WaypointMoveType.Walk;
        }

        getScript().setPathId(entry);

        return true;
    }

    private void returnToLastOOCPos() {
        if (!isAIControlled()) {
            return;
        }

        me.setWalk(false);
        me.getMotionMaster().movePoint(EventId.SmartEscortLastOCCPoint, me.getHomePosition());
    }

    private boolean isEscortInvokerInRange() {
        var targets = getScript().getStoredTargetList(SharedConst.SmartEscortTargets, me);

        if (targets != null) {
            float checkDist = me.getInstanceScript() != null ? SMART_ESCORT_MAX_PLAYER_DIST * 2 : SMART_ESCORT_MAX_PLAYER_DIST;

            if (targets.size() == 1 && getScript().isPlayer(targets.get(0))) {
                var player = targets.get(0).AsPlayer;

                if (me.GetDistance(player) <= checkDist) {
                    return true;
                }

                var group = player.Group;

                if (group) {
                    for (var groupRef = group.FirstMember; groupRef != null; groupRef = groupRef.Next()) {
                        var groupGuy = groupRef.Source;

                        if (groupGuy.IsInMap(player) && me.GetDistance(groupGuy) <= checkDist) {
                            return true;
                        }
                    }
                }
            } else {
                for (var obj : targets) {
                    if (getScript().isPlayer(obj)) {
                        if (me.GetDistance(obj.getAsPlayer()) <= checkDist) {
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
        if (!me.getTemplate().typeFlags.HasAnyFlag(CreatureTypeFlags.CanAssist)) {
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
        if (who.isCreature() && who.getAsCreature().isInEvadeMode()) {
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


//ORIGINAL LINE: void CheckConditions(uint diff)
    private void checkConditions(int diff) {
        if (!hasConditions) {
            return;
        }

        if (conditionsTimer <= diff) {
            var vehicleKit = me.getVehicleKit();

            if (vehicleKit != null) {
                for (var pair : vehicleKit.seats.entrySet()) {
                    var passenger = Global.getObjAccessor().getUnit(me, pair.getValue().Passenger.Guid);

                    if (passenger != null) {
                        var player = passenger.getAsPlayer();

                        if (player != null) {
                            if (!Global.getConditionMgr().IsObjectMeetingNotGroupedConditions(ConditionSourceType.CreatureTemplateVehicle, me.getEntry(), player, me)) {
                                player.exitVehicle();

                                return; // check other pessanger in next tick
                            }
                        }
                    }
                }
            }

            conditionsTimer = 1000;
        } else {
            conditionsTimer -= diff;
        }
    }


//ORIGINAL LINE: void UpdatePath(uint diff)
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
            escortInvokerCheckTimer -= diff;
        }

        // handle pause
        if (hasEscortState(SmartEscortState.Paused) && (waypointReached || waypointPauseForced)) {
            // Resume only if there was a pause timer set
            if (waypointPauseTimer != 0 && !me.isInCombat() && !hasEscortState(SmartEscortState.Returning)) {
                if (waypointPauseTimer <= diff) {
                    resumePath();
                } else {
                    waypointPauseTimer -= diff;
                }
            }
        } else if (waypointPathEnded) { // end path
            waypointPathEnded = false;
            stopPath();

            return;
        }

        if (hasEscortState(SmartEscortState.Returning)) {
            if (oOCReached) { //reached OOC WP
                oOCReached = false;
                removeEscortState(SmartEscortState.Returning);

                if (!hasEscortState(SmartEscortState.Paused)) {
                    resumePath();
                }
            }
        }
    }


//ORIGINAL LINE: void UpdateFollow(uint diff)
    private void updateFollow(int diff) {
        if (followGuid.isEmpty()) {
            if (followArrivedTimer < diff) {
                if (me.findNearestCreature(followArrivedEntry, SharedConst.InteractionDistance, true)) {
                    stopFollow(true);

                    return;
                }

                followArrivedTimer = 1000;
            } else {
                followArrivedTimer -= diff;
            }
        }
    }


//ORIGINAL LINE: void UpdateDespawn(uint diff)
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
            despawnTime -= diff;
        }
    }
}