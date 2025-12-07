package com.github.azeroth.game.ai;


import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.world.WorldContext;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class UnitAI {

    protected final Unit me;
    protected final WorldContext worldContext;


    public UnitAI(Unit unit, WorldContext worldContext) {
        this.me = unit;
        this.worldContext = worldContext;
    }

    public void attackStart(Unit victim) {
        if (me.attack(victim, true)) {
            // Clear distracted state on attacking
            if (me.hasUnitState(UnitState.DISTRACTED)) {
                me.clearUnitState(UnitState.DISTRACTED);
                me.getMotionMaster().clear();
            }

            me.getMotionMaster().moveChase(victim);
        }
    }

    public final void attackStartCaster(Unit victim, float dist) {
        if (me.attack(victim, false)) {
            me.getMotionMaster().moveChase(victim, dist);
        }
    }

    public final void doMeleeAttackIfReady() {
        Creature creature;
        tangible.OutObject<Creature> tempOutCreature = new tangible.OutObject<Creature>();
        if (getMe().hasUnitState(UnitState.Casting) || (getMe().tryGetAsCreature(tempOutCreature) && !creature.getCanMelee())) {
        creature = tempOutCreature.outArgValue;
            return;
        } else {
        creature = tempOutCreature.outArgValue;
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

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public bool DoSpellAttackIfReady(uint spellId)
    public final boolean doSpellAttackIfReady(int spellId) {
        if (getMe().hasUnitState(UnitState.Casting) || !getMe().isAttackReady()) {
            return true;
        }

        var spellInfo = Global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

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
      Select the best target (in
      <targetType>
       order) from the threat list that fulfill the following:
       - Not among the first
       <offset>
        entries in
        <targetType>
         order (or MAXTHREAT order, if
         <targetType>
          is RANDOM).
          - Within at most
          <dist>
           yards (if dist > 0.0f)
           - At least -
           <dist>
            yards away (if dist
            < 0.0f)
             - Is a player ( if playerOnly= true)
               - Not the current tank ( if withTank= false)
               - Has aura with ID
            <aura>
             (if aura > 0)
             - Does not have aura with ID -<aura> (if aura < 0)
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Unit SelectTarget(SelectTargetMethod targetType, uint offset = 0, float dist = 0.0f, bool playerOnly = false, bool withTank = true, int aura = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final Unit selectTarget(SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura) {
        return SelectTarget(targetType, offset, new DefaultTargetSelector(getMe(), dist, playerOnly, withTank, aura));
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public Unit SelectTarget(SelectTargetMethod targetType, uint offset, ICheck<Unit> selector)
    public final Unit selectTarget(SelectTargetMethod targetType, int offset, ICheck<Unit> selector) {
        return SelectTarget(targetType, offset, selector.Invoke);
    }

    /** 
      Select the best target (in
      <targetType>
       order) satisfying
       <predicate>
        from the threat list.
        If <offset> is nonzero, the first <offset> entries in <targetType> order (or MAXTHREAT order, if <targetType> is RANDOM) are skipped.
    */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public Unit SelectTarget(SelectTargetMethod targetType, uint offset, Func<Unit, bool> selector)
    public final Unit selectTarget(SelectTargetMethod targetType, int offset, tangible.Func1Param<Unit, Boolean> selector) {
        var mgr = getThreatManager();

        // shortcut: if we ignore the first <offset> elements, and there are at most <offset> elements, then we ignore ALL elements
        if (mgr.getThreatListSize() <= offset) {
            return null;
        }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: var targetList = SelectTargetList((uint)mgr.ThreatListSize, targetType, offset, selector);
        var targetList = selectTargetList((int)mgr.getThreatListSize(), targetType, offset, selector);

        // maybe nothing fulfills the predicate
        if (targetList.Empty()) {
            return null;
        }

        return switch (targetType) {
            case MaxThreat, MinThreat, MaxDistance, MinDistance -> targetList.get(0);
            case Random -> targetList.SelectRandom();
            default -> null;
        };
    }

    /** 
      Select the best (up to)
      <num>
       targets (in
       <targetType>
        order) from the threat list that fulfill the following:
        - Not among the first
        <offset>
         entries in
         <targetType>
          order (or MAXTHREAT order, if
          <targetType>
           is RANDOM).
           - Within at most
           <dist>
            yards (if dist > 0.0f)
            - At least -
            <dist>
             yards away (if dist
             < 0.0f)
              - Is a player ( if playerOnly= true)
                - Not the current tank ( if withTank= false)
                - Has aura with ID
             <aura>
              (if aura > 0)
              - Does not have aura with ID -
              <aura>
               (if aura
               < 0)
                The resulting targets are stored in
               <targetList> (which is cleared first).
    */

    public final java.util.ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank) {
        return selectTargetList(num, targetType, offset, dist, playerOnly, withTank, 0);
    }

    public final java.util.ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly) {
        return selectTargetList(num, targetType, offset, dist, playerOnly, true, 0);
    }

    public final java.util.ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist) {
        return selectTargetList(num, targetType, offset, dist, false, true, 0);
    }

    public final java.util.ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset) {
        return selectTargetList(num, targetType, offset, 0f, false, true, 0);
    }

    public final java.util.ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType) {
        return selectTargetList(num, targetType, 0, 0f, false, true, 0);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<Unit> SelectTargetList(uint num, SelectTargetMethod targetType, uint offset = 0, float dist = 0f, bool playerOnly = false, bool withTank = true, int aura = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, float dist, boolean playerOnly, boolean withTank, int aura) {
        return selectTargetList(num, targetType, offset, (new DefaultTargetSelector(getMe(), dist, playerOnly, withTank, aura)).Invoke);
    }

    /** 
      Select the best (up to)
      <num>
       targets (in
       <targetType>
        order) satisfying
        <predicate>
         from the threat list and stores them in
         <targetList>
          (which is cleared first).
          If <offset> is nonzero, the first <offset> entries in <targetType> order (or MAXTHREAT order, if <targetType> is RANDOM) are skipped.
    */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public List<Unit> SelectTargetList(uint num, SelectTargetMethod targetType, uint offset, Func<Unit, bool> selector)
    public final ArrayList<Unit> selectTargetList(int num, SelectTargetMethod targetType, int offset, tangible.Func1Param<Unit, Boolean> selector) {
        var targetList = new ArrayList<Unit>();

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
            Collections.reverse(targetList);
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

        if (targetType == SelectTargetMethod.Random) {
            targetList = targetList.SelectRandom(num).ToList();
        } else {
            targetList.Resize(num);
        }

        return targetList;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public SpellCastResult DoCast(uint spellId)
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
                var spellInfo = Global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

                if (spellInfo != null) {
                    DefaultTargetSelector targetSelectorInner = new DefaultTargetSelector(getMe(), spellInfo.getMaxRange(false), false, true, 0);

//C# TO JAVA CONVERTER TODO TASK: Local functions are not converted by C# to Java Converter:
//                    bool targetSelector(Unit candidate)
//                        {
//                            if (!candidate.IsPlayer)
//                            {
//                                if (spellInfo.HasAttribute(SpellAttr3.OnlyOnPlayer))
//                                    return false;
//
//                                if (spellInfo.HasAttribute(SpellAttr5.NotOnPlayerControlledNpc) && candidate.IsControlledByPlayer)
//                                    return false;
//                            }
//                            else if (spellInfo.HasAttribute(SpellAttr5.NotOnPlayer))
//                            {
//                                return false;
//                            }
//
//                            return targetSelectorInner.Invoke(candidate);
//                        }

                    ;
                    target = SelectTarget(SelectTargetMethod.Random, 0, targetSelector);
                }

                break;
            }
            case Ally:
            case Buff:
                target = getMe();

                break;
            case Debuff: {
                var spellInfo = Global.getSpellMgr().getSpellInfo(spellId, getMe().getMap().getDifficultyID());

                if (spellInfo != null) {
                    var range = spellInfo.getMaxRange(false);

                    DefaultTargetSelector targetSelectorInner = new DefaultTargetSelector(getMe(), range, false, true, -(int)spellId);

//C# TO JAVA CONVERTER TODO TASK: Local functions are not converted by C# to Java Converter:
//                    bool targetSelector(Unit candidate)
//                        {
//                            if (!candidate.IsPlayer)
//                            {
//                                if (spellInfo.HasAttribute(SpellAttr3.OnlyOnPlayer))
//                                    return false;
//
//                                if (spellInfo.HasAttribute(SpellAttr5.NotOnPlayerControlledNpc) && candidate.IsControlledByPlayer)
//                                    return false;
//                            }
//                            else if (spellInfo.HasAttribute(SpellAttr5.NotOnPlayer))
//                            {
//                                return false;
//                            }
//
//                            return targetSelectorInner.Invoke(candidate);
//                        }

                    ;

                    if (!spellInfo.hasAuraInterruptFlag(SpellAuraInterruptFlags.NotVictim) && targetSelector(getMe().getVictim())) {
                        target = getMe().getVictim();
                    } else {
                        target = SelectTarget(SelectTargetMethod.Random, 0, targetSelector);
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SpellCastResult DoCast(Unit victim, uint spellId, CastSpellExtraArgs args = null)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final SpellCastResult doCast(Unit victim, int spellId, CastSpellExtraArgs args) {
        args = args != null ? args : new CastSpellExtraArgs();

        if (getMe().hasUnitState(UnitState.Casting) && !args.triggerFlags.HasAnyFlag(TriggerCastFlags.IgnoreCastInProgress)) {
            return SpellCastResult.SpellInProgress;
        }

        return getMe().castSpell(victim, spellId, args);
    }


    public final SpellCastResult doCastSelf(int spellId) {
        return doCastSelf(spellId, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SpellCastResult DoCastSelf(uint spellId, CastSpellExtraArgs args = null)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final SpellCastResult doCastSelf(int spellId, CastSpellExtraArgs args) {
        return doCast(getMe(), spellId, args);
    }


    public final SpellCastResult doCastVictim(int spellId) {
        return doCastVictim(spellId, null);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SpellCastResult DoCastVictim(uint spellId, CastSpellExtraArgs args = null)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SpellCastResult DoCastAOE(uint spellId, CastSpellExtraArgs args = null)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public final SpellCastResult doCastAOE(int spellId, CastSpellExtraArgs args) {
        return doCast(null, spellId, args);
    }

    public boolean canAIAttack(Unit victim) {
        return true;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void UpdateAI(uint diff)
    public void updateAI(int diff) {
    }

    public void initializeAI() {
        if (!getMe().isDead()) {
            reset();
        }
    }

    public void reset() {
    }

    /** 
    */
    // Called when unit's charm state changes with isNew = false
    // Implementation should call me->ScheduleAIChange() if AI replacement is desired
    // If this call is made, AI will be replaced on the next tick
    // When replacement is made, OnCharmed is called with isNew = true
    /** 
     @param apply 
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual uint GetData(uint id = 0)
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
    public int getData(int id) {
        return 0;
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void SetData(uint id, uint value)
    public void setData(int id, int value) {
    }

    public void setGUID(ObjectGuid guid) {
        setGUID(guid, 0);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual void SetGUID(ObjectGuid guid, int id = 0)
    public void setGUID(ObjectGuid guid, int id) {
    }


    public ObjectGuid getGUID() {
        return getGUID(0);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual ObjectGuid GetGUID(int id = 0)
    public ObjectGuid getGUID(int id) {
        return ObjectGuid.empty;
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

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual void DamageTaken(Unit attacker, ref double damage, DamageEffectType damageType, SpellInfo spellInfo = null)
    public void damageTaken(Unit attacker, tangible.RefObject<Double> damage, DamageEffectType damageType, SpellInfo spellInfo) {
    }
    public void healReceived(Unit by, double addhealth) {
    }
    public void healDone(Unit to, double addhealth) {
    }
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void SpellInterrupted(uint spellId, uint unTimeMs)
    public void spellInterrupted(int spellId, int unTimeMs) {
    }

    /** 
      Called when a game event starts or ends
    */
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public virtual void OnGameEvent(bool start, ushort eventId)
    public void onGameEvent(boolean start, short eventId) {
    }

    public String getDebugInfo() {
        return String.format("Me: %1$s", (getMe() != null ? getMe().getDebugInfo() : "NULL"));
    }

    public static void fillAISpellInfo() {
        Global.getSpellMgr().forEachSpellInfo(spellInfo -> {
                AISpellInfoType aIInfo = new AISpellInfoType();

                if (spellInfo.HasAttribute(SpellAttr0.AllowCastWhileDead)) {
                    aIInfo.condition = AICondition.Die;
                } else if (spellInfo.IsPassive || spellInfo.Duration == -1) {
                    aIInfo.condition = AICondition.Aggro;
                } else {
                    aIInfo.condition = AICondition.Combat;
                }

                if (aIInfo.cooldown.getTotalMilliseconds() < spellInfo.RecoveryTime) {
                    aIInfo.cooldown = TimeSpan.FromMilliseconds(spellInfo.RecoveryTime);
                }

                if (spellInfo.GetMaxRange(false) != 0) {
                    for (var spellEffectInfo : spellInfo.Effects) {
                        var targetType = spellEffectInfo.TargetA.Target;

                        if (targetType == Targets.UnitTargetEnemy || targetType == Targets.DestTargetEnemy) {
                            if (aIInfo.target.getValue() < AITarget.Victim.getValue()) {
                                aIInfo.target = AITarget.Victim;
                            }
                        } else if (targetType == Targets.UnitDestAreaEnemy) {
                            if (aIInfo.target.getValue() < AITarget.Enemy.getValue()) {
                                aIInfo.target = AITarget.Enemy;
                            }
                        }

                        if (spellEffectInfo.IsEffect(SpellEffectName.ApplyAura)) {
                            if (targetType == Targets.UnitTargetEnemy) {
                                if (aIInfo.target.getValue() < AITarget.Debuff.getValue()) {
                                    aIInfo.target = AITarget.Debuff;
                                }
                            } else if (spellInfo.IsPositive) {
                                if (aIInfo.target.getValue() < AITarget.Buff.getValue()) {
                                    aIInfo.target = AITarget.Buff;
                                }
                            }
                        }
                    }
                }

                aIInfo.realCooldown = TimeSpan.FromMilliseconds(spellInfo.RecoveryTime + spellInfo.StartRecoveryTime);
                aIInfo.maxRange = spellInfo.GetMaxRange(false) * 3 / 4;

                aIInfo.effects = 0;
                aIInfo.targets = 0;

                for (var spellEffectInfo : spellInfo.Effects) {
                    // Spell targets self.
                    if (spellEffectInfo.TargetA.Target == Targets.UnitCaster) {
                        aIInfo.targets |= 1 << (SelectTargetType.Self.getValue() - 1);
                    }

                    // Spell targets a single enemy.
                    if (spellEffectInfo.TargetA.Target == Targets.UnitTargetEnemy || spellEffectInfo.TargetA.Target == Targets.DestTargetEnemy) {
                        aIInfo.targets |= 1 << (SelectTargetType.SingleEnemy.getValue() - 1);
                    }

                    // Spell targets AoE at enemy.
                    if (spellEffectInfo.TargetA.Target == Targets.UnitSrcAreaEnemy || spellEffectInfo.TargetA.Target == Targets.UnitDestAreaEnemy || spellEffectInfo.TargetA.Target == Targets.SrcCaster || spellEffectInfo.TargetA.Target == Targets.DestDynobjEnemy) {
                        aIInfo.targets |= 1 << (SelectTargetType.AoeEnemy.getValue() - 1);
                    }

                    // Spell targets an enemy.
                    if (spellEffectInfo.TargetA.Target == Targets.UnitTargetEnemy || spellEffectInfo.TargetA.Target == Targets.DestTargetEnemy || spellEffectInfo.TargetA.Target == Targets.UnitSrcAreaEnemy || spellEffectInfo.TargetA.Target == Targets.UnitDestAreaEnemy || spellEffectInfo.TargetA.Target == Targets.SrcCaster || spellEffectInfo.TargetA.Target == Targets.DestDynobjEnemy) {
                        aIInfo.targets |= 1 << (SelectTargetType.AnyEnemy.getValue() - 1);
                    }

                    // Spell targets a single friend (or self).
                    if (spellEffectInfo.TargetA.Target == Targets.UnitCaster || spellEffectInfo.TargetA.Target == Targets.UnitTargetAlly || spellEffectInfo.TargetA.Target == Targets.UnitTargetParty) {
                        aIInfo.targets |= 1 << (SelectTargetType.SingleFriend.getValue() - 1);
                    }

                    // Spell targets AoE friends.
                    if (spellEffectInfo.TargetA.Target == Targets.UnitCasterAreaParty || spellEffectInfo.TargetA.Target == Targets.UnitLastTargetAreaParty || spellEffectInfo.TargetA.Target == Targets.SrcCaster) {
                        aIInfo.targets |= 1 << (SelectTargetType.AoeFriend.getValue() - 1);
                    }

                    // Spell targets any friend (or self).
                    if (spellEffectInfo.TargetA.Target == Targets.UnitCaster || spellEffectInfo.TargetA.Target == Targets.UnitTargetAlly || spellEffectInfo.TargetA.Target == Targets.UnitTargetParty || spellEffectInfo.TargetA.Target == Targets.UnitCasterAreaParty || spellEffectInfo.TargetA.Target == Targets.UnitLastTargetAreaParty || spellEffectInfo.TargetA.Target == Targets.SrcCaster) {
                        aIInfo.targets |= 1 << (SelectTargetType.AnyFriend.getValue() - 1);
                    }

                    // Make sure that this spell includes a damage effect.
                    if (spellEffectInfo.Effect == SpellEffectName.SchoolDamage || spellEffectInfo.Effect == SpellEffectName.Instakill || spellEffectInfo.Effect == SpellEffectName.EnvironmentalDamage || spellEffectInfo.Effect == SpellEffectName.HealthLeech) {
                        aIInfo.effects |= 1 << (SelectEffect.Damage.getValue() - 1);
                    }

                    // Make sure that this spell includes a healing effect (or an apply aura with a periodic heal).
                    if (spellEffectInfo.Effect == SpellEffectName.Heal || spellEffectInfo.Effect == SpellEffectName.HealMaxHealth || spellEffectInfo.Effect == SpellEffectName.HealMechanical || (spellEffectInfo.Effect == SpellEffectName.ApplyAura && spellEffectInfo.ApplyAuraName == AuraType.PeriodicHeal)) {
                        aIInfo.effects |= 1 << (SelectEffect.Healing.getValue() - 1);
                    }

                    // Make sure that this spell applies an aura.
                    if (spellEffectInfo.Effect == SpellEffectName.ApplyAura) {
                        aIInfo.effects |= 1 << (SelectEffect.Aura.getValue() - 1);
                    }
                }

                aiSpellInfo.put((spellInfo.Id, spellInfo.Difficulty), aIInfo);
        });
    }

//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: public static AISpellInfoType GetAISpellInfo(uint spellId, Difficulty difficulty)
    public static AISpellInfoType getAISpellInfo(int spellId, Difficulty difficulty) {
        return aiSpellInfo.LookupByKey((spellId, difficulty));
    }

    private void sortByDistance(ArrayList<Unit> targets, boolean ascending) {
        Collections.sort(targets, new ObjectDistanceOrderPred(getMe(), ascending));
    }
}