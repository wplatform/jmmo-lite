package com.github.azeroth.game.script;

import com.github.azeroth.common.Logs;
import com.github.azeroth.defines.*;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.DamageInfo;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.script.model.*;
import com.github.azeroth.game.spell.Spell;
import com.github.azeroth.game.spell.SpellDestination;
import com.github.azeroth.game.spell.SpellEffectInfo;
import com.github.azeroth.game.spell.SpellInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * SpellScript is a class that contains all hooks that are executed at specified event of spell.
 * Hooks are executed in following order, at specified event of spell:
 * 1. OnPrecast - executed during spell preparation (before cast bar starts)
 * 2. BeforeCast - executed when spell preparation is finished (when cast bar becomes full) before cast is handled
 * 3. OnCheckCast - allows to override result of CheckCast function
 * 4a. OnObjectAreaTargetSelect - executed just before adding selected targets to final target list (for area targets)
 * 4b. OnObjectTargetSelect - executed just before adding selected target to final target list (for single unit targets)
 * 4c. OnDestinationTargetSelect - executed just before adding selected target to final target list (for destination targets)
 * 5. OnCast - executed just before spell is launched (creates missile) or executed
 * 6. AfterCast - executed after spell missile is launched and immediate spell actions are done
 * 7. OnEffectLaunch - executed just before specified effect handler call - when spell missile is launched
 * 8. OnCalcCritChance - executed just after specified effect handler call - when spell missile is launched - called for each target from spell target map
 * 9. OnEffectLaunchTarget - executed just before specified effect handler call - when spell missile is launched - called for each target from spell target map
 * 10a. CalcDamage - executed during specified effect handler call - when spell missile is launched - called for each target from spell target map
 * 10b. CalcHealing - executed during specified effect handler call - when spell missile is launched - called for each target from spell target map
 * 11. OnCalculateResistAbsorb - executed when damage resist/absorbs is calculated - before spell hit target
 * 12. OnEffectHit - executed just before specified effect handler call - when spell missile hits dest
 * 13. BeforeHit - executed just before spell hits a target - called for each target from spell target map
 * 14. OnEffectHitTarget - executed just before specified effect handler call - called for each target from spell target map
 * 15. OnHit - executed just before spell deals damage and procs auras - when spell hits target - called for each target from spell target map
 * 16. AfterHit - executed just after spell finishes all it's jobs for target - called for each target from spell target map
 * <p>
 * this hook is only executed after a successful dispel of any aura
 * OnEffectSuccessfulDispel - executed just after effect successfully dispelled aura(s)
 */
public abstract class SpellScript extends SpellValidator {
    private Spell spell;
    private int hitPreventEffectMask;
    private int hitPreventDefaultEffectMask;

    public abstract void onPrecast();


    //
    // hooks to which you can attach your functions
    //
    // example: BeforeCast += SpellCastFn(class::function);
    protected List<Runnable> beforeCast = new ArrayList<>(1);
    // example: OnCast += SpellCastFn(class::function);
    protected List<Runnable> onCast = new ArrayList<>(1);
    // example: AfterCast += SpellCastFn(class::function);
    protected List<Runnable> afterCast = new ArrayList<>(1);

    // example: OnCheckCast += SpellCheckCastFn();
    // where function is SpellCastResult function()
    protected List<Supplier<SpellCastResult>> onCheckCast = new ArrayList<>(1);

    // example: int32 CalcCastTime(int32 castTime) override { return 1500; }
    public abstract int calcCastTime(int castTime);

    // example: OnEffect**** += SpellEffectFn(class::function, EffectIndexSpecifier, EffectNameSpecifier);
    // where function is void function(SpellEffIndex effIndex)
    protected List<EffectHook<Consumer<SpellEffIndex>, SpellEffectName>> onEffectLaunch = new ArrayList<>(1);
    protected List<EffectHook<Consumer<SpellEffIndex>, SpellEffectName>> onEffectLaunchTarget = new ArrayList<>(1);
    protected List<EffectHook<Consumer<SpellEffIndex>, SpellEffectName>> onEffectHit = new ArrayList<>(1);
    protected List<EffectHook<Consumer<SpellEffIndex>, SpellEffectName>> onEffectHitTarget = new ArrayList<>(1);
    protected List<EffectHook<Consumer<SpellEffIndex>, SpellEffectName>> onEffectSuccessfulDispel = new ArrayList<>(1);

    // example: BeforeHit += BeforeSpellHitFn(class::function);
    // where function is void function(SpellMissInfo missInfo)
    protected List<Consumer<SpellMissInfo>> beforeHit = new ArrayList<>(1);

    // example: OnHit += SpellHitFn(class::function);
    protected List<Runnable> onHit = new ArrayList<>(1);
    // example: AfterHit += SpellHitFn(class::function);
    protected List<Runnable> afterHit = new ArrayList<>(1);
    // where function is: void function()

    // example: OnCalcCritChance += SpellOnCalcCritChanceFn(class::function);
    // where function is: void function(Unit* victim, float& critChance)
    protected List<Function<Unit, Float>> onCalcCritChance = new ArrayList<>(1);

    // example: OnObjectAreaTargetSelect += SpellObjectAreaTargetSelectFn(class::function, EffectIndexSpecifier, TargetsNameSpecifier);
    // where function is void function(std::list<WorldObject*>& targets)
    protected List<TargetHook<Consumer<List<WorldObject>>>> onObjectAreaTargetSelect = new ArrayList<>(1);

    // example: OnObjectTargetSelect += SpellObjectTargetSelectFn(class::function, EffectIndexSpecifier, TargetsNameSpecifier);
    // where function is void function(WorldObject*& target)
    protected List<TargetHook<Consumer<WorldObject>>> onObjectTargetSelect = new ArrayList<>(1);

    // example: OnDestinationTargetSelect += SpellDestinationTargetSelectFn(class::function, EffectIndexSpecifier, TargetsNameSpecifier);
    // where function is void function(SpellDestination& target)
    protected List<TargetHook<Consumer<SpellDestination>>> onDestinationTargetSelect = new ArrayList<>(1);

    // example: CalcDamage += SpellCalcDamageFn(class::function);
    // where function is void function(SpellEffectInfo const& effectInfo, Unit* victim, int32& damage, int32& flatMod, float& pctMod)
    protected List<Function<SpellEffectInfo, DamageOrHealing>> calcDamage = new ArrayList<>(1);

    // example: CalcHealing += SpellCalcHealingFn(class::function);
    // where function is void function(SpellEffectInfo const& effectInfo, Unit* victim, int32& healing, int32& flatMod, float& pctMod)
    protected List<Function<SpellEffectInfo, DamageOrHealing>> calcHealing = new ArrayList<>(1);

    // example: OnCalculateResistAbsorb += SpellOnResistAbsorbCalculateFn(class::function);
    // where function is void function(DamageInfo const& damageInfo, uint32& resistAmount, int32& absorbAmount)
    protected List<Function<DamageInfo, ResistAbsorb>> onCalculateResistAbsorb = new ArrayList<>(1);


    @Override
    protected boolean _Validate(SpellInfo entry) {
        onEffectLaunch.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectLaunch` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onEffectLaunchTarget.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectLaunchTarget` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onEffectHit.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectHit` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onEffectHitTarget.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectHitTarget` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onEffectSuccessfulDispel.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnEffectSuccessfulDispel` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onObjectAreaTargetSelect.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnObjectAreaTargetSelect` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onObjectTargetSelect.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnObjectTargetSelect` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });

        onDestinationTargetSelect.forEach(hook -> {
            if (hook.getAffectedEffectsMask(entry) == 0)
                Logs.SCRIPTS.error("Spell `{}` Effect `{}` of script `{}` did not match dbc effect data - handler bound to hook `OnDestinationTargetSelect` of SpellScript won't be executed", entry.getId(), hook, scriptName);
        });


        if (!calcDamage.isEmpty()) {
            if (!entry.hasEffect(SpellEffectName.SCHOOL_DAMAGE)
                    && !entry.hasEffect(SpellEffectName.POWER_DRAIN)
                    && !entry.hasEffect(SpellEffectName.HEALTH_LEECH)
                    && !entry.hasEffect(SpellEffectName.WEAPON_DAMAGE)
                    && !entry.hasEffect(SpellEffectName.WEAPON_DAMAGE_NO_SCHOOL)
                    && !entry.hasEffect(SpellEffectName.NORMALIZED_WEAPON_DMG)
                    && !entry.hasEffect(SpellEffectName.WEAPON_PERCENT_DAMAGE))
                Logs.SCRIPTS.error("Spell `{}` script `{}` does not have a damage effect - handler bound to hook `CalcDamage` of SpellScript won't be executed", entry.getId(), scriptName);
        }

        if (!calcHealing.isEmpty()) {
            if (!entry.hasEffect(SpellEffectName.HEAL)
                    && !entry.hasEffect(SpellEffectName.HEAL_PCT)
                    && !entry.hasEffect(SpellEffectName.HEAL_MECHANICAL)
                    && !entry.hasEffect(SpellEffectName.HEALTH_LEECH))
                Logs.SCRIPTS.error("Spell `{}` script `{}` does not have a healing effect - handler bound to hook `CalcHealing` of SpellScript won't be executed", entry.getId(), scriptName);
        }

        return super._Validate(entry);
    }


    protected final void spellBeforeCastFn(Runnable beforeCast) {
        this.beforeCast.add(beforeCast);
    }

    protected final void spellCastFn(Runnable onCast) {
        this.onCast.add(onCast);
    }

    protected final void spellAfterCastFn(Runnable afterCast) {
        this.afterCast.add(afterCast);
    }

    protected final void spellCheckCastFn(Supplier<SpellCastResult> onCheckCast) {
        this.onCheckCast.add(onCheckCast);
    }

    protected final void spellEffectLaunchFn(Consumer<SpellEffIndex> fn, SpellEffIndex effIndex, SpellEffectName effectName) {
        this.onEffectLaunch.add(new EffectHook<>(effIndex, effectName, fn));
    }

    protected final void spellEffectLaunchTargetFn(Consumer<SpellEffIndex> fn, SpellEffIndex effIndex, SpellEffectName effectName) {
        this.onEffectLaunchTarget.add(new EffectHook<>(effIndex, effectName, fn));
    }

    protected final void spellEffectHitFn(Consumer<SpellEffIndex> fn, SpellEffIndex effIndex, SpellEffectName effectName) {
        this.onEffectHit.add(new EffectHook<>(effIndex, effectName, fn));
    }

    protected final void spellEffectHitTargetFn(Consumer<SpellEffIndex> fn, SpellEffIndex effIndex, SpellEffectName effectName) {
        this.onEffectHitTarget.add(new EffectHook<>(effIndex, effectName, fn));
    }

    protected final void spellEffectSuccessfulDispelFn(Consumer<SpellEffIndex> fn, SpellEffIndex effIndex, SpellEffectName effectName) {
        this.onEffectSuccessfulDispel.add(new EffectHook<>(effIndex, effectName, fn));
    }

    protected final void beforeSpellHitFn(Consumer<SpellMissInfo> fn) {
        this.beforeHit.add(fn);
    }

    protected final void spellHitFn(Runnable fn) {
        this.onHit.add(fn);
    }

    protected final void spellAfterHitFn(Runnable fn) {
        this.afterHit.add(fn);
    }

    protected final void spellOnCalcCritChanceFn(Function<Unit, Float> fn) {
        this.onCalcCritChance.add(fn);
    }

    protected final void spellObjectAreaTargetSelectFn(Consumer<List<WorldObject>> fn, SpellEffIndex effIndex, Target target) {
        this.onObjectAreaTargetSelect.add(new TargetHook<>(effIndex, fn, target, true, false));
    }

    protected final void spellObjectTargetSelectFn(Consumer<WorldObject> fn, SpellEffIndex effIndex, Target target) {
        this.onObjectTargetSelect.add(new TargetHook<>(effIndex, fn, target, false, false));
    }

    protected final void spellDestinationTargetSelectFn(Consumer<SpellDestination> fn, SpellEffIndex effIndex, Target target) {
        this.onDestinationTargetSelect.add(new TargetHook<>(effIndex, fn, target, false, true));
    }

    protected final void spellCalcDamageFn(Function<SpellEffectInfo, DamageOrHealing> fn) {
        this.calcDamage.add(fn);
    }

    protected final void spellCalcHealingFn(Function<SpellEffectInfo, DamageOrHealing> fn) {
        this.calcHealing.add(fn);
    }

    protected final void spellOnResistAbsorbCalculateFn(Function<DamageInfo, ResistAbsorb> fn) {
        this.onCalculateResistAbsorb.add(fn);
    }
}