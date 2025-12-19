package com.github.azeroth.game.ai;



import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.creature.Creature;

import java.util.*;




public class PetAI extends CreatureAI {
    private final ArrayList<ObjectGuid> allySet = new ArrayList<ObjectGuid>();

//ORIGINAL LINE: uint _updateAlliesTimer;
    private int updateAlliesTimer;

    public PetAI(Creature creature) {
        super(creature);
        updateAllies();
    }


//ORIGINAL LINE: public override void UpdateAI(uint diff)
    @Override
    public void updateAI(int diff) {
        if (!me.isAlive() || me.getCharmInfo() == null) {
            return;
        }

        var owner = me.getCharmerOrOwner();

        if (updateAlliesTimer <= diff) {
            // UpdateAllies self set update timer
            updateAllies();
        } else {
            updateAlliesTimer -= diff;
        }

        if (me.getVictim() && me.getVictim().isAlive()) {
            // is only necessary to stop casting, the pet must not exit combat
            if (!me.getCurrentSpell(CurrentSpellTypes.Channeled) && (me.getVictim() && me.getVictim().hasBreakableByDamageCrowdControlAura(me))) {
                me.interruptNonMeleeSpells(false);

                return;
            }

            if (needToStop()) {
                Log.outTrace(LogFilter.ScriptsAi, String.format("PetAI::UpdateAI: AI stopped attacking %1$s", me.getGUID().clone()));
                stopAttack();

                return;
            }

            // Check before attacking to prevent pets from leaving stay position
            if (me.getCharmInfo().hasCommandState(CommandStates.Stay)) {
                if (me.getCharmInfo().isCommandAttack() || (me.getCharmInfo().isAtStay() && me.isWithinMeleeRange(me.getVictim()))) {
                    doMeleeAttackIfReady();
                }
            } else {
                doMeleeAttackIfReady();
            }
        } else {
            if (me.hasReactState(ReactStates.Aggressive) || me.getCharmInfo().isAtStay()) {
                // Every update we need to check targets only in certain cases
                // Aggressive - Allow auto select if owner or pet don't have a target
                // Stay - Only pick from pet or owner targets / attackers so targets won't run by
                //   while chasing our owner. Don't do auto select.
                // All other cases (ie: defensive) - Targets are assigned by DamageTaken(), OwnerAttackedBy(), OwnerAttacked(), etc.
                var nextTarget = selectNextTarget(me.hasReactState(ReactStates.Aggressive));

                if (nextTarget) {
                    attackStart(nextTarget);
                } else {
                    handleReturnMovement();
                }
            } else {
                handleReturnMovement();
            }
        }

        // Autocast (casted only in combat or persistent spells in any state)
        if (!me.hasUnitState(UnitState.Casting)) {
            ArrayList<Tuple<Unit, Spell>> targetSpellStore = new ArrayList<Tuple<Unit, Spell>>();


//ORIGINAL LINE: for (byte i = 0; i < Me.PetAutoSpellSize; ++i)
            for (byte i = 0; i < me.getPetAutoSpellSize(); ++i) {
                var spellID = me.getPetAutoSpellOnPos(i);

                if (spellID == 0) {
                    continue;
                }

                var spellInfo = Global.getSpellMgr().getSpellInfo(spellID, me.getMap().getDifficultyID());

                if (spellInfo == null) {
                    continue;
                }

                if (me.getSpellHistory().hasGlobalCooldown(spellInfo)) {
                    continue;
                }

                // check spell cooldown
                if (!me.getSpellHistory().isReady(spellInfo)) {
                    continue;
                }

                if (spellInfo.isPositive()) {
                    if (spellInfo.getCanBeUsedInCombat()) {
                        // Check if we're in combat or commanded to attack
                        if (!me.isInCombat() && !me.getCharmInfo().isCommandAttack()) {
                            continue;
                        }
                    }

                    Spell spell = new Spell(me, spellInfo, TriggerCastFlags.None);
                    var spellUsed = false;

                    // Some spells can target enemy or friendly (DK Ghoul's Leap)
                    // Check for enemy first (pet then owner)
                    var target = me.getAttackerForHelper();

                    if (!target && owner) {
                        target = owner.getAttackerForHelper();
                    }

                    if (target) {
                        if (canAttack(target) && spell.canAutoCast(target)) {
                            targetSpellStore.add(Tuple.Create(target, spell));
                            spellUsed = true;
                        }
                    }

                    if (spellInfo.hasEffect(SpellEffectName.JumpDest)) {
                        if (!spellUsed) {
                            spell.close();
                        }

                        continue; // Pets must only jump to target
                    }

                    // No enemy, check friendly
                    if (!spellUsed) {
                        for (var tar : allySet) {
                            var ally = Global.getObjAccessor().getUnit(me, tar.clone());

                            //only buff targets that are in combat, unless the spell can only be cast while out of combat
                            if (!ally) {
                                continue;
                            }

                            if (spell.canAutoCast(ally)) {
                                targetSpellStore.add(Tuple.Create(ally, spell));
                                spellUsed = true;

                                break;
                            }
                        }
                    }

                    // No valid targets at all
                    if (!spellUsed) {
                        spell.close();
                    }
                } else if (me.getVictim() && canAttack(me.getVictim()) && spellInfo.getCanBeUsedInCombat()) {
                    Spell spell = new Spell(me, spellInfo, TriggerCastFlags.None);

                    if (spell.canAutoCast(me.getVictim())) {
                        targetSpellStore.add(Tuple.Create(me.getVictim(), spell));
                    } else {
                        spell.close();
                    }
                }
            }

            //found units to cast on to
            if (!targetSpellStore.Empty()) {
                var index = RandomHelper.IRand(0, targetSpellStore.size() - 1);
                var tss = targetSpellStore.get(index);

//C# TO JAVA CONVERTER TODO TASK: Java has no equivalent to C# deconstruction declarations:
                var(target, spell) = tss;

                targetSpellStore.remove(index);

                SpellCastTargets targets = new SpellCastTargets();
                targets.setUnitTarget(target);

                spell.Prepare(targets);
            }

            // deleted cached Spell objects
            for (var pair : targetSpellStore) {
                pair.getItem2().Dispose();
            }
        }

        // Update speed as needed to prevent dropping too far behind and despawning
        me.updateSpeed(UnitMoveType.Run);
        me.updateSpeed(UnitMoveType.Walk);
        me.updateSpeed(UnitMoveType.Flight);
    }

    @Override
    public void killedUnit(Unit victim) {
        // Called from Unit.Kill() in case where pet or owner kills something
        // if owner killed this victim, pet may still be attacking something else
        if (me.getVictim() && me.getVictim() != victim) {
            return;
        }

        // Clear target just in case. May help problem where health / focus / mana
        // regen gets stuck. Also resets attack command.
        // Can't use StopAttack() because that activates movement handlers and ignores
        // next target selection
        me.attackStop();
        me.interruptNonMeleeSpells(false);

        // Before returning to owner, see if there are more things to attack
        var nextTarget = selectNextTarget(false);

        if (nextTarget) {
            attackStart(nextTarget);
        } else {
            handleReturnMovement(); // Return
        }
    }

    @Override
    public void attackStart(Unit target) {
        // Overrides Unit.AttackStart to prevent pet from switching off its assigned target
        if (target == null || target == me) {
            return;
        }

        if (me.getVictim() != null && me.getVictim().isAlive()) {
            return;
        }

        _AttackStart(target);
    }

    public final void _AttackStart(Unit target) {
        // Check all pet states to decide if we can attack this target
        if (!canAttack(target)) {
            return;
        }

        // Only chase if not commanded to stay or if stay but commanded to attack
        doAttack(target, (!me.getCharmInfo().hasCommandState(CommandStates.Stay) || me.getCharmInfo().isCommandAttack()));
    }

    @Override
    public void ownerAttackedBy(Unit attacker) {
        // Called when owner takes damage. This function helps keep pets from running off
        //  simply due to owner gaining aggro.

        if (attacker == null || !me.isAlive()) {
            return;
        }

        // Passive pets don't do anything
        if (me.hasReactState(ReactStates.Passive)) {
            return;
        }

        // Prevent pet from disengaging from current target
        if (me.getVictim() && me.getVictim().isAlive()) {
            return;
        }

        // Continue to evaluate and attack if necessary
        attackStart(attacker);
    }

    @Override
    public void ownerAttacked(Unit target) {
        // Called when owner attacks something. Allows defensive pets to know
        //  that they need to assist

        // Target might be null if called from spell with invalid cast targets
        if (target == null || !me.isAlive()) {
            return;
        }

        // Passive pets don't do anything
        if (me.hasReactState(ReactStates.Passive)) {
            return;
        }

        // Prevent pet from disengaging from current target
        if (me.getVictim() && me.getVictim().isAlive()) {
            return;
        }

        // Continue to evaluate and attack if necessary
        attackStart(target);
    }


//ORIGINAL LINE: public override void MovementInform(MovementGeneratorType type, uint id)
    @Override
    public void movementInform(MovementGeneratorType type, int id) {
        // Receives notification when pet reaches stay or follow owner
        switch (type) {
            case Point: {
                // Pet is returning to where stay was clicked. data should be
                // pet's GUIDLow since we set that as the waypoint ID
                if (id == me.getGUID().getCounter() && me.getCharmInfo().isReturning()) {
                    clearCharmInfoFlags();
                    me.getCharmInfo().setIsAtStay(true);
                    me.getMotionMaster().moveIdle();
                }

                break;
            }
            case Follow: {
                // If data is owner's GUIDLow then we've reached follow point,
                // otherwise we're probably chasing a creature
                if (me.getCharmerOrOwner() && me.getCharmInfo() != null && id == me.getCharmerOrOwner().getGUID().getCounter() && me.getCharmInfo().isReturning()) {
                    clearCharmInfoFlags();
                    me.getCharmInfo().setIsFollowing(true);
                }

                break;
            }
            default:
                break;
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

    public final boolean canAttack(Unit victim) {
        // Evaluates wether a pet can attack a specific target based on CommandState, ReactState and other flags
        // IMPORTANT: The order in which things are checked is important, be careful if you add or remove checks

        // Hmmm...
        if (!victim) {
            return false;
        }

        if (!victim.isAlive()) {
            // if target is invalid, pet should evade automaticly
            // Clear target to prevent getting stuck on dead targets
            //me.AttackStop();
            //me.InterruptNonMeleeSpells(false);
            return false;
        }

        if (me.getCharmInfo() == null) {
            Log.outWarn(LogFilter.ScriptsAi, String.format("me.GetCharmInfo() is NULL in PetAI::CanAttack(). Debug info: %1$s", getDebugInfo()));

            return false;
        }

        // Passive - passive pets can attack if told to
        if (me.hasReactState(ReactStates.Passive)) {
            return me.getCharmInfo().isCommandAttack();
        }

        // CC - mobs under crowd control can be attacked if owner commanded
        if (victim.hasBreakableByDamageCrowdControlAura()) {
            return me.getCharmInfo().isCommandAttack();
        }

        // Returning - pets ignore attacks only if owner clicked follow
        if (me.getCharmInfo().isReturning()) {
            return !me.getCharmInfo().isCommandFollow();
        }

        // Stay - can attack if target is within range or commanded to
        if (me.getCharmInfo().hasCommandState(CommandStates.Stay)) {
            return (me.isWithinMeleeRange(victim) || me.getCharmInfo().isCommandAttack());
        }

        //  Pets attacking something (or chasing) should only switch targets if owner tells them to
        if (me.getVictim() && me.getVictim() != victim) {
            // Check if our owner selected this target and clicked "attack"
            Unit ownerTarget;
            var owner = me.getCharmerOrOwner().getAsPlayer();

            if (owner) {
                ownerTarget = owner.getSelectedUnit();
            } else {
                ownerTarget = me.getCharmerOrOwner().getVictim();
            }

            if (ownerTarget && me.getCharmInfo().isCommandAttack()) {
                return (ObjectGuid.opEquals(victim.getGUID().clone(), ownerTarget.getGUID().clone()));
            }
        }

        // Follow
        if (me.getCharmInfo().hasCommandState(CommandStates.Follow)) {
            return !me.getCharmInfo().isReturning();
        }

        // default, though we shouldn't ever get here
        return false;
    }

    @Override
    public void receiveEmote(Player player, TextEmotes emoteId) {
        if (ObjectGuid.opNotEquals(me.getOwnerGUID().clone(), player.getGUID().clone())) {
            return;
        }

        switch (emoteId) {
            case Cower:
                if (me.isPet() && me.getAsPet().isPetGhoul()) {
                    me.handleEmoteCommand(Emote.OneshotOmnicastGhoul);
                }

                break;
            case Angry:
                if (me.isPet() && me.getAsPet().isPetGhoul()) {
                    me.handleEmoteCommand(Emote.StateStun);
                }

                break;
            case Glare:
                if (me.isPet() && me.getAsPet().isPetGhoul()) {
                    me.handleEmoteCommand(Emote.StateStun);
                }

                break;
            case Soothe:
                if (me.isPet() && me.getAsPet().isPetGhoul()) {
                    me.handleEmoteCommand(Emote.OneshotOmnicastGhoul);
                }

                break;
        }
    }

    @Override
    public void onCharmed(boolean isNew) {
        if (!me.isPossessedByPlayer() && me.isCharmed()) {
            me.getMotionMaster().moveFollow(me.getCharmer(), SharedConst.PetFollowDist, me.getFollowAngle());
        }

        super.onCharmed(isNew);
    }


    @Override
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType) {
        damageTaken(attacker, damage, damageType, null);
    }


//ORIGINAL LINE: public override void DamageTaken(Unit attacker, ref double damage, DamageEffectType damageType, SpellInfo spellInfo = null)
    @Override
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo) {
        attackStart(attacker);
    }

    @Override
    public void justEnteredCombat(Unit who) {
        engagementStart(who);
    }

    @Override
    public void justExitedCombat() {
        engagementOver();
    }

    // The following aren't used by the PetAI but need to be defined to override
    //  default CreatureAI functions which interfere with the PetAI
    @Override
    public void moveInLineOfSight(Unit who) {
    }
    @Override
    public void moveInLineOfSightSafe(Unit who) {
    }
    @Override
    public void justAppeared() {
    } // we will control following manually
    @Override
    public void enterEvadeMode(EvadeReason why) {
    }

    private Unit selectNextTarget(boolean allowAutoSelect) {
        // Provides next target selection after current target death.
        // This function should only be called internally by the AI
        // Targets are not evaluated here for being valid targets, that is done in _CanAttack()
        // The parameter: allowAutoSelect lets us disable aggressive pet auto targeting for certain situations

        // Passive pets don't do next target selection
        if (me.hasReactState(ReactStates.Passive)) {
            return null;
        }

        // Check pet attackers first so we don't drag a bunch of targets to the owner
        var myAttacker = me.getAttackerForHelper();

        if (myAttacker) {
            if (!myAttacker.hasBreakableByDamageCrowdControlAura()) {
                return myAttacker;
            }
        }

        // Not sure why we wouldn't have an owner but just in case...
        if (!me.getCharmerOrOwner()) {
            return null;
        }

        // Check owner attackers
        var ownerAttacker = me.getCharmerOrOwner().getAttackerForHelper();

        if (ownerAttacker) {
            if (!ownerAttacker.hasBreakableByDamageCrowdControlAura()) {
                return ownerAttacker;
            }
        }

        // Check owner victim
        // 3.0.2 - Pets now start attacking their owners victim in defensive mode as soon as the hunter does
        var ownerVictim = me.getCharmerOrOwner().getVictim();

        if (ownerVictim) {
            return ownerVictim;
        }

        // Neither pet or owner had a target and aggressive pets can pick any target
        // To prevent aggressive pets from chain selecting targets and running off, we
        //  only select a random target if certain conditions are met.
        if (me.hasReactState(ReactStates.Aggressive) && allowAutoSelect) {
            if (!me.getCharmInfo().isReturning() || me.getCharmInfo().isFollowing() || me.getCharmInfo().isAtStay()) {
                var nearTarget = me.selectNearestHostileUnitInAggroRange(true, true);

                if (nearTarget) {
                    return nearTarget;
                }
            }
        }

        // Default - no valid targets
        return null;
    }

    private void handleReturnMovement() {
        // Handles moving the pet back to stay or owner

        // Prevent activating movement when under control of spells
        // such as "Eyes of the Beast"
        if (me.isCharmed()) {
            return;
        }

        if (me.getCharmInfo() == null) {
            Log.outWarn(LogFilter.ScriptsAi, String.format("me.GetCharmInfo() is NULL in PetAI::HandleReturnMovement(). Debug info: %1$s", getDebugInfo()));

            return;
        }

        if (me.getCharmInfo().hasCommandState(CommandStates.Stay)) {
            if (!me.getCharmInfo().isAtStay() && !me.getCharmInfo().isReturning()) {
                // Return to previous position where stay was clicked

                float x;
                tangible.OutObject<Float> tempOutX = new tangible.OutObject<Float>();
                float y;
                tangible.OutObject<Float> tempOutY = new tangible.OutObject<Float>();
                float z;
                tangible.OutObject<Float> tempOutZ = new tangible.OutObject<Float>();
                me.getCharmInfo().getStayPosition(tempOutX, tempOutY, tempOutZ);
            z = tempOutZ.outArgValue;
            y = tempOutY.outArgValue;
            x = tempOutX.outArgValue;
                clearCharmInfoFlags();
                me.getCharmInfo().setIsReturning(true);

                if (me.hasUnitState(UnitState.Chase)) {
                    me.getMotionMaster().remove(MovementGeneratorType.Chase);
                }


//ORIGINAL LINE: Me.MotionMaster.MovePoint((uint)Me.GUID.Counter, x, y, z);
                me.getMotionMaster().movePoint((int)me.getGUID().getCounter(), x, y, z);
            }
        }
        else { // COMMAND_FOLLOW
            if (!me.getCharmInfo().isFollowing() && !me.getCharmInfo().isReturning()) {
                clearCharmInfoFlags();
                me.getCharmInfo().setIsReturning(true);

                if (me.hasUnitState(UnitState.Chase)) {
                    me.getMotionMaster().remove(MovementGeneratorType.Chase);
                }

                me.getMotionMaster().moveFollow(me.getCharmerOrOwner(), SharedConst.PetFollowDist, me.getFollowAngle());
            }
        }

        me.removeUnitFlag(UnitFlags.PetInCombat); // on player pets, this flag indicates that we're actively going after a target - we're returning, so remove it
    }

    private void doAttack(Unit target, boolean chase) {
        // Handles attack with or without chase and also resets flags
        // for next update / creature kill

        if (me.attack(target, true)) {
            me.setUnitFlag(UnitFlags.PetInCombat); // on player pets, this flag indicates we're actively going after a target - that's what we're doing, so set it

            // Play sound to let the player know the pet is attacking something it picked on its own
            if (me.hasReactState(ReactStates.Aggressive) && !me.getCharmInfo().isCommandAttack()) {
                me.sendPetAIReaction(me.getGUID().clone());
            }

            if (chase) {
                var oldCmdAttack = me.getCharmInfo().isCommandAttack(); // This needs to be reset after other flags are cleared
                clearCharmInfoFlags();
                me.getCharmInfo().setIsCommandAttack(oldCmdAttack); // For passive pets commanded to attack so they will use spells

                if (me.hasUnitState(UnitState.Follow)) {
                    me.getMotionMaster().remove(MovementGeneratorType.Follow);
                }

                // Pets with ranged attacks should not care about the chase angle at all.
                var chaseDistance = me.getPetChaseDistance();
                var angle = chaseDistance == 0.0f ? MathF.PI : 0.0f;
                var tolerance = chaseDistance == 0.0f ? MathFunctions.PiOver4 : (MathF.PI * 2);
                me.getMotionMaster().moveChase(target, new ChaseRange(0.0f, chaseDistance), new ChaseAngle(angle, tolerance));
            } else {
                clearCharmInfoFlags();
                me.getCharmInfo().setIsAtStay(true);

                if (me.hasUnitState(UnitState.Follow)) {
                    me.getMotionMaster().remove(MovementGeneratorType.Follow);
                }

                me.getMotionMaster().moveIdle();
            }
        }
    }

    private boolean needToStop() {
        // This is needed for charmed creatures, as once their target was reset other effects can trigger threat
        if (me.isCharmed() && me.getVictim() == me.getCharmer()) {
            return true;
        }

        // dont allow pets to follow targets far away from owner
        var owner = me.getCharmerOrOwner();

        if (owner) {
            if (owner.location.getExactDist(me.location) >= (owner.getVisibilityRange() - 10.0f)) {
                return true;
            }
        }

        return !me.isValidAttackTarget(me.getVictim());
    }

    private void stopAttack() {
        if (!me.isAlive()) {
            me.getMotionMaster().clear();
            me.getMotionMaster().moveIdle();
            me.combatStop();

            return;
        }

        me.attackStop();
        me.interruptNonMeleeSpells(false);
        me.getCharmInfo().setIsCommandAttack(false);
        clearCharmInfoFlags();
        handleReturnMovement();
    }

    private void updateAllies() {
        updateAlliesTimer = 10 * Time.InMilliseconds; // update friendly targets every 10 seconds, lesser checks increase performance

        var owner = me.getCharmerOrOwner();

        if (!owner) {
            return;
        }

        PlayerGroup group = null;
        var player = owner.getAsPlayer();

        if (player) {
            group = player.getGroup();
        }

        // only pet and owner/not in group.ok
        if (allySet.size() == 2 && !group) {
            return;
        }

        // owner is in group; group members filled in already (no raid . subgroupcount = whole count)
        if (group && !group.isRaidGroup() && allySet.size() == (group.getMembersCount() + 2)) {
            return;
        }

        allySet.clear();
        allySet.add(me.getGUID().clone());

        if (group) { // add group
            for (var refe = group.getFirstMember(); refe != null; refe = refe.Next()) {
                var target = refe.getSource();

                if (!target || !target.isInMap(owner) || !group.sameSubGroup(owner.getAsPlayer(), target)) {
                    continue;
                }

                if (ObjectGuid.opEquals(target.getGUID().clone(), owner.getGUID().clone())) {
                    continue;
                }

                allySet.add(target.getGUID().clone());
            }
        }
        else { // remove group
            allySet.add(owner.getGUID().clone());
        }
    }

    /** 
      Quick access to set all flags to FALSE
    */
    private void clearCharmInfoFlags() {
        var ci = me.getCharmInfo();

        if (ci != null) {
            ci.setIsAtStay(false);
            ci.setIsCommandAttack(false);
            ci.setIsCommandFollow(false);
            ci.setIsFollowing(false);
            ci.setIsReturning(false);
        }
    }
}