package com.github.azeroth.game.ai;


import com.github.azeroth.game.combat.CombatManager;
import com.github.azeroth.game.domain.creature.TempSummonType;
import com.github.azeroth.game.entity.areatrigger.AreaTrigger;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.gobject.GameObject;
import com.github.azeroth.game.domain.object.Position;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.map.AreaBoundary;
import com.github.azeroth.game.spell.SpellInfo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;

public class CreatureAI extends UnitAI {

    protected final Creature me;

    protected EventMap events = new eventMap();
    protected taskScheduler schedulerProtected = new taskScheduler();
    protected Instancescript script;
    private boolean isEngaged;
    private boolean moveInLosLocked;
    private ArrayList<AreaBoundary> boundary = new ArrayList<>();
    private boolean negateBoundary;

    public CreatureAI(Creature creature) {
        super(creature);
        me = creature;
        moveInLosLocked = false;
    }

    // adapted from logic in Spell:EffectSummonType
    public static boolean shouldFollowOnSpawn(SummonProperties properties) {
        // Summons without SummonProperties are generally scripted summons that don't belong to any owner
        if (properties == null) {
            return false;
        }

        switch (properties.Control) {
            case Pet:
                return true;
            case Wild:
            case Ally:
            case Unk:
                if (properties.getFlags().hasFlag(SummonPropertiesFlags.JoinSummonerSpawnGroup)) {
                    return true;
                }

                switch (properties.title) {
                    case Pet:
                    case Guardian:
                    case Runeblade:
                    case Minion:
                    case Companion:
                        return true;
                    default:
                        return false;
                }
            default:
                return false;
        }
    }

    public static boolean isInBounds(ArrayList<AreaBoundary> boundary, Position pos) {
        for (var areaBoundary : boundary) {
            if (!areaBoundary.isWithinBoundary(pos)) {
                return false;
            }
        }

        return true;
    }

    public final TaskScheduler getScheduler() {
        return schedulerProtected;
    }

    public final ArrayList<AreaBoundary> getBoundary() {
        return boundary;
    }

    public final void setBoundary(ArrayList<AreaBoundary> boundary) {
        setBoundary(boundary, false);
    }

    public final boolean isEngaged() {
        return isEngaged;
    }

    public final void talk(int id) {
        talk(id, null);
    }

    public final void talk(int id, WorldObject whisperTarget) {
        global.getCreatureTextMgr().sendChat(me, (byte) id, whisperTarget);
    }

    @Override
    public void onCharmed(boolean isNew) {
        if (isNew && !me.isCharmed() && !me.getLastCharmerGuid().isEmpty()) {
            if (!me.hasReactState(ReactStates.Passive)) {
                var lastCharmer = global.getObjAccessor().GetUnit(me, me.getLastCharmerGuid());

                if (lastCharmer != null) {
                    me.engageWithTarget(lastCharmer);
                }
            }

            me.getLastCharmerGuid().clear();
        }

        super.onCharmed(isNew);
    }

    public final void doZoneInCombat() {
        doZoneInCombat(null);
    }

    public final void doZoneInCombat(Creature creature) {
        if (creature == null) {
            creature = me;
        }

        var map = creature.getMap();

        if (!map.isDungeon()) // use IsDungeon instead of Instanceable, in case Battlegrounds will be instantiated
        {
            Log.outError(LogFilter.Server, "DoZoneInCombat call for map that isn't an instance (creature entry = {0})", creature.isTypeId(TypeId.UNIT) ? creature.toCreature().getEntry() : 0);

            return;
        }

        if (!map.havePlayers()) {
            return;
        }

        for (var player : map.getPlayers()) {
            if (player != null) {
                if (!player.isAlive() || !CombatManager.canBeginCombat(creature, player)) {
                    continue;
                }

                creature.engageWithTarget(player);

                for (var pet : player.getControlled()) {
                    creature.engageWithTarget(pet);
                }

                var vehicle = player.getVehicleBase();

                if (vehicle != null) {
                    creature.engageWithTarget(vehicle);
                }
            }
        }
    }

    public void moveInLineOfSight_Safe(Unit who) {
        if (moveInLosLocked) {
            return;
        }

        moveInLosLocked = true;
        moveInLineOfSight(who);
        moveInLosLocked = false;
    }

    public void moveInLineOfSight(Unit who) {
        if (me.isEngaged()) {
            return;
        }

        if (me.hasReactState(ReactStates.Aggressive) && me.canStartAttack(who, false)) {
            me.engageWithTarget(who);
        }
    }

    // Distract creature, if player gets too close while stealthed/prowling
    public final void triggerAlert(Unit who) {
        // If there's no target, or target isn't a player do nothing
        if (!who || !who.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // If this unit isn't an NPC, is already distracted, is fighting, is confused, stunned or fleeing, do nothing
        if (!me.isTypeId(TypeId.UNIT) || me.isEngaged() || me.hasUnitState(UnitState.Confused.getValue() | UnitState.Stunned.getValue().getValue() | UnitState.Fleeing.getValue().getValue().getValue() | UnitState.Distracted.getValue().getValue().getValue())) {
            return;
        }

        // Only alert for hostiles!
        if (me.isCivilian() || me.hasReactState(ReactStates.Passive) || !me.isHostileTo(who) || !me._IsTargetAcceptable(who)) {
            return;
        }

        // Send alert sound (if any) for this creature
        me.sendAIReaction(AiReaction.Alert);

        // Face the unit (stealthed player) and set distracted state for 5 seconds
        me.getMotionMaster().moveDistract(5 * time.InMilliseconds, me.getLocation().getAbsoluteAngle(who.getLocation()));
    }

    // Called for reaction at stopping attack at no attackers or targets

    // Called when creature appears in the world (spawn, respawn, grid load etc...)
    public void justAppeared() {
        if (!isEngaged()) {
            var summon = me.toTempSummon();

            if (summon != null) {
                // Only apply this to specific types of summons
                if (!summon.getVehicle1() && shouldFollowOnSpawn(summon.summonProperty) && summon.canFollowOwner()) {
                    var owner = summon.getCharmerOrOwner();

                    if (owner != null) {
                        summon.getMotionMaster().clear();
                        summon.getMotionMaster().moveFollow(owner, SharedConst.PetFollowDist, summon.getFollowAngle());
                    }
                }
            }
        }
    }

    @Override
    public void justEnteredCombat(Unit who) {
        if (!isEngaged() && !me.getCanHaveThreatList()) {
            engagementStart(who);
        }
    }

    public void enterEvadeMode() {
        enterEvadeMode(EvadeReason.other);
    }

    public void enterEvadeMode(EvadeReason why) {
        if (!_EnterEvadeMode(why)) {
            return;
        }

        Log.outDebug(LogFilter.unit, String.format("CreatureAI::EnterEvadeMode: entering evade mode (why: %1$s) (%2$s)", why, me.getGUID()));

        if (me.getVehicle1() == null) // otherwise me will be in evade mode forever
        {
            var owner = me.getCharmerOrOwner();

            if (owner != null) {
                me.getMotionMaster().clear();
                me.getMotionMaster().moveFollow(owner, SharedConst.PetFollowDist, me.getFollowAngle());
            } else {
                // Required to prevent attacking creatures that are evading and cause them to reenter combat
                // Does not apply to MoveFollow
                me.addUnitState(UnitState.Evade);
                me.getMotionMaster().moveTargetedHome();
            }
        }

        reset();
    }

    public final boolean updateVictim() {
        if (!isEngaged()) {
            return false;
        }

        if (!me.isAlive()) {
            engagementOver();

            return false;
        }

        if (!me.hasReactState(ReactStates.Passive)) {
            var victim = me.selectVictim();

            if (victim != null && victim != me.getVictim()) {
                attackStart(victim);
            }

            return me.getVictim() != null;
        } else if (!me.isInCombat()) {
            enterEvadeMode(EvadeReason.NoHostiles);

            return false;
        } else if (me.getVictim() != null) {
            me.attackStop();
        }

        return true;
    }

    public final void engagementStart(Unit who) {
        if (isEngaged) {
            Log.outError(LogFilter.ScriptsAi, String.format("CreatureAI::EngagementStart called even though creature is already engaged. Creature debug info:\n%1$s", me.getDebugInfo()));

            return;
        }

        isEngaged = true;

        me.atEngage(who);
    }

    public final void engagementOver() {
        if (!isEngaged) {
            Log.outDebug(LogFilter.ScriptsAi, String.format("CreatureAI::EngagementOver called even though creature is not currently engaged. Creature debug info:\n%1$s", me.getDebugInfo()));

            return;
        }

        isEngaged = false;

        me.atDisengage();
    }

    public final boolean _EnterEvadeMode() {
        return _EnterEvadeMode(EvadeReason.other);
    }

    public final boolean _EnterEvadeMode(EvadeReason why) {
        if (me.isInEvadeMode()) {
            return false;
        }

        if (!me.isAlive()) {
            engagementOver();

            return false;
        }

        me.removeAurasOnEvade();

        // sometimes bosses stuck in combat?
        me.combatStop(true);
        me.setTappedBy(null);
        me.resetPlayerDamageReq();
        me.setLastDamagedTime(0);
        me.setCannotReachTarget(false);
        me.doNotReacquireSpellFocusTarget();
        me.setTarget(ObjectGuid.Empty);
        me.getSpellHistory().resetAllCooldowns();
        engagementOver();

        return true;
    }

    public final SysMessage visualizeBoundary(Duration duration, Unit owner) {
        return visualizeBoundary(duration, owner, false);
    }

    public final SysMessage visualizeBoundary(Duration duration) {
        return visualizeBoundary(duration, null, false);
    }

    public final SysMessage visualizeBoundary(Duration duration, Unit owner, boolean fill) {
        if (owner == null) {
            return 0;
        }

        if (boundary.isEmpty()) {
            return SysMessage.CreatureMovementNotBounded;
        }

        ArrayList<java.util.Map.entry<Integer, Integer>> q = new ArrayList<java.util.Map.entry<Integer, Integer>>();
        ArrayList<java.util.Map.entry<Integer, Integer>> alreadyChecked = new ArrayList<java.util.Map.entry<Integer, Integer>>();
        ArrayList<java.util.Map.entry<Integer, Integer>> outOfBounds = new ArrayList<java.util.Map.entry<Integer, Integer>>();

        Position startPosition = owner.getLocation();

        if (!isInBoundary(startPosition)) // fall back to creature position
        {
            startPosition = me.getLocation();

            if (!isInBoundary(startPosition)) {
                startPosition = me.getHomePosition();

                if (!isInBoundary(startPosition)) // fall back to creature home position
                {
                    return SysMessage.CreatureNoInteriorPointFound;
                }
            }
        }

        var spawnZ = startPosition.getZ() + SharedConst.BoundaryVisualizeSpawnHeight;

        var boundsWarning = false;
        q.add(new KeyValuePair<Integer, Integer>(0, 0));

        while (!q.isEmpty()) {
            var front = q.get(0);
            var hasOutOfBoundsNeighbor = false;

            for (var off : new ArrayList<java.util.Map.entry<Integer, Integer>>(Arrays.asList(new (1, 0),new (0, 1),new
            (-1, 0),new (0, -1))))
            {
                var next = new KeyValuePair<Integer, Integer>(front.key + off.key, front.value + off.value);

                if (next.getKey() > SharedConst.BoundaryVisualizeFailsafeLimit || next.getKey() < -SharedConst.BoundaryVisualizeFailsafeLimit || next.getValue() > SharedConst.BoundaryVisualizeFailsafeLimit || next.getValue() < -SharedConst.BoundaryVisualizeFailsafeLimit) {
                    boundsWarning = true;

                    continue;
                }

                if (!alreadyChecked.contains(next)) // never check a coordinate twice
                {
                    Position nextPos = new Position(startPosition.getX() + next.getKey() * SharedConst.BoundaryVisualizeStepSize, startPosition.getY() + next.getValue() * SharedConst.BoundaryVisualizeStepSize, startPosition.getZ());

                    if (isInBoundary(nextPos)) {
                        q.add(next);
                    } else {
                        outOfBounds.add(next);
                        hasOutOfBoundsNeighbor = true;
                    }

                    alreadyChecked.add(next);
                } else if (outOfBounds.contains(next)) {
                    hasOutOfBoundsNeighbor = true;
                }
            }

            if (fill || hasOutOfBoundsNeighbor) {
                var pos = new Position(startPosition.getX() + front.Key * SharedConst.BoundaryVisualizeStepSize, startPosition.getY() + front.Value * SharedConst.BoundaryVisualizeStepSize, spawnZ);
                var point = owner.summonCreature(SharedConst.BoundaryVisualizeCreature, pos, TempSummonType.TimedDespawn, duration);

                if (point) {
                    point.ObjectScale = SharedConst.BoundaryVisualizeCreatureScale;
                    point.setUnitFlag(UnitFlag.Stunned);
                    point.setImmuneToAll(true);

                    if (!hasOutOfBoundsNeighbor) {
                        point.setUnitFlag(UnitFlag.Uninteractible);
                    }
                }

                q.remove(front);
            }
        }

        return boundsWarning ? SysMessage.CreatureMovementMaybeUnbounded : 0;
    }

    public final boolean isInBoundary() {
        return isInBoundary(null);
    }

    public final boolean isInBoundary(Position who) {
        if (boundary == null) {
            return true;
        }

        if (who == null) {
            who = me.getLocation();
        }

        return isInBounds(boundary, who) != negateBoundary;
    }

    public boolean checkInRoom() {
        if (isInBoundary()) {
            return true;
        } else {
            enterEvadeMode(EvadeReason.boundary);

            return false;
        }
    }

    public final Creature doSummon(int entry, Position pos, Duration despawnTime) {
        return doSummon(entry, pos, despawnTime, TempSummonType.CORPSE_TIMED_DESPAWN);
    }

    public final Creature doSummon(int entry, Position pos, Duration despawnTime, TempSummonType summonType) {
        return me.summonCreature(entry, pos, summonType, despawnTime);
    }

    public final Creature doSummon(int entry, WorldObject obj, float radius, Duration despawnTime) {
        return doSummon(entry, obj, radius, despawnTime, TempSummonType.CORPSE_TIMED_DESPAWN);
    }

    public final Creature doSummon(int entry, WorldObject obj, float radius) {
        return doSummon(entry, obj, radius, null, TempSummonType.CorpseTimedDespawn);
    }

    public final Creature doSummon(int entry, WorldObject obj) {
        return doSummon(entry, obj, 5.0f, null, TempSummonType.CorpseTimedDespawn);
    }

    public final Creature doSummon(int entry, WorldObject obj, float radius, Duration despawnTime, TempSummonType summonType) {
        var pos = obj.getRandomNearPosition(radius);

        return me.summonCreature(entry, pos, summonType, despawnTime);
    }

    public final Creature doSummonFlyer(int entry, WorldObject obj, float flightZ, float radius, Duration despawnTime) {
        return doSummonFlyer(entry, obj, flightZ, radius, despawnTime, TempSummonType.CorpseTimedDespawn);
    }

    public final Creature doSummonFlyer(int entry, WorldObject obj, float flightZ, float radius) {
        return doSummonFlyer(entry, obj, flightZ, radius, null, TempSummonType.CorpseTimedDespawn);
    }

    public final Creature doSummonFlyer(int entry, WorldObject obj, float flightZ) {
        return doSummonFlyer(entry, obj, flightZ, 5.0f, null, TempSummonType.CorpseTimedDespawn);
    }

    public final Creature doSummonFlyer(int entry, WorldObject obj, float flightZ, float radius, Duration despawnTime, TempSummonType summonType) {
        var pos = obj.getRandomNearPosition(radius);
        pos.setZ(pos.getZ() + flightZ);

        return me.summonCreature(entry, pos, summonType, despawnTime);
    }

    public final void setBoundary(ArrayList<AreaBoundary> boundary, boolean negateBoundaries) {
        boundary = boundary;
        negateBoundary = negateBoundaries;
        me.doImmediateBoundaryCheck();
    }

    // Called for reaction whenever a new non-offline unit is added to the threat list
    public void justStartedThreateningMe(Unit who) {
        if (!isEngaged()) {
            engagementStart(who);
        }
    }

    // Called for reaction when initially engaged - this will always happen _after_ JustEnteredCombat
    public void justEngagedWith(Unit who) {
    }

    // Called when the creature is killed
    public void justDied(Unit killer) {
    }

    // Called when the creature kills a unit
    public void killedUnit(Unit victim) {
    }

    // Called when the creature summon successfully other creature
    public void justSummoned(Creature summon) {
    }

    public void isSummonedBy(WorldObject summoner) {
    }

    public void summonedCreatureDespawn(Creature summon) {
    }

    public void summonedCreatureDies(Creature summon, Unit killer) {
    }

    // Called when the creature successfully summons a gameobject
    public void justSummonedGameobject(GameObject gameobject) {
    }

    public void summonedGameobjectDespawn(GameObject gameobject) {
    }

    // Called when the creature successfully registers a dynamicobject
    public void justRegisteredDynObject(DynamicObject dynObject) {
    }

    public void justUnregisteredDynObject(DynamicObject dynObject) {
    }

    // Called when the creature successfully registers an areatrigger
    public void justRegisteredAreaTrigger(AreaTrigger areaTrigger) {
    }

    public void justUnregisteredAreaTrigger(AreaTrigger areaTrigger) {
    }

    // Called when hit by a spell
    public void spellHit(WorldObject caster, SpellInfo spellInfo) {
    }

    // Called when spell hits a target
    public void spellHitTarget(WorldObject target, SpellInfo spellInfo) {
    }

    // Called when a spell finishes
    public void onSpellCast(SpellInfo spell) {
    }

    // Called when a spell fails
    public void onSpellFailed(SpellInfo spell) {
    }

    // Called when a spell starts
    public void onSpellStart(SpellInfo spell) {
    }

    // Called when a channeled spell finishes
    public void onChannelFinished(SpellInfo spell) {
    }

    // Should return true if the NPC is currently being escorted
    public boolean isEscorted() {
        return false;
    }

    // Called at waypoint reached or point movement finished
    public void movementInform(MovementGeneratorType type, int id) {
    }

    // Called at reaching home after evade
    public void justReachedHome() {
    }

    // Called at text emote receive from player
    public void receiveEmote(Player player, TextEmotes emoteId) {
    }

    // Called when owner takes damage
    public void ownerAttackedBy(Unit attacker) {
        onOwnerCombatInteraction(attacker);
    }

    // Called when owner attacks something
    public void ownerAttacked(Unit target) {
        onOwnerCombatInteraction(target);
    }

    // called when the corpse of this creature gets removed
    public void corpseRemoved(long respawnDelay) {
    }

    /**
     * == Gossip system ================================
     */

    // Called when the dialog status between a player and the creature is requested.
    public QuestGiverStatus getDialogStatus(Player player) {
        return null;
    }

    // Called when a player opens a gossip dialog with the creature.
    public boolean onGossipHello(Player player) {
        return false;
    }

    // Called when a player selects a gossip item in the creature's gossip menu.
    public boolean onGossipSelect(Player player, int menuId, int gossipListId) {
        return false;
    }

    // Called when a player selects a gossip with a code in the creature's gossip menu.
    public boolean onGossipSelectCode(Player player, int menuId, int gossipListId, String code) {
        return false;
    }

    // Called when a player accepts a quest from the creature.
    public void onQuestAccept(Player player, Quest quest) {
    }

    // Called when a player completes a quest and is rewarded, opt is the selected item's index or 0
    public void onQuestReward(Player player, Quest quest, LootItemType type, int opt) {
    }

    /**
     * == Waypoints system =============================
     */
    public void waypointStarted(int nodeId, int pathId) {
    }


    public void waypointReached(int nodeId, int pathId) {
    }


    public void waypointPathEnded(int nodeId, int pathId) {
    }

    public void passengerBoarded(Unit passenger, byte seatId, boolean apply) {
    }

    public void onSpellClick(Unit clicker, tangible.RefObject<Boolean> spellClickHandled) {
    }

    public boolean canSeeAlways(WorldObject obj) {
        return false;
    }

    // Called when a player is charmed by the creature
    // If a PlayerAI* is returned, that AI is placed on the player instead of the default charm AI
    // Object destruction is handled by Unit::RemoveCharmedBy
    public PlayerAI getAIForCharmedPlayer(Player who) {
        return null;
    }

    private void onOwnerCombatInteraction(Unit target) {
        if (target == null || !me.isAlive()) {
            return;
        }

        if (!me.hasReactState(ReactStates.Passive) && me.canStartAttack(target, true)) {
            me.engageWithTarget(target);
        }
    }
}
