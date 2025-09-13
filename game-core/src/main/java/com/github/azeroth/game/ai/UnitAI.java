package com.github.azeroth.game.ai;


import com.github.azeroth.defines.SpellCastResult;
import com.github.azeroth.game.combat.ThreatManager;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.CalcDamageInfo;
import com.github.azeroth.game.entity.unit.ObjectDistanceOrderPred;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.CastSpellExtraArgs;
import com.github.azeroth.game.spell.SpellInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.collections;
import java.util.random;


public class UnitAI implements IUnitAI {
	private static final HashMap<(
    int id, Difficulty
    difficulty),AISpellInfoType>AISPELLINFO =new HashMap<(
    int id, Difficulty
    difficulty),AISpellInfoType>();

    private Unit me;

    public UnitAI(Unit unit) {
        setMe(unit);
    }

    public static void fillAISpellInfo() {
        global.getSpellMgr().forEachSpellInfo(spellInfo ->
        {
            AISpellInfoType AIInfo = new AISpellInfoType();

            if (spellInfo.hasAttribute(SpellAttr0.AllowCastWhileDead)) {
                AIInfo.condition = AICondition.Die;
            } else if (spellInfo.isPassive || spellInfo.duration == -1) {
                AIInfo.condition = AICondition.Aggro;
            } else {
                AIInfo.condition = AICondition.Combat;
            }

            if (AIInfo.cooldown.TotalMilliseconds < spellInfo.recoveryTime) {
                AIInfo.cooldown = duration.ofSeconds(spellInfo.recoveryTime);
            }

            if (spellInfo.getMaxRange(false) != 0) {
                for (var spellEffectInfo : spellInfo.effects) {
                    var targetType = spellEffectInfo.targetA.target;

                    if (targetType == targets.UnitTargetEnemy || targetType == targets.DestTargetEnemy) {
                        if (AIInfo.target.getValue() < AITarget.victim.getValue()) {
                            AIInfo.target = AITarget.victim;
                        }
                    } else if (targetType == targets.UnitDestAreaEnemy) {
                        if (AIInfo.target.getValue() < AITarget.Enemy.getValue()) {
                            AIInfo.target = AITarget.Enemy;
                        }
                    }

                    if (spellEffectInfo.isEffect(SpellEffectName.ApplyAura)) {
                        if (targetType == targets.UnitTargetEnemy) {
                            if (AIInfo.target.getValue() < AITarget.Debuff.getValue()) {
                                AIInfo.target = AITarget.Debuff;
                            }
                        } else if (spellInfo.IsPositive) {
                            if (AIInfo.target.getValue() < AITarget.Buff.getValue()) {
                                AIInfo.target = AITarget.Buff;
                            }
                        }
                    }
                }
            }

            AIInfo.realCooldown = duration.ofSeconds(spellInfo.recoveryTime + spellInfo.startRecoveryTime);
            AIInfo.maxRange = spellInfo.getMaxRange(false) * 3 / 4;

            AIInfo.effects = 0;
            AIInfo.targets = 0;

            for (var spellEffectInfo : spellInfo.effects) {
                // Spell targets self.
                if (spellEffectInfo.targetA.target == targets.UnitCaster) {
                    AIInfo.targets |= 1 << (SelectTargetType.Self.getValue() - 1);
                }

                // Spell targets a single enemy.
                if (spellEffectInfo.targetA.target == targets.UnitTargetEnemy || spellEffectInfo.targetA.target == targets.DestTargetEnemy) {
                    AIInfo.targets |= 1 << (SelectTargetType.SingleEnemy.getValue() - 1);
                }

                // Spell targets AoE at enemy.
                if (spellEffectInfo.targetA.target == targets.UnitSrcAreaEnemy || spellEffectInfo.targetA.target == targets.UnitDestAreaEnemy || spellEffectInfo.targetA.target == targets.SrcCaster || spellEffectInfo.targetA.target == targets.DestDynobjEnemy) {
                    AIInfo.targets |= 1 << (SelectTargetType.AoeEnemy.getValue() - 1);
                }

                // Spell targets an enemy.
                if (spellEffectInfo.targetA.target == targets.UnitTargetEnemy || spellEffectInfo.targetA.target == targets.DestTargetEnemy || spellEffectInfo.targetA.target == targets.UnitSrcAreaEnemy || spellEffectInfo.targetA.target == targets.UnitDestAreaEnemy || spellEffectInfo.targetA.target == targets.SrcCaster || spellEffectInfo.targetA.target == targets.DestDynobjEnemy) {
                    AIInfo.targets |= 1 << (SelectTargetType.AnyEnemy.getValue() - 1);
                }

                // Spell targets a single friend (or self).
                if (spellEffectInfo.targetA.target == targets.UnitCaster || spellEffectInfo.targetA.target == targets.UnitTargetAlly || spellEffectInfo.targetA.target == targets.UnitTargetParty) {
                    AIInfo.targets |= 1 << (SelectTargetType.SingleFriend.getValue() - 1);
                }

                // Spell targets AoE friends.
                if (spellEffectInfo.targetA.target == targets.UnitCasterAreaParty || spellEffectInfo.targetA.target == targets.UnitLastTargetAreaParty || spellEffectInfo.targetA.target == targets.SrcCaster) {
                    AIInfo.targets |= 1 << (SelectTargetType.AoeFriend.getValue() - 1);
                }

                // Spell targets any friend (or self).
                if (spellEffectInfo.targetA.target == targets.UnitCaster || spellEffectInfo.targetA.target == targets.UnitTargetAlly || spellEffectInfo.targetA.target == targets.UnitTargetParty || spellEffectInfo.targetA.target == targets.UnitCasterAreaParty || spellEffectInfo.targetA.target == targets.UnitLastTargetAreaParty || spellEffectInfo.targetA.target == targets.SrcCaster) {
                    AIInfo.targets |= 1 << (SelectTargetType.AnyFriend.getValue() - 1);
                }

                // Make sure that this spell includes a damage effect.
                if (spellEffectInfo.effect == SpellEffectName.SchoolDamage || spellEffectInfo.effect == SpellEffectName.Instakill || spellEffectInfo.effect == SpellEffectName.EnvironmentalDamage || spellEffectInfo.effect == SpellEffectName.HealthLeech) {
                    AIInfo.effects |= 1 << (SelectEffect.damage.getValue() - 1);
                }

                // Make sure that this spell includes a healing effect (or an apply aura with a periodic heal).
                if (spellEffectInfo.effect == SpellEffectName.Heal || spellEffectInfo.effect == SpellEffectName.HealMaxHealth || spellEffectInfo.effect == SpellEffectName.HealMechanical || (spellEffectInfo.effect == SpellEffectName.ApplyAura && spellEffectInfo.applyAuraName == AuraType.PeriodicHeal)) {
                    AIInfo.effects |= 1 << (SelectEffect.healing.getValue() - 1);
                }

                // Make sure that this spell applies an aura.
                if (spellEffectInfo.effect == SpellEffectName.ApplyAura) {
                    AIInfo.effects |= 1 << (SelectEffect.aura.getValue() - 1);
                }
            }

            AISPELLINFO.put((spellInfo.id, spellInfo.Difficulty), AIInfo);
        });
    }

    public static AISpellInfoType getAISpellInfo(int spellId, Difficulty difficulty) {
        return AISPELLINFO.get((spellId, difficulty));
    }

    protected final Unit getMe() {
        return me;
    }

    private void setMe(Unit value) {
        me = value;
    }

    private ThreatManager getThreatManager() {
        return getMe().getThreatManager();
    }

    public void attackStart(Unit victim) {
        if (victim != null && getMe().attack(victim, true)) {
            // Clear distracted state on attacking
            if (getMe().hasUnitState(UnitState.Distracted)) {
                getMe().clearUnitState(UnitState.Distracted);
                getMe().getMotionMaster().clear();
            }

            getMe().getMotionMaster().moveChase(victim);
        }
    }

    public final void attackStartCaster(Unit victim, float dist) {
        if (victim != null && getMe().attack(victim, false)) {
            getMe().getMotionMaster().moveChase(victim, dist);
        }
    }

    public final void doMeleeAttackIfReady() {
        Creature creature;
        tangible.OutObject<Creature> tempOut_creature = new tangible.OutObject<Creature>();
        if (getMe().hasUnitState(UnitState.Casting) || (getMe().isCreature(tempOut_creature) && !creature.getCanMelee())) {
            creature = tempOut_creature.outArgValue;
            return;
        } else {
            creature = tempOut_creature.outArgValue;
        }

        var victim = getMe().getVictim();

        if (!getMe().isWithinMeleeRange(victim)) {
            return;
        }

        //Make sure our attack is ready and we aren't currently casting before checking distance
        if (getMe().isAttackReady()) {
            getMe().attackerStateUpdate(victim);
            getMe().resetAttackTimer();
        }

        if (getMe().haveOffhandWeapon() && getMe().isAttackReady(WeaponAttackType.OffAttack)) {
            getMe().attackerStateUpdate(victim, WeaponAttackType.OffAttack);
            getMe().resetAttackTimer(WeaponAttackType.OffAttack);
        }
    }

    public void onMeleeAttack(CalcDamageInfo damageInfo, WeaponAttackType attType, boolean extra) {
    }

    public final boolean doSpellAttackIfReady(int spellId) {
        if (getMe().hasUnitState(UnitState.Casting) || !getMe().isAttackReady()) {
            return true;
        }

        var spellInfo = global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

        if (spellInfo != null) {
            if (getMe().isWithinCombatRange(getMe().getVictim(), spellInfo.getMaxRange(false))) {
                getMe().castSpell(getMe().getVictim(), spellId, new CastSpellExtraArgs(getMe().getMap().getDifficultyID()));
                getMe().resetAttackTimer();

                return true;
            }
        }

        return false;
    }

    /**
     * Select the best target (in
     * <targetType>
     * order) from the threat list that fulfill the following:
     * - Not among the first
     * <offset>
     * entries in
     * <targetType>
     * order (or MAXTHREAT order, if
     * <targetType>
     * is RANDOM).
     * - Within at most
     * <dist>
     * yards (if dist > 0.0f)
     * - At least -
     * <dist>
     * yards away (if dist
     * < 0.0f)
     * - Is a player ( if playerOnly= true)
     * - Not the current tank ( if withTank= false)
     * - Has aura with ID
     * <aura>
     * (if aura > 0)
     * - Does not have aura with ID -<aura> (if aura < 0)
     */

    public final Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank) {
        return selectTarget(targetType, offset, dist, playerOnly, withTank, 0);
    }

    public final Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly) {
        return selectTarget(targetType, offset, dist, playerOnly, true, 0);
    }

    public final Unit selectTarget(SelectTargetMethod targetType, int offset, float dist) {
        return selectTarget(targetType, offset, dist, false, true, 0);
    }

    public final Unit selectTarget(SelectTargetMethod targetType, int offset) {
        return selectTarget(targetType, offset, 0.0f, false, true, 0);
    }

    public final Unit selectTarget(SelectTargetMethod targetType) {
        return selectTarget(targetType, 0, 0.0f, false, true, 0);
    }

    public final Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura) {
        return selectTarget(targetType, offset, new DefaultTargetSelector(getMe(), dist, playerOnly, withTank, aura));
    }

    public final Unit selectTarget(SelectTargetMethod targetType, int offset, ICheck<unit> selector) {
        return selectTarget(targetType, offset, selector.Invoke);
    }

    /**
     * Select the best target (in
     * <targetType>
     * order) satisfying
     * <predicate>
     * from the threat list.
     * If <offset> is nonzero, the first <offset> entries in <targetType> order (or MAXTHREAT order, if <targetType> is RANDOM) are skipped.
     */
    public final Unit selectTarget(SelectTargetMethod targetType, int offset, tangible.Func1Param<unit, Boolean> selector) {
        var mgr = getThreatManager();

        // shortcut: if we ignore the first <offset> elements, and there are at most <offset> elements, then we ignore ALL elements
        if (mgr.getThreatListSize() <= offset) {
            return null;
        }

        var targetList = selectTargetList((int) mgr.getThreatListSize(), targetType, offset, selector);

        // maybe nothing fulfills the predicate
        if (targetList.isEmpty()) {
            return null;
        }

        return switch (targetType) {
            case MaxThreat, MinThreat, MaxDistance, MinDistance -> targetList.get(0);
            case Random -> targetList.SelectRandom();
            default -> null;
        };
    }

    /**
     * Select the best (up to)
     * <num>
     * targets (in
     * <targetType>
     * order) from the threat list that fulfill the following:
     * - Not among the first
     * <offset>
     * entries in
     * <targetType>
     * order (or MAXTHREAT order, if
     * <targetType>
     * is RANDOM).
     * - Within at most
     * <dist>
     * yards (if dist > 0.0f)
     * - At least -
     * <dist>
     * yards away (if dist
     * < 0.0f)
     * - Is a player ( if playerOnly= true)
     * - Not the current tank ( if withTank= false)
     * - Has aura with ID
     * <aura>
     * (if aura > 0)
     * - Does not have aura with ID -
     * <aura>
     * (if aura
     * < 0)
     * The resulting targets are stored in
     * <targetList> (which is cleared first).
     */

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank) {
        return selectTargetList(num, targetType, offset, dist, playerOnly, withTank, 0);
    }

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly) {
        return selectTargetList(num, targetType, offset, dist, playerOnly, true, 0);
    }

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist) {
        return selectTargetList(num, targetType, offset, dist, false, true, 0);
    }

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset) {
        return selectTargetList(num, targetType, offset, 0f, false, true, 0);
    }

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType) {
        return selectTargetList(num, targetType, 0, 0f, false, true, 0);
    }

    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura) {
        return selectTargetList(num, targetType, offset, (new DefaultTargetSelector(getMe(), dist, playerOnly, withTank, aura)).Invoke);
    }

    /**
     * Select the best (up to)
     * <num>
     * targets (in
     * <targetType>
     * order) satisfying
     * <predicate>
     * from the threat list and stores them in
     * <targetList>
     * (which is cleared first).
     * If <offset> is nonzero, the first <offset> entries in <targetType> order (or MAXTHREAT order, if <targetType> is RANDOM) are skipped.
     */
    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, tangible.Func1Param<unit, Boolean> selector) {
        var targetList = new ArrayList<>();

        var mgr = getThreatManager();

        // shortcut: we're gonna ignore the first <offset> elements, and there's at most <offset> elements, so we ignore them all - nothing to do here
        if (mgr.getThreatListSize() <= offset) {
            return targetList;
        }

        if (targetType == SelectTargetMethod.MaxDistance || targetType == SelectTargetMethod.MinDistance) {
            for (var refe : mgr.getSortedThreatList()) {
                if (!refe.isOnline()) {
                    continue;
                }

                targetList.add(refe.getVictim());
            }
        } else {
            var currentVictim = mgr.getCurrentVictim();

            if (currentVictim != null) {
                targetList.add(currentVictim);
            }

            for (var refe : mgr.getSortedThreatList()) {
                if (!refe.isOnline()) {
                    continue;
                }

                var thisTarget = refe.getVictim();

                if (thisTarget != currentVictim) {
                    targetList.add(thisTarget);
                }
            }
        }

        // shortcut: the list isn't gonna get any larger
        if (targetList.size() <= offset) {
            targetList.clear();

            return targetList;
        }

        // right now, list is unsorted for DISTANCE types - re-sort by MAXDISTANCE
        if (targetType == SelectTargetMethod.MaxDistance || targetType == SelectTargetMethod.MinDistance) {
            sortByDistance(targetList, targetType == SelectTargetMethod.MinDistance);
        }

        // now the list is MAX sorted, reverse for MIN types
        if (targetType == SelectTargetMethod.MinThreat) {
            collections.reverse(targetList);
        }

        // ignore the first <offset> elements
        while (offset != 0) {
            targetList.remove(0);
            --offset;
        }

        // then finally filter by predicate
        tangible.ListHelper.removeAll(targetList, unit -> !selector.invoke(unit));

        if (targetList.size() <= num) {
            return targetList;
        }

        if (targetType == SelectTargetMethod.random) {
            targetList = targetList.SelectRandom(num).ToList();
        } else {
            targetList.Resize(num);
        }

        return targetList;
    }

    public final SpellCastResult doCast(int spellId) {
        Unit target = null;
        var aiTargetType = AITarget.Self;

        var info = getAISpellInfo(spellId, getMe().getMap().getDifficultyID());

        if (info != null) {
            aiTargetType = info.target;
        }

        switch (aiTargetType) {
            default:
            case Self:
                target = getMe();

                break;
            case Victim:
                target = getMe().getVictim();

                break;
            case Enemy: {
                var spellInfo = global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

                if (spellInfo != null) {
                    DefaultTargetSelector targetSelectorInner = new DefaultTargetSelector(getMe(), spellInfo.getMaxRange(false), false, true, 0);


//					bool targetSelector(Unit candidate)
//						{
//							if (!candidate.IsPlayer)
//							{
//								if (spellInfo.hasAttribute(SpellAttr3.OnlyOnPlayer))
//									return false;
//
//								if (spellInfo.hasAttribute(SpellAttr5.NotOnPlayerControlledNpc) && candidate.IsControlledByPlayer)
//									return false;
//							}
//							else if (spellInfo.hasAttribute(SpellAttr5.NotOnPlayer))
//							{
//								return false;
//							}
//
//							return targetSelectorInner.invoke(candidate);
//						}

                    ;
                    target = selectTarget(SelectTargetMethod.random, 0, targetSelector);
                }

                break;
            }
            case Ally:
            case Buff:
                target = getMe();

                break;
            case Debuff: {
                var spellInfo = global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

                if (spellInfo != null) {
                    var range = spellInfo.getMaxRange(false);

                    DefaultTargetSelector targetSelectorInner = new DefaultTargetSelector(getMe(), range, false, true, -(int) spellId);


//					bool targetSelector(Unit candidate)
//						{
//							if (!candidate.IsPlayer)
//							{
//								if (spellInfo.hasAttribute(SpellAttr3.OnlyOnPlayer))
//									return false;
//
//								if (spellInfo.hasAttribute(SpellAttr5.NotOnPlayerControlledNpc) && candidate.IsControlledByPlayer)
//									return false;
//							}
//							else if (spellInfo.hasAttribute(SpellAttr5.NotOnPlayer))
//							{
//								return false;
//							}
//
//							return targetSelectorInner.invoke(candidate);
//						}

                    ;

                    if (!spellInfo.hasAuraInterruptFlag(SpellAuraInterruptFlags.NotVictim) && targetSelector(getMe().getVictim())) {
                        target = getMe().getVictim();
                    } else {
                        target = selectTarget(SelectTargetMethod.random, 0, targetSelector);
                    }
                }

                break;
            }
        }

        if (target != null) {
            return getMe().castSpell(target, spellId, false);
        }

        return SpellCastResult.BadTargets;
    }

    public final SpellCastResult doCast(Unit victim, int spellId) {
        return doCast(victim, spellId, null);
    }

    public final SpellCastResult doCast(Unit victim, int spellId, CastSpellExtraArgs args) {
        args = args != null ? args : new CastSpellExtraArgs();

        if (getMe().hasUnitState(UnitState.Casting) && !args.triggerFlags.hasFlag(TriggerCastFlags.IgnoreCastInProgress)) {
            return SpellCastResult.SpellInProgress;
        }

        return getMe().castSpell(victim, spellId, args);
    }

    public final SpellCastResult doCastSelf(int spellId) {
        return doCastSelf(spellId, null);
    }

    public final SpellCastResult doCastSelf(int spellId, CastSpellExtraArgs args) {
        return doCast(getMe(), spellId, args);
    }

    public final SpellCastResult doCastVictim(int spellId) {
        return doCastVictim(spellId, null);
    }

    public final SpellCastResult doCastVictim(int spellId, CastSpellExtraArgs args) {
        var victim = getMe().getVictim();

        if (victim != null) {
            return doCast(victim, spellId, args);
        }

        return SpellCastResult.BadTargets;
    }

    public final SpellCastResult doCastAOE(int spellId) {
        return doCastAOE(spellId, null);
    }

    public final SpellCastResult doCastAOE(int spellId, CastSpellExtraArgs args) {
        return doCast(null, spellId, args);
    }

    public boolean canAIAttack(Unit victim) {
        return true;
    }

    public void updateAI(int diff) {
    }

    /**
     */
    // Called when unit's charm state changes with isNew = false
    // Implementation should call me->ScheduleAIChange() if AI replacement is desired
    // If this call is made, AI will be replaced on the next tick
    // When replacement is made, OnCharmed is called with isNew = true

    public void initializeAI() {
        if (!getMe().isDead()) {
            reset();
        }
    }

    public void reset() {
    }

    /**
     * @param apply
     */
    public void onCharmed(boolean isNew) {
        if (!isNew) {
            getMe().scheduleAIChange();
        }
    }

    public boolean shouldSparWith(Unit target) {
        return false;
    }

    public void doAction(int action) {
    }

    public int getData() {
        return getData(0);
    }

    public int getData(int id) {
        return 0;
    }

    public void setData(int id, int value) {
    }

    public void setGUID(ObjectGuid guid, int id) {
    }

    public ObjectGuid getGUID() {
        return getGUID(0);
    }

    public void setGUID(ObjectGuid guid) {
        setGUID(guid, 0);
    }

    public ObjectGuid getGUID(int id) {
        return ObjectGuid.Empty;
    }

    // Called when the unit enters combat
    // (NOTE: Creature engage logic should NOT be here, but in JustEngagedWith, which happens once threat is established!)
    public void justEnteredCombat(Unit who) {
    }

    // Called when the unit leaves combat
    public void justExitedCombat() {
    }

    // Called when the unit is about to be removed from the world (despawn, grid unload, corpse disappearing, player logging out etc.)
    public void onDespawn() {
    }

    // Called at any Damage to any victim (before damage apply)
    public void damageDealt(Unit victim, tangible.RefObject<Double> damage, DamageEffectType damageType) {
    }

    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType) {
        damageTaken(attacker, damage, damageType, null);
    }

    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo) {
    }

    public void healReceived(Unit by, double addhealth) {
    }

    public void healDone(Unit to, double addhealth) {
    }

    public void spellInterrupted(int spellId, int unTimeMs) {
    }

    /**
     * Called when a game event starts or ends
     */
    public void onGameEvent(boolean start, short eventId) {
    }

    public String getDebugInfo() {
        return String.format("Me: %1$s", (getMe() != null ? getMe().getDebugInfo() : "NULL"));
    }

    private void sortByDistance(ArrayList<Unit> targets, boolean ascending) {
        collections.sort(targets, new ObjectDistanceOrderPred(getMe(), ascending));
    }
}
