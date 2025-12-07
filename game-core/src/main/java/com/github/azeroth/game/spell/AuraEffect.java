package com.github.azeroth.game.spell;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.defines.SpellAttr10;
import com.github.azeroth.defines.SpellAttr5;
import com.github.azeroth.defines.SpellAttr8;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.domain.creature.CreatureModel;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.item.ItemPosCount;
import com.github.azeroth.game.entity.player.enums.PlayerSpellState;
import com.github.azeroth.game.entity.player.model.SpellModifier;
import com.github.azeroth.game.entity.player.model.SpellModifierByClassMask;
import com.github.azeroth.game.entity.player.model.SpellPctModifierByLabel;
import com.github.azeroth.game.entity.unit.*;
import com.github.azeroth.game.map.AnyUnfriendlyUnitInObjectRangeCheck;
import com.github.azeroth.game.map.UnitListSearcher;
import com.github.azeroth.game.map.grid.Cell;

import com.github.azeroth.game.spell.auras.enums.AuraEffectHandleMode;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.auras.enums.ShapeShiftForm;
import com.github.azeroth.game.spell.enums.SpellModOp;
import com.github.azeroth.utils.MathUtil;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.BitSet;

@Getter
@Setter
public class AuraEffect {
    private final Aura auraBase;
    private final SpellInfo spellInfo;
    private final SpellEffectInfo effectInfo;
    public float baseAmount;
    private SpellModifier spellModifier;
    private float amount;
    private Double estimatedAmount = null; // for periodic damage and healing auras this will include damage done bonuses

    // periodic stuff
    private int periodicTimer;
    private int period; // time between consecutive ticks
    private int ticksDone; // ticks counter

    private boolean canBeRecalculated;
    private boolean isPeriodic;

    public AuraEffect(Aura baseAura, SpellEffectInfo spellEfffectInfo, Double baseAmount, Unit caster) {
        auraBase = baseAura;
        spellInfo = baseAura.getSpellInfo();
        effectInfo = spellEfffectInfo;
        baseAmount = baseAmount != null ? baseAmount.doubleValue() : effectInfo.calcBaseValue(caster, baseAura.getAuraObjType() == AuraObjectType.Unit ? baseAura.getOwner().toUnit() : null, baseAura.getCastItemId(), baseAura.getCastItemLevel());
        canBeRecalculated = true;
        isPeriodic = false;

        calculatePeriodic(caster, true, false);
        amount = calculateAmount(caster);
        calculateSpellMod();
    }

    public final Unit getCaster() {
        return auraBase.getCaster();
    }

    public final ObjectGuid getCasterGuid() {
        return auraBase.getCasterGuid();
    }

    public final Aura getBase() {
        return auraBase;
    }

    public final SpellInfo getSpellInfo() {
        return spellInfo;
    }

    public final int getId() {
        return spellInfo.getId();
    }

    public final int getEffIndex() {
        return effectInfo.effectIndex;
    }

    public final int getPeriod() {
        return period;
    }

    public final void setPeriod(int value) {
        period = value;
    }

    public final int getMiscValueB() {
        return effectInfo.miscValueB;
    }

    public final int getMiscValue() {
        return effectInfo.miscValue;
    }

    public final AuraType getAuraType() {
        return effectInfo.applyAuraName;
    }


    public final void setAmount(float amount) {
        this.amount = amount;
        canBeRecalculated = false;
    }

    public final float calculateAmount(Unit caster) {
        // default amount calculation
        float amount = 0;

        if (!spellInfo.hasAttribute(SpellAttr8.MASTERY_AFFECTS_POINTS) || MathUtil.fuzzyEq(getSpellEffectInfo().bonusCoefficient, 0.0f)) {
            amount = getSpellEffectInfo().calcValue(caster, baseAmount, getBase().getOwner().toUnit(), getBase().getCastItemId(), getBase().getCastItemLevel());
        } else if (caster != null && caster.isTypeId(TypeId.PLAYER)) {
            amount = caster.toPlayer().getActivePlayerData().Mastery * getSpellEffectInfo().bonusCoefficient;
        }

        // custom amount calculations go here
        switch (getAuraType()) {
            // crowd control auras
            case MOD_CONFUSE:
            case MOD_FEAR:
            case MOD_STUN:
            case MOD_ROOT:
            case TRANSFORM:
            case MOD_ROOT_2:
                canBeRecalculated = false;

                if (spellInfo.getProcFlags() == null) {
                    break;
                }

                amount = (int) (getBase().getOwnerAsUnit().countPctFromMaxHealth(10));

                break;
            case SCHOOL_ABSORB:
            case MANA_SHIELD:
                canBeRecalculated = false;

                break;
            case MOUNTED:
                var mountType = (int) getMiscValueB();
                var mountEntry = global.getDB2Mgr().GetMount(getId());

                if (mountEntry != null) {
                    mountType = mountEntry.MountTypeID;
                }

                var mountCapability = getBase().getOwnerAsUnit().getMountCapability(mountType);

                if (mountCapability != null) {
                    amount = (int) mountCapability.id;
                }

                break;
            case SHOW_CONFIRMATION_PROMPT_WITH_DIFFICULTY:
                if (caster != null) {
                    amount = caster.getMap().getDifficultyID().getValue();
                }

                canBeRecalculated = false;

                break;
            default:
                break;
        }

        if (getSpellInfo().hasAttribute(SpellAttr10.ROLLING_PERIODIC)) {
            var periodicAuras = getBase().getOwnerAsUnit().getAuraEffectsByType(getAuraType());

            amount = periodicAuras.Aggregate(0d, (val, aurEff) ->
            {
                if (Objects.equals(aurEff.casterGuid, getCasterGuid()) && aurEff.id == getId() && aurEff.effIndex == getEffIndex() && aurEff.getTotalTicks() > 0) {
                    val += aurEff.Amount * aurEff.getRemainingTicks() / aurEff.getTotalTicks();
                }

                return val;
            });
        }

        tangible.RefObject<Double> tempRef_amount = new tangible.RefObject<Double>(amount);
        tangible.RefObject<Boolean> tempRef__canBeRecalculated = new tangible.RefObject<Boolean>(canBeRecalculated);
        getBase().callScriptEffectCalcAmountHandlers(this, tempRef_amount, tempRef__canBeRecalculated);
        canBeRecalculated = tempRef__canBeRecalculated.refArgValue;
        amount = tempRef_amount.refArgValue;

        if (!getSpellEffectInfo().effectAttributes.hasFlag(SpellEffectAttributes.NoScaleWithStack)) {
            amount *= getBase().getStackAmount();
        }

        if (caster && getBase().getAuraObjType() == AuraObjectType.unit) {
            var stackAmountForBonuses = !getSpellEffectInfo().effectAttributes.hasFlag(SpellEffectAttributes.NoScaleWithStack) ? getBase().getStackAmount() : 1;

            switch (getAuraType()) {
                case PERIODIC_DAMAGE:
                case PERIODIC_LEECH:
                    estimatedAmount = caster.spellDamageBonusDone(getBase().getOwnerAsUnit(), getSpellInfo(), amount, DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);

                    break;
                case PERIODIC_HEAL:
                    estimatedAmount = caster.spellHealingBonusDone(getBase().getOwnerAsUnit(), getSpellInfo(), amount, DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);

                    break;
                default:
                    break;
            }
        }

        return amount;
    }

    public final int getTotalTicks() {
        int totalTicks = 0;

        if (period != 0 && !getBase().isPermanent()) {
            totalTicks = (int) (getBase().getMaxDuration() / period);

            if (spellInfo.hasAttribute(SpellAttr5.EXTRA_INITIAL_PERIOD)) {
                ++totalTicks;
            }
        }

        return totalTicks;
    }

    public final void calculatePeriodic(Unit caster, boolean resetPeriodicTimer) {
        calculatePeriodic(caster, resetPeriodicTimer, false);
    }

    public final void calculatePeriodic(Unit caster) {
        calculatePeriodic(caster, true, false);
    }

    public final void calculatePeriodic(Unit caster, boolean resetPeriodicTimer, boolean load) {
        period = (int) getSpellEffectInfo().applyAuraPeriod;

        // prepare periodics
        switch (getAuraType()) {
            case OBS_MOD_POWER:
            case PERIODIC_DAMAGE:
            case PERIODIC_HEAL:
            case OBS_MOD_HEALTH:
            case PERIODIC_TRIGGER_SPELL:
            case PERIODIC_TRIGGER_SPELL_FROM_CLIENT:
            case PERIODIC_ENERGIZE:
            case PERIODIC_LEECH:
            case PERIODIC_HEALTH_FUNNEL:
            case PERIODIC_MANA_LEECH:
            case PERIODIC_DAMAGE_PERCENT:
            case POWER_BURN:
            case PERIODIC_DUMMY:
            case PERIODIC_TRIGGER_SPELL_WITH_VALUE:
                isPeriodic = true;

                break;
            default:
                break;
        }

        tangible.RefObject<Boolean> tempRef__isPeriodic = new tangible.RefObject<Boolean>(isPeriodic);
        tangible.RefObject<Integer> tempRef__period = new tangible.RefObject<Integer>(period);
        getBase().callScriptEffectCalcPeriodicHandlers(this, tempRef__isPeriodic, tempRef__period);
        period = tempRef__period.refArgValue;
        isPeriodic = tempRef__isPeriodic.refArgValue;

        if (!isPeriodic) {
            return;
        }

        var modOwner = caster != null ? caster.getSpellModOwner() : null;

        // Apply casting time mods
        if (period != 0) {
            // Apply periodic time mod
            if (modOwner != null) {
                tangible.RefObject<Integer> tempRef__period2 = new tangible.RefObject<Integer>(period);
                modOwner.applySpellMod(getSpellInfo(), SpellModOp.period, tempRef__period2);
                period = tempRef__period2.refArgValue;
            }

            if (caster != null) {
                // Haste modifies periodic time of channeled spells
                if (spellInfo.isChanneled()) {
                    tangible.RefObject<Integer> tempRef__period3 = new tangible.RefObject<Integer>(period);
                    caster.modSpellDurationTime(spellInfo, tempRef__period3);
                    period = tempRef__period3.refArgValue;
                } else if (spellInfo.hasAttribute(SpellAttr5.SpellHasteAffectsPeriodic)) {
                    period = (int) (_period * caster.getUnitData().modCastingSpeed);
                }
            }
        } else // prevent infinite loop on Update
        {
            isPeriodic = false;
        }

        if (load) // aura loaded from db
        {
            if (period != 0 && !getBase().isPermanent()) {
                var elapsedTime = (int) (getBase().getMaxDuration() - getBase().getDuration());
                ticksDone = elapsedTime / (int) period;
                periodicTimer = (int) (elapsedTime % period);
            }

            if (spellInfo.hasAttribute(SpellAttr5.ExtraInitialPeriod)) {
                ++ticksDone;
            }
        } else // aura just created or reapplied
        {
            // reset periodic timer on aura create or reapply
            // we don't reset periodic timers when aura is triggered by proc
            resetPeriodic(resetPeriodicTimer);
        }
    }

    public final void calculateSpellMod() {
        switch (getAuraType()) {
            case ADD_FLAT_MODIFIER:
            case ADD_PCT_MODIFIER:
                if (spellModifier == null) {
                    SpellModifierByClassMask spellmod = new SpellModifierByClassMask(getBase());
                    spellmod.setOp(SpellModOp.forValue(getMiscValue()));

                    spellmod.setType(getAuraType() == AuraType.AddPctModifier ? SpellModType.Pct : SpellModType.Flat);
                    spellmod.setSpellId(getId());
                    spellmod.mask = getSpellEffectInfo().spellClassMask;
                    spellModifier = spellmod;
                }

                (_spellModifier instanceof SpellModifierByClassMask ? (SpellModifierByClassMask) _spellModifier : null).value = getAmount();

                break;
            case ADD_FLAT_MODIFIER_BY_SPELL_LABEL:
                if (spellModifier == null) {
                    SpellFlatModifierByLabel spellmod = new SpellFlatModifierByLabel(getBase());
                    spellmod.setOp(SpellModOp.forValue(getMiscValue()));

                    spellmod.setType(SpellModType.LabelFlat);
                    spellmod.setSpellId(getId());
                    spellmod.value.modIndex = getMiscValue();
                    spellmod.value.labelID = getMiscValueB();
                    spellModifier = spellmod;
                }

                (_spellModifier instanceof SpellFlatModifierByLabel ? (SpellFlatModifierByLabel) _spellModifier : null).value.modifierValue = getAmount();

                break;
            case ADD_PCT_MODIFIER_BY_SPELL_LABEL:
                if (spellModifier == null) {
                    SpellPctModifierByLabel spellmod = new SpellPctModifierByLabel(getBase());
                    spellmod.setOp(SpellModOp.forValue(getMiscValue()));

                    spellmod.setType(SpellModType.LabelPct);
                    spellmod.setSpellId(getId());
                    spellmod.value.modIndex = getMiscValue();
                    spellmod.value.labelID = getMiscValueB();
                    spellModifier = spellmod;
                }

                (_spellModifier instanceof SpellPctModifierByLabel ? (SpellPctModifierByLabel) _spellModifier : null).value.modifierValue = 1.0f + MathUtil.CalculatePct(1.0f, getAmount());

                break;
            default:
                break;
        }

        getBase().callScriptEffectCalcSpellModHandlers(this, spellModifier);
    }

    public final void changeAmount(double newAmount, boolean mark, boolean onStackOrReapply) {
        changeAmount(newAmount, mark, onStackOrReapply, null);
    }

    public final void changeAmount(double newAmount, boolean mark) {
        changeAmount(newAmount, mark, false, null);
    }

    public final void changeAmount(double newAmount) {
        changeAmount(newAmount, true, false, null);
    }

    public final void changeAmount(double newAmount, boolean mark, boolean onStackOrReapply, AuraEffect triggeredBy) {
        // Reapply if amount change
        AuraEffectHandleMode handleMask = AuraEffectHandleMode.forValue(0);

        if (newAmount != getAmount()) {
            handleMask = AuraEffectHandleModes.forValue(handleMask.getValue() | AuraEffectHandleModes.ChangeAmount.getValue());
        }

        if (onStackOrReapply) {
            handleMask = AuraEffectHandleModes.forValue(handleMask.getValue() | AuraEffectHandleModes.Reapply.getValue());
        }

        if (handleMask == 0) {
            return;
        }

        ArrayList<AuraApplication> effectApplications;
        tangible.OutObject<ArrayList<AuraApplication>> tempOut_effectApplications = new tangible.OutObject<ArrayList<AuraApplication>>();
        getApplicationList(tempOut_effectApplications);
        effectApplications = tempOut_effectApplications.outArgValue;

        for (var aurApp : effectApplications) {
            aurApp.getTarget()._RegisterAuraEffect(this, false);
            handleEffect(aurApp, handleMask, false, triggeredBy);
        }

        if ((boolean) (handleMask.getValue() & AuraEffectHandleModes.ChangeAmount.getValue())) {
            if (!mark) {
                amount = newAmount;
            } else {
                setAmount(newAmount);
            }

            calculateSpellMod();
        }

        for (var aurApp : effectApplications) {
            if (aurApp.getRemoveMode() != AuraRemoveMode.NONE) {
                continue;
            }

            aurApp.getTarget()._RegisterAuraEffect(this, true);
            handleEffect(aurApp, handleMask, true, triggeredBy);
        }

        if (getSpellInfo().hasAttribute(SpellAttr8.AuraSendAmount) || aura.effectTypeNeedsSendingAmount(getAuraType())) {
            getBase().setNeedClientUpdateForTargets();
        }
    }

    public final void handleEffect(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        handleEffect(aurApp, mode, apply, null);
    }

    public final void handleEffect(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply, AuraEffect triggeredBy) {
        // register/unregister effect in lists in case of real AuraEffect apply/remove
        // registration/unregistration is done always before real effect handling (some effect handlers code is depending on this)
        if (mode.hasFlag(AuraEffectHandleModes.Real)) {
            aurApp.getTarget()._RegisterAuraEffect(this, apply);
        }

        // real aura apply/remove, handle modifier
        if (mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            applySpellMod(aurApp.getTarget(), apply, triggeredBy);
        }

        // call scripts helping/replacing effect handlers
        boolean prevented;

        if (apply) {
            prevented = getBase().callScriptEffectApplyHandlers(this, aurApp, mode);
        } else {
            prevented = getBase().callScriptEffectRemoveHandlers(this, aurApp, mode);
        }

        // check if script events have removed the aura already
        if (apply && aurApp.getHasRemoveMode()) {
            return;
        }

        // call default effect handler if it wasn't prevented
        if (!prevented) {
            global.getSpellMgr().getAuraEffectHandler(getAuraType()).invoke(this, aurApp, mode, apply);
        }

        // check if the default handler reemoved the aura
        if (apply && aurApp.getHasRemoveMode()) {
            return;
        }

        // call scripts triggering additional events after apply/remove
        if (apply) {
            getBase().callScriptAfterEffectApplyHandlers(this, aurApp, mode);
        } else {
            getBase().callScriptAfterEffectRemoveHandlers(this, aurApp, mode);
        }
    }

    public final void handleEffect(Unit target, AuraEffectHandleModes mode, boolean apply) {
        handleEffect(target, mode, apply, null);
    }

    public final void handleEffect(Unit target, AuraEffectHandleModes mode, boolean apply, AuraEffect triggeredBy) {
        var aurApp = getBase().getApplicationOfTarget(target.getGUID());
        handleEffect(aurApp, mode, apply, triggeredBy);
    }

    public final void update(int diff, Unit caster) {
        if (!isPeriodic || (getBase().getDuration() < 0 && !getBase().isPassive() && !getBase().isPermanent())) {
            return;
        }

        var totalTicks = getTotalTicks();

        periodicTimer += (int) diff;

        while (periodicTimer >= period) {
            _periodicTimer -= period;

            if (!getBase().isPermanent() && (ticksDone + 1) > totalTicks) {
                break;
            }

            ++ticksDone;

            getBase().callScriptEffectUpdatePeriodicHandlers(this);

            ArrayList<AuraApplication> effectApplications;
            tangible.OutObject<ArrayList<AuraApplication>> tempOut_effectApplications = new tangible.OutObject<ArrayList<AuraApplication>>();
            getApplicationList(tempOut_effectApplications);
            effectApplications = tempOut_effectApplications.outArgValue;

            // tick on targets of effects
            for (var appt : effectApplications) {
                periodicTick(appt, caster);
            }
        }
    }

    public final double getCritChanceFor(Unit caster, Unit target) {
        return target.spellCritChanceTaken(caster, null, this, getSpellInfo().getSchoolMask(), calcPeriodicCritChance(caster), getSpellInfo().getAttackType());
    }

    public final boolean isAffectingSpell(SpellInfo spell) {
        if (spell == null) {
            return false;
        }

        // Check family name and EffectClassMask
        if (!spell.isAffected(spellInfo.getSpellFamilyName(), getSpellEffectInfo().spellClassMask)) {
            return false;
        }

        return true;
    }

    public final boolean checkEffectProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var result = getBase().callScriptCheckEffectProcHandlers(this, aurApp, eventInfo);

        if (!result) {
            return false;
        }

        var spellInfo = eventInfo.getSpellInfo();

        switch (getAuraType()) {
            case MOD_CONFUSE:
            case MOD_FEAR:
            case MOD_STUN:
            case MOD_ROOT:
            case TRANSFORM: {
                var damageInfo = eventInfo.getDamageInfo();

                if (damageInfo == null || damageInfo.getDamage() == 0) {
                    return false;
                }

                // Spell own damage at apply won't break CC
                if (spellInfo != null && spellInfo == getSpellInfo()) {
                    var aura = getBase();

                    // called from spellcast, should not have ticked yet
                    if (aura.getDuration() == aura.getMaxDuration()) {
                        return false;
                    }
                }

                break;
            }
            case MECHANIC_IMMUNITY:
            case MOD_MECHANIC_RESISTANCE:
                // compare mechanic
                if (spellInfo == null || (spellInfo.getAllEffectsMechanicMask() & (1 << getMiscValue())) == 0) {
                    return false;
                }

                break;
            case MOD_CASTING_SPEED_NOT_STACK:
                // skip melee hits and instant cast spells
                if (!eventInfo.getProcSpell() || eventInfo.getProcSpell().getCastTime() == 0) {
                    return false;
                }

                break;
            case MOD_SCHOOL_MASK_DAMAGE_FROM_CASTER:
            case MOD_SPELL_DAMAGE_FROM_CASTER:
                // Compare casters
                if (ObjectGuid.opNotEquals(getCasterGuid(), eventInfo.getActor().getGUID())) {
                    return false;
                }

                break;
            case MOD_POWER_COST_SCHOOL:
            case MOD_POWER_COST_SCHOOL_PCT: {
                // Skip melee hits and spells with wrong school or zero cost
                if (spellInfo == null || !(boolean) (spellInfo.getSchoolMask().getValue() & getMiscValue()) || !eventInfo.getProcSpell()) {
                    return false;
                }

                // Costs Check
                var costs = eventInfo.getProcSpell().getPowerCost();
                var m = tangible.ListHelper.find(costs, cost -> cost.amount > 0);

                if (m == null) {
                    return false;
                }

                break;
            }
            case REFLECT_SPELLS_SCHOOL:
                // Skip melee hits and spells with wrong school
                if (spellInfo == null || !(boolean) (spellInfo.getSchoolMask().getValue() & getMiscValue())) {
                    return false;
                }

                break;
            case PROC_TRIGGER_SPELL:
            case PROC_TRIGGER_SPELL_WITH_VALUE: {
                // Don't proc extra attacks while already processing extra attack spell
                var triggerSpellId = getSpellEffectInfo().triggerSpell;
                var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpellId, getBase().getCastDifficulty());

                if (triggeredSpellInfo != null) {
                    if (triggeredSpellInfo.hasEffect(SpellEffectName.AddExtraAttacks)) {
                        var lastExtraAttackSpell = eventInfo.getActor().getLastExtraAttackSpell();

                        // Patch 1.12.0(?) extra attack abilities can no longer chain proc themselves
                        if (lastExtraAttackSpell == triggerSpellId) {
                            return false;
                        }
                    }
                }

                break;
            }
            case MOD_SPELL_CRIT_CHANCE:
                // skip spells that can't crit
                if (spellInfo == null || !spellInfo.hasAttribute(SpellCustomAttributes.CanCrit)) {
                    return false;
                }

                break;
            default:
                break;
        }

        return result;
    }

    public final void handleProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var prevented = getBase().callScriptEffectProcHandlers(this, aurApp, eventInfo);

        if (prevented) {
            return;
        }

        switch (getAuraType()) {
            // CC Auras which use their amount to drop
            // Are there any more auras which need this?
            case MOD_CONFUSE:
            case MOD_FEAR:
            case MOD_STUN:
            case MOD_ROOT:
            case TRANSFORM:
            case MOD_ROOT_2:
                handleBreakableCCAuraProc(aurApp, eventInfo);

                break;
            case DUMMY:
            case PROC_TRIGGER_SPELL:
                handleProcTriggerSpellAuraProc(aurApp, eventInfo);

                break;
            case PROC_TRIGGER_SPELL_WITH_VALUE:
                handleProcTriggerSpellWithValueAuraProc(aurApp, eventInfo);

                break;
            case PROC_TRIGGER_DAMAGE:
                handleProcTriggerDamageAuraProc(aurApp, eventInfo);

                break;
            default:
                break;
        }

        getBase().callScriptAfterEffectProcHandlers(this, aurApp, eventInfo);
    }

    public final void handleShapeshiftBoosts(Unit target, boolean apply) {
        int spellId = 0;
        int spellId2 = 0;
        int spellId3 = 0;
        int spellId4 = 0;

        switch (ShapeShiftForm.values()[getMiscValue()]) {
            case FORM_CAT_FORM:
                spellId = 3025;
                spellId2 = 48629;
                spellId3 = 106840;
                spellId4 = 113636;

                break;
            case FORM_TREE_OF_LIFE:
                spellId = 5420;
                spellId2 = 81097;

                break;
            case FORM_TRAVEL_FORM:
                spellId = 5419;

                break;
            case FORM_AQUATIC_FORM:
                spellId = 5421;

                break;
            case FORM_BEAR_FORM:
                spellId = 1178;
                spellId2 = 21178;
                spellId3 = 106829;
                spellId4 = 106899;

                break;
            case FORM_FLIGHT_FORM:
                spellId = 33948;
                spellId2 = 34764;

                break;
            case FORM_FLIGHT_FORM_EPIC:
                spellId = 40122;
                spellId2 = 40121;

                break;
            case FORM_SPIRIT_OF_REDEMPTION:
                spellId = 27792;
                spellId2 = 27795;
                spellId3 = 62371;

                break;
            case FORM_SHADOWFORM:
                if (target.hasAura(107906)) // Glyph of Shadow
                {
                    spellId = 107904;
                } else if (target.hasAura(126745)) // Glyph of Shadowy Friends
                {
                    spellId = 142024;
                } else {
                    spellId = 107903;
                }

                break;
            case FORM_GHOST_WOLF:
                if (target.hasAura(58135)) // Glyph of Spectral Wolf
                {
                    spellId = 160942;
                }

                break;
            default:
                break;
        }

        if (apply) {
            if (spellId != 0) {
                target.castSpell(target, spellId, new CastSpellExtraArgs(this));
            }

            if (spellId2 != 0) {
                target.castSpell(target, spellId2, new CastSpellExtraArgs(this));
            }

            if (spellId3 != 0) {
                target.castSpell(target, spellId3, new CastSpellExtraArgs(this));
            }

            if (spellId4 != 0) {
                target.castSpell(target, spellId4, new CastSpellExtraArgs(this));
            }

            if (target.isTypeId(TypeId.PLAYER)) {
                var plrTarget = target.toPlayer();

                var sp_list = plrTarget.getSpellMap();

                for (var pair : sp_list.entrySet()) {
                    if (pair.getValue().state == PlayerSpellState.REMOVED || pair.getValue().disabled) {
                        continue;
                    }

                    if (pair.getKey() == spellId || pair.getKey() == spellId2 || pair.getKey() == spellId3 || pair.getKey() == spellId4) {
                        continue;
                    }

                    var spellInfo = global.getSpellMgr().getSpellInfo(pair.getKey(), Difficulty.NONE);

                    if (spellInfo == null || !(spellInfo.isPassive || spellInfo.hasAttribute(SpellAttr0.DoNotDisplaySpellbookAuraIconCombatLog))) {
                        continue;
                    }

                    if ((boolean) (spellInfo.stances & (1 << (getMiscValue() - 1)))) {
                        target.castSpell(target, pair.getKey(), new CastSpellExtraArgs(this));
                    }
                }
            }
        } else {
            if (spellId != 0) {
                target.removeOwnedAura(spellId, target.getGUID());
            }

            if (spellId2 != 0) {
                target.removeOwnedAura(spellId2, target.getGUID());
            }

            if (spellId3 != 0) {
                target.removeOwnedAura(spellId3, target.getGUID());
            }

            if (spellId4 != 0) {
                target.removeOwnedAura(spellId4, target.getGUID());
            }

            var shapeshifts = target.getAuraEffectsByType(AuraType.MOD_SHAPESHIFT);
            AuraEffect newAura = null;

            // Iterate through all the shapeshift auras that the target has, if there is another aura with SPELL_AURA_MOD_SHAPESHIFT, then this aura is being removed due to that one being applied
            for (var eff : shapeshifts) {
                if (eff != this) {
                    newAura = eff;

                    break;
                }
            }

            target.getAppliedAuras().CallOnMatch((app) ->
            {
                if (app == null) {
                    return false;
                }

                // Use the new aura to see on what stance the target will be
                var newStance = newAura != null ? (1 << (newAura.getMiscValue() - 1)) : 0;

                // If the stances are not compatible with the spell, remove it
                if (app.base.isRemovedOnShapeLost(target) && !(boolean) (app.base.spellInfo.stances & newStance)) {
                    return true;
                }

                return false;
            }, (app) -> target.removeAura(app));
        }
    }

    public final boolean hasAmount() {
        return amount != 0;
    }

    public final void modAmount(double amount) {
        amount += amount;
        canBeRecalculated = false;
    }

    public final void modAmount(long amount) {
        modAmount((double) amount);
    }

    public final void modAmount(int amount) {
        modAmount((double) amount);
    }

    public final void modAmount(int amount) {
        modAmount((double) amount);
    }

    public final Double getEstimatedAmount() {
        return estimatedAmount;
    }

    public final boolean tryGetEstimatedAmount(tangible.OutObject<Double> amount) {
        amount.outArgValue = estimatedAmount != null ? estimatedAmount.doubleValue() : 0;

        return estimatedAmount != null;
    }

    public final int getPeriodicTimer() {
        return periodicTimer;
    }

    public final void setPeriodicTimer(int periodicTimer) {
        periodicTimer = periodicTimer;
    }


    public final void recalculateAmount() {
        recalculateAmount(null);
    }

    public final void recalculateAmount(AuraEffect triggeredBy) {
        if (!canBeRecalculated()) {
            return;
        }

        changeAmount(calculateAmount(getCaster()), false, false, triggeredBy);
    }


    public final void recalculateAmount(Unit caster) {
        recalculateAmount(caster, null);
    }

    public final void recalculateAmount(Unit caster, AuraEffect triggeredBy) {
        if (!canBeRecalculated()) {
            return;
        }

        changeAmount(calculateAmount(caster), false, false, triggeredBy);
    }

    public final boolean canBeRecalculated() {
        return canBeRecalculated;
    }

    public final void setCanBeRecalculated(boolean val) {
        canBeRecalculated = val;
    }

    public final void resetTicks() {
        ticksDone = 0;
    }

    public final int getTickNumber() {
        return ticksDone;
    }

    public final int getRemainingTicks() {
        return getTotalTicks() - ticksDone;
    }


    public final double getRemainingAmount() {
        return getRemainingAmount(0);
    }

    public final double getRemainingAmount(int maxDurationIfPermanent) {
        return getRemainingAmount((double) maxDurationIfPermanent);
    }


    public final double getRemainingAmount() {
        return getRemainingAmount(0);
    }

    public final double getRemainingAmount(double maxDurationIfPermanent) {
        var ticks = getTotalTicks();

        if (!getBase().isPermanent()) {
            ticks -= getTickNumber();
        }

        var total = getAmount() * ticks;

        if (total > maxDurationIfPermanent) {
            return maxDurationIfPermanent;
        }

        return total;
    }


    public final boolean isPeriodic() {
        return isPeriodic;
    }

    public final void setPeriodic(boolean isPeriodic) {
        isPeriodic = isPeriodic;
    }

    public final SpellEffectInfo getSpellEffectInfo() {
        return effectInfo;
    }

    public final boolean isEffect() {
        return effectInfo.effect != 0;
    }

    public final boolean isEffect(SpellEffectName effectName) {
        return effectInfo.effect == effectName;
    }

    public final boolean isAreaAuraEffect() {
        return effectInfo.isAreaAuraEffect();
    }

    
    public final void handleAuraModSpellPowerPercent(AuraApplication aurApp, AuraEffectHandleMode mode, boolean apply) {
        if ((mode.getValue() & (AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleMode.AURA_EFFECT_HANDLE_STAT.value).getValue()) == 0) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        // Recalculate bonus
        target.updateSpellDamageAndHealingBonus();
    }

    
    public final void handleModNextSpell(AuraApplication aurApp, AuraEffectHandleMode mode, boolean apply) {
        if ((mode.getValue() & AuraEffectHandleModes.Real.getValue()) == 0) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (player == null) {
            return;
        }

        var triggeredSpellId = getSpellEffectInfo().triggerSpell;

        if (apply) {
            player.addTemporarySpell(triggeredSpellId);
        } else {
            player.removeTemporarySpell(triggeredSpellId);
        }
    }

    private void getTargetList(tangible.OutObject<ArrayList<Unit>> targetList) {
        targetList.outArgValue = new ArrayList<>();
        var targetMap = getBase().getApplicationMap();

        // remove all targets which were not added to new list - they no longer deserve area aura
        for (var app : targetMap.values()) {
            if (app.hasEffect(getEffIndex())) {
                targetList.outArgValue.add(app.target);
            }
        }
    }

    private void getApplicationList(tangible.OutObject<ArrayList<AuraApplication>> applicationList) {
        applicationList.outArgValue = new ArrayList<>();
        var targetMap = getBase().getApplicationMap();

        for (var app : targetMap.values()) {
            if (app.hasEffect(getEffIndex())) {
                applicationList.outArgValue.add(app);
            }
        }
    }


    private void resetPeriodic() {
        resetPeriodic(false);
    }

    private void resetPeriodic(boolean resetPeriodicTimer) {
        ticksDone = 0;

        if (resetPeriodicTimer) {
            periodicTimer = 0;

            // Start periodic on next tick or at aura apply
            if (spellInfo.hasAttribute(SpellAttr5.ExtraInitialPeriod)) {
                periodicTimer = period;
            }
        }
    }


    private void applySpellMod(Unit target, boolean apply) {
        applySpellMod(target, apply, null);
    }

    private void applySpellMod(Unit target, boolean apply, AuraEffect triggeredBy) {
        if (spellModifier == null || !target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        target.toPlayer().addSpellMod(spellModifier, apply);

        // Auras with charges do not mod amount of passive auras
        if (getBase().isUsingCharges()) {
            return;
        }

        // reapply some passive spells after add/remove related spellmods
        // Warning: it is a dead loop if 2 auras each other amount-shouldn't happen
        BitSet recalculateEffectMask = new bitSet(Math.max(getBase().getAuraEffects().size(), 5));

        switch (SpellModOp.forValue(getMiscValue())) {
            case Points:
                recalculateEffectMask.setAll(true);

                break;
            case PointsIndex0:
                recalculateEffectMask.set(0, true);

                break;
            case PointsIndex1:
                recalculateEffectMask.set(1, true);

                break;
            case PointsIndex2:
                recalculateEffectMask.set(2, true);

                break;
            case PointsIndex3:
                recalculateEffectMask.set(3, true);

                break;
            case PointsIndex4:
                recalculateEffectMask.set(4, true);

                break;
            default:
                break;
        }

        if (recalculateEffectMask.Any()) {
            if (triggeredBy == null) {
                triggeredBy = this;
            }

            var guid = target.getGUID();

            // only passive and permament auras-active auras should have amount set on spellcast and not be affected
            // if aura is cast by others, it will not be affected
            target.getAppliedAurasQuery().hasCasterGuid(guid).isPassiveOrPerm().alsoMatches(arApp -> arApp.base.spellInfo.isAffectedBySpellMod(spellModifier)).forEachResult(arApp ->
            {
                var aura = arApp.base;

                for (var i = 0; i < recalculateEffectMask.count; ++i) {
                    if (recalculateEffectMask[i]) {
                        var aurEff = aura.getEffect(i);

                        if (aurEff != null) {
                            if (aurEff != triggeredBy) {
                                aurEff.recalculateAmount(triggeredBy);
                            }
                        }
                    }
                }
            });
        }
    }

    private void sendTickImmune(Unit target, Unit caster) {
        if (caster != null) {
            caster.sendSpellDamageImmune(target, spellInfo.getId(), true);
        }
    }

    private void periodicTick(AuraApplication aurApp, Unit caster) {
        var prevented = getBase().callScriptEffectPeriodicHandlers(this, aurApp);

        if (prevented) {
            return;
        }

        var target = aurApp.getTarget();

        // Update serverside orientation of tracking channeled auras on periodic update ticks
        // exclude players because can turn during channeling and shouldn't desync orientation client/server
        if (caster != null && !caster.isPlayer() && spellInfo.isChanneled() && spellInfo.hasAttribute(SpellAttr1.TrackTargetInChannel) && caster.getUnitData().channelObjects.size() != 0) {
            var channelGuid = caster.getUnitData().channelObjects.get(0);

            if (ObjectGuid.opNotEquals(channelGuid, caster.getGUID())) {
                var objectTarget = global.getObjAccessor().GetWorldObject(caster, channelGuid);

                if (objectTarget != null) {
                    caster.setInFront(objectTarget);
                }
            }
        }

        switch (getAuraType()) {
            case PeriodicDummy:
                // handled via scripts
                break;
            case PeriodicTriggerSpell:
                handlePeriodicTriggerSpellAuraTick(target, caster);

                break;
            case PeriodicTriggerSpellFromClient:
                // Don't actually do anything - client will trigger casts of these spells by itself
                break;
            case PeriodicTriggerSpellWithValue:
                handlePeriodicTriggerSpellWithValueAuraTick(target, caster);

                break;
            case PeriodicDamage:
            case PeriodicWeaponPercentDamage:
            case PeriodicDamagePercent:
                handlePeriodicDamageAurasTick(target, caster);

                break;
            case PeriodicLeech:
                handlePeriodicHealthLeechAuraTick(target, caster);

                break;
            case PeriodicHealthFunnel:
                handlePeriodicHealthFunnelAuraTick(target, caster);

                break;
            case PeriodicHeal:
            case ObsModHealth:
                handlePeriodicHealAurasTick(target, caster);

                break;
            case PeriodicManaLeech:
                handlePeriodicManaLeechAuraTick(target, caster);

                break;
            case ObsModPower:
                handleObsModPowerAuraTick(target, caster);

                break;
            case PeriodicEnergize:
                handlePeriodicEnergizeAuraTick(target, caster);

                break;
            case PowerBurn:
                handlePeriodicPowerBurnAuraTick(target, caster);

                break;
            default:
                break;
        }
    }

    private boolean hasSpellClassMask() {
        return getSpellEffectInfo().spellClassMask;
    }



    ///#region AuraEffect Handlers

    /**************************************/
    /***       VISIBILITY & PHASES      ***/
    /**************************************/

    private void handleUnused(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
    }

    
    private void handleModInvisibilityDetect(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();
        var type = InvisibilityType.forValue(getMiscValue());

        if (apply) {
            target.getInvisibilityDetect().addFlag(type);
            target.getInvisibilityDetect().addValue(type, getAmount());
        } else {
            if (!target.hasAuraType(AuraType.ModInvisibilityDetect)) {
                target.getInvisibilityDetect().delFlag(type);
            }

            target.getInvisibilityDetect().addValue(type, -getAmount());
        }

        // call functions which may have additional effects after changing state of unit
        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleModInvisibility(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountSendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();
        var playerTarget = target.toPlayer();
        var type = InvisibilityType.forValue(getMiscValue());

        if (apply) {
            // apply glow vision
            if (playerTarget != null && type == InvisibilityType.General) {
                playerTarget.addAuraVision(PlayerFieldByte2Flags.InvisibilityGlow);
            }

            target.getInvisibility().addFlag(type);
            target.getInvisibility().addValue(type, getAmount());

            target.setVisFlag(UnitVisFlags.Invisible);
        } else {
            if (!target.hasAuraType(AuraType.ModInvisibility)) {
                // if not have different invisibility auras.
                // always remove glow vision
                if (playerTarget != null) {
                    playerTarget.removeAuraVision(PlayerFieldByte2Flags.InvisibilityGlow);
                }

                target.getInvisibility().delFlag(type);
            } else {
                var found = false;
                var invisAuras = target.getAuraEffectsByType(AuraType.ModInvisibility);

                for (var eff : invisAuras) {
                    if (getMiscValue() == eff.getMiscValue()) {
                        found = true;

                        break;
                    }
                }

                if (!found) {
                    // if not have invisibility auras of type INVISIBILITY_GENERAL
                    // remove glow vision
                    if (playerTarget != null && type == InvisibilityType.General) {
                        playerTarget.removeAuraVision(PlayerFieldByte2Flags.InvisibilityGlow);
                    }

                    target.getInvisibility().delFlag(type);

                    target.removeVisFlag(UnitVisFlags.Invisible);
                }
            }

            target.getInvisibility().addValue(type, -getAmount());
        }

        // call functions which may have additional effects after changing state of unit
        if (apply && mode.hasFlag(AuraEffectHandleModes.Real)) {
            // drop flag at invisibiliy in bg
            target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.StealthOrInvis);
        }

        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleModStealthDetect(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();
        var type = StealthType.forValue(getMiscValue());

        if (apply) {
            target.getStealthDetect().addFlag(type);
            target.getStealthDetect().addValue(type, getAmount());
        } else {
            if (!target.hasAuraType(AuraType.ModStealthDetect)) {
                target.getStealthDetect().delFlag(type);
            }

            target.getStealthDetect().addValue(type, -getAmount());
        }

        // call functions which may have additional effects after changing state of unit
        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleModStealth(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountSendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();
        var type = StealthType.forValue(getMiscValue());

        if (apply) {
            target.getStealth().addFlag(type);
            target.getStealth().addValue(type, getAmount());
            target.setVisFlag(UnitVisFlags.Stealthed);
            var playerTarget = target.toPlayer();

            if (playerTarget != null) {
                playerTarget.addAuraVision(PlayerFieldByte2Flags.stealth);
            }
        } else {
            target.getStealth().addValue(type, -getAmount());

            if (!target.hasAuraType(AuraType.ModStealth)) // if last SPELL_AURA_MOD_STEALTH
            {
                target.getStealth().delFlag(type);

                target.removeVisFlag(UnitVisFlags.Stealthed);
                var playerTarget = target.toPlayer();

                if (playerTarget != null) {
                    playerTarget.removeAuraVision(PlayerFieldByte2Flags.stealth);
                }
            }
        }

        // call functions which may have additional effects after changing state of unit
        if (apply && mode.hasFlag(AuraEffectHandleModes.Real)) {
            // drop flag at stealth in bg
            target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.StealthOrInvis);
        }

        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleModStealthLevel(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();
        var type = StealthType.forValue(getMiscValue());

        if (apply) {
            target.getStealth().addValue(type, getAmount());
        } else {
            target.getStealth().addValue(type, -getAmount());
        }

        // call functions which may have additional effects after changing state of unit
        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleDetectAmore(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (apply) {
            var playerTarget = target.toPlayer();

            if (playerTarget != null) {
                playerTarget.addAuraVision(PlayerFieldByte2Flags.forValue(1 << (getMiscValue() - 1)));
            }
        } else {
            if (target.hasAuraType(AuraType.DetectAmore)) {
                var amoreAuras = target.getAuraEffectsByType(AuraType.DetectAmore);

                for (var auraEffect : amoreAuras) {
                    if (getMiscValue() == auraEffect.getMiscValue()) {
                        return;
                    }
                }
            }

            var playerTarget = target.toPlayer();

            if (playerTarget != null) {
                playerTarget.removeAuraVision(PlayerFieldByte2Flags.forValue(1 << (getMiscValue() - 1)));
            }
        }
    }

    
    private void handleSpiritOfRedemption(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // prepare spirit state
        if (apply) {
            if (target.isTypeId(TypeId.PLAYER)) {
                // set stand state (expected in this form)
                if (!target.isStandState()) {
                    target.setStandState(UnitStandStateType.Stand);
                }
            }
        }
        // die at aura end
        else if (target.isAlive()) {
            // call functions which may have additional effects after changing state of unit
            target.setDeathState(deathState.JustDied);
        }
    }

    
    private void handleAuraGhost(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setPlayerFlag(PlayerFlag.Ghost);
            target.getServerSideVisibility().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Ghost);
            target.getServerSideVisibilityDetect().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Ghost);
        } else {
            if (target.hasAuraType(AuraType.Ghost)) {
                return;
            }

            target.removePlayerFlag(PlayerFlag.Ghost);
            target.getServerSideVisibility().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Alive);
            target.getServerSideVisibilityDetect().setValue(ServerSideVisibilityType.Ghost, GhostVisibilityType.Alive);
        }
    }

    
    private void handlePhase(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            PhasingHandler.addPhase(target, (int) getMiscValueB(), true);
        } else {
            PhasingHandler.removePhase(target, (int) getMiscValueB(), true);
        }
    }

    
    private void handlePhaseGroup(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            PhasingHandler.addPhaseGroup(target, (int) getMiscValueB(), true);
        } else {
            PhasingHandler.removePhaseGroup(target, (int) getMiscValueB(), true);
        }
    }

    
    private void handlePhaseAlwaysVisible(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            PhasingHandler.setAlwaysVisible(target, true, true);
        } else {
            if (target.hasAuraType(AuraType.PhaseAlwaysVisible) || (target.isPlayer() && target.toPlayer().isGameMaster())) {
                return;
            }

            PhasingHandler.setAlwaysVisible(target, false, true);
        }
    }

    /**********************/
    /***   UNIT MODEL   ***/
    /**********************/

    private void handleAuraModShapeshift(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.RealOrReapplyMask)) {
            return;
        }

        var shapeInfo = CliDB.SpellShapeshiftFormStorage.get(getMiscValue());
        //ASSERT(shapeInfo, "Spell {0} uses unknown shapeshiftForm (%u).", getId(), GetMiscValue());

        var target = aurApp.getTarget();
        var form = ShapeShiftForm.forValue(getMiscValue());
        var modelid = target.getModelForForm(form, getId());

        if (apply) {
            // remove polymorph before changing display id to keep new display id
            switch (form) {
                case CatForm:
                case TreeOfLife:
                case TravelForm:
                case AquaticForm:
                case BearForm:
                case FlightFormEpic:
                case FlightForm:
                case MoonkinForm: {
                    // remove movement affects
                    target.removeAurasByShapeShift();

                    // and polymorphic affects
                    if (target.isPolymorphed()) {
                        target.removeAura(target.getTransformSpell());
                    }

                    break;
                }
                default:
                    break;
            }

            // remove other shapeshift before applying a new one
            target.removeAurasByType(AuraType.ModShapeshift, ObjectGuid.Empty, getBase());

            // stop handling the effect if it was removed by linked event
            if (aurApp.getHasRemoveMode()) {
                return;
            }

            var prevForm = target.getShapeshiftForm();
            target.setShapeshiftForm(form);

            // add the shapeshift aura's boosts
            if (prevForm != form) {
                handleShapeshiftBoosts(target, true);
            }

            if (modelid > 0) {
                var transformSpellInfo = global.getSpellMgr().getSpellInfo(target.getTransformSpell(), getBase().getCastDifficulty());

                if (transformSpellInfo == null || !getSpellInfo().isPositive()) {
                    target.setDisplayId(modelid);
                }
            }

            if (!shapeInfo.flags.hasFlag(SpellShapeshiftFormFlags.Stance)) {
                target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.Shapeshifting, getSpellInfo());
            }
        } else {
            // reset model id if no other auras present
            // may happen when aura is applied on linked event on aura removal
            if (!target.hasAuraType(AuraType.ModShapeshift)) {
                target.setShapeshiftForm(ShapeShiftForm.NONE);

                if (target.getUnitClass() == UnitClass.Druid) {
                    // Remove movement impairing effects also when shifting out
                    target.removeAurasByShapeShift();
                }
            }

            if (modelid > 0) {
                target.restoreDisplayId(target.isMounted());
            }

            switch (form) {
                // Nordrassil Harness - bonus
                case BearForm:
                case CatForm:
                    var dummy = target.getAuraEffect(37315, 0);

                    if (dummy != null) {
                        target.castSpell(target, 37316, new CastSpellExtraArgs(dummy));
                    }

                    break;
                // Nordrassil Regalia - bonus
                case MoonkinForm:
                    dummy = target.getAuraEffect(37324, 0);

                    if (dummy != null) {
                        target.castSpell(target, 37325, new CastSpellExtraArgs(dummy));
                    }

                    break;
                default:
                    break;
            }

            // remove the shapeshift aura's boosts
            handleShapeshiftBoosts(target, apply);
        }

        var playerTarget = target.toPlayer();

        if (playerTarget != null) {
            playerTarget.sendMovementSetCollisionHeight(playerTarget.getCollisionHeight(), UpdateCollisionHeightReason.FORCE);
            playerTarget.initDataForForm();
        } else {
            target.updateDisplayPower();
        }

        if (target.getUnitClass() == UnitClass.Druid) {
            // Dash
            var aurEff = target.getAuraEffect(AuraType.ModIncreaseSpeed, SpellFamilyNames.Druid, new Flag128(0, 0, 0x8));

            if (aurEff != null) {
                aurEff.recalculateAmount();
            }

            // Disarm handling
            // If druid shifts while being disarmed we need to deal with that since forms aren't affected by disarm
            // and also HandleAuraModDisarm is not triggered
            if (!target.canUseAttackType(WeaponAttackType.BASE_ATTACK)) {
                var pItem = target.toPlayer().getItemByPos(InventorySlots.Bag0, EquipmentSlot.MainHand);

                if (pItem != null) {
                    target.toPlayer()._ApplyWeaponDamage(EquipmentSlot.MainHand, pItem, apply);
                }
            }
        }

        // stop handling the effect if it was removed by linked event
        if (apply && aurApp.getHasRemoveMode()) {
            return;
        }

        if (target.isTypeId(TypeId.PLAYER)) {
            // Learn spells for shapeshift form - no need to send action bars or add spells to spellbook
            for (byte i = 0; i < SpellConst.MaxShapeshift; ++i) {
                if (shapeInfo.PresetSpellID[i] == 0) {
                    continue;
                }

                if (apply) {
                    target.toPlayer().addTemporarySpell(shapeInfo.PresetSpellID[i]);
                } else {
                    target.toPlayer().removeTemporarySpell(shapeInfo.PresetSpellID[i]);
                }
            }
        }
    }

    
    private void handleAuraTransform(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            // update active transform spell only when transform not set or not overwriting negative by positive case
            var transformSpellInfo = global.getSpellMgr().getSpellInfo(target.getTransformSpell(), getBase().getCastDifficulty());

            if (transformSpellInfo == null || !getSpellInfo().isPositive() || transformSpellInfo.isPositive()) {
                target.setTransformSpell(getId());

                // special case (spell specific functionality)
                if (getMiscValue() == 0) {
                    var isFemale = target.getNativeGender() == gender.Female;

                    switch (getId()) {
                        // Orb of Deception
                        case 16739: {
                            if (!target.isTypeId(TypeId.PLAYER)) {
                                return;
                            }

                            switch (target.getRace()) {
                                // Blood Elf
                                case BloodElf:
                                    target.setDisplayId(isFemale ? 17830 : 17829);

                                    break;
                                // Orc
                                case Orc:
                                    target.setDisplayId(isFemale ? 10140 : 10139);

                                    break;
                                // Troll
                                case Troll:
                                    target.setDisplayId(isFemale ? 10134 : 10135);

                                    break;
                                // Tauren
                                case Tauren:
                                    target.setDisplayId(isFemale ? 10147 : 10136);

                                    break;
                                // Undead
                                case Undead:
                                    target.setDisplayId(isFemale ? 10145 : 10146);

                                    break;
                                // Draenei
                                case Draenei:
                                    target.setDisplayId(isFemale ? 17828 : 17827);

                                    break;
                                // Dwarf
                                case Dwarf:
                                    target.setDisplayId(isFemale ? 10142 : 10141);

                                    break;
                                // Gnome
                                case Gnome:
                                    target.setDisplayId(isFemale ? 10149 : 10148);

                                    break;
                                // Human
                                case Human:
                                    target.setDisplayId(isFemale ? 10138 : 10137);

                                    break;
                                // Night Elf
                                case NightElf:
                                    target.setDisplayId(isFemale ? 10144 : 10143);

                                    break;
                                default:
                                    break;
                            }

                            break;
                        }
                        // Murloc costume
                        case 42365:
                            target.setDisplayId(21723);

                            break;
                        // Dread Corsair
                        case 50517:
                            // Corsair Costume
                        case 51926: {
                            if (!target.isTypeId(TypeId.PLAYER)) {
                                return;
                            }

                            switch (target.getRace()) {
                                // Blood Elf
                                case BloodElf:
                                    target.setDisplayId(isFemale ? 25043 : 25032);

                                    break;
                                // Orc
                                case Orc:
                                    target.setDisplayId(isFemale ? 25050 : 25039);

                                    break;
                                // Troll
                                case Troll:
                                    target.setDisplayId(isFemale ? 25052 : 25041);

                                    break;
                                // Tauren
                                case Tauren:
                                    target.setDisplayId(isFemale ? 25051 : 25040);

                                    break;
                                // Undead
                                case Undead:
                                    target.setDisplayId(isFemale ? 25053 : 25042);

                                    break;
                                // Draenei
                                case Draenei:
                                    target.setDisplayId(isFemale ? 25044 : 25033);

                                    break;
                                // Dwarf
                                case Dwarf:
                                    target.setDisplayId(isFemale ? 25045 : 25034);

                                    break;
                                // Gnome
                                case Gnome:
                                    target.setDisplayId(isFemale ? 25035 : 25046);

                                    break;
                                // Human
                                case Human:
                                    target.setDisplayId(isFemale ? 25037 : 25048);

                                    break;
                                // Night Elf
                                case NightElf:
                                    target.setDisplayId(isFemale ? 25038 : 25049);

                                    break;
                                default:
                                    break;
                            }

                            break;
                        }
                        // Pygmy Oil
                        case 53806:
                            target.setDisplayId(22512);

                            break;
                        // Honor the Dead
                        case 65386:
                        case 65495:
                            target.setDisplayId(isFemale ? 29204 : 29203);

                            break;
                        // Darkspear Pride
                        case 75532:
                            target.setDisplayId(isFemale ? 31738 : 31737);

                            break;
                        // Gnomeregan Pride
                        case 75531:
                            target.setDisplayId(isFemale ? 31655 : 31654);

                            break;
                        default:
                            break;
                    }
                } else {
                    var ci = global.getObjectMgr().getCreatureTemplate((int) getMiscValue());

                    if (ci == null) {
                        target.setDisplayId(16358); // pig pink ^_^
                        Log.outError(LogFilter.spells, "Auras: unknown creature id = {0} (only need its modelid) From Spell Aura Transform in Spell ID = {1}", getMiscValue(), getId());
                    } else {
                        int model_id = 0;
                        var modelid = ObjectManager.chooseDisplayId(ci).creatureDisplayId;

                        if (modelid != 0) {
                            model_id = modelid; // Will use the default model here
                        }

                        target.setDisplayId(model_id);

                        // Dragonmaw Illusion (set mount model also)
                        if (getId() == 42016 && target.getMountDisplayId() != 0 && !target.getAuraEffectsByType(AuraType.ModIncreaseMountedFlightSpeed).isEmpty()) {
                            target.setMountDisplayId(16314);
                        }
                    }
                }
            }

            // polymorph case
            if (mode.hasFlag(AuraEffectHandleModes.Real) && target.isTypeId(TypeId.PLAYER) && target.isPolymorphed()) {
                // for players, start regeneration after 1s (in polymorph fast regeneration case)
                // only if caster is player (after patch 2.4.2)
                if (getCasterGuid().isPlayer()) {
                    target.toPlayer().setRegenTimerCount(1 * time.InMilliseconds);
                }

                //dismount polymorphed target (after patch 2.4.2)
                if (target.isMounted()) {
                    target.removeAurasByType(AuraType.Mounted);
                }
            }
        } else {
            if (target.getTransformSpell() == getId()) {
                target.setTransformSpell(0);
            }

            target.restoreDisplayId(target.isMounted());

            // Dragonmaw Illusion (restore mount model)
            if (getId() == 42016 && target.getMountDisplayId() == 16314) {
                if (!target.getAuraEffectsByType(AuraType.Mounted).isEmpty()) {
                    var cr_id = target.getAuraEffectsByType(AuraType.Mounted).get(0).getMiscValue();
                    var ci = global.getObjectMgr().getCreatureTemplate((int) cr_id);

                    if (ci != null) {
                        var model = ObjectManager.chooseDisplayId(ci);
                        tangible.RefObject<CreatureModel> tempRef_model = new tangible.RefObject<CreatureModel>(model);
                        global.getObjectMgr().getCreatureModelRandomGender(tempRef_model, ci);
                        model = tempRef_model.refArgValue;

                        target.setMountDisplayId(model.creatureDisplayId);
                    }
                }
            }
        }
    }

    
    private void handleAuraModScale(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountSendForClientMask)) {
            return;
        }

        aurApp.getTarget().recalculateObjectScale();
    }

    
    private void handleAuraCloneCaster(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            var caster = getCaster();

            if (caster == null || caster == target) {
                return;
            }

            // What must be cloned? at least display and scale
            target.setDisplayId(caster.getDisplayId());
            //target.SetObjectScale(caster.getFloatValue(OBJECT_FIELD_SCALE_X)); // we need retail info about how scaling is handled (aura maybe?)
            target.setUnitFlag2(UnitFlag2.mirrorImage);
        } else {
            target.setDisplayId(target.getNativeDisplayId());
            target.removeUnitFlag2(UnitFlag2.mirrorImage);
        }
    }

    /************************/
    /***      FIGHT       ***/
    /************************/

    private void handleFeignDeath(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            ArrayList<Unit> targets = new ArrayList<>();
            var u_check = new AnyUnfriendlyUnitInObjectRangeCheck(target, target, target.getMap().getVisibilityRange(), u -> u.hasUnitState(UnitState.Casting));
            var searcher = new UnitListSearcher(target, targets, u_check, gridType.All);

            Cell.visitGrid(target, searcher, target.getMap().getVisibilityRange());

            for (var unit : targets) {
                for (var i = CurrentSpellTypes.generic; i.getValue() < CurrentSpellTypes.max.getValue(); i++) {
                    if (unit.getCurrentSpell(i) != null && Objects.equals(unit.getCurrentSpell(i).targets.getUnitTargetGUID(), target.getGUID())) {
                        unit.interruptSpell(i, false);
                    }
                }
            }

            for (var pair : target.getThreatManager().getThreatenedByMeList().entrySet()) {
                pair.getValue().scaleThreat(0.0f);
            }

            if (target.getMap().isDungeon()) // feign death does not remove combat in dungeons
            {
                target.attackStop();
                var targetPlayer = target.toPlayer();

                if (targetPlayer != null) {
                    targetPlayer.sendAttackSwingCancelAttack();
                }
            } else {
                target.combatStop(false, false);
            }

            // prevent interrupt message
            if (Objects.equals(getCasterGuid(), target.getGUID()) && target.getCurrentSpell(CurrentSpellTypes.generic) != null) {
                target.finishSpell(CurrentSpellTypes.generic, SpellCastResult.Interrupted);
            }

            target.interruptNonMeleeSpells(true);

            // stop handling the effect if it was removed by linked event
            if (aurApp.getHasRemoveMode()) {
                return;
            }

            target.setUnitFlag(UnitFlag.PreventEmotesFromChatText);
            target.setUnitFlag2(UnitFlag2.FeignDeath);
            target.setUnitFlag3(unitFlags3.FakeDead);
            target.addUnitState(UnitState.Died);

            var creature = target.toCreature();

            if (creature != null) {
                creature.setReactState(ReactStates.Passive);
            }
        } else {
            target.removeUnitFlag(UnitFlag.PreventEmotesFromChatText);
            target.removeUnitFlag2(UnitFlag2.FeignDeath);
            target.removeUnitFlag3(unitFlags3.FakeDead);
            target.clearUnitState(UnitState.Died);

            var creature = target.toCreature();

            if (creature != null) {
                creature.initializeReactState();
            }
        }
    }

    
    private void handleModUnattackable(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
        if (!apply && target.hasAuraType(AuraType.ModUnattackable)) {
            return;
        }

        if (apply) {
            target.setUnitFlag(UnitFlag.NonAttackable2);
        } else {
            target.removeUnitFlag(UnitFlag.NonAttackable2);
        }

        // call functions which may have additional effects after changing state of unit
        if (apply && mode.hasFlag(AuraEffectHandleModes.Real)) {
            if (target.getMap().isDungeon()) {
                target.attackStop();
                var targetPlayer = target.toPlayer();

                if (targetPlayer != null) {
                    targetPlayer.sendAttackSwingCancelAttack();
                }
            } else {
                target.combatStop();
            }
        }
    }

    
    private void handleAuraModDisarm(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        //Prevent handling aura twice
        var type = getAuraType();

        if (apply ? target.getAuraEffectsByType(type).size() > 1 : target.hasAuraType(type)) {
            return;
        }

        tangible.Action1Param<unit> flagChangeFunc = null;
        byte slot;
        WeaponAttackType attType;

        switch (type) {
            case ModDisarm:
                if (apply) {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.setUnitFlag(UnitFlag.Disarmed);
                    };
                } else {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.removeUnitFlag(UnitFlag.Disarmed);
                    };
                }

                slot = EquipmentSlot.MainHand;
                attType = WeaponAttackType.BASE_ATTACK;

                break;
            case ModDisarmOffhand:
                if (apply) {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.setUnitFlag2(UnitFlag2.DisarmOffhand);
                    };
                } else {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.removeUnitFlag2(UnitFlag2.DisarmOffhand);
                    };
                }

                slot = EquipmentSlot.OffHand;
                attType = WeaponAttackType.OFF_ATTACK;

                break;
            case ModDisarmRanged:
                if (apply) {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.setUnitFlag2(UnitFlag2.DisarmRanged);
                    };
                } else {
                    flagChangeFunc = (Unit unit) ->
                    {
                        unit.removeUnitFlag2(UnitFlag2.DisarmRanged);
                    };
                }

                slot = EquipmentSlot.MainHand;
                attType = WeaponAttackType.RangedAttack;

                break;
            default:
                return;
        }

        // set/remove flag before weapon bonuses so it's properly reflected in CanUseAttackType
        if (flagChangeFunc != null) {
            flagChangeFunc.invoke(target);
        }

        // Handle damage modification, shapeshifted druids are not affected
        if (target.isTypeId(TypeId.PLAYER) && !target.isInFeralForm()) {
            var player = target.toPlayer();

            var item = player.getItemByPos(InventorySlots.Bag0, slot);

            if (item != null) {
                var attackType = player.getAttackBySlot(slot, item.getTemplate().getInventoryType());

                player.applyItemDependentAuras(item, !apply);

                if (attackType.getValue() < WeaponAttackType.max.getValue()) {
                    player._ApplyWeaponDamage(slot, item, !apply);

                    if (!apply) // apply case already handled on item dependent aura removal (if any)
                    {
                        player.updateWeaponDependentAuras(attackType);
                    }
                }
            }
        }

        if (target.isTypeId(TypeId.UNIT) && target.toCreature().getCurrentEquipmentId() != 0) {
            target.updateDamagePhysical(attType);
        }
    }

    
    private void handleAuraModSilence(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setSilencedSchoolMask(spellSchoolMask.forValue(getMiscValue()));

            // call functions which may have additional effects after changing state of unit
            // Stop cast only spells vs preventionType & SPELL_PREVENTION_TYPE_SILENCE
            for (var i = CurrentSpellTypes.Melee; i.getValue() < CurrentSpellTypes.max.getValue(); ++i) {
                var spell = target.getCurrentSpell(i);

                if (spell != null) {
                    if (spell.spellInfo.getPreventionType().hasFlag(SpellPreventionType.Silence)) {
                        // Stop spells on prepare or casting state
                        target.interruptSpell(i, false);
                    }
                }
            }
        } else {
            var silenceSchoolMask = 0;

            for (var eff : target.getAuraEffectsByType(AuraType.ModSilence)) {
                silenceSchoolMask |= eff.getMiscValue();
            }

            for (var eff : target.getAuraEffectsByType(AuraType.ModPacifySilence)) {
                silenceSchoolMask |= eff.getMiscValue();
            }

            target.replaceAllSilencedSchoolMask((int) silenceSchoolMask);
        }
    }

    
    private void handleAuraModPacify(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag(UnitFlag.Pacified);
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(AuraType.ModPacify) || target.hasAuraType(AuraType.ModPacifySilence)) {
                return;
            }

            target.removeUnitFlag(UnitFlag.Pacified);
        }
    }

    
    private void handleAuraModPacifyAndSilence(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        // Vengeance of the Blue flight (@todo REMOVE THIS!)
        // @workaround
        if (spellInfo.getId() == 45839) {
            if (apply) {
                target.setUnitFlag(UnitFlag.NonAttackable);
            } else {
                target.removeUnitFlag(UnitFlag.NonAttackable);
            }
        }

        if (!(apply)) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(AuraType.ModPacifySilence)) {
                return;
            }
        }

        handleAuraModPacify(aurApp, mode, apply);
        handleAuraModSilence(aurApp, mode, apply);
    }

    
    private void handleAuraModNoActions(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag2(UnitFlag2.NoActions);

            // call functions which may have additional effects after changing state of unit
            // Stop cast only spells vs preventionType & SPELL_PREVENTION_TYPE_SILENCE
            for (var i = CurrentSpellTypes.Melee; i.getValue() < CurrentSpellTypes.max.getValue(); ++i) {
                var spell = target.getCurrentSpell(i);

                if (spell) {
                    if (spell.spellInfo.getPreventionType().hasFlag(SpellPreventionType.NoActions)) {
                        // Stop spells on prepare or casting state
                        target.interruptSpell(i, false);
                    }
                }
            }
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(AuraType.ModNoActions)) {
                return;
            }

            target.removeUnitFlag2(UnitFlag2.NoActions);
        }
    }

    /****************************/
    /***      TRACKING        ***/
    /****************************/

    private void handleAuraTrackCreatures(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setTrackCreatureFlag(1 << (getMiscValue() - 1));
        } else {
            target.removeTrackCreatureFlag(1 << (getMiscValue() - 1));
        }
    }

    
    private void handleAuraTrackStealthed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (!(apply)) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        if (apply) {
            target.setPlayerLocalFlag(PlayerLocalFlags.TrackStealthed);
        } else {
            target.removePlayerLocalFlag(PlayerLocalFlags.TrackStealthed);
        }
    }

    
    private void handleAuraModStalked(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        // used by spells: Hunter's Mark, Mind Vision, Syndicate Tracker (MURP) DND
        if (apply) {
            target.setDynamicFlag(UnitDynFlag.TrackUnit);
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (!target.hasAuraType(getAuraType())) {
                target.removeDynamicFlag(UnitDynFlag.TrackUnit);
            }
        }

        // call functions which may have additional effects after changing state of unit
        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleAuraUntrackable(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setVisFlag(UnitVisFlags.Untrackable);
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }

            target.removeVisFlag(UnitVisFlags.Untrackable);
        }
    }

    /****************************/
    /***  SKILLS & TALENTS    ***/
    /****************************/

    private void handleAuraModSkill(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.skill.getValue()))) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        var prot = SkillType.forValue(getMiscValue());
        var points = getAmount();

        if (prot == SkillType.Defense) {
            return;
        }

        target.modifySkillBonus(prot, (int) (apply ? points : -points), getAuraType() == AuraType.ModSkillTalent);
    }

    
    private void handleAuraAllowTalentSwapping(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setUnitFlag2(UnitFlag2.AllowChangingTalents);
        } else if (!target.hasAuraType(getAuraType())) {
            target.removeUnitFlag2(UnitFlag2.AllowChangingTalents);
        }
    }

    /****************************/
    /***       MOVEMENT       ***/
    /****************************/

    private void handleAuraMounted(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountSendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            if (mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
                var creatureEntry = (int) getMiscValue();
                int displayId = 0;
                int vehicleId = 0;

                var mountEntry = global.getDB2Mgr().GetMount(getId());

                if (mountEntry != null) {
                    var mountDisplays = global.getDB2Mgr().GetMountDisplays(mountEntry.id);

                    if (mountDisplays != null) {
                        if (mountEntry.IsSelfMount()) {
                            displayId = SharedConst.DisplayIdHiddenMount;
                        } else {
                            var usableDisplays = mountDisplays.stream().filter(mountDisplay ->
                            {
                                var playerTarget = target.toPlayer();

                                if (playerTarget != null) {
                                    var playerCondition = CliDB.PlayerConditionStorage.get(mountDisplay.playerConditionID);

                                    if (playerCondition != null) {
                                        return ConditionManager.isPlayerMeetingCondition(playerTarget, playerCondition);
                                    }
                                }

                                return true;
                            }).collect(Collectors.toList());

                            if (!usableDisplays.isEmpty()) {
                                displayId = usableDisplays.SelectRandom().creatureDisplayInfoID;
                            }
                        }
                    }
                    // TODO: CREATE TABLE mount_vehicle (mountId, vehicleCreatureId) for future mounts that are vehicles (new mounts no longer have proper data in miscValue)
                    //if (MountVehicle const* mountVehicle = sObjectMgr->GetMountVehicle(mountEntry->id))
                    //    creatureEntry = mountVehicle->VehicleCreatureId;
                }

                var creatureInfo = global.getObjectMgr().getCreatureTemplate(creatureEntry);

                if (creatureInfo != null) {
                    vehicleId = creatureInfo.vehicleId;

                    if (displayId == 0) {
                        var model = ObjectManager.chooseDisplayId(creatureInfo);
                        tangible.RefObject<CreatureModel> tempRef_model = new tangible.RefObject<CreatureModel>(model);
                        global.getObjectMgr().getCreatureModelRandomGender(tempRef_model, creatureInfo);
                        model = tempRef_model.refArgValue;
                        displayId = model.creatureDisplayId;
                    }

                    //some spell has one aura of mount and one of vehicle
                    for (var effect : getSpellInfo().getEffects()) {
                        if (effect.isEffect(SpellEffectName.summon) && effect.miscValue == getMiscValue()) {
                            displayId = 0;
                        }
                    }
                }

                target.mount(displayId, vehicleId, creatureEntry);
            }

            // cast speed aura
            if (mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
                var mountCapability = CliDB.MountCapabilityStorage.get(getAmount());

                if (mountCapability != null) {
                    target.castSpell(target, mountCapability.ModSpellAuraID, new CastSpellExtraArgs(this));
                }
            }
        } else {
            if (mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
                target.dismount();
            }

            //some mounts like Headless Horseman's Mount or broom stick are skill based spell
            // need to remove ALL arura related to mounts, this will stop client crash with broom stick
            // and never endless flying after using Headless Horseman's Mount
            if (mode.hasFlag(AuraEffectHandleModes.Real)) {
                target.removeAurasByType(AuraType.Mounted);
            }

            if (mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
                // remove speed aura
                var mountCapability = CliDB.MountCapabilityStorage.get(getAmount());

                if (mountCapability != null) {
                    target.removeAurasDueToSpell(mountCapability.ModSpellAuraID, target.getGUID());
                }
            }
        }
    }

    
    private void handleAuraAllowFlight(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType()) || target.hasAuraType(AuraType.ModIncreaseMountedFlightSpeed)) {
                return;
            }
        }

        target.setCanTransitionBetweenSwimAndFly(apply);

        if (target.setCanFly(apply)) {
            if (!apply && !target.isGravityDisabled()) {
                target.getMotionMaster().moveFall();
            }
        }
    }

    
    private void handleAuraWaterWalk(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setWaterWalking(apply);
    }

    
    private void handleAuraFeatherFall(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setFeatherFall(apply);

        // start fall from current height
        if (!apply && target.isTypeId(TypeId.PLAYER)) {
            target.toPlayer().setFallInformation(0, target.getLocation().getZ());
        }
    }

    
    private void handleAuraHover(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setHover(apply); //! Sets movementflags
    }

    
    private void handleWaterBreathing(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        // update timers in client
        if (target.isTypeId(TypeId.PLAYER)) {
            target.toPlayer().updateMirrorTimers();
        }
    }

    
    private void handleForceMoveForward(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag2(UnitFlag2.ForceMovement);
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }

            target.removeUnitFlag2(UnitFlag2.ForceMovement);
        }
    }

    
    private void handleAuraCanTurnWhileFalling(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setCanTurnWhileFalling(apply);
    }

    
    private void handleIgnoreMovementForces(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setIgnoreMovementForces(apply);
    }

    
    private void handleDisableInertia(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        target.setDisableInertia(apply);
    }

    /****************************/
    /***        THREAT        ***/
    /****************************/

    private void handleModThreat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        aurApp.getTarget().getThreatManager().updateMySpellSchoolModifiers();
    }

    
    private void handleAuraModTotalThreat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isAlive() || !target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var caster = getCaster();

        if (caster != null && caster.isAlive()) {
            caster.getThreatManager().updateMyTempModifiers();
        }
    }

    
    private void handleModTaunt(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isAlive() || !target.getCanHaveThreatList()) {
            return;
        }

        target.getThreatManager().tauntUpdate();
    }

    
    private void handleModDetaunt(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var caster = getCaster();
        var target = aurApp.getTarget();

        if (!caster || !caster.isAlive() || !target.isAlive() || !caster.getCanHaveThreatList()) {
            return;
        }

        caster.getThreatManager().tauntUpdate();
    }

    /*****************************/
    /***        CONTROL        ***/
    /*****************************/

    private void handleModConfuse(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Confused);

        if (apply) {
            target.getThreatManager().evaluateSuppressed();
        }
    }

    
    private void handleModFear(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Fleeing);
    }

    
    private void handleAuraModStun(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Stunned);

        if (apply) {
            target.getThreatManager().evaluateSuppressed();
        }
    }

    
    private void handleAuraModRoot(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Root);
    }

    
    private void handlePreventFleeing(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        // Since patch 3.0.2 this mechanic no longer affects fear effects. It will ONLY prevent humanoids from fleeing due to low health.
        if (!apply || target.hasAuraType(AuraType.ModFear)) {
            return;
        }

        // TODO: find a way to cancel fleeing for assistance.
        // Currently this will only stop creatures fleeing due to low health that could not find nearby allies to flee towards.
        target.setControlled(false, UnitState.Fleeing);
    }

    
    private void handleAuraModRootAndDisableGravity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Root);

        // Do not remove DisableGravity if there are more than this auraEffect of that kind on the unit or if it's a creature with DisableGravity on its movement template.
        if (!apply && (target.hasAuraType(getAuraType()) || target.hasAuraType(AuraType.ModStunDisableGravity) || (target.isCreature() && target.toCreature().getMovementTemplate().flight == CreatureFlightMovementType.DisableGravity))) {
            return;
        }

        if (target.setDisableGravity(apply)) {
            if (!apply && !target.isFlying()) {
                target.getMotionMaster().moveFall();
            }
        }
    }

    
    private void handleAuraModStunAndDisableGravity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.setControlled(apply, UnitState.Stunned);

        if (apply) {
            target.getThreatManager().evaluateSuppressed();
        }

        // Do not remove DisableGravity if there are more than this auraEffect of that kind on the unit or if it's a creature with DisableGravity on its movement template.
        if (!apply && (target.hasAuraType(getAuraType()) || target.hasAuraType(AuraType.ModStunDisableGravity) || (target.isCreature() && target.toCreature().getMovementTemplate().flight == CreatureFlightMovementType.DisableGravity))) {
            return;
        }

        if (target.setDisableGravity(apply)) {
            if (!apply && !target.isFlying()) {
                target.getMotionMaster().moveFall();
            }
        }
    }

    /***************************/
    /***        CHARM        ***/
    /***************************/

    private void handleModPossess(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        var caster = getCaster();

        // no support for posession AI yet
        if (caster != null && caster.isTypeId(TypeId.UNIT)) {
            handleModCharm(aurApp, mode, apply);

            return;
        }

        if (apply) {
            target.setCharmedBy(caster, CharmType.Possess, aurApp);
        } else {
            target.removeCharmedBy(caster);
        }
    }

    
    private void handleModPossessPet(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var caster = getCaster();

        if (caster == null || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.UNIT) || !target.isPet()) {
            return;
        }

        var pet = target.toPet();

        if (apply) {
            if (caster.toPlayer().getCurrentPet() != pet) {
                return;
            }

            pet.setCharmedBy(caster, CharmType.Possess, aurApp);
        } else {
            pet.removeCharmedBy(caster);

            if (!pet.isWithinDistInMap(caster, pet.getMap().getVisibilityRange())) {
                pet.remove(PetSaveMode.NotInSlot, true);
            } else {
                // Reinitialize the pet bar or it will appear greyed out
                caster.toPlayer().petSpellInitialize();

                // TODO: remove this
                if (pet.getVictim() == null && !pet.getCharmInfo().hasCommandState(CommandStates.Stay)) {
                    pet.getMotionMaster().moveFollow(caster, SharedConst.PetFollowDist, pet.getFollowAngle());
                }
            }
        }
    }

    
    private void handleModCharm(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        var caster = getCaster();

        if (apply) {
            target.setCharmedBy(caster, CharmType.charm, aurApp);
        } else {
            target.removeCharmedBy(caster);
        }
    }

    
    private void handleCharmConvert(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        var caster = getCaster();

        if (apply) {
            target.setCharmedBy(caster, CharmType.Convert, aurApp);
        } else {
            target.removeCharmedBy(caster);
        }
    }

    /**
     * Such auras are applied from a caster(=player) to a vehicle.
     * This has been verified using spell #49256
     */

    private void handleAuraControlVehicle(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isVehicle()) {
            return;
        }

        var caster = getCaster();

        if (caster == null || caster == target) {
            return;
        }

        if (apply) {
            // Currently spells that have base points  0 and DieSides 0 = "0/0" exception are pushed to -1,
            // however the idea of 0/0 is to ingore flag VEHICLE_SEAT_FLAG_CAN_ENTER_OR_EXIT and -1 checks for it,
            // so this break such spells or most of them.
            // Current formula about m_amount: effect base points + dieside - 1
            // TO DO: Reasearch more about 0/0 and fix it.
            caster._EnterVehicle(target.getVehicleKit(), (byte) (getAmount() - 1), aurApp);
        } else {
            // Remove pending passengers before exiting vehicle - might cause an Uninstall
            target.getVehicleKit().RemovePendingEventsForPassenger(caster);

            if (getId() == 53111) // Devour Humanoid
            {
                unit.kill(target, caster);

                if (caster.isTypeId(TypeId.UNIT)) {
                    caster.toCreature().despawnOrUnsummon();
                }
            }

            var seatChange = mode.hasFlag(AuraEffectHandleModes.ChangeAmount) || target.hasAuraTypeWithCaster(AuraType.ControlVehicle, caster.getGUID()); // Seat change to a proxy vehicle (for example turret mounted on a siege engine)

            if (!seatChange) {
                caster._ExitVehicle();
            } else {
                target.getVehicleKit().removePassenger(caster); // Only remove passenger from vehicle without launching exit movement or despawning the vehicle
            }

            // some SPELL_AURA_CONTROL_VEHICLE auras have a dummy effect on the player - remove them
            caster.removeAura(getId());
        }
    }

    /*********************************************************/
    /***                  MODIFY SPEED                     ***/
    /*********************************************************/

    private void handleAuraModIncreaseSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        target.updateSpeed(UnitMoveType.run);
    }

    
    private void handleAuraModIncreaseMountedSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        handleAuraModIncreaseSpeed(aurApp, mode, apply);
    }

    
    private void handleAuraModIncreaseFlightSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountSendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            target.updateSpeed(UnitMoveType.flight);
        }

        //! Update ability to fly
        if (getAuraType() == AuraType.ModIncreaseMountedFlightSpeed) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (mode.hasFlag(AuraEffectHandleModes.SendForClientMask) && (apply || (!target.hasAuraType(AuraType.ModIncreaseMountedFlightSpeed) && !target.hasAuraType(AuraType.Fly)))) {
                target.setCanTransitionBetweenSwimAndFly(apply);

                if (target.setCanFly(apply)) {
                    if (!apply && !target.isGravityDisabled()) {
                        target.getMotionMaster().moveFall();
                    }
                }
            }

            //! Someone should clean up these hacks and remove it from this function. It doesn't even belong here.
            if (mode.hasFlag(AuraEffectHandleModes.Real)) {
                //Players on flying mounts must be immune to polymorph
                if (target.isTypeId(TypeId.PLAYER)) {
                    target.applySpellImmune(getId(), SpellImmunity.mechanic, (int) mechanics.Polymorph.getValue(), apply);
                }

                // Dragonmaw Illusion (overwrite mount model, mounted aura already applied)
                if (apply && target.hasAuraEffect(42016, 0) && target.getMountDisplayId() != 0) {
                    target.setMountDisplayId(16314);
                }
            }
        }
    }

    
    private void handleAuraModIncreaseSwimSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        target.updateSpeed(UnitMoveType.swim);
    }

    
    private void handleAuraModDecreaseSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        target.updateSpeed(UnitMoveType.run);
        target.updateSpeed(UnitMoveType.swim);
        target.updateSpeed(UnitMoveType.flight);
        target.updateSpeed(UnitMoveType.RunBack);
        target.updateSpeed(UnitMoveType.SwimBack);
        target.updateSpeed(UnitMoveType.FlightBack);
    }

    
    private void handleAuraModUseNormalSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.updateSpeed(UnitMoveType.run);
        target.updateSpeed(UnitMoveType.swim);
        target.updateSpeed(UnitMoveType.flight);
    }

    
    private void handleAuraModMinimumSpeedRate(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        target.updateSpeed(UnitMoveType.run);
    }

    
    private void handleModMovementForceMagnitude(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        aurApp.getTarget().updateMovementForcesModMagnitude();
    }

    /*********************************************************/
    /***                     IMMUNITY                      ***/
    /*********************************************************/

    private void handleModMechanicImmunityMask(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);
    }

    
    private void handleModMechanicImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);
    }

    
    private void handleAuraModEffectImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);

        // when removing flag aura, handle flag drop
        // TODO: this should be handled in aura script for flag spells using AfterEffectRemove hook
        var player = target.toPlayer();

        if (!apply && player != null && getSpellInfo().hasAuraInterruptFlag(SpellAuraInterruptFlag.StealthOrInvis)) {
            if (player.getInBattleground()) {
                var bg = player.getBattleground();

                if (bg) {
                    bg.eventPlayerDroppedFlag(player);
                }
            } else {
                global.getOutdoorPvPMgr().handleDropFlag(player, getSpellInfo().getId());
            }
        }
    }

    
    private void handleAuraModStateImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);
    }

    
    private void handleAuraModSchoolImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);

        if (getSpellInfo().getMechanic() == mechanics.Banish) {
            if (apply) {
                target.addUnitState(UnitState.Isolated);
            } else {
                var banishFound = false;
                var banishAuras = target.getAuraEffectsByType(getAuraType());

                for (var aurEff : banishAuras) {
                    if (aurEff.getSpellInfo().getMechanic() == mechanics.Banish) {
                        banishFound = true;

                        break;
                    }
                }

                if (!banishFound) {
                    target.clearUnitState(UnitState.Isolated);
                }
            }
        }

        {
            // TODO: should be changed to a proc script on flag spell (they have "Taken positive" proc flags in db2)
            if (apply && getMiscValue() == spellSchoolMask.NORMAL.getValue()) {
                target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.StealthOrInvis);
            }

            // remove all flag auras (they are positive, but they must be removed when you are immune)
            if (getSpellInfo().hasAttribute(SpellAttr1.ImmunityPurgesEffect) && getSpellInfo().hasAttribute(SpellAttr2.FailOnAllTargetsImmune)) {
                target.removeAurasWithInterruptFlags(SpellAuraInterruptFlag.StealthOrInvis);
            }
        }

        if (apply) {
            target.setUnitFlag(UnitFlag.Immune);
            target.getThreatManager().evaluateSuppressed();
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit
            if (target.hasAuraType(getAuraType()) || target.hasAuraType(AuraType.DamageImmunity)) {
                return;
            }

            target.removeUnitFlag(UnitFlag.Immune);
        }
    }

    
    private void handleAuraModDmgImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);

        if (apply) {
            target.setUnitFlag(UnitFlag.Immune);
            target.getThreatManager().evaluateSuppressed();
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit
            if (target.hasAuraType(getAuraType()) || target.hasAuraType(AuraType.SchoolImmunity)) {
                return;
            }

            target.removeUnitFlag(UnitFlag.Immune);
        }
    }

    
    private void handleAuraModDispelImmunity(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        spellInfo.applyAllSpellImmunitiesTo(target, getSpellEffectInfo(), apply);
    }

    /*********************************************************/
    /***                  MODIFY STATS                     ***/
    /*********************************************************/

    /********************************/
    /***        RESISTANCE        ***/
    /********************************/

    private void handleAuraModResistance(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        for (var x = (byte) SpellSchool.NORMAL.getValue(); x < (byte) SpellSchool.max.getValue(); x++) {
            if ((boolean) (getMiscValue() & (1 << x))) {
                target.handleStatFlatModifier(UnitMods.ResistanceStart + x, UnitModifierFlatType.Total, getAmount(), apply);
            }
        }
    }

    
    private void handleAuraModBaseResistancePCT(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        // only players have base stats
        if (!target.isTypeId(TypeId.PLAYER)) {
            //pets only have base armor
            if (target.isPet() && (boolean) (getMiscValue() & spellSchoolMask.NORMAL.getValue())) {
                if (apply) {
                    target.applyStatPctModifier(UnitMods.armor, UnitModifierPctType.base, getAmount());
                } else {
                    var amount = target.getTotalAuraMultiplierByMiscMask(AuraType.ModBaseResistancePct, (int) spellSchoolMask.NORMAL.getValue());
                    target.setStatPctModifier(UnitMods.armor, UnitModifierPctType.base, amount);
                }
            }
        } else {
            for (var x = (byte) SpellSchool.NORMAL.getValue(); x < (byte) SpellSchool.max.getValue(); x++) {
                if ((boolean) (getMiscValue() & (1 << x))) {
                    if (apply) {
                        target.applyStatPctModifier(UnitMods.ResistanceStart + x, UnitModifierPctType.base, getAmount());
                    } else {
                        var amount = target.getTotalAuraMultiplierByMiscMask(AuraType.ModBaseResistancePct, 1 << x);
                        target.setStatPctModifier(UnitMods.ResistanceStart + x, UnitModifierPctType.base, amount);
                    }
                }
            }
        }
    }

    
    private void handleModResistancePercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        for (var i = (byte) SpellSchool.NORMAL.getValue(); i < (byte) SpellSchool.max.getValue(); i++) {
            if ((boolean) (getMiscValue() & (1 << i))) {
                var amount = target.getTotalAuraMultiplierByMiscMask(AuraType.ModResistancePct, 1 << i);

                if (target.getPctModifierValue(UnitMods.ResistanceStart + i, UnitModifierPctType.Total) == amount) {
                    continue;
                }

                target.setStatPctModifier(UnitMods.ResistanceStart + i, UnitModifierPctType.Total, amount);
            }
        }
    }

    
    private void handleModBaseResistance(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        // only players have base stats
        if (!target.isTypeId(TypeId.PLAYER)) {
            //only pets have base stats
            if (target.isPet() && (boolean) (getMiscValue() & spellSchoolMask.NORMAL.getValue())) {
                target.handleStatFlatModifier(UnitMods.armor, UnitModifierFlatType.Total, getAmount(), apply);
            }
        } else {
            for (var i = (byte) SpellSchool.NORMAL.getValue(); i < (byte) SpellSchool.max.getValue(); i++) {
                if ((boolean) (getMiscValue() & (1 << i))) {
                    target.handleStatFlatModifier(UnitMods.ResistanceStart + i, UnitModifierFlatType.Total, getAmount(), apply);
                }
            }
        }
    }

    
    private void handleModTargetResistance(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        // applied to damage as HandleNoImmediateEffect in unit.CalcAbsorbResist and unit.CalcArmorReducedDamage

        // show armor penetration
        if (target.isTypeId(TypeId.PLAYER) && (boolean) (getMiscValue() & spellSchoolMask.NORMAL.getValue())) {
            target.applyModTargetPhysicalResistance(getAmountAsInt(), apply);
        }

        // show as spell penetration only full spell penetration bonuses (all resistances except armor and holy
        if (target.isTypeId(TypeId.PLAYER) && (spellSchoolMask.forValue(getMiscValue()).getValue() & spellSchoolMask.spell.getValue()) == spellSchoolMask.spell.getValue()) {
            target.applyModTargetResistance(getAmountAsInt(), apply);
        }
    }

    /********************************/
    /***           STAT           ***/
    /********************************/

    private void handleAuraModStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        if (getMiscValue() < -2 || getMiscValue() > 4) {
            Log.outError(LogFilter.spells, "WARNING: Spell {0} effect {1} has an unsupported misc value ({2}) for SPELL_AURA_MOD_STAT ", getId(), getEffIndex(), getMiscValue());

            return;
        }

        var target = aurApp.getTarget();
        var spellGroupVal = target.getHighestExclusiveSameEffectSpellGroupValue(this, AuraType.ModStat, true, getMiscValue());

        if (Math.abs(spellGroupVal) >= Math.abs(getAmount())) {
            return;
        }

        for (var i = stats.Strength; i.getValue() < stats.max.getValue(); i++) {
            // -1 or -2 is all stats (misc < -2 checked in function beginning)
            if (getMiscValue() < 0 || getMiscValue() == i.getValue()) {
                if (spellGroupVal != 0) {
                    target.handleStatFlatModifier((UnitMods.StatStart + i.getValue()), UnitModifierFlatType.Total, (double) spellGroupVal, !apply);

                    if (target.isTypeId(TypeId.PLAYER) || target.isPet()) {
                        target.updateStatBuffMod(i);
                    }
                }

                target.handleStatFlatModifier(UnitMods.StatStart + i.getValue(), UnitModifierFlatType.Total, getAmount(), apply);

                if (target.isTypeId(TypeId.PLAYER) || target.isPet()) {
                    target.updateStatBuffMod(i);
                }
            }
        }
    }

    
    private void handleModPercentStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (getMiscValue() < -1 || getMiscValue() > 4) {
            Log.outError(LogFilter.spells, "WARNING: Misc Value for SPELL_AURA_MOD_PERCENT_STAT not valid");

            return;
        }

        // only players have base stats
        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        for (var i = stats.Strength.getValue(); i < stats.max.getValue(); ++i) {
            if (getMiscValue() == i || getMiscValue() == -1) {
                if (apply) {
                    target.applyStatPctModifier(UnitMods.StatStart + i, UnitModifierPctType.base, getAmount());
                } else {
                    var amount = target.getTotalAuraMultiplier(AuraType.ModPercentStat, aurEff ->
                    {
                        if (aurEff.miscValue == i || aurEff.miscValue == -1) {
                            return true;
                        }

                        return false;
                    });

                    target.setStatPctModifier(UnitMods.StatStart + i, UnitModifierPctType.base, amount);
                }
            }
        }
    }

    
    private void handleModSpellDamagePercentFromStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // Magic damage modifiers implemented in unit.SpellDamageBonus
        // This information for client side use only
        // Recalculate bonus
        target.toPlayer().updateSpellDamageAndHealingBonus();
    }

    
    private void handleModSpellHealingPercentFromStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // Recalculate bonus
        target.toPlayer().updateSpellDamageAndHealingBonus();
    }

    
    private void handleModHealingDone(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // implemented in unit.SpellHealingBonus
        // this information is for client side only
        target.toPlayer().updateSpellDamageAndHealingBonus();
    }

    
    private void handleModHealingDonePct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (player) {
            player.updateHealingDonePercentMod();
        }
    }

    
    private void handleModTotalPercentStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        // save current health state
        double healthPct = target.getHealthPct();
        var zeroHealth = !target.isAlive();

        // players in corpse state may mean two different states:
        /** 1. player just died but did not release (in this case health == 0)
         2. player is corpse running (ie ghost) (in this case health == 1)
         */
        if (target.deathState == deathState.Corpse) {
            zeroHealth = target.getHealth() == 0;
        }

        for (var i = stats.Strength.getValue(); i < stats.max.getValue(); i++) {
            if ((boolean) (getMiscValueB() & 1 << i) || getMiscValueB() == 0) // 0 is also used for all stats
            {
                var amount = target.getTotalAuraMultiplier(AuraType.ModTotalStatPercentage, aurEff ->
                {
                    if ((aurEff.miscValueB & 1 << i) != 0 || aurEff.miscValueB == 0) {
                        return true;
                    }

                    return false;
                });

                if (target.getPctModifierValue(UnitMods.StatStart + i, UnitModifierPctType.Total) == amount) {
                    continue;
                }

                target.setStatPctModifier(UnitMods.StatStart + i, UnitModifierPctType.Total, amount);

                if (target.isTypeId(TypeId.PLAYER) || target.isPet()) {
                    target.updateStatBuffMod(stats.forValue(i));
                }
            }
        }

        // recalculate current HP/MP after applying aura modifications (only for spells with SPELL_ATTR0_ABILITY 0x00000010 flag)
        // this check is total bullshit i think
        if (((boolean) (getMiscValueB() & 1 << stats.Stamina.getValue()) || getMiscValueB() == 0) && spellInfo.hasAttribute(SpellAttr0.IsAbility)) {
            target.setHealth(Math.max(MathUtil.CalculatePct(target.getMaxHealth(), healthPct), (zeroHealth ? 0 : 1L)));
        }
    }

    
    private void handleAuraModExpertise(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        target.toPlayer().updateExpertise(WeaponAttackType.BASE_ATTACK);
        target.toPlayer().updateExpertise(WeaponAttackType.OFF_ATTACK);
    }

    // Increase armor by <AuraEffect.basePoints> % of your <primary stat>

    private void handleModArmorPctFromStat(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        // only players have primary stats
        var player = aurApp.getTarget().toPlayer();

        if (!player) {
            return;
        }

        player.updateArmor();
    }

    
    private void handleModBonusArmor(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        aurApp.getTarget().handleStatFlatModifier(UnitMods.armor, UnitModifierFlatType.base, getAmount(), apply);
    }

    
    private void handleModBonusArmorPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        aurApp.getTarget().updateArmor();
    }

    
    private void handleModStatBonusPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (getMiscValue() < -1 || getMiscValue() > 4) {
            Log.outError(LogFilter.spells, "WARNING: Misc Value for SPELL_AURA_MOD_STAT_BONUS_PCT not valid");

            return;
        }

        // only players have base stats
        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        for (var stat = stats.Strength; stat.getValue() < stats.max.getValue(); ++stat) {
            if (getMiscValue() == stat.getValue() || getMiscValue() == -1) {
                target.handleStatFlatModifier(UnitMods.StatStart + stat.getValue(), UnitModifierFlatType.BasePCTExcludeCreate, getAmount(), apply);
                target.updateStatBuffMod(stat);
            }
        }
    }

    
    private void handleOverrideSpellPowerByAttackPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (!target) {
            return;
        }

        target.applyModOverrideSpellPowerByAPPercent(getAmountAsFloat(), apply);
        target.updateSpellDamageAndHealingBonus();
    }

    
    private void handleOverrideAttackPowerBySpellPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (!target) {
            return;
        }

        target.applyModOverrideAPBySpellPowerPercent(getAmountAsFloat(), apply);
        target.updateAttackPowerAndDamage();
        target.updateAttackPowerAndDamage(true);
    }

    
    private void handleModVersatilityByPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target) {
            target.setVersatilityBonus((float) target.getTotalAuraModifier(AuraType.ModVersatility));
            target.updateHealingDonePercentMod();
            target.updateVersatilityDamageDone();
        }
    }

    
    private void handleAuraModMaxPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        var power = powerType.forValue((byte) getMiscValue());
        var unitMod = UnitMods.forValue(UnitMods.PowerStart + power.getValue());

        target.handleStatFlatModifier(unitMod, UnitModifierFlatType.Total, getAmount(), apply);
    }

    /********************************/
    /***      HEAL & ENERGIZE     ***/
    /********************************/

    private void handleModPowerRegen(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // Update manaregen value
        if (getMiscValue() == powerType.mana.getValue()) {
            target.toPlayer().updateManaRegen();
        } else if (getMiscValue() == powerType.runes.getValue()) {
            target.toPlayer().updateAllRunesRegen();
        }
        // other powers are not immediate effects - implemented in player.Regenerate, CREATURE.Regenerate
    }

    
    private void handleModPowerRegenPCT(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        handleModPowerRegen(aurApp, mode, apply);
    }

    
    private void handleModManaRegenPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isPlayer()) {
            return;
        }

        target.toPlayer().updateManaRegen();
    }

    
    private void handleAuraModIncreaseHealth(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        var amt = apply ? getAmountAsLong() : -getAmountAsLong();

        if (amt < 0) {
            target.modifyHealth(Math.max(1L - target.getHealth(), amt));
        }

        target.handleStatFlatModifier(UnitMods.health, UnitModifierFlatType.Total, getAmount(), apply);

        if (amt > 0) {
            target.modifyHealth(amt);
        }
    }

    private void handleAuraModIncreaseMaxHealth(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        double percent = target.getHealthPct();

        target.handleStatFlatModifier(UnitMods.health, UnitModifierFlatType.Total, getAmount(), apply);

        // refresh percentage
        if (target.getHealth() > 0) {
            var newHealth = Math.max(target.countPctFromMaxHealth(percent), 1);
            target.setHealth(newHealth);
        }
    }

    
    private void handleAuraModIncreaseEnergy(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();
        var powerType = powerType.forValue((byte) getMiscValue());

        var unitMod = (UnitMods.PowerStart + powerType.getValue());
        target.handleStatFlatModifier(unitMod, UnitModifierFlatType.Total, getAmount(), apply);
    }

    
    private void handleAuraModIncreaseEnergyPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();
        var powerType = powerType.forValue((byte) getMiscValue());

        var unitMod = UnitMods.PowerStart + powerType.getValue();

        // Save old powers for further calculation
        var oldPower = target.getPower(powerType);
        var oldMaxPower = target.getMaxPower(powerType);

        // Handle aura effect for max power
        if (apply) {
            target.applyStatPctModifier(unitMod, UnitModifierPctType.Total, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModIncreaseEnergyPercent, aurEff ->
            {
                if (aurEff.miscValue == powerType.getValue()) {
                    return true;
                }

                return false;
            });

            amount *= target.getTotalAuraMultiplier(AuraType.ModMaxPowerPct, aurEff ->
            {
                if (aurEff.miscValue == powerType.getValue()) {
                    return true;
                }

                return false;
            });

            target.setStatPctModifier(unitMod, UnitModifierPctType.Total, amount);
        }

        // Calculate the current power change
        var change = target.getMaxPower(powerType) - oldMaxPower;
        change = (oldPower + change) - target.getPower(powerType);
    }

    
    private void handleAuraModIncreaseHealthPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        // Unit will keep hp% after MaxHealth being modified if unit is alive.
        double percent = target.getHealthPct();

        if (apply) {
            target.applyStatPctModifier(UnitMods.health, UnitModifierPctType.Total, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModIncreaseHealthPercent) * target.getTotalAuraMultiplier(AuraType.ModIncreaseHealthPercent2);
            target.setStatPctModifier(UnitMods.health, UnitModifierPctType.Total, amount);
        }

        if (target.getHealth() > 0) {
            var newHealth = Math.max(MathUtil.CalculatePct(target.getMaxHealth(), percent), 1);
            target.setHealth(newHealth);
        }
    }

    
    private void handleAuraIncreaseBaseHealthPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.applyStatPctModifier(UnitMods.health, UnitModifierPctType.base, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModBaseHealthPct);
            target.setStatPctModifier(UnitMods.health, UnitModifierPctType.base, amount);
        }
    }

    
    private void handleAuraModIncreaseBaseManaPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.applyStatPctModifier(UnitMods.mana, UnitModifierPctType.base, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModBaseManaPct);
            target.setStatPctModifier(UnitMods.mana, UnitModifierPctType.base, amount);
        }
    }

    
    private void handleModManaCostPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        aurApp.getTarget().applyModManaCostMultiplier(getAmountAsFloat() / 100.0f, apply);
    }

    
    private void handleAuraModPowerDisplay(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.RealOrReapplyMask)) {
            return;
        }

        if (getMiscValue() >= powerType.max.getValue()) {
            return;
        }

        if (apply) {
            aurApp.getTarget().removeAurasByType(getAuraType(), ObjectGuid.Empty, getBase());
        }

        aurApp.getTarget().updateDisplayPower();
    }

    
    private void handleAuraModOverridePowerDisplay(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var powerDisplay = CliDB.PowerDisplayStorage.get(getMiscValue());

        if (powerDisplay == null) {
            return;
        }

        var target = aurApp.getTarget();

        if (target.getPowerIndex(powerType.forValue(powerDisplay.ActualType)) == powerType.max.getValue()) {
            return;
        }

        if (apply) {
            target.removeAurasByType(getAuraType(), ObjectGuid.Empty, getBase());
            target.setOverrideDisplayPowerId(powerDisplay.id);
        } else {
            target.setOverrideDisplayPowerId(0);
        }
    }

    
    private void handleAuraModMaxPowerPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isPlayer()) {
            return;
        }

        var powerType = powerType.forValue((byte) getMiscValue());
        var unitMod = UnitMods.PowerStart + powerType.getValue();

        // Save old powers for further calculation
        var oldPower = target.getPower(powerType);
        var oldMaxPower = target.getMaxPower(powerType);

        // Handle aura effect for max power
        if (apply) {
            target.applyStatPctModifier(unitMod, UnitModifierPctType.Total, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModMaxPowerPct, aurEff ->
            {
                if (aurEff.miscValue == powerType.getValue()) {
                    return true;
                }

                return false;
            });

            amount *= target.getTotalAuraMultiplier(AuraType.ModIncreaseEnergyPercent, aurEff ->
            {
                if (aurEff.miscValue == powerType.getValue()) {
                    return true;
                }

                return false;
            });

            target.setStatPctModifier(unitMod, UnitModifierPctType.Total, amount);
        }

        // Calculate the current power change
        var change = target.getMaxPower(powerType) - oldMaxPower;
        change = (oldPower + change) - target.getPower(powerType);
        target.modifyPower(powerType, change);
    }

    
    private void handleTriggerSpellOnHealthPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real) || !apply) {
            return;
        }

        var target = aurApp.getTarget();
        var thresholdPct = getAmount();
        var triggerSpell = getSpellEffectInfo().triggerSpell;

        switch (AuraTriggerOnHealthChangeDirection.forValue(getMiscValue())) {
            case Above:
                if (!target.healthAbovePct(thresholdPct)) {
                    return;
                }

                break;
            case Below:
                if (!target.healthBelowPct(thresholdPct)) {
                    return;
                }

                break;
            default:
                break;
        }

        target.castSpell(target, triggerSpell, new CastSpellExtraArgs(this));
    }

    /********************************/
    /***          FIGHT           ***/
    /********************************/

    private void handleAuraModParryPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        target.toPlayer().updateParryPercentage();
    }

    
    private void handleAuraModDodgePercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        target.toPlayer().updateDodgePercentage();
    }

    
    private void handleAuraModBlockPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        target.toPlayer().updateBlockPercentage();
    }

    
    private void handleAuraModRegenInterrupt(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isPlayer()) {
            return;
        }

        target.toPlayer().updateManaRegen();
    }

    
    private void handleAuraModWeaponCritPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (!target) {
            return;
        }

        target.updateAllWeaponDependentCritAuras();
    }

    
    private void handleModSpellHitChance(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (target.isTypeId(TypeId.PLAYER)) {
            target.toPlayer().updateSpellHitChances();
        } else {
            target.setModSpellHitChance(target.getModSpellHitChance() + (apply) ? getAmount() : (-getAmount()));
        }
    }

    
    private void handleModSpellCritChance(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (target.isTypeId(TypeId.PLAYER)) {
            target.toPlayer().updateSpellCritChance();
        } else {
            target.setBaseSpellCritChance(target.getBaseSpellCritChance() + (apply) ? getAmount() : -getAmount());
        }
    }

    
    private void handleAuraModCritPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            target.setBaseSpellCritChance(target.getBaseSpellCritChance() + (apply) ? getAmount() : -getAmount());

            return;
        }

        target.toPlayer().updateAllWeaponDependentCritAuras();

        // included in player.UpdateSpellCritChance calculation
        target.toPlayer().updateSpellCritChance();
    }

    /********************************/
    /***         ATTACK SPEED     ***/
    /********************************/

    private void handleModCastingSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        // Do not apply such auras in normal way
        if (getAmount() >= 1000) {
            if (apply) {
                target.setInstantCast(true);
            } else {
                // only SPELL_AURA_MOD_CASTING_SPEED_NOT_STACK can have this high amount
                // it's some rare case that you have 2 auras like that, but just in case ;)

                var remove = true;
                var castingSpeedNotStack = target.getAuraEffectsByType(AuraType.ModCastingSpeedNotStack);

                for (var aurEff : castingSpeedNotStack) {
                    if (aurEff != this && aurEff.getAmount() >= 1000) {
                        remove = false;

                        break;
                    }
                }

                if (remove) {
                    target.setInstantCast(false);
                }
            }

            return;
        }

        var spellGroupVal = target.getHighestExclusiveSameEffectSpellGroupValue(this, getAuraType());

        if (Math.abs(spellGroupVal) >= Math.abs(getAmount())) {
            return;
        }

        if (spellGroupVal != 0) {
            target.applyCastTimePercentMod(spellGroupVal, !apply);
        }

        target.applyCastTimePercentMod(getAmount(), apply);
    }

    
    private void handleModMeleeRangedSpeedPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        //! ToDo: Haste auras with the same handler _CAN'T_ stack together
        var target = aurApp.getTarget();

        target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.OFF_ATTACK, getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.RangedAttack, getAmount(), apply);
    }

    
    private void handleModCombatSpeedPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();
        var spellGroupVal = target.getHighestExclusiveSameEffectSpellGroupValue(this, AuraType.MeleeSlow);

        if (Math.abs(spellGroupVal) >= Math.abs(getAmount())) {
            return;
        }

        if (spellGroupVal != 0) {
            target.applyCastTimePercentMod(spellGroupVal, !apply);
            target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, spellGroupVal, !apply);
            target.applyAttackTimePercentMod(WeaponAttackType.OFF_ATTACK, spellGroupVal, !apply);
            target.applyAttackTimePercentMod(WeaponAttackType.RangedAttack, spellGroupVal, !apply);
        }

        target.applyCastTimePercentMod(getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.OFF_ATTACK, getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.RangedAttack, getAmount(), apply);
    }

    
    private void handleModAttackSpeed(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, getAmount(), apply);
        target.updateDamagePhysical(WeaponAttackType.BASE_ATTACK);
    }

    
    private void handleModMeleeSpeedPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        //! ToDo: Haste auras with the same handler _CAN'T_ stack together
        var target = aurApp.getTarget();
        var spellGroupVal = target.getHighestExclusiveSameEffectSpellGroupValue(this, AuraType.ModMeleeHaste);

        if (Math.abs(spellGroupVal) >= Math.abs(getAmount())) {
            return;
        }

        if (spellGroupVal != 0) {
            target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, spellGroupVal, !apply);
            target.applyAttackTimePercentMod(WeaponAttackType.OFF_ATTACK, spellGroupVal, !apply);
        }

        target.applyAttackTimePercentMod(WeaponAttackType.BASE_ATTACK, getAmount(), apply);
        target.applyAttackTimePercentMod(WeaponAttackType.OFF_ATTACK, getAmount(), apply);
    }

    
    private void handleAuraModRangedHaste(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        //! ToDo: Haste auras with the same handler _CAN'T_ stack together
        var target = aurApp.getTarget();

        target.applyAttackTimePercentMod(WeaponAttackType.RangedAttack, getAmount(), apply);
    }

    /********************************/
    /***       COMBAT RATING      ***/
    /********************************/

    private void handleModRating(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        for (var rating = 0; rating < CombatRating.max.getValue(); ++rating) {
            if ((boolean) (getMiscValue() & (1 << rating))) {
                target.toPlayer().applyRatingMod(CombatRating.forValue(rating), getAmountAsInt(), apply);
            }
        }
    }

    
    private void handleModRatingPct(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        // Just recalculate ratings
        for (var rating = 0; rating < CombatRating.max.getValue(); ++rating) {
            if ((boolean) (getMiscValue() & (1 << rating))) {
                target.toPlayer().updateRating(CombatRating.forValue(rating));
            }
        }
    }

    /********************************/
    /***        ATTACK POWER      ***/
    /********************************/

    private void handleAuraModAttackPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        target.handleStatFlatModifier(UnitMods.attackPower, UnitModifierFlatType.Total, getAmount(), apply);
    }

    
    private void handleAuraModRangedAttackPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if ((target.getClassMask() & (int) UnitClass.ClassMaskWandUsers.getValue()) != 0) {
            return;
        }

        target.handleStatFlatModifier(UnitMods.AttackPowerRanged, UnitModifierFlatType.Total, getAmount(), apply);
    }

    
    private void handleAuraModAttackPowerPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        //UNIT_FIELD_ATTACK_POWER_MULTIPLIER = multiplier - 1
        if (apply) {
            target.applyStatPctModifier(UnitMods.attackPower, UnitModifierPctType.Total, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModAttackPowerPct);
            target.setStatPctModifier(UnitMods.attackPower, UnitModifierPctType.Total, amount);
        }
    }

    
    private void handleAuraModRangedAttackPowerPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if ((target.getClassMask() & (int) UnitClass.ClassMaskWandUsers.getValue()) != 0) {
            return;
        }

        //UNIT_FIELD_RANGED_ATTACK_POWER_MULTIPLIER = multiplier - 1
        if (apply) {
            target.applyStatPctModifier(UnitMods.AttackPowerRanged, UnitModifierPctType.Total, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModRangedAttackPowerPct);
            target.setStatPctModifier(UnitMods.AttackPowerRanged, UnitModifierPctType.Total, amount);
        }
    }

    /********************************/
    /***        DAMAGE BONUS      ***/
    /********************************/

    private void handleModDamageDone(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        if ((getMiscValue() & spellSchoolMask.NORMAL.getValue()) != 0) {
            target.updateAllDamageDoneMods();
        }

        // Magic damage modifiers implemented in Unit::SpellBaseDamageBonusDone
        // This information for client side use only
        var playerTarget = target.toPlayer();

        if (playerTarget != null) {
            for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
                if ((boolean) (getMiscValue() & (1 << i))) {
                    if (getAmount() >= 0) {
                        playerTarget.applyModDamageDonePos(SpellSchool.forValue(i), getAmountAsInt(), apply);
                    } else {
                        playerTarget.applyModDamageDoneNeg(SpellSchool.forValue(i), getAmountAsInt(), apply);
                    }
                }
            }

            var pet = playerTarget.getGuardianPet();

            if (pet) {
                pet.updateAttackPowerAndDamage();
            }
        }
    }

    
    private void handleModDamagePercentDone(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        // also handles spell group stacks
        if ((boolean) (getMiscValue() & spellSchoolMask.NORMAL.getValue())) {
            target.updateAllDamagePctDoneMods();
        }

        var thisPlayer = target.toPlayer();

        if (thisPlayer != null) {
            for (var i = SpellSchool.NORMAL; i.getValue() < SpellSchool.max.getValue(); ++i) {
                if ((boolean) (getMiscValue() & (1 << i.getValue()))) {
                    // only aura type modifying PLAYER_FIELD_MOD_DAMAGE_DONE_PCT
                    var amount = thisPlayer.getTotalAuraMultiplierByMiscMask(AuraType.ModDamagePercentDone, 1 << i.getValue());
                    thisPlayer.setModDamageDonePercent(i, (float) amount);
                }
            }
        }
    }

    
    private void handleModOffhandDamagePercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        // also handles spell group stacks
        target.updateDamagePctDoneMods(WeaponAttackType.OFF_ATTACK);
    }

    private void handleShieldBlockValue(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue())) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (player != null) {
            player.handleBaseModFlatValue(BaseModGroup.ShieldBlockValue, getAmount(), apply);
        }
    }

    
    private void handleShieldBlockValuePercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Stat.getValue()))) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (!target) {
            return;
        }

        if (apply) {
            target.applyBaseModPctValue(BaseModGroup.ShieldBlockValue, getAmount());
        } else {
            var amount = target.getTotalAuraMultiplier(AuraType.ModShieldBlockvaluePct);
            target.setBaseModPctValue(BaseModGroup.ShieldBlockValue, amount);
        }
    }

    /********************************/
    /***        POWER COST        ***/
    /********************************/

    private void handleModPowerCost(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        // handled in SpellInfo::CalcPowerCost, this is only for client UI
        if ((getMiscValueB() & (1 << powerType.mana.getValue())) == 0) {
            return;
        }

        var target = aurApp.getTarget();

        for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
            if ((boolean) (getMiscValue() & (1 << i))) {
                target.applyModManaCostModifier(SpellSchool.forValue(i), getAmountAsInt(), apply);
            }
        }
    }

    
    private void handleArenaPreparation(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag(UnitFlag.preparation);
        } else {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }

            target.removeUnitFlag(UnitFlag.preparation);
        }

        target.modifyAuraState(AuraStateType.ArenaPreparation, apply);
    }

    
    private void handleNoReagentUseAura(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        Flag128 mask = new Flag128();
        var noReagent = target.getAuraEffectsByType(AuraType.NoReagentUse);

        for (var eff : noReagent) {
            var effect = eff.getSpellEffectInfo();

            if (effect != null) {
                mask |= effect.spellClassMask;
            }
        }

        target.toPlayer().setNoRegentCostMask(mask);
    }

    /*********************************************************/
    /***                    OTHERS                         ***/
    /*********************************************************/

    private void handleAuraDummy(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag((AuraEffectHandleModes.ChangeAmountMask.getValue() | AuraEffectHandleModes.Reapply.getValue()))) {
            return;
        }

        var target = aurApp.getTarget();

        var caster = getCaster();

        // pet auras
        if (target.getObjectTypeId() == TypeId.PLAYER && mode.hasFlag(AuraEffectHandleModes.Real)) {
            var petSpell = global.getSpellMgr().getPetAura(getId(), (byte) getEffIndex());

            if (petSpell != null) {
                if (apply) {
                    target.toPlayer().addPetAura(petSpell);
                } else {
                    target.toPlayer().removePetAura(petSpell);
                }
            }
        }

        if (mode.hasFlag(AuraEffectHandleModes.Real.getValue() | AuraEffectHandleModes.Reapply.getValue())) {
            // AT APPLY
            if (apply) {
                switch (getId()) {
                    case 1515: // Tame beast
                        // FIX_ME: this is 2.0.12 threat effect replaced in 2.1.x by dummy aura, must be checked for correctness
                        if (caster != null && target.getCanHaveThreatList()) {
                            target.getThreatManager().addThreat(caster, 10.0f);
                        }

                        break;
                    case 13139: // net-o-matic
                        // root to self part of (root_target.charge.root_self sequence
                        if (caster != null) {
                            caster.castSpell(caster, 13138, new CastSpellExtraArgs(this));
                        }

                        break;
                    case 34026: // kill command
                    {
                        Unit pet = target.getGuardianPet();

                        if (pet == null) {
                            break;
                        }

                        target.castSpell(target, 34027, new CastSpellExtraArgs(this));

                        // set 3 stacks and 3 charges (to make all auras not disappear at once)
                        var owner_aura = target.getAura(34027, getCasterGuid());
                        var pet_aura = pet.getAura(58914, getCasterGuid());

                        if (owner_aura != null) {
                            owner_aura.setStackAmount((byte) owner_aura.spellInfo.stackAmount);

                            if (pet_aura != null) {
                                pet_aura.setCharges(0);
                                pet_aura.setStackAmount((byte) owner_aura.spellInfo.stackAmount);
                            }
                        }

                        break;
                    }
                    case 37096: // Blood Elf Illusion
                    {
                        if (caster != null) {
                            if (caster.getGender() == gender.Female) {
                                caster.castSpell(target, 37095, new CastSpellExtraArgs(this)); // Blood Elf Disguise
                            } else {
                                caster.castSpell(target, 37093, new CastSpellExtraArgs(this));
                            }
                        }

                        break;
                    }
                    case 39850: // Rocket Blast
                        if (RandomUtil.randChance(20)) // backfire stun
                        {
                            target.castSpell(target, 51581, new CastSpellExtraArgs(this));
                        }

                        break;
                    case 43873: // Headless Horseman Laugh
                        target.playDistanceSound(11965);

                        break;
                    case 46354: // Blood Elf Illusion
                        if (caster != null) {
                            if (caster.getGender() == gender.Female) {
                                caster.castSpell(target, 46356, new CastSpellExtraArgs(this));
                            } else {
                                caster.castSpell(target, 46355, new CastSpellExtraArgs(this));
                            }
                        }

                        break;
                    case 46361: // Reinforced Net
                        if (caster != null) {
                            target.getMotionMaster().moveFall();
                        }

                        break;
                }
            }
            // AT REMOVE
            else {
                switch (spellInfo.getSpellFamilyName()) {
                    case Generic:
                        switch (getId()) {
                            case 2584: // Waiting to Resurrect
                                // Waiting to resurrect spell cancel, we must remove player from resurrect queue
                                if (target.isTypeId(TypeId.PLAYER)) {
                                    var bg = target.toPlayer().getBattleground();

                                    if (bg) {
                                        bg.removePlayerFromResurrectQueue(target.getGUID());
                                    }

                                    var bf = global.getBattleFieldMgr().getBattlefieldToZoneId(target.getMap(), target.getZoneId());

                                    if (bf != null) {
                                        bf.removePlayerFromResurrectQueue(target.getGUID());
                                    }
                                }

                                break;
                            case 36730: // Flame Strike
                                target.castSpell(target, 36731, new CastSpellExtraArgs(this));

                                break;
                            case 43681: // Inactive
                            {
                                if (!target.isTypeId(TypeId.PLAYER) || aurApp.getRemoveMode() != AuraRemoveMode.Expire) {
                                    return;
                                }

                                if (target.getMap().isBattleground()) {
                                    target.toPlayer().leaveBattleground();
                                }

                                break;
                            }
                            case 42783: // Wrath of the Astromancer
                                target.castSpell(target, (int) getAmount(), new CastSpellExtraArgs(this));

                                break;
                            case 46308: // Burning Winds casted only at creatures at spawn
                                target.castSpell(target, 47287, new CastSpellExtraArgs(this));

                                break;
                            case 52172: // Coyote Spirit Despawn Aura
                            case 60244: // Blood Parrot Despawn Aura
                                target.castSpell((unit) null, (int) getAmount(), new CastSpellExtraArgs(this));

                                break;
                            case 91604: // Restricted Flight Area
                                if (aurApp.getRemoveMode() == AuraRemoveMode.Expire) {
                                    target.castSpell(target, 58601, new CastSpellExtraArgs(this));
                                }

                                break;
                        }

                        break;
                    case Deathknight:
                        // Summon Gargoyle (Dismiss Gargoyle at remove)
                        if (getId() == 61777) {
                            target.castSpell(target, (int) getAmount(), new CastSpellExtraArgs(this));
                        }

                        break;
                    default:
                        break;
                }
            }
        }

        // AT APPLY & REMOVE

        switch (spellInfo.getSpellFamilyName()) {
            case Generic: {
                if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
                    break;
                }

                switch (getId()) {
                    // Recently Bandaged
                    case 11196:
                        target.applySpellImmune(getId(), SpellImmunity.mechanic, (int) getMiscValue(), apply);

                        break;
                    // Unstable Power
                    case 24658: {
                        int spellId = 24659;

                        if (apply && caster != null) {
                            var spell = global.getSpellMgr().getSpellInfo(spellId, getBase().getCastDifficulty());
                            CastSpellExtraArgs args = new CastSpellExtraArgs();
                            args.triggerFlags = TriggerCastFlags.FullMask;
                            args.originalCaster = getCasterGuid();
                            args.originalCastId = getBase().getCastId();
                            args.castDifficulty = getBase().getCastDifficulty();

                            for (int i = 0; i < spell.getStackAmount(); ++i) {
                                caster.castSpell(target, spell.getId(), args);
                            }

                            break;
                        }

                        target.removeAura(spellId);

                        break;
                    }
                    // Restless Strength
                    case 24661: {
                        int spellId = 24662;

                        if (apply && caster != null) {
                            var spell = global.getSpellMgr().getSpellInfo(spellId, getBase().getCastDifficulty());
                            CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlags.FullMask);
                            args.originalCaster = getCasterGuid();
                            args.originalCastId = getBase().getCastId();
                            args.castDifficulty = getBase().getCastDifficulty();

                            for (int i = 0; i < spell.getStackAmount(); ++i) {
                                caster.castSpell(target, spell.getId(), args);
                            }

                            break;
                        }

                        target.removeAura(spellId);

                        break;
                    }
                    // Tag Murloc
                    case 30877: {
                        // Tag/untag Blacksilt Scout
                        target.setEntry((int) (apply ? 17654 : 17326));

                        break;
                    }
                    case 57819: // Argent Champion
                    case 57820: // Ebon Champion
                    case 57821: // Champion of the Kirin Tor
                    case 57822: // Wyrmrest Champion
                    {
                        if (!caster || !caster.isTypeId(TypeId.PLAYER)) {
                            break;
                        }

                        int factionID = 0;

                        if (apply) {
                            switch (spellInfo.getId()) {
                                case 57819:
                                    factionID = 1106; // Argent Crusade

                                    break;
                                case 57820:
                                    factionID = 1098; // Knights of the Ebon Blade

                                    break;
                                case 57821:
                                    factionID = 1090; // Kirin Tor

                                    break;
                                case 57822:
                                    factionID = 1091; // The Wyrmrest Accord

                                    break;
                            }
                        }

                        caster.toPlayer().setChampioningFaction(factionID);

                        break;
                    }
                    // LK Intro VO (1)
                    case 58204:
                        if (target.isTypeId(TypeId.PLAYER)) {
                            // Play part 1
                            if (apply) {
                                target.playDirectSound(14970, target.toPlayer());
                            }
                            // continue in 58205
                            else {
                                target.castSpell(target, 58205, new CastSpellExtraArgs(this));
                            }
                        }

                        break;
                    // LK Intro VO (2)
                    case 58205:
                        if (target.isTypeId(TypeId.PLAYER)) {
                            // Play part 2
                            if (apply) {
                                target.playDirectSound(14971, target.toPlayer());
                            }
                            // Play part 3
                            else {
                                target.playDirectSound(14972, target.toPlayer());
                            }
                        }

                        break;
                }

                break;
            }
        }
    }

    
    private void handleChannelDeathItem(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        if (apply || aurApp.getRemoveMode() != AuraRemoveMode.Death) {
            return;
        }

        var caster = getCaster();

        if (caster == null || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        var plCaster = caster.toPlayer();
        var target = aurApp.getTarget();

        // Item amount
        if (getAmount() <= 0) {
            return;
        }

        if (getSpellEffectInfo().itemType == 0) {
            return;
        }

        // Soul Shard
        if (getSpellEffectInfo().itemType == 6265) {
            // Soul Shard only from units that grant XP or honor
            if (!plCaster.isHonorOrXPTarget(target) || (target.isTypeId(TypeId.UNIT) && !target.toCreature().isTappedBy(plCaster))) {
                return;
            }
        }

        //Adding items
        var count = (int) getAmount();

        ArrayList<ItemPosCount> dest = new ArrayList<>();
        int noSpaceForCount;
        tangible.OutObject<Integer> tempOut_noSpaceForCount = new tangible.OutObject<Integer>();
        var msg = plCaster.canStoreNewItem(ItemConst.NullBag, ItemConst.NullSlot, dest, getSpellEffectInfo().itemType, count, tempOut_noSpaceForCount);
        noSpaceForCount = tempOut_noSpaceForCount.outArgValue;

        if (msg != InventoryResult.Ok) {
            count -= noSpaceForCount;
            plCaster.sendEquipError(msg, null, null, getSpellEffectInfo().itemType);

            if (count == 0) {
                return;
            }
        }

        var newitem = plCaster.storeNewItem(dest, getSpellEffectInfo().itemType, true);

        if (newitem == null) {
            plCaster.sendEquipError(InventoryResult.ItemNotFound);

            return;
        }

        plCaster.sendNewItem(newitem, count, true, true);
    }

    
    private void handleBindSight(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        var caster = getCaster();

        if (caster == null || !caster.isTypeId(TypeId.PLAYER)) {
            return;
        }

        caster.toPlayer().setViewpoint(target, apply);
    }

    
    private void handleForceReaction(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        var player = target.toPlayer();

        if (player == null) {
            return;
        }

        var factionId = (int) getMiscValue();
        var factionRank = ReputationRank.forValue(getAmount());

        player.getReputationMgr().applyForceReaction(factionId, factionRank, apply);
        player.getReputationMgr().sendForceReactions();

        // stop fighting at apply (if forced rank friendly) or at remove (if real rank friendly)
        if ((apply && factionRank.getValue() >= ReputationRank.Friendly.getValue()) || (!apply && player.getReputationRank(factionId) >= ReputationRank.Friendly.getValue())) {
            player.stopAttackFaction(factionId);
        }
    }

    
    private void handleAuraEmpathy(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!apply) {
            // do not remove unit flag if there are more than this auraEffect of that kind on unit on unit
            if (target.hasAuraType(getAuraType())) {
                return;
            }
        }

        if (target.getCreatureType() == creatureType.Beast) {
            if (apply) {
                target.setDynamicFlag(UnitDynFlag.SpecialInfo);
            } else {
                target.removeDynamicFlag(UnitDynFlag.SpecialInfo);
            }
        }
    }

    
    private void handleAuraModFaction(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setFaction((int) getMiscValue());

            if (target.isTypeId(TypeId.PLAYER)) {
                target.removeUnitFlag(UnitFlag.PlayerControlled);
            }
        } else {
            target.restoreFaction();

            if (target.isTypeId(TypeId.PLAYER)) {
                target.setUnitFlag(UnitFlag.PlayerControlled);
            }
        }
    }

    
    private void handleLearnSpell(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (player == null) {
            return;
        }

        if (apply) {
            player.learnSpell((int) getMiscValue(), true, 0, true);
        } else {
            player.removeSpell((int) getMiscValue(), false, false, true);
        }
    }

    
    private void handleComprehendLanguage(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag2(UnitFlag2.ComprehendLang);
        } else {
            if (target.hasAuraType(getAuraType())) {
                return;
            }

            target.removeUnitFlag2(UnitFlag2.ComprehendLang);
        }
    }

    
    private void handleModAlternativeDefaultLanguage(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.SendForClientMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.setUnitFlag3(unitFlags3.AlternativeDefaultLanguage);
        } else {
            if (target.hasAuraType(getAuraType())) {
                return;
            }

            target.removeUnitFlag3(unitFlags3.AlternativeDefaultLanguage);
        }
    }

    
    private void handleAuraLinked(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        var target = aurApp.getTarget();

        var triggeredSpellId = getSpellEffectInfo().triggerSpell;
        var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggeredSpellId, getBase().getCastDifficulty());

        if (triggeredSpellInfo == null) {
            return;
        }

        var caster = triggeredSpellInfo.needsToBeTriggeredByCaster(spellInfo) ? getCaster() : target;

        if (!caster) {
            return;
        }

        if (mode.hasFlag(AuraEffectHandleModes.Real)) {
            if (apply) {
                CastSpellExtraArgs args = new CastSpellExtraArgs(this);

                if (getAmount() != 0) // If amount avalible cast with basepoints (Crypt Fever for example)
                {
                    args.addSpellMod(SpellValueMod.BasePoint0, getAmount());
                }

                caster.castSpell(target, triggeredSpellId, args);
            } else {
                var casterGUID = triggeredSpellInfo.needsToBeTriggeredByCaster(spellInfo) ? getCasterGuid() : target.getGUID();
                target.removeAura(triggeredSpellId, casterGUID);
            }
        } else if (mode.hasFlag(AuraEffectHandleModes.Reapply) && apply) {
            var casterGUID = triggeredSpellInfo.needsToBeTriggeredByCaster(spellInfo) ? getCasterGuid() : target.getGUID();
            // change the stack amount to be equal to stack amount of our aura
            var triggeredAura = target.getAura(triggeredSpellId, casterGUID);

            if (triggeredAura != null) {
                triggeredAura.modStackAmount(getBase().getStackAmount() - triggeredAura.getStackAmount());
            }
        }
    }

    
    private void handleTriggerSpellOnPowerPercent(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real) || !apply) {
            return;
        }

        var target = aurApp.getTarget();

        var effectAmount = getAmount();
        var triggerSpell = getSpellEffectInfo().triggerSpell;
        double powerAmountPct = MathUtil.GetPctOf(target.getPower(powerType.forValue((byte) getMiscValue())), target.getMaxPower(powerType.forValue((byte) getMiscValue())));

        switch (AuraTriggerOnPowerChangeDirection.forValue(getMiscValueB())) {
            case Gain:
                if (powerAmountPct < effectAmount) {
                    return;
                }

                break;
            case Loss:
                if (powerAmountPct > effectAmount) {
                    return;
                }

                break;
            default:
                break;
        }

        target.castSpell(target, triggerSpell, new CastSpellExtraArgs(this));
    }

    
    private void handleTriggerSpellOnPowerAmount(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real) || !apply) {
            return;
        }

        var target = aurApp.getTarget();

        var effectAmount = getAmount();
        var triggerSpell = getSpellEffectInfo().triggerSpell;
        double powerAmount = target.getPower(powerType.forValue((byte) getMiscValue()));

        switch (AuraTriggerOnPowerChangeDirection.forValue(getMiscValueB())) {
            case Gain:
                if (powerAmount < effectAmount) {
                    return;
                }

                break;
            case Loss:
                if (powerAmount > effectAmount) {
                    return;
                }

                break;
            default:
                break;
        }

        target.castSpell(target, triggerSpell, new CastSpellExtraArgs(this));
    }

    
    private void handleTriggerSpellOnExpire(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real) || apply || aurApp.getRemoveMode() != AuraRemoveMode.Expire) {
            return;
        }

        var caster = aurApp.getTarget();

        if (getMiscValue() > 0) {
            caster = getCaster();
        }

        caster.castSpell(aurApp.getTarget(), getSpellEffectInfo().triggerSpell, new CastSpellExtraArgs(this));
    }

    
    private void handleAuraOpenStable(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isTypeId(TypeId.PLAYER) || !target.isInWorld()) {
            return;
        }

        if (apply) {
            target.toPlayer().getSession().sendStablePet(target.getGUID());
        }

        // client auto close stable dialog at !apply aura
    }

    
    private void handleAuraModFakeInebriation(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.ChangeAmountMask)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            target.getInvisibilityDetect().addFlag(InvisibilityType.drunk);
            target.getInvisibilityDetect().addValue(InvisibilityType.drunk, getAmountAsInt());

            var playerTarget = target.toPlayer();

            if (playerTarget) {
                playerTarget.applyModFakeInebriation(getAmountAsInt(), true);
            }
        } else {
            var removeDetect = !target.hasAuraType(AuraType.ModFakeInebriate);

            target.getInvisibilityDetect().addValue(InvisibilityType.drunk, -getAmountAsInt());

            var playerTarget = target.toPlayer();

            if (playerTarget != null) {
                playerTarget.applyModFakeInebriation(getAmountAsInt(), false);

                if (removeDetect) {
                    removeDetect = playerTarget.getDrunkValue() == 0;
                }
            }

            if (removeDetect) {
                target.getInvisibilityDetect().delFlag(InvisibilityType.drunk);
            }
        }

        // call functions which may have additional effects after changing state of unit
        if (target.isInWorld()) {
            target.updateObjectVisibility();
        }
    }

    
    private void handleAuraOverrideSpells(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null || !target.isInWorld()) {
            return;
        }

        var overrideId = (int) getMiscValue();

        if (apply) {
            target.setOverrideSpellsId(overrideId);
            var overrideSpells = CliDB.OverrideSpellDataStorage.get(overrideId);

            if (overrideSpells != null) {
                for (byte i = 0; i < SharedConst.MaxOverrideSpell; ++i) {
                    var spellId = overrideSpells.Spells[i];

                    if (spellId != 0) {
                        target.addTemporarySpell(spellId);
                    }
                }
            }
        } else {
            target.setOverrideSpellsId(0);
            var overrideSpells = CliDB.OverrideSpellDataStorage.get(overrideId);

            if (overrideSpells != null) {
                for (byte i = 0; i < SharedConst.MaxOverrideSpell; ++i) {
                    var spellId = overrideSpells.Spells[i];

                    if (spellId != 0) {
                        target.removeTemporarySpell(spellId);
                    }
                }
            }
        }
    }

    
    private void handleAuraSetVehicle(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (!target.isInWorld()) {
            return;
        }

        var vehicleId = getMiscValue();

        if (apply) {
            if (!target.createVehicleKit((int) vehicleId, 0)) {
                return;
            }
        } else if (target.getVehicleKit() != null) {
            target.removeVehicleKit();
        }

        if (!target.isTypeId(TypeId.PLAYER)) {
            return;
        }

        if (apply) {
            target.toPlayer().sendOnCancelExpectedVehicleRideAura();
        }
    }

    
    private void handlePreventResurrection(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.removePlayerLocalFlag(PlayerLocalFlags.ReleaseTimer);
        } else if (!target.getMap().isInstanceable()) {
            target.setPlayerLocalFlag(PlayerLocalFlags.ReleaseTimer);
        }
    }

    
    private void handleMastery(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        target.updateMastery();
    }

    private void handlePeriodicTriggerSpellAuraTick(Unit target, Unit caster) {
        var triggerSpellId = getSpellEffectInfo().triggerSpell;

        if (triggerSpellId == 0) {
            Log.outWarn(LogFilter.spells, String.format("AuraEffect::HandlePeriodicTriggerSpellAuraTick: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", getId(), getEffIndex()));

            return;
        }

        var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpellId, getBase().getCastDifficulty());

        if (triggeredSpellInfo != null) {
            var triggerCaster = triggeredSpellInfo.needsToBeTriggeredByCaster(spellInfo) ? caster : target;

            if (triggerCaster != null) {
                triggerCaster.castSpell(target, triggerSpellId, new CastSpellExtraArgs(this));
                Log.outDebug(LogFilter.spells, "AuraEffect.HandlePeriodicTriggerSpellAuraTick: Spell {0} Trigger {1}", getId(), triggeredSpellInfo.getId());
            }
        } else {
            Log.outError(LogFilter.spells, "AuraEffect.HandlePeriodicTriggerSpellAuraTick: Spell {0} has non-existent spell {1} in EffectTriggered[{2}] and is therefor not triggered.", getId(), triggerSpellId, getEffIndex());
        }
    }

    private void handlePeriodicTriggerSpellWithValueAuraTick(Unit target, Unit caster) {
        var triggerSpellId = getSpellEffectInfo().triggerSpell;

        if (triggerSpellId == 0) {
            Log.outWarn(LogFilter.spells, String.format("AuraEffect::HandlePeriodicTriggerSpellWithValueAuraTick: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", getId(), getEffIndex()));

            return;
        }

        var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpellId, getBase().getCastDifficulty());

        if (triggeredSpellInfo != null) {
            var triggerCaster = triggeredSpellInfo.needsToBeTriggeredByCaster(spellInfo) ? caster : target;

            if (triggerCaster != null) {
                CastSpellExtraArgs args = new CastSpellExtraArgs(this);

                for (var effect : triggeredSpellInfo.getEffects()) {
                    args.addSpellMod(SpellValueMod.BasePoint0 + effect.effectIndex, getAmount());
                }

                triggerCaster.castSpell(target, triggerSpellId, args);
                Log.outDebug(LogFilter.spells, "AuraEffect.HandlePeriodicTriggerSpellWithValueAuraTick: Spell {0} Trigger {1}", getId(), triggeredSpellInfo.getId());
            }
        } else {
            Log.outError(LogFilter.spells, "AuraEffect.HandlePeriodicTriggerSpellWithValueAuraTick: Spell {0} has non-existent spell {1} in EffectTriggered[{2}] and is therefor not triggered.", getId(), triggerSpellId, getEffIndex());
        }
    }

    private void handlePeriodicDamageAurasTick(Unit target, Unit caster) {
        if (!target.isAlive()) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated) || target.isImmunedToDamage(getSpellInfo())) {
            sendTickImmune(target, caster);

            return;
        }

        // Consecrate ticks can miss and will not show up in the combat log
        // dynobj auras must always have a caster
        if (getSpellEffectInfo().isEffect(SpellEffectName.PersistentAreaAura) && caster.spellHitResult(target, getSpellInfo(), false) != SpellMissInfo.NONE) {
            return;
        }

        CleanDamage cleanDamage = new cleanDamage(0, 0, WeaponAttackType.BASE_ATTACK, MeleeHitOutcome.NORMAL);

        var stackAmountForBonuses = !getSpellEffectInfo().effectAttributes.hasFlag(SpellEffectAttributes.NoScaleWithStack) ? getBase().getStackAmount() : 1;

        // ignore non positive values (can be result apply spellmods to aura damage
        var damage = Math.max(getAmount(), 0);

        // Script Hook For HandlePeriodicDamageAurasTick -- Allow scripts to change the Damage pre class mitigation calculations
        tangible.RefObject<Double> tempRef_damage = new tangible.RefObject<Double>(damage);
        global.getScriptMgr().<IUnitModifyPeriodicDamageAurasTick>ForEach(p -> p.ModifyPeriodicDamageAurasTick(target, caster, tempRef_damage));
        damage = tempRef_damage.refArgValue;

        switch (getAuraType()) {
            case PeriodicDamage: {
                if (caster != null) {
                    damage = caster.spellDamageBonusDone(target, getSpellInfo(), damage, DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);
                }

                damage = target.spellDamageBonusTaken(caster, getSpellInfo(), damage, DamageEffectType.DOT);

                // There is a Chance to make a Soul Shard when Drain soul does damage
                if (caster != null && getSpellInfo().getSpellFamilyName() == SpellFamilyNames.Warlock && getSpellInfo().getSpellFamilyFlags().get(0).hasFlag(0x00004000)) {
                    if (caster.isTypeId(TypeId.PLAYER) && caster.toPlayer().isHonorOrXPTarget(target)) {
                        caster.castSpell(caster, 95810, new CastSpellExtraArgs(this));
                    }
                } else if (getSpellInfo().getSpellFamilyName() == SpellFamilyNames.generic) {
                    switch (getId()) {
                        case 70911: // Unbound Plague
                        case 72854: // Unbound Plague
                        case 72855: // Unbound Plague
                        case 72856: // Unbound Plague
                            damage *= Math.pow(1.25f, ticksDone);

                            break;
                        default:
                            break;
                    }
                }

                break;
            }
            case PeriodicWeaponPercentDamage: {
                var attackType = getSpellInfo().getAttackType();

                damage = MathUtil.CalculatePct(caster.calculateDamage(attackType, false, true), getAmount());

                // Add melee damage bonuses (also check for negative)
                if (caster != null) {
                    damage = caster.meleeDamageBonusDone(target, damage, attackType, DamageEffectType.DOT, getSpellInfo());
                }

                damage = target.meleeDamageBonusTaken(caster, damage, attackType, DamageEffectType.DOT, getSpellInfo());

                break;
            }
            case PeriodicDamagePercent:
                // ceil obtained value, it may happen that 10 ticks for 10% damage may not kill owner
                damage = Math.ceil(MathUtil.CalculatePct((double) target.getMaxHealth(), damage));
                damage = target.spellDamageBonusTaken(caster, getSpellInfo(), damage, DamageEffectType.DOT);

                break;
            default:
                break;
        }

        var crit = RandomUtil.randChance(getCritChanceFor(caster, target));

        if (crit) {
            damage = unit.spellCriticalDamageBonus(caster, spellInfo, damage, target);
        }

        // Calculate armor mitigation
        if (unit.isDamageReducedByArmor(getSpellInfo().getSchoolMask(), getSpellInfo())) {
            var damageReducedArmor = unit.calcArmorReducedDamage(caster, target, damage, getSpellInfo(), getSpellInfo().getAttackType(), getBase().getCasterLevel());
            cleanDamage.setMitigatedDamage(cleanDamage.getMitigatedDamage() + damage - damageReducedArmor);
            damage = damageReducedArmor;
        }

        if (!getSpellInfo().hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers)) {
            if (getSpellEffectInfo().isTargetingArea() || getSpellEffectInfo().isAreaAuraEffect() || getSpellEffectInfo().isEffect(SpellEffectName.PersistentAreaAura) || getSpellInfo().hasAttribute(SpellAttr5.TreatAsAreaEffect)) {
                damage = target.calculateAOEAvoidance(damage, (int) spellInfo.getSchoolMask().getValue(), getBase().getCastItemGuid());
            }
        }

        var dmg = damage;

        if (!getSpellInfo().hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers) && caster != null && caster.canApplyResilience()) {
            tangible.RefObject<Double> tempRef_dmg = new tangible.RefObject<Double>(dmg);
            unit.applyResilience(target, tempRef_dmg);
            dmg = tempRef_dmg.refArgValue;
        }

        damage = dmg;

        DamageInfo damageInfo = new DamageInfo(caster, target, damage, getSpellInfo(), getSpellInfo().getSchoolMask(), DamageEffectType.DOT, WeaponAttackType.BASE_ATTACK);
        unit.calcAbsorbResist(damageInfo);
        damage = damageInfo.getDamage();

        var absorb = damageInfo.getAbsorb();
        var resist = damageInfo.getResist();
        tangible.RefObject<Double> tempRef_damage2 = new tangible.RefObject<Double>(damage);
        tangible.RefObject<Double> tempRef_absorb = new tangible.RefObject<Double>(absorb);
        unit.dealDamageMods(caster, target, tempRef_damage2, tempRef_absorb);
        absorb = tempRef_absorb.refArgValue;
        damage = tempRef_damage2.refArgValue;

        // Set trigger flag
        var procAttacker = new EnumFlag<ProcFlag>(procFlags.DealHarmfulPeriodic);
        var procVictim = new EnumFlag<ProcFlag>(procFlags.TakeHarmfulPeriodic);
        var hitMask = damageInfo.getHitMask();

        if (damage != 0) {
            hitMask = hitMask.getValue() | crit.getValue() ? ProcFlagsHit.Critical : ProcFlagsHit.NORMAL;
            procVictim.Or(procFlags.TakeAnyDamage);
        }

        var overkill = damage - target.getHealth();

        if (overkill < 0) {
            overkill = 0;
        }

        SpellPeriodicAuraLogInfo pInfo = new SpellPeriodicAuraLogInfo(this, damage, dmg, overkill, absorb, resist, 0.0f, crit);

        unit.dealDamage(caster, target, damage, cleanDamage, DamageEffectType.DOT, getSpellInfo().getSchoolMask(), getSpellInfo(), true);

        unit.procSkillsAndAuras(caster, target, procAttacker, procVictim, ProcFlagsSpellType.damage, ProcFlagsSpellPhase.hit, hitMask, null, damageInfo, null);
        target.sendPeriodicAuraLog(pInfo);
    }

    private void handlePeriodicHealthLeechAuraTick(Unit target, Unit caster) {
        if (!target.isAlive()) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated) || target.isImmunedToDamage(getSpellInfo())) {
            sendTickImmune(target, caster);

            return;
        }

        // dynobj auras must always have a caster
        if (getSpellEffectInfo().isEffect(SpellEffectName.PersistentAreaAura) && caster.spellHitResult(target, getSpellInfo(), false) != SpellMissInfo.NONE) {
            return;
        }

        CleanDamage cleanDamage = new cleanDamage(0, 0, getSpellInfo().getAttackType(), MeleeHitOutcome.NORMAL);

        var stackAmountForBonuses = !getSpellEffectInfo().effectAttributes.hasFlag(SpellEffectAttributes.NoScaleWithStack) ? getBase().getStackAmount() : 1;

        // ignore negative values (can be result apply spellmods to aura damage
        var damage = Math.max(getAmount(), 0);

        if (caster) {
            damage = caster.spellDamageBonusDone(target, getSpellInfo(), damage, DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);
        }

        damage = target.spellDamageBonusTaken(caster, getSpellInfo(), damage, DamageEffectType.DOT);

        var crit = RandomUtil.randChance(getCritChanceFor(caster, target));

        if (crit) {
            damage = unit.spellCriticalDamageBonus(caster, spellInfo, damage, target);
        }

        // Calculate armor mitigation
        if (unit.isDamageReducedByArmor(getSpellInfo().getSchoolMask(), getSpellInfo())) {
            var damageReducedArmor = unit.calcArmorReducedDamage(caster, target, damage, getSpellInfo(), getSpellInfo().getAttackType(), getBase().getCasterLevel());
            cleanDamage.setMitigatedDamage(cleanDamage.getMitigatedDamage() + damage - damageReducedArmor);
            damage = damageReducedArmor;
        }

        if (!getSpellInfo().hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers)) {
            if (getSpellEffectInfo().isTargetingArea() || getSpellEffectInfo().isAreaAuraEffect() || getSpellEffectInfo().isEffect(SpellEffectName.PersistentAreaAura) || getSpellInfo().hasAttribute(SpellAttr5.TreatAsAreaEffect)) {
                damage = target.calculateAOEAvoidance(damage, (int) spellInfo.getSchoolMask().getValue(), getBase().getCastItemGuid());
            }
        }

        var dmg = damage;

        if (!getSpellInfo().hasAttribute(SpellAttr4.IgnoreDamageTakenModifiers) && caster != null && caster.canApplyResilience()) {
            tangible.RefObject<Double> tempRef_dmg = new tangible.RefObject<Double>(dmg);
            unit.applyResilience(target, tempRef_dmg);
            dmg = tempRef_dmg.refArgValue;
        }

        damage = dmg;

        DamageInfo damageInfo = new DamageInfo(caster, target, damage, getSpellInfo(), getSpellInfo().getSchoolMask(), DamageEffectType.DOT, getSpellInfo().getAttackType());
        unit.calcAbsorbResist(damageInfo);

        var absorb = damageInfo.getAbsorb();
        var resist = damageInfo.getResist();

        // SendSpellNonMeleeDamageLog expects non-absorbed/non-resisted damage
        SpellNonMeleeDamage log = new SpellNonMeleeDamage(caster, target, getSpellInfo(), getBase().getSpellVisual(), getSpellInfo().getSchoolMask(), getBase().getCastId());
        log.damage = damage;
        log.originalDamage = dmg;
        log.absorb = absorb;
        log.resist = resist;
        log.periodicLog = true;

        if (crit) {
            log.hitInfo |= SpellHitType.crit.getValue();
        }

        // Set trigger flag
        var procAttacker = new EnumFlag<ProcFlag>(procFlags.DealHarmfulPeriodic);
        var procVictim = new EnumFlag<ProcFlag>(procFlags.TakeHarmfulPeriodic);
        var hitMask = damageInfo.getHitMask();

        if (damage != 0) {
            hitMask = hitMask.getValue() | crit.getValue() ? ProcFlagsHit.Critical : ProcFlagsHit.NORMAL;
            procVictim.Or(procFlags.TakeAnyDamage);
        }

        var new_damage = unit.dealDamage(caster, target, damage, cleanDamage, DamageEffectType.DOT, getSpellInfo().getSchoolMask(), getSpellInfo(), false);
        unit.procSkillsAndAuras(caster, target, procAttacker, procVictim, ProcFlagsSpellType.damage, ProcFlagsSpellPhase.hit, hitMask, null, damageInfo, null);

        // process caster heal from now on (must be in world)
        if (!caster || !caster.isAlive()) {
            return;
        }

        var gainMultiplier = getSpellEffectInfo().calcValueMultiplier(caster);

        var heal = caster.spellHealingBonusDone(caster, getSpellInfo(), (new_damage * gainMultiplier), DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);
        heal = caster.spellHealingBonusTaken(caster, getSpellInfo(), heal, DamageEffectType.DOT);

        HealInfo healInfo = new HealInfo(caster, caster, heal, getSpellInfo(), getSpellInfo().getSchoolMask());
        caster.healBySpell(healInfo);

        caster.getThreatManager().forwardThreatForAssistingMe(caster, healInfo.getEffectiveHeal() * 0.5f, getSpellInfo());
        unit.procSkillsAndAuras(caster, caster, new EnumFlag<ProcFlag>(procFlags.DealHelpfulPeriodic), new EnumFlag<ProcFlag>(procFlags.TakeHelpfulPeriodic), ProcFlagsSpellType.Heal, ProcFlagsSpellPhase.hit, hitMask, null, null, healInfo);

        caster.sendSpellNonMeleeDamageLog(log);
    }

    private void handlePeriodicHealthFunnelAuraTick(Unit target, Unit caster) {
        if (caster == null || !caster.isAlive() || !target.isAlive()) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated)) {
            sendTickImmune(target, caster);

            return;
        }

        var damage = Math.max(getAmount(), 0);

        // do not kill health donator
        if (caster.getHealth() < damage) {
            damage = caster.getHealth() - 1;
        }

        if (damage == 0) {
            return;
        }

        caster.modifyHealth(-damage);
        Log.outDebug(LogFilter.spells, "PeriodicTick: donator {0} target {1} damage {2}.", caster.getEntry(), target.getEntry(), damage);

        var gainMultiplier = getSpellEffectInfo().calcValueMultiplier(caster);

        damage = damage * gainMultiplier;

        HealInfo healInfo = new HealInfo(caster, target, damage, getSpellInfo(), getSpellInfo().getSchoolMask());
        caster.healBySpell(healInfo);
        unit.procSkillsAndAuras(caster, target, new EnumFlag<ProcFlag>(procFlags.DealHarmfulPeriodic), new EnumFlag<ProcFlag>(procFlags.TakeHarmfulPeriodic), ProcFlagsSpellType.Heal, ProcFlagsSpellPhase.hit, ProcFlagsHit.NORMAL, null, null, healInfo);
    }

    private void handlePeriodicHealAurasTick(Unit target, Unit caster) {
        if (!target.isAlive()) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated)) {
            sendTickImmune(target, caster);

            return;
        }

        // don't regen when permanent aura target has full power
        if (getBase().isPermanent() && target.isFullHealth()) {
            return;
        }

        var stackAmountForBonuses = !getSpellEffectInfo().effectAttributes.hasFlag(SpellEffectAttributes.NoScaleWithStack) ? getBase().getStackAmount() : 1;

        // ignore negative values (can be result apply spellmods to aura damage
        var damage = Math.max(getAmount(), 0);

        if (getAuraType() == AuraType.ObsModHealth) {
            damage = target.countPctFromMaxHealth(damage);
        } else if (caster != null) {
            damage = caster.spellHealingBonusDone(target, getSpellInfo(), damage, DamageEffectType.DOT, getSpellEffectInfo(), stackAmountForBonuses);
        }

        damage = target.spellHealingBonusTaken(caster, getSpellInfo(), damage, DamageEffectType.DOT);

        var crit = RandomUtil.randChance(getCritChanceFor(caster, target));

        if (crit) {
            damage = unit.spellCriticalHealingBonus(caster, spellInfo, damage, target);
        }

        Log.outDebug(LogFilter.spells, "PeriodicTick: {0} (TypeId: {1}) heal of {2} (TypeId: {3}) for {4} health inflicted by {5}", getCasterGuid().toString(), getCaster().getObjectTypeId(), target.getGUID().toString(), target.getObjectTypeId(), damage, getId());

        var heal = damage;

        HealInfo healInfo = new HealInfo(caster, target, heal, getSpellInfo(), getSpellInfo().getSchoolMask());
        unit.calcHealAbsorb(healInfo);
        unit.dealHeal(healInfo);

        SpellPeriodicAuraLogInfo pInfo = new SpellPeriodicAuraLogInfo(this, heal, damage, heal - healInfo.getEffectiveHeal(), healInfo.getAbsorb(), 0, 0.0f, crit);
        target.sendPeriodicAuraLog(pInfo);

        if (caster != null) {
            target.getThreatManager().forwardThreatForAssistingMe(caster, healInfo.getEffectiveHeal() * 0.5f, getSpellInfo());
        }

        // %-based heal - does not proc auras
        if (getAuraType() == AuraType.ObsModHealth) {
            return;
        }

        var procAttacker = new EnumFlag<ProcFlag>(procFlags.DealHelpfulPeriodic);
        var procVictim = new EnumFlag<ProcFlag>(procFlags.TakeHelpfulPeriodic);
        var hitMask = crit ? ProcFlagsHit.Critical : ProcFlagsHit.NORMAL;

        // ignore item heals
        if (getBase().getCastItemGuid().isEmpty()) {
            unit.procSkillsAndAuras(caster, target, procAttacker, procVictim, ProcFlagsSpellType.Heal, ProcFlagsSpellPhase.hit, hitMask, null, null, healInfo);
        }
    }

    private void handlePeriodicManaLeechAuraTick(Unit target, Unit caster) {
        var powerType = powerType.forValue((byte) getMiscValue());

        if (caster == null || !caster.isAlive() || !target.isAlive() || target.getDisplayPowerType() != powerType) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated) || target.isImmunedToDamage(getSpellInfo())) {
            sendTickImmune(target, caster);

            return;
        }

        if (getSpellEffectInfo().isEffect(SpellEffectName.PersistentAreaAura) && caster.spellHitResult(target, getSpellInfo(), false) != SpellMissInfo.NONE) {
            return;
        }

        // ignore negative values (can be result apply spellmods to aura damage
        var drainAmount = Math.max(getAmount(), 0);

        double drainedAmount = -target.modifyPower(powerType, -drainAmount);
        var gainMultiplier = getSpellEffectInfo().calcValueMultiplier(caster);

        SpellPeriodicAuraLogInfo pInfo = new SpellPeriodicAuraLogInfo(this, drainedAmount, drainAmount, 0, 0, 0, gainMultiplier, false);

        var gainAmount = drainedAmount * gainMultiplier;
        var gainedAmount = 0;

        if (gainAmount != 0) {
            gainedAmount = caster.modifyPower(powerType, gainAmount);

            // energize is not modified by threat modifiers
            if (!getSpellInfo().hasAttribute(SpellAttr4.NoHelpfulThreat)) {
                target.getThreatManager().addThreat(caster, gainedAmount * 0.5f, getSpellInfo(), true);
            }
        }

        // Drain Mana
        if (caster.getGuardianPet() != null && spellInfo.getSpellFamilyName() == SpellFamilyNames.Warlock && spellInfo.getSpellFamilyFlags().get(0).<Integer>HasAnyFlag(0x00000010)) {
            double manaFeedVal = 0;
            var aurEff = getBase().getEffect(1);

            if (aurEff != null) {
                manaFeedVal = aurEff.getAmount();
            }

            if (manaFeedVal > 0) {
                var feedAmount = MathUtil.CalculatePct(gainedAmount, manaFeedVal);

                CastSpellExtraArgs args = new CastSpellExtraArgs(this);
                args.addSpellMod(SpellValueMod.BasePoint0, feedAmount);
                caster.castSpell(caster, 32554, args);
            }
        }

        target.sendPeriodicAuraLog(pInfo);
    }

    private void handleObsModPowerAuraTick(Unit target, Unit caster) {
        Power powerType;

        if (getMiscValue() == powerType.All.getValue()) {
            powerType = target.getDisplayPowerType();
        } else {
            powerType = powerType.forValue((byte) getMiscValue());
        }

        if (!target.isAlive() || target.getMaxPower(powerType) == 0) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated)) {
            sendTickImmune(target, caster);

            return;
        }

        // don't regen when permanent aura target has full power
        if (getBase().isPermanent() && target.getPower(powerType) == target.getMaxPower(powerType)) {
            return;
        }

        // ignore negative values (can be result apply spellmods to aura damage
        var amount = Math.max(getAmount(), 0) * target.getMaxPower(powerType) / 100;

        SpellPeriodicAuraLogInfo pInfo = new SpellPeriodicAuraLogInfo(this, amount, amount, 0, 0, 0, 0.0f, false);

        var gain = target.modifyPower(powerType, amount);

        if (caster != null) {
            target.getThreatManager().forwardThreatForAssistingMe(caster, gain * 0.5f, getSpellInfo(), true);
        }

        target.sendPeriodicAuraLog(pInfo);
    }

    private void handlePeriodicEnergizeAuraTick(Unit target, Unit caster) {
        var powerType = powerType.forValue((byte) getMiscValue());

        if (!target.isAlive() || target.getMaxPower(powerType) == 0) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated)) {
            sendTickImmune(target, caster);

            return;
        }

        // don't regen when permanent aura target has full power
        if (getBase().isPermanent() && target.getPower(powerType) == target.getMaxPower(powerType)) {
            return;
        }

        // ignore negative values (can be result apply spellmods to aura damage
        var amount = Math.max(getAmount(), 0);

        SpellPeriodicAuraLogInfo pInfo = new SpellPeriodicAuraLogInfo(this, amount, amount, 0, 0, 0, 0.0f, false);
        var gain = target.modifyPower(powerType, amount);

        if (caster != null) {
            target.getThreatManager().forwardThreatForAssistingMe(caster, gain * 0.5f, getSpellInfo(), true);
        }

        target.sendPeriodicAuraLog(pInfo);
    }

    private void handlePeriodicPowerBurnAuraTick(Unit target, Unit caster) {
        var powerType = powerType.forValue((byte) getMiscValue());

        if (caster == null || !target.isAlive() || target.getDisplayPowerType() != powerType) {
            return;
        }

        if (target.hasUnitState(UnitState.Isolated) || target.isImmunedToDamage(getSpellInfo())) {
            sendTickImmune(target, caster);

            return;
        }

        // ignore negative values (can be result apply spellmods to aura damage
        var damage = Math.max(getAmount(), 0);

        double gain = -target.modifyPower(powerType, -damage);

        var dmgMultiplier = getSpellEffectInfo().calcValueMultiplier(caster);

        var spellProto = getSpellInfo();
        // maybe has to be sent different to client, but not by SMSG_PERIODICAURALOG
        SpellNonMeleeDamage damageInfo = new SpellNonMeleeDamage(caster, target, spellProto, getBase().getSpellVisual(), spellProto.getSchoolMask(), getBase().getCastId());
        damageInfo.periodicLog = true;
        // no SpellDamageBonus for burn mana
        caster.calculateSpellDamageTaken(damageInfo, gain * dmgMultiplier, spellProto);

        tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.damage);
        tangible.RefObject<Double> tempRef_Absorb = new tangible.RefObject<Double>(damageInfo.absorb);
        unit.dealDamageMods(damageInfo.attacker, damageInfo.target, tempRef_Damage, tempRef_Absorb);
        damageInfo.absorb = tempRef_Absorb.refArgValue;
        damageInfo.damage = tempRef_Damage.refArgValue;

        // Set trigger flag
        var procAttacker = new EnumFlag<ProcFlag>(procFlags.DealHarmfulPeriodic);
        var procVictim = new EnumFlag<ProcFlag>(procFlags.TakeHarmfulPeriodic);
        var hitMask = unit.createProcHitMask(damageInfo, SpellMissInfo.NONE);
        var spellTypeMask = ProcFlagsSpellType.NoDmgHeal;

        if (damageInfo.damage != 0) {
            procVictim.Or(procFlags.TakeAnyDamage);
            spellTypeMask = ProcFlagsSpellType.forValue(spellTypeMask.getValue() | ProcFlagsSpellType.damage.getValue());
        }

        caster.dealSpellDamage(damageInfo, true);

        DamageInfo dotDamageInfo = new DamageInfo(damageInfo, DamageEffectType.DOT, WeaponAttackType.BASE_ATTACK, hitMask);
        unit.procSkillsAndAuras(caster, target, procAttacker, procVictim, spellTypeMask, ProcFlagsSpellPhase.hit, hitMask, null, dotDamageInfo, null);

        caster.sendSpellNonMeleeDamageLog(damageInfo);
    }

    private boolean canPeriodicTickCrit() {
        if (getSpellInfo().hasAttribute(SpellAttr2.CantCrit)) {
            return false;
        }

        return true;
    }

    private double calcPeriodicCritChance(Unit caster) {
        if (!caster || !canPeriodicTickCrit()) {
            return 0.0f;
        }

        var modOwner = caster.getSpellModOwner();

        if (!modOwner) {
            return 0.0f;
        }

        var critChance = modOwner.spellCritChanceDone(null, this, getSpellInfo().getSchoolMask(), getSpellInfo().getAttackType());

        return Math.max(0.0f, critChance);
    }

    private void handleBreakableCCAuraProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var damageLeft = getAmount() - eventInfo.getDamageInfo().getDamage();

        if (damageLeft <= 0) {
            aurApp.getTarget().removeAura(aurApp);
        } else {
            changeAmount(damageLeft);
        }
    }

    private void handleProcTriggerSpellAuraProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var triggerCaster = aurApp.getTarget();
        var triggerTarget = eventInfo.getProcTarget();

        var triggerSpellId = getSpellEffectInfo().triggerSpell;

        if (triggerSpellId == 0) {
            Log.outWarn(LogFilter.spells, String.format("AuraEffect::HandleProcTriggerSpellAuraProc: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", getId(), getEffIndex()));

            return;
        }

        var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpellId, getBase().getCastDifficulty());

        if (triggeredSpellInfo != null) {
            Log.outDebug(LogFilter.spells, String.format("AuraEffect.HandleProcTriggerSpellAuraProc: Triggering spell %1$s from aura %2$s proc", triggeredSpellInfo.getId(), getId()));
            triggerCaster.castSpell(triggerTarget, triggeredSpellInfo.getId(), (new CastSpellExtraArgs(this)).setTriggeringSpell(eventInfo.getProcSpell()));
        } else if (triggerSpellId != 0 && getAuraType() != AuraType.DUMMY) {
            Log.outError(LogFilter.spells, String.format("AuraEffect.HandleProcTriggerSpellAuraProc: Spell %1$s has non-existent spell %2$s in EffectTriggered[%3$s] and is therefore not triggered.", getId(), triggerSpellId, getEffIndex()));
        }
    }

    private void handleProcTriggerSpellWithValueAuraProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var triggerCaster = aurApp.getTarget();
        var triggerTarget = eventInfo.getProcTarget();

        var triggerSpellId = getSpellEffectInfo().triggerSpell;

        if (triggerSpellId == 0) {
            Log.outWarn(LogFilter.spells, String.format("AuraEffect::HandleProcTriggerSpellAuraProc: Spell %1$s [EffectIndex: %2$s] does not have triggered spell.", getId(), getEffIndex()));

            return;
        }

        var triggeredSpellInfo = global.getSpellMgr().getSpellInfo(triggerSpellId, getBase().getCastDifficulty());

        if (triggeredSpellInfo != null) {
            CastSpellExtraArgs args = new CastSpellExtraArgs(this);
            args.setTriggeringSpell(eventInfo.getProcSpell());
            args.addSpellMod(SpellValueMod.BasePoint0, getAmount());
            triggerCaster.castSpell(triggerTarget, triggerSpellId, args);
            Log.outDebug(LogFilter.spells, "AuraEffect.HandleProcTriggerSpellWithValueAuraProc: Triggering spell {0} with value {1} from aura {2} proc", triggeredSpellInfo.getId(), getAmount(), getId());
        } else {
            Log.outError(LogFilter.spells, "AuraEffect.HandleProcTriggerSpellWithValueAuraProc: Spell {GetId()} has non-existent spell {triggerSpellId} in EffectTriggered[{GetEffIndex()}] and is therefore not triggered.");
        }
    }

    private void handleProcTriggerDamageAuraProc(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var target = aurApp.getTarget();
        var triggerTarget = eventInfo.getProcTarget();

        if (triggerTarget.hasUnitState(UnitState.Isolated) || triggerTarget.isImmunedToDamage(getSpellInfo())) {
            sendTickImmune(triggerTarget, target);

            return;
        }

        SpellNonMeleeDamage damageInfo = new SpellNonMeleeDamage(target, triggerTarget, getSpellInfo(), getBase().getSpellVisual(), getSpellInfo().getSchoolMask(), getBase().getCastId());
        var damage = target.spellDamageBonusDone(triggerTarget, getSpellInfo(), getAmount(), DamageEffectType.SpellDirect, getSpellEffectInfo());
        damage = triggerTarget.spellDamageBonusTaken(target, getSpellInfo(), damage, DamageEffectType.SpellDirect);
        target.calculateSpellDamageTaken(damageInfo, damage, getSpellInfo());
        tangible.RefObject<Double> tempRef_Damage = new tangible.RefObject<Double>(damageInfo.damage);
        tangible.RefObject<Double> tempRef_Absorb = new tangible.RefObject<Double>(damageInfo.absorb);
        unit.dealDamageMods(damageInfo.attacker, damageInfo.target, tempRef_Damage, tempRef_Absorb);
        damageInfo.absorb = tempRef_Absorb.refArgValue;
        damageInfo.damage = tempRef_Damage.refArgValue;
        target.dealSpellDamage(damageInfo, true);
        target.sendSpellNonMeleeDamageLog(damageInfo);
    }

    
    private void handleAuraForceWeather(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.sendPacket(new WeatherPkt(WeatherState.forValue(getMiscValue()), 1.0f));
        } else {
            target.getMap().sendZoneWeather(target.getZoneId(), target);
        }
    }

    
    private void handleEnableAltPower(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var altPowerId = getMiscValue();
        var powerEntry = CliDB.UnitPowerBarStorage.get(altPowerId);

        if (powerEntry == null) {
            return;
        }

        if (apply) {
            aurApp.getTarget().setMaxPower(powerType.AlternatePower, (int) powerEntry.maxPower);
        } else {
            aurApp.getTarget().setMaxPower(powerType.AlternatePower, 0);
        }
    }

    
    private void handleModSpellCategoryCooldown(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (player) {
            player.sendSpellCategoryCooldowns();
        }
    }

    
    private void handleShowConfirmationPrompt(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (!player) {
            return;
        }

        if (apply) {
            player.addTemporarySpell(effectInfo.triggerSpell);
        } else {
            player.removeTemporarySpell(effectInfo.triggerSpell);
        }
    }

    
    private void handleOverridePetSpecs(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (!player) {
            return;
        }

        if (player.getUnitClass() != UnitClass.Hunter) {
            return;
        }

        var pet = player.getCurrentPet();

        if (!pet) {
            return;
        }

        var currSpec = CliDB.ChrSpecializationStorage.get(pet.getSpecialization());

        if (currSpec == null) {
            return;
        }

        pet.SetSpecialization(global.getDB2Mgr().GetChrSpecializationByIndex(apply ? UnitClass.Max : 0, currSpec.orderIndex).id);
    }

    
    private void handleAllowUsingGameobjectsWhileMounted(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setPlayerLocalFlag(PlayerLocalFlags.CanUseObjectsMounted);
        } else if (!target.hasAuraType(AuraType.AllowUsingGameobjectsWhileMounted)) {
            target.removePlayerLocalFlag(PlayerLocalFlags.CanUseObjectsMounted);
        }
    }

    
    private void handlePlayScene(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var player = aurApp.getTarget().toPlayer();

        if (!player) {
            return;
        }

        if (apply) {
            player.getSceneMgr().playScene((int) getMiscValue());
        } else {
            player.getSceneMgr().cancelSceneBySceneId((int) getMiscValue());
        }
    }

    
    private void handleCreateAreaTrigger(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();

        if (apply) {
            areaTrigger.createAreaTrigger((int) getMiscValue(), getCaster(), target, getSpellInfo(), target.getLocation(), getBase().getDuration(), getBase().getSpellVisual(), ObjectGuid.Empty, this);
        } else {
            var caster = getCaster();

            if (caster) {
                caster.removeAreaTrigger(this);
            }
        }
    }

    
    private void handleAuraPvpTalents(AuraApplication auraApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = auraApp.getTarget().toPlayer();

        if (target) {
            if (apply) {
                target.togglePvpTalents(true);
            } else if (!target.hasAuraType(AuraType.pvpTalents)) {
                target.togglePvpTalents(false);
            }
        }
    }

    
    private void handleLinkedSummon(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget();
        var triggerSpellInfo = global.getSpellMgr().getSpellInfo(getSpellEffectInfo().triggerSpell, getBase().getCastDifficulty());

        if (triggerSpellInfo == null) {
            return;
        }

        // on apply cast summon spell
        if (apply) {
            CastSpellExtraArgs args = new CastSpellExtraArgs(this);
            args.castDifficulty = triggerSpellInfo.getDifficulty();
            target.castSpell(target, triggerSpellInfo.getId(), args);
        }
        // on unapply we need to search for and remove the summoned creature
        else {
            ArrayList<Integer> summonedEntries = new ArrayList<>();

            for (var spellEffectInfo : triggerSpellInfo.getEffects()) {
                if (spellEffectInfo.isEffect(SpellEffectName.summon)) {
                    var summonEntry = (int) spellEffectInfo.miscValue;

                    if (summonEntry != 0) {
                        summonedEntries.add(summonEntry);
                    }
                }
            }

            // we don't know if there can be multiple summons for the same effect, so consider only 1 summon for each effect
            // most of the spells have multiple effects with the same summon spell id for multiple spawns, so right now it's safe to assume there's only 1 spawn per effect
            for (var summonEntry : summonedEntries) {
                var nearbyEntries = target.getCreatureListWithEntryInGrid(summonEntry);

                for (var creature : nearbyEntries) {
                    if (creature.getOwnerUnit() == target) {
                        creature.despawnOrUnsummon();

                        break;
                    } else {
                        var tempSummon = creature.toTempSummon();

                        if (tempSummon) {
                            if (tempSummon.getSummoner() == target) {
                                tempSummon.despawnOrUnsummon();

                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    
    private void handleSetFFAPvP(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (!target) {
            return;
        }

        target.updatePvPState(true);
    }

    
    private void handleModOverrideZonePVPType(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        if (apply) {
            target.setOverrideZonePvpType(ZonePVPTypeOverride.forValue(getMiscValue()));
        } else if (target.hasAuraType(AuraType.ModOverrideZonePvpType)) {
            target.setOverrideZonePvpType(ZonePVPTypeOverride.forValue(target.getAuraEffectsByType(AuraType.ModOverrideZonePvpType).get(target.getAuraEffectsByType(AuraType.ModOverrideZonePvpType).size() - 1).miscValue));
        } else {
            target.setOverrideZonePvpType(ZonePVPTypeOverride.NONE);
        }

        target.updateHostileAreaState(CliDB.AreaTableStorage.get(target.getZoneId()));
        target.updatePvPState();
    }

    
    private void handleBattlegroundPlayerPosition(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var target = aurApp.getTarget().toPlayer();

        if (target == null) {
            return;
        }

        var battlegroundMap = target.getMap().toBattlegroundMap();

        if (battlegroundMap == null) {
            return;
        }

        var bg = battlegroundMap.getBG();

        if (bg == null) {
            return;
        }

        if (apply) {
            BattlegroundPlayerPosition playerPosition = new BattlegroundPlayerPosition();
            playerPosition.guid = target.getGUID();
            playerPosition.arenaSlot = (byte) getMiscValue();
            playerPosition.pos = target.getLocation();

            if (getAuraType() == AuraType.BattleGroundPlayerPositionFactional) {
                playerPosition.iconID = target.getEffectiveTeam() == Team.ALLIANCE ? BattlegroundConst.PlayerPositionIconHordeFlag : BattlegroundConst.PlayerPositionIconAllianceFlag;
            } else if (getAuraType() == AuraType.BattleGroundPlayerPosition) {
                playerPosition.iconID = target.getEffectiveTeam() == Team.ALLIANCE ? BattlegroundConst.PlayerPositionIconAllianceFlag : BattlegroundConst.PlayerPositionIconHordeFlag;
            } else {
                Log.outWarn(LogFilter.spells, String.format("Unknown aura effect %1$s handled by HandleBattlegroundPlayerPosition.", getAuraType()));
            }

            bg.addPlayerPosition(playerPosition);
        } else {
            bg.removePlayerPosition(target.getGUID());
        }
    }

    
    private void handleStoreTeleportReturnPoint(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var playerTarget = aurApp.getTarget().toPlayer();

        if (playerTarget == null) {
            return;
        }

        if (apply) {
            playerTarget.addStoredAuraTeleportLocation(getSpellInfo().getId());
        } else if (!playerTarget.getSession().isLogingOut()) {
            playerTarget.removeStoredAuraTeleportLocation(getSpellInfo().getId());
        }
    }

    
    private void handleMountRestrictions(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        aurApp.getTarget().updateMountCapability();
    }

    
    private void handleCosmeticMounted(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        if (apply) {
            aurApp.getTarget().setCosmeticMountDisplayId((int) getMiscValue());
        } else {
            aurApp.getTarget().setCosmeticMountDisplayId(0); // set cosmetic mount to 0, even if multiple auras are active; tested with zandalari racial + divine steed
        }

        var playerTarget = aurApp.getTarget().toPlayer();

        if (playerTarget == null) {
            return;
        }

        playerTarget.sendMovementSetCollisionHeight(playerTarget.getCollisionHeight(), UpdateCollisionHeightReason.FORCE);
    }

    
    private void handleSuppressItemPassiveEffectBySpellLabel(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        // Refresh applications
        aurApp.getTarget().getAuraQuery().hasLabel(new integer(getMiscValue())).forEachResult(aura -> aura.applyForTargets());
    }

    
    private void handleForceBreathBar(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        if (!mode.hasFlag(AuraEffectHandleModes.Real)) {
            return;
        }

        var playerTarget = aurApp.getTarget().toPlayer();

        if (playerTarget == null) {
            return;
        }

        playerTarget.updatePositionData();
    }


    ///#endregion
}
