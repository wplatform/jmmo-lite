package com.github.azeroth.game.script;

import com.github.azeroth.common.Logs;
import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.entity.player.model.SpellModifier;
import com.github.azeroth.game.entity.unit.*;
import com.github.azeroth.game.script.model.*;
import com.github.azeroth.game.spell.Aura;
import com.github.azeroth.game.spell.AuraApplication;
import com.github.azeroth.game.spell.AuraEffect;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.auras.enums.AuraEffectHandleMode;
import com.github.azeroth.game.spell.auras.enums.AuraType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AuraScript extends SpellValidator {
    protected Aura aura;
    protected AuraApplication auraApplication;
    protected boolean defaultActionPrevented = false;

    //
    // AuraScript interface
    // hooks to which you can attach your functions
    //
    // executed when area aura checks if it can be applied on target
    // example: OnEffectApply += AuraEffectApplyFn(class::function);
    // where function is: bool function(Unit* target);
    protected final List<Function<Unit, Boolean>> doCheckAreaTarget = new ArrayList<>(1);

    // executed when aura is dispelled by a unit
    // example: OnDispel += AuraDispelFn(class::function);
    // where function is: void function(DispelInfo* dispelInfo);
    protected final List<Consumer<DispelInfo>> onDispel = new ArrayList<>(1);
    // executed after aura is dispelled by a unit
    // example: AfterDispel += AuraDispelFn(class::function);
    // where function is: void function(DispelInfo* dispelInfo);
    protected final List<Consumer<DispelInfo>> afterDispel = new ArrayList<>(1);

    // executed on every heartbeat of a unit
    // example: OnHeartbeat += AuraHeartbeatFn(class::function);
    // where function is: void function();
    protected final List<Runnable> onHeartbeat = new ArrayList<>(1);

    // executed when aura effect is applied with specified mode to target
    // should be used when when effect handler preventing/replacing is needed, do not use this hook for triggering spellcasts/removing auras etc - may be unsafe
    // example: OnEffectApply += AuraEffectApplyFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier, AuraEffectHandleModes);
    // where function is: void function(AuraEffect const* aurEff, AuraEffectHandleModes mode);
    protected List<EffectHook<BiConsumer<AuraEffect, AuraEffectHandleMode>, AuraType>> onEffectApply = new ArrayList<>(1);
    // executed after aura effect is applied with specified mode to target
    // example: AfterEffectApply += AuraEffectApplyFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier, AuraEffectHandleModes);
    // where function is: void function(AuraEffect const* aurEff, AuraEffectHandleModes mode);
    protected List<EffectHook<BiConsumer<AuraEffect, AuraEffectHandleMode>, AuraType>> afterEffectApply = new ArrayList<>(1);

    // executed after aura effect is removed with specified mode from target
    // should be used when effect handler preventing/replacing is needed, do not use this hook for triggering spellcasts/removing auras etc - may be unsafe
    // example: OnEffectRemove += AuraEffectRemoveFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier, AuraEffectHandleModes);
    // where function is: void function(AuraEffect const* aurEff, AuraEffectHandleModes mode);
    protected List<EffectHook<BiConsumer<AuraEffect, AuraEffectHandleMode>, AuraType>> onEffectRemove = new ArrayList<>(1);
    // executed when aura effect is removed with specified mode from target
    // example: AfterEffectRemove += AuraEffectRemoveFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier, AuraEffectHandleModes);
    // where function is: void function(AuraEffect const* aurEff, AuraEffectHandleModes mode);
    protected List<EffectHook<BiConsumer<AuraEffect, AuraEffectHandleMode>, AuraType>> afterEffectRemove = new ArrayList<>(1);

    // executed when periodic aura effect ticks on target
    // example: OnEffectPeriodic += AuraEffectPeriodicFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect const* aurEff);
    protected List<EffectHook<Consumer<AuraEffect>, AuraType>> onEffectPeriodic = new ArrayList<>(1);

    // executed when periodic aura effect is updated
    // example: OnEffectUpdatePeriodic += AuraEffectUpdatePeriodicFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect* aurEff);
    protected List<EffectHook<Consumer<AuraEffect>, AuraType>> onEffectUpdatePeriodic = new ArrayList<>(1);

    // executed when aura effect calculates amount
    // example: DoEffectCalcAmount += AuraEffectCalcAmounFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect* aurEff, int32& amount, bool& canBeRecalculated);
    protected List<EffectHook<Function<AuraEffect, EffectAmount>, AuraType>> doEffectCalcAmount = new ArrayList<>(1);

    // executed when aura effect calculates periodic data
    // example: DoEffectCalcPeriodic += AuraEffectCalcPeriodicFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect const* aurEff, bool& isPeriodic, int32& amplitude);
    protected List<EffectHook<Function<AuraEffect, EffectAmount>, AuraType>> doEffectCalcPeriodic = new ArrayList<>(1);

    // executed when aura effect calculates spellmod
    // example: DoEffectCalcSpellMod += AuraEffectCalcSpellModFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect const* aurEff, SpellModifier*& spellMod);
    protected List<EffectHook<Function<AuraEffect, SpellModifier>, AuraType>> doEffectCalcSpellMod = new ArrayList<>(1);

    // executed when aura effect calculates crit chance for dots and hots
    // example: DoEffectCalcCritChance += AuraEffectCalcCritChanceFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect const* aurEff, Unit* victim, float& critChance);
    protected List<EffectHook<BiFunction<AuraEffect, Unit, Float>, AuraType>> doEffectCalcCritChance = new ArrayList<>(1);

    // executed when aura effect calculates damage or healing for dots and hots
    // example: DoEffectCalcDamageAndHealing += AuraEffectCalcDamageFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // example: DoEffectCalcDamageAndHealing += AuraEffectCalcHealingFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect const* aurEff, Unit* victim, int32& damageOrHealing, int32& flatMod, float& pctMod);
    protected List<EffectHook<BiFunction<AuraEffect, Unit, DamageOrHealing>, AuraType>> doEffectCalcDamageAndHealing = new ArrayList<>(1);

    // executed when absorb aura effect is going to reduce damage
    // example: OnEffectAbsorb += AuraEffectAbsorbFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect* aurEff, DamageInfo& dmgInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, DamageInfo, Integer>, AuraType>> onEffectAbsorb = new ArrayList<>(1);

    // executed after absorb aura effect reduced damage to target - absorbAmount is real amount absorbed by aura
    // example: AfterEffectAbsorb += AuraEffectAbsorbFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect* aurEff, DamageInfo& dmgInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, DamageInfo, Integer>, AuraType>> afterEffectAbsorb = new ArrayList<>(1);

    // executed when absorb aura effect is going to reduce damage
    // example: OnEffectAbsorbHeal += AuraEffectAbsorbHealFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect const* aurEff, HealInfo& healInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, HealInfo, Integer>, AuraType>> onEffectAbsorbHeal = new ArrayList<>(1);

    // executed after absorb aura effect reduced heal to target - absorbAmount is real amount absorbed by aura
    // example: AfterEffectAbsorbHeal += AuraEffectAbsorbHealFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect* aurEff, HealInfo& healInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, HealInfo, Integer>, AuraType>> afterEffectAbsorbHeal = new ArrayList<>(1);

    // executed when mana shield aura effect is going to reduce damage
    // example: OnEffectManaShield += AuraEffectManaShieldFn(class::function, EffectIndexSpecifier);
    // where function is: void function (AuraEffect* aurEff, DamageInfo& dmgInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, DamageInfo, Integer>, AuraType>> onEffectManaShield = new ArrayList<>(1);

    // executed after mana shield aura effect reduced damage to target - absorbAmount is real amount absorbed by aura
    // example: AfterEffectManaShield += AuraEffectManaShieldFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect* aurEff, DamageInfo& dmgInfo, uint32& absorbAmount);
    protected List<EffectHook<BiFunction<AuraEffect, DamageInfo, Integer>, AuraType>> afterEffectManaShield = new ArrayList<>(1);

    // executed when the caster of some spell with split dmg aura gets damaged through it
    // example: OnEffectSplit += AuraEffectSplitFn(class::function, EffectIndexSpecifier);
    // where function is: void function(AuraEffect* aurEff, DamageInfo& dmgInfo, uint32& splitAmount);
    protected List<EffectHook<BiFunction<AuraEffect, DamageInfo, Integer>, AuraType>> onEffectSplit = new ArrayList<>(1);

    // executed when aura checks if it can proc
    // example: DoCheckProc += AuraCheckProcFn(class::function);
    // where function is: bool function(ProcEventInfo& eventInfo);
    protected List<Function<ProcEventInfo, Boolean>> doCheckProc = new ArrayList<>(1);

    // executed when aura effect checks if it can proc the aura
    // example: DoCheckEffectProc += AuraCheckEffectProcFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is bool function(AuraEffect const* aurEff, ProcEventInfo& eventInfo);
    protected List<EffectHook<BiFunction<AuraEffect, ProcEventInfo, Boolean>, AuraType>> doCheckEffectProc = new ArrayList<>(1);

    // executed before aura procs (possibility to prevent charge drop/cooldown)
    // example: DoPrepareProc += AuraProcFn(class::function);
    // where function is: void function(ProcEventInfo& eventInfo);
    protected List<Consumer<ProcEventInfo>> doPrepareProc = new ArrayList<>(1);
    // executed when aura procs
    // example: OnProc += AuraProcFn(class::function);
    // where function is: void function(ProcEventInfo& eventInfo);
    protected List<Consumer<ProcEventInfo>> onProc = new ArrayList<>(1);
    // executed after aura proced
    // example: AfterProc += AuraProcFn(class::function);
    // where function is: void function(ProcEventInfo& eventInfo);
    protected List<Consumer<ProcEventInfo>> afterProc = new ArrayList<>(1);

    // executed when aura effect procs
    // example: OnEffectProc += AuraEffectProcFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect* aurEff, ProcEventInfo& procInfo);
    protected List<EffectHook<BiConsumer<AuraEffect, ProcEventInfo>, AuraType>> onEffectProc = new ArrayList<>(1);
    // executed after aura effect proced
    // example: AfterEffectProc += AuraEffectProcFn(class::function, EffectIndexSpecifier, EffectAuraNameSpecifier);
    // where function is: void function(AuraEffect* aurEff, ProcEventInfo& procInfo);
    protected List<EffectHook<BiConsumer<AuraEffect, ProcEventInfo>, AuraType>> afterEffectProc = new ArrayList<>(1);

    // executed when target enters or leaves combat
    // example: OnEnterLeaveCombat += AuraEnterLeaveCombatFn(class::function)
    // where function is: void function (bool isNowInCombat);
    protected List<Consumer<Boolean>> onEnterLeaveCombat = new ArrayList<>(1);

    // AuraScript interface - hook/effect execution manipulators

    // prevents default action of a hook from being executed (works only while called in a hook which default action can be prevented)
    protected abstract void preventDefaultAction();

    // AuraScript interface - functions which are redirecting to Aura class

    @Override
    boolean _Validate(SpellInfo entry) {

        doCheckAreaTarget.forEach(consumer -> {
            if (!entry.hasAreaAuraEffect() && !entry.hasEffect(SpellEffectName.PERSISTENT_AREA_AURA) && !entry.hasEffect(SpellEffectName.APPLY_AURA))
                Logs.SCRIPTS.error("Spell `{}` of script `{}` does not have apply aura effect - handler bound to hook `DoCheckAreaTarget` of AuraScript won't be executed", entry.getId(), scriptName);

        });

        onDispel.forEach(consumer -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` does not have apply aura effect - handler bound to hook `OnDispel` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        afterDispel.forEach(consumer -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` does not have apply aura effect - handler bound to hook `AfterDispel` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        onEffectApply.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectApply` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectRemove.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectRemove` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        afterEffectApply.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterEffectApply` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });
        afterEffectRemove.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterEffectRemove` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectPeriodic.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectPeriodic` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectUpdatePeriodic.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectUpdatePeriodic` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doEffectCalcAmount.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoEffectCalcAmount` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doEffectCalcPeriodic.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoEffectCalcPeriodic` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doEffectCalcSpellMod.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoEffectCalcSpellMod` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doEffectCalcCritChance.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoEffectCalcCritChance` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doEffectCalcDamageAndHealing.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoEffectCalcDamageAndHealing` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectAbsorb.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectAbsorb` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        afterEffectAbsorb.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterEffectAbsorb` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectManaShield.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectManaShield` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        afterEffectManaShield.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterEffectManaShield` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        onEffectSplit.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectSplit` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        doCheckProc.forEach(item -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoCheckProc` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        doCheckEffectProc.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoCheckEffectProc` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        doPrepareProc.forEach(item -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` did not match dbc effect data - handler bound to hook `DoPrepareProc` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        onProc.forEach(item -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnProc` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        afterProc.forEach(item -> {
            if (!entry.hasEffect(SpellEffectName.APPLY_AURA) && !entry.hasAreaAuraEffect())
                Logs.SCRIPTS.error("Spell `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterProc` of AuraScript won't be executed", entry.getId(), scriptName);
        });

        onEffectProc.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectProc` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        afterEffectProc.forEach(item -> {
            if (item.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `AfterEffectProc` of AuraScript won't be executed", entry.getId(), item, scriptName);
        });

        return super._Validate(entry);
    }

    protected final void auraCheckAreaTargetFn(Function<Unit, Boolean> fn) {
        doCheckAreaTarget.add(fn);
    }

    protected final void auraDispelFn(Consumer<DispelInfo> fn) {
        onDispel.add(fn);
    }

    protected final void auraAfterDispelFn(Consumer<DispelInfo> fn) {
        afterDispel.add(fn);
    }

    protected final void auraHeartbeatFn(Runnable fn) {
        onHeartbeat.add(fn);
    }

    protected final void auraEffectApplyFn(
            BiConsumer<AuraEffect, AuraEffectHandleMode> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName, AuraEffectHandleMode handleMode) {

        BiConsumer<AuraEffect, AuraEffectHandleMode> fnToApply = (_auraEffect, _handleMode) -> {
            if (_handleMode == handleMode) {
                fn.accept(_auraEffect, _handleMode);
            }
        };
        onEffectApply.add(new EffectHook<>(effectIndex, effectAuraName, fnToApply));
    }

    protected final void auraAfterEffectApplyFn(
            BiConsumer<AuraEffect, AuraEffectHandleMode> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName, AuraEffectHandleMode handleMode) {
        BiConsumer<AuraEffect, AuraEffectHandleMode> fnToApply = (_auraEffect, _handleMode) -> {
            if (_handleMode == handleMode) {
                fn.accept(_auraEffect, _handleMode);
            }
        };
        afterEffectApply.add(new EffectHook<>(effectIndex, effectAuraName, fnToApply));
    }

    protected final void auraEffectRemoveFn(
            BiConsumer<AuraEffect, AuraEffectHandleMode> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName, AuraEffectHandleMode handleMode) {
        BiConsumer<AuraEffect, AuraEffectHandleMode> fnToApply = (_auraEffect, _handleMode) -> {
            if (_handleMode == handleMode) {
                fn.accept(_auraEffect, _handleMode);
            }
        };
        onEffectRemove.add(new EffectHook<>(effectIndex, effectAuraName, fnToApply));
    }

    protected final void auraAfterEffectRemoveFn(
            BiConsumer<AuraEffect, AuraEffectHandleMode> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName, AuraEffectHandleMode handleMode) {
        BiConsumer<AuraEffect, AuraEffectHandleMode> fnToApply = (_auraEffect, _handleMode) -> {
            if (_handleMode == handleMode) {
                fn.accept(_auraEffect, _handleMode);
            }
        };
        afterEffectRemove.add(new EffectHook<>(effectIndex, effectAuraName, fnToApply));
    }

    protected final void auraEffectPeriodicFn(
            Consumer<AuraEffect> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectPeriodic.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectUpdatePeriodicFn(
            Consumer<AuraEffect> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectUpdatePeriodic.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectCalcAmountFn(
            Function<AuraEffect, EffectAmount> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcAmount.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectCalcPeriodicFn(
            Function<AuraEffect, EffectAmount> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcPeriodic.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectCalcSpellModFn(
            Function<AuraEffect, SpellModifier> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcSpellMod.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectCalcCritChanceFn(
            BiFunction<AuraEffect, Unit, Float> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcCritChance.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }


    protected final void auraEffectCalcHealingFn(
            BiFunction<AuraEffect, Unit, DamageOrHealing> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcDamageAndHealing.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }


    protected final void auraEffectCalcDamageFn(
            BiFunction<AuraEffect, Unit, DamageOrHealing> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doEffectCalcDamageAndHealing.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }


    protected final void auraEffectAbsorbFn(
            BiFunction<AuraEffect, DamageInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectAbsorb.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectAfterAbsorbFn(
            BiFunction<AuraEffect, DamageInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        afterEffectAbsorb.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectAbsorbHealFn(
            BiFunction<AuraEffect, HealInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectAbsorbHeal.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectAfterAbsorbHealFn(
            BiFunction<AuraEffect, HealInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        afterEffectAbsorbHeal.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectManaShieldFn(
            BiFunction<AuraEffect, DamageInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectManaShield.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraAfterEffectManaShieldFn(
            BiFunction<AuraEffect, DamageInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        afterEffectManaShield.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEffectSplitFn(
            BiFunction<AuraEffect, DamageInfo, Integer> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectSplit.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }


    protected final void auraCheckProcFn(Function<ProcEventInfo, Boolean> fn) {
        doCheckProc.add(fn);
    }

    protected final void auraEffectCheckEffectProcFn(
            BiFunction<AuraEffect, ProcEventInfo, Boolean> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        doCheckEffectProc.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraPrepareProcFn(Consumer<ProcEventInfo> fn) {
        doPrepareProc.add(fn);
    }

    protected final void auraProcFn(Consumer<ProcEventInfo> fn) {
        onProc.add(fn);
    }

    protected final void auraAfterProcFn(
            Consumer<ProcEventInfo> fn) {
        afterProc.add(fn);
    }

    protected final void auraEffectProcFn(
            BiConsumer<AuraEffect, ProcEventInfo> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        onEffectProc.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraAfterEffectProcFn(
            BiConsumer<AuraEffect, ProcEventInfo> fn, SpellEffIndex effectIndex,
            AuraType effectAuraName) {
        afterEffectProc.add(new EffectHook<>(effectIndex, effectAuraName, fn));
    }

    protected final void auraEnterLeaveCombatFn(
            Consumer<Boolean> fn) {
        onEnterLeaveCombat.add(fn);
    }
}