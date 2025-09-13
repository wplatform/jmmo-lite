package com.github.azeroth.game.spell;

import Time.GameTime;
import com.github.azeroth.common.Assert;
import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.dbc.domain.SpellPower;
import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.entity.dynamic.DynamicObject;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.player.model.SpellModifier;
import com.github.azeroth.game.entity.unit.*;

import com.github.azeroth.game.networking.packet.spell.SpellCastVisual;
import com.github.azeroth.game.script.AuraScript;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.game.spell.enums.SpellModOp;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class Aura {

    private final SpellInfo spellInfo;
    private final Difficulty castDifficulty;
    private final long applyTime;
    private final WorldObject owner;
    private final ArrayList<SpellPower> periodicCosts = new ArrayList<>(); // Periodic costs

    private final int casterLevel; // Aura level (store caster level for correct show level dep amount)
    private final HashMap<ObjectGuid, AuraApplication> auraApplications = new HashMap<ObjectGuid, AuraApplication>();
    private final ArrayList<AuraApplication> removedApplications = new ArrayList<>();
    private final ObjectGuid castId;
    private final ObjectGuid casterGuid;
    private final SpellCastVisual spellCastVisual;
    private ArrayList<AuraScript> loadedScripts = new ArrayList<>();
    private ObjectGuid castItemGuid;
    private int castItemId;
    private int castItemLevel;
    private int maxDuration; // Max aura duration
    private int duration; // Current time
    private int timeCla; // Timer for power per sec calcultion
    private int updateTargetMapInterval; // Timer for UpdateTargetMapOfEffect
    private byte procCharges; // Aura charges (0 for infinite)
    private byte stackAmount; // Aura stack amount
    //might need to be arrays still
    private HashMap<Integer, AuraEffect> effects;
    private boolean isRemoved;
    private boolean isSingleTarget; // true if it's a single target spell and registered at caster - can change at spell steal for example
    private boolean isUsingCharges;
    private ChargeDropEvent chargeDropEvent;
    private LocalDateTime procCooldown = LocalDateTime.MIN;
    private LocalDateTime lastProcAttemptTime = LocalDateTime.MIN;
    private LocalDateTime lastProcSuccessTime = LocalDateTime.MIN;
    private Byte empoweredStage = null;

    public Aura(AuraCreateInfo createInfo) {
        this.spellInfo = createInfo.spellInfo;
        this.castDifficulty = createInfo.castDifficulty;
        this.castId = createInfo.castId;
        this.casterGuid = createInfo.casterGuid;
        this.castItemGuid = createInfo.castItemGuid;
        this.castItemId = createInfo.castItemId;
        this.castItemLevel = createInfo.castItemLevel;
        this.spellCastVisual = new SpellCastVisual(createInfo.caster != null ? createInfo.caster.getCastSpellXSpellVisualId(createInfo.spellInfo) : createInfo.spellInfo.getSpellXSpellVisualId(), 0);
        this.applyTime = GameTime.getGameTime();
        this.owner = createInfo.ower;
        this.timeCla = 0;
        this.updateTargetMapInterval = 0;
        this.casterLevel = createInfo.Caster ? createInfo.caster.getLevel() : spellInfo.getSpellLevel();
        this.procCharges = 0;
        this.stackAmount = 1;
        this.isRemoved = false;
        this.isSingleTarget = false;
        this.isUsingCharges = false;
        this.lastProcAttemptTime = (LocalDateTime.now() - duration.FromSeconds(10));
        this.lastProcSuccessTime = (LocalDateTime.now() - duration.FromSeconds(120));

        for (var power : spellInfo.powerCosts) {
            if (power != null && (power.ManaPerSecond != 0 || power.PowerPctPerSecond > 0.0f)) {
                this.periodicCosts.add(power);
            }
        }

        if (!periodicCosts.isEmpty()) {
            this.timeCla = 1 * time.InMilliseconds;
        }

        this.maxDuration = calcMaxDuration(createInfo.caster);
        this.duration = maxDuration;
        this.procCharges = calcMaxCharges(createInfo.caster);
        this.isUsingCharges = procCharges != 0;
        // m_casterLevel = cast item level/caster level, caster level should be saved to db, confirmed with sniffs
    }

    public static Aura tryRefreshStackOrCreate(AuraCreateInfo createInfo, boolean updateEffectMask) {


        Assert.state(createInfo.caster != null || !createInfo.casterGuid.isEmpty());
        createInfo.isRefresh = false;

        createInfo.auraEffectMask = buildEffectMaskForOwner(createInfo.getSpellInfo(), createInfo.auraEffectMask, createInfo.ower);
        createInfo.targetEffectMask &= createInfo.auraEffectMask;

        var effMask = createInfo.auraEffectMask;
        if (createInfo.targetEffectMask != 0) {
            effMask = createInfo.targetEffectMask;
        }

        if (effMask == 0) {
            return null;
        }

        var foundAura = createInfo.ower.toUnit()._TryStackingOrRefreshingExistingAura(createInfo);

        if (foundAura != null) {
            // we've here aura, which script triggered removal after modding stack amount
            // check the state here, so we won't create new Aura object
            if (foundAura.isRemoved()) {
                return null;
            }

            createInfo.isRefresh = true;

            // add owner
            var unit = createInfo.ower.toUnit();

            // check effmask on owner application (if existing)
            if (updateEffectMask) {
                var aurApp = foundAura.getApplicationOfTarget(unit.getGUID());

                if (aurApp != null) {
                    aurApp.updateApplyEffectMask(effMask, false);
                }
            }

            return foundAura;
        } else {
            return create(createInfo);
        }
    }

    public static Aura create(AuraCreateInfo createInfo) {
        // try to get caster of aura
        if (!createInfo.casterGuid.isEmpty()) {
            if (createInfo.casterGuid.isUnit()) {
                if (Objects.equals(createInfo.ower.getGUID(), createInfo.casterGuid)) {
                    createInfo.caster = createInfo.ower.toUnit();
                } else {
                    createInfo.caster = global.getObjAccessor().GetUnit(createInfo.ower, createInfo.casterGuid);
                }
            }
        } else if (createInfo.caster != null) {
            createInfo.casterGuid = createInfo.caster.getGUID();
        }

        // check if aura can be owned by owner
        if (createInfo.getOwner().isType(TypeMask.unit)) {
            if (!createInfo.getOwner().isInWorld() || createInfo.getOwner().toUnit().isDuringRemoveFromWorld()) {
                // owner not in world so don't allow to own not self casted single target auras
                if (ObjectGuid.opNotEquals(createInfo.casterGuid, createInfo.getOwner().getGUID()) && createInfo.getSpellInfo().isSingleTarget()) {
                    return null;
                }
            }
        }

        Aura aura;

        switch (createInfo.getOwner().getObjectTypeId()) {
            case Unit:
            case Player:
                aura = new UnitAura(createInfo);

                // aura can be removed in Unit::_AddAura call
                if (aura.isRemoved()) {
                    return null;
                }

                // add owner
                var effMask = createInfo.auraEffectMask;

                if (!createInfo.targetEffectMask.isEmpty()) {
                    effMask = createInfo.targetEffectMask;
                }

                effMask = buildEffectMaskForOwner(createInfo.getSpellInfo(), effMask, createInfo.getOwner());

                var unit = createInfo.getOwner().toUnit();
                aura.toUnitAura().addStaticApplication(unit, effMask);

                break;
            case DynamicObject:
                createInfo.auraEffectMask = buildEffectMaskForOwner(createInfo.getSpellInfo(), createInfo.auraEffectMask, createInfo.getOwner());

                aura = new dynObjAura(createInfo);

                break;
            default:
                return null;
        }

        // scripts, etc.
        if (aura.isRemoved()) {
            return null;
        }

        return aura;
    }

    public static int calcMaxDuration(SpellInfo spellInfo, WorldObject caster) {
        Player modOwner = null;
        int maxDuration;

        if (caster != null) {
            modOwner = caster.getSpellModOwner();
            maxDuration = caster.calcSpellDuration(spellInfo);
        } else {
            maxDuration = spellInfo.getDuration();
        }

        if (spellInfo.isPassive() && spellInfo.getDurationEntry() == null) {
            maxDuration = -1;
        }

        // isPermanent() checks max duration (which we are supposed to calculate here)
        if (maxDuration != -1 && modOwner != null) {
            tangible.RefObject<Integer> tempRef_maxDuration = new tangible.RefObject<Integer>(maxDuration);
            modOwner.applySpellMod(spellInfo, SpellModOp.Duration, tempRef_maxDuration);
            maxDuration = tempRef_maxDuration.refArgValue;
        }

        return maxDuration;
    }

    public static boolean effectTypeNeedsSendingAmount(AuraType type) {
        return switch (type) {
            case OVERRIDE_ACTIONBAR_SPELLS, OVERRIDE_ACTIONBAR_SPELLS_TRIGGERED, MOD_SPELL_CATEGORY_COOLDOWN,
                 MOD_MAX_CHARGES, CHARGE_RECOVERY_MOD, CHARGE_RECOVERY_MULTIPLIER -> true;
            default -> false;
        };

    }

    //Static Methods
    public static int buildEffectMaskForOwner(SpellInfo spellProto, int availableEffectMask, WorldObject owner) {
        int effMask = 0;
        switch (owner.getObjectTypeId()) {
            case UNIT:
            case PLAYER:
                for (var spellEffectInfo : spellProto.getEffects()) {
                    if (spellEffectInfo.isUnitOwnedAuraEffect()) {
                        effMask |= 1 << spellEffectInfo.effectIndex.ordinal();
                    }
                }

                break;
            case DYNAMIC_OBJECT:
                for (var spellEffectInfo : spellProto.getEffects()) {
                    if (spellEffectInfo.effect == SpellEffectName.PERSISTENT_AREA_AURA) {
                        effMask |= 1 << spellEffectInfo.effectIndex.ordinal();
                    }
                }

                break;
            default:
                break;
        }

        return effMask & availableEffectMask;
    }

    public static Aura tryRefreshStackOrCreate(AuraCreateInfo createInfo) {
        return tryRefreshStackOrCreate(createInfo, true);
    }

    public static Aura tryCreate(AuraCreateInfo createInfo) {
        var effMask = createInfo.auraEffectMask;

        if (!createInfo.targetEffectMask.isEmpty()) {
            effMask = createInfo.targetEffectMask;
        }

        effMask = buildEffectMaskForOwner(createInfo.getSpellInfo(), effMask, createInfo.getOwner());

        if (effMask.isEmpty()) {
            return null;
        }

        return create(createInfo);
    }

    public final UUID getGuid() {
        return guid;
    }

    public final Byte getEmpoweredStage() {
        return empoweredStage;
    }

    public final void setEmpoweredStage(Byte value) {
        empoweredStage = value;
    }

    public final SpellInfo getSpellInfo() {
        return spellInfo;
    }

    public final int getId() {
        return spellInfo.getId();
    }

    public final Difficulty getCastDifficulty() {
        return castDifficulty;
    }

    public final ObjectGuid getCastId() {
        return castId;
    }

    public final int getCastItemId() {
        return castItemId;
    }

    public final void setCastItemId(int value) {
        castItemId = value;
    }

    public final int getCastItemLevel() {
        return castItemLevel;
    }

    public final void setCastItemLevel(int value) {
        castItemLevel = value;
    }

    public final ObjectGuid getCasterGuid() {
        return casterGuid;
    }

    public final ObjectGuid getCastItemGuid() {
        return castItemGuid;
    }

    public final void setCastItemGuid(ObjectGuid value) {
        castItemGuid = value;
    }

    public final WorldObject getOwner() {
        return owner;
    }

    public final Unit getOwnerAsUnit() {
        return owner.toUnit();
    }

    public final DynamicObject getDynobjOwner() {
        return owner.toDynObject();
    }

    public final long getApplyTime() {
        return applyTime;
    }

    public final int getMaxDuration() {
        return maxDuration;
    }

    public final void setMaxDuration(double duration) {
        setMaxDuration((int) duration);
    }

    public final void setMaxDuration(int duration) {
        maxDuration = duration;
    }

    public final int getDuration() {
        return duration;
    }

    public final void setDuration(double duration) {
        setDuration(duration, false, false);
    }

    public final void setDuration(int duration) {
        setDuration(duration, false, false);
    }

    public final boolean isExpired() {
        return getDuration() == 0 && chargeDropEvent == null;
    }

    public final boolean isPermanent() {
        return maxDuration == -1;
    }

    public final byte getCharges() {
        return procCharges;
    }

    public final void setCharges(int charges) {
        if (procCharges == charges) {
            return;
        }

        procCharges = (byte) charges;
        isUsingCharges = procCharges != 0;
        setNeedClientUpdateForTargets();
    }

    public final byte getStackAmount() {
        return stackAmount;
    }

    public final void setStackAmount(byte stackAmount) {
        stackAmount = stackAmount;
        var caster = getCaster();

        var applications = getApplicationList();

        for (var aurApp : applications) {
            if (!aurApp.getHasRemoveMode()) {
                handleAuraSpecificMods(aurApp, caster, false, true);
            }
        }

        for (var aurEff : getAuraEffects().entrySet()) {
            aurEff.getValue().changeAmount(aurEff.getValue().calculateAmount(caster), false, true);
        }

        for (var aurApp : applications) {
            if (!aurApp.getHasRemoveMode()) {
                handleAuraSpecificMods(aurApp, caster, true, true);
            }
        }

        setNeedClientUpdateForTargets();
    }

    public final byte getCasterLevel() {
        return (byte) casterLevel;
    }

    public final boolean isRemoved() {
        return isRemoved;
    }

    public final boolean isSingleTarget() {
        return isSingleTarget;
    }

    public final void setSingleTarget(boolean value) {
        isSingleTarget = value;
    }

    public final HashMap<ObjectGuid, AuraApplication> getApplicationMap() {
        return auraApplications;
    }

    public final boolean isUsingCharges() {
        return isUsingCharges;
    }

    public final void setUsingCharges(boolean value) {
        isUsingCharges = value;
    }

    public final HashMap<Integer, AuraEffect> getAuraEffects() {
        return effects;
    }

    public final AuraObjectType getAuraObjType() {
        return (owner.getObjectTypeId() == TypeId.DynamicObject) ? AuraObjectType.DynObj : AuraObjectType.unit;
    }

    public final boolean isPassive() {
        return spellInfo.isPassive();
    }

    public final boolean isDeathPersistent() {
        return getSpellInfo().isDeathPersistent();
    }

    public final <T extends AuraScript> T getScript() {
        return (T) getScriptByType(T.class);
    }

    public final AuraScript getScriptByType(Class type) {
        for (var auraScript : loadedScripts) {
            if (auraScript.getClass() == type) {
                return auraScript;
            }
        }

        return null;
    }

    public final void _InitEffects(HashSet<Integer> effMask, Unit caster, HashMap<Integer, Double> baseAmount) {
        // shouldn't be in constructor - functions in AuraEffect.AuraEffect use polymorphism
        effects = new HashMap<Integer, AuraEffect>();

        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (effMask.contains(spellEffectInfo.effectIndex)) {
                effects.put(spellEffectInfo.effectIndex, new AuraEffect(this, spellEffectInfo, baseAmount != null ? baseAmount.get(spellEffectInfo.effectIndex) : null, caster));
            }
        }
    }

    public void dispose() {
        // unload scripts
        for (var itr : loadedScripts) {
            itr._Unload();
        }

        if (!auraApplications.isEmpty()) {
            for (var app : auraApplications.values().ToArray()) {
                if (getOwnerAsUnit() != null) {
                    getOwnerAsUnit().removeAura(app);
                }
            }
        }

        auraApplications.clear();
        _DeleteRemovedApplications();
    }

    public final Unit getCaster() {
        if (Objects.equals(owner.getGUID(), casterGuid)) {
            return getOwnerAsUnit();
        }

        return global.getObjAccessor().GetUnit(owner, casterGuid);
    }

    public final SpellCastVisual getSpellVisual() {
        return spellCastVisual;
    }

    // removes aura from all targets
    // and marks aura as removed
    public final void _Remove(AuraRemoveMode removeMode) {
        isRemoved = true;

        for (var pair : auraApplications.ToList()) {
            var aurApp = pair.value;
            var target = aurApp.target;
            target._UnapplyAura(aurApp, removeMode);
        }

        if (chargeDropEvent != null) {
            chargeDropEvent.ScheduleAbort();
            chargeDropEvent = null;
        }

        this.<IAuraOnRemove>ForEachAuraScript(a -> a.AuraRemoved(removeMode));
    }

    public final void updateTargetMap(Unit caster) {
        updateTargetMap(caster, true);
    }

    public void _ApplyForTarget(Unit target, Unit caster, AuraApplication auraApp) {
        if (target == null || auraApp == null) {
            return;
        }
        // aura mustn't be already applied on target
        //Cypher.Assert(!isAppliedOnTarget(target.getGUID()) && "Aura._ApplyForTarget: aura musn't be already applied on target");

        auraApplications.put(target.getGUID(), auraApp);

        // set infinity cooldown state for spells
        if (caster != null && caster.isTypeId(TypeId.PLAYER)) {
            if (spellInfo.isCooldownStartedOnEvent()) {
                var castItem = !castItemGuid.isEmpty() ? caster.toPlayer().getItemByGuid(castItemGuid) : null;
                caster.getSpellHistory().startCooldown(spellInfo, castItem != null ? castItem.Entry : 0, null, true);
            }
        }

        this.<IAuraOnApply>ForEachAuraScript(a -> a.AuraApply());
    }

    public void _UnapplyForTarget(Unit target, Unit caster, AuraApplication auraApp) {
        if (target == null || !auraApp.getHasRemoveMode() || auraApp == null) {
            return;
        }

        var app = auraApplications.get(target.getGUID());

        // @todo Figure out why this happens
        if (app == null) {
            Log.outError(LogFilter.spells, "Aura._UnapplyForTarget, target: {0}, caster: {1}, spell: {2} was not found in owners application map!", target.getGUID().toString(), caster ? caster.getGUID().toString() : "", auraApp.getBase().getSpellInfo().getId());

            return;
        }

        // aura has to be already applied

        auraApplications.remove(target.getGUID());

        removedApplications.add(auraApp);

        // reset cooldown state for spells
        if (caster != null && getSpellInfo().isCooldownStartedOnEvent()) {
            // note: item based cooldowns and cooldown spell mods with charges ignored (unknown existed cases)
            caster.getSpellHistory().sendCooldownEvent(getSpellInfo());
        }
    }

    public final void updateOwner(int diff, WorldObject owner) {
        var caster = getCaster();
        // Apply spellmods for channeled auras
        // used for example when triggered spell of spell:10 is modded
        Spell modSpell = null;
        Player modOwner = null;

        if (caster != null) {
            modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                modSpell = modOwner.findCurrentSpellBySpellId(getId());

                if (modSpell != null) {
                    modOwner.setSpellModTakingSpell(modSpell, true);
                }
            }
        }

        update(diff, caster);

        if (updateTargetMapInterval <= diff) {
            updateTargetMap(caster);
        } else {
            _updateTargetMapInterval -= (int) diff;
        }

        // update aura effects
        for (var effect : getAuraEffects().entrySet()) {
            effect.getValue().update(diff, caster);
        }

        // remove spellmods after effects update
        if (modSpell != null) {
            modOwner.setSpellModTakingSpell(modSpell, false);
        }

        _DeleteRemovedApplications();
    }

    public final int calcMaxDuration(Unit caster) {
        return calcMaxDuration(getSpellInfo(), caster);
    }

    public final void setDuration(double duration, boolean withMods) {
        setDuration(duration, withMods, false);
    }

    public final void setDuration(double duration, boolean withMods, boolean updateMaxDuration) {
        setDuration((int) duration, withMods, updateMaxDuration);
    }

    public final void setDuration(int duration, boolean withMods) {
        setDuration(duration, withMods, false);
    }

    public final void setDuration(int duration, boolean withMods, boolean updateMaxDuration) {
        if (withMods) {
            var caster = getCaster();

            if (caster) {
                var modOwner = caster.getSpellModOwner();

                if (modOwner) {
                    tangible.RefObject<Integer> tempRef_duration = new tangible.RefObject<Integer>(duration);
                    modOwner.applySpellMod(getSpellInfo(), SpellModOp.duration, tempRef_duration);
                    duration = tempRef_duration.refArgValue;
                }
            }
        }

        if (updateMaxDuration && duration > maxDuration) {
            maxDuration = duration;
        }

        duration = duration;
        setNeedClientUpdateForTargets();
    }

    /**
     * Adds the given duration to the auras duration.
     */

    public final void modDuration(int duration, boolean withMods) {
        modDuration(duration, withMods, false);
    }

    public final void modDuration(int duration) {
        modDuration(duration, false, false);
    }

    public final void modDuration(int duration, boolean withMods, boolean updateMaxDuration) {
        setDuration(getDuration() + duration, withMods, updateMaxDuration);
    }

    public final void modDuration(double duration, boolean withMods) {
        modDuration(duration, withMods, false);
    }

    public final void modDuration(double duration) {
        modDuration(duration, false, false);
    }

    public final void modDuration(double duration, boolean withMods, boolean updateMaxDuration) {
        setDuration((int) duration, withMods, updateMaxDuration);
    }

    public final void refreshDuration() {
        refreshDuration(false);
    }

    public final void refreshDuration(boolean withMods) {
        var caster = getCaster();

        if (withMods && caster) {
            var duration = spellInfo.getMaxDuration();

            // Calculate duration of periodics affected by haste.
            if (spellInfo.hasAttribute(SpellAttr8.HasteAffectsDuration)) {
                duration = (int) (duration * caster.getUnitData().modCastingSpeed);
            }

            setMaxDuration(duration);
            setDuration(duration);
        } else {
            setDuration(getMaxDuration());
        }

        if (!periodicCosts.isEmpty()) {
            timeCla = 1 * time.InMilliseconds;
        }

        // also reset periodic counters
        for (var aurEff : getAuraEffects().entrySet()) {
            aurEff.getValue().resetTicks();
        }
    }

    public final boolean modCharges(int num) {
        return modCharges(num, AuraRemoveMode.Default);
    }

    public final boolean modCharges(int num, AuraRemoveMode removeMode) {
        if (isUsingCharges()) {
            var charges = procCharges + num;
            int maxCharges = calcMaxCharges();

            // limit charges (only on charges increase, charges may be changed manually)
            if ((num > 0) && (charges > maxCharges)) {
                charges = maxCharges;
            }
            // we're out of charges, remove
            else if (charges <= 0) {
                remove(removeMode);

                return true;
            }

            setCharges((byte) charges);
        }

        return false;
    }

    public final void modChargesDelayed(int num) {
        modChargesDelayed(num, AuraRemoveMode.Default);
    }

    public final void modChargesDelayed(int num, AuraRemoveMode removeMode) {
        chargeDropEvent = null;
        modCharges(num, removeMode);
    }

    public final void dropChargeDelayed(int delay) {
        dropChargeDelayed(delay, AuraRemoveMode.Default);
    }

    public final void updateTargetMap(Unit caster, boolean apply) {
        if (isRemoved()) {
            return;
        }

        updateTargetMapInterval = UPDATE_TARGET_MAP_INTERVAL;

        // fill up to date target list
        //       target, effMask
        var targets = fillTargetMap(caster);

        ArrayList<Unit> targetsToRemove = new ArrayList<>();

        // mark all auras as ready to remove
        for (var app : auraApplications.entrySet()) {
            // not found in current area - remove the aura
            TValue existing;
            if (!(targets.containsKey(app.getValue().target) && (existing = targets.get(app.getValue().target)) == existing)) {
                targetsToRemove.add(app.getValue().target);
            } else {
                // needs readding - remove now, will be applied in next update cycle
                // (dbcs do not have auras which apply on same type of targets but have different radius, so this is not really needed)
                if (app.getValue().target.isImmunedToSpell(getSpellInfo(), caster, true) || !canBeAppliedOn(app.getValue().target)) {
                    targetsToRemove.add(app.getValue().target);

                    continue;
                }

                // check target immunities (for existing targets)
                for (var spellEffectInfo : getSpellInfo().getEffects()) {
                    if (app.getValue().target.isImmunedToSpellEffect(getSpellInfo(), spellEffectInfo, caster, true)) {
                        existing.remove(spellEffectInfo.effectIndex);
                    }
                }

                targets.put(app.getValue().target, existing);

                // needs to add/remove effects from application, don't remove from map so it gets updated
                if (!app.getValue().effectMask.SetEquals(existing)) {
                    continue;
                }

                // nothing to do - aura already applied
                // remove from auras to register list
                targets.remove(app.getValue().target);
            }
        }

        // register auras for units
        for (var unit : targets.keySet().ToList()) {
            var addUnit = true;
            // check target immunities
            var aurApp = getApplicationOfTarget(unit.GUID);

            if (aurApp == null) {
                // check target immunities (for new targets)
                for (var spellEffectInfo : getSpellInfo().getEffects()) {
                    if (unit.isImmunedToSpellEffect(getSpellInfo(), spellEffectInfo, caster)) {
                        targets.get(unit).remove(spellEffectInfo.effectIndex);
                    }
                }

                if (targets.get(unit).isEmpty() || unit.isImmunedToSpell(getSpellInfo(), caster) || !canBeAppliedOn(unit)) {
                    addUnit = false;
                }
            }

            if (addUnit && !unit.isHighestExclusiveAura(this, true)) {
                addUnit = false;
            }

            // Dynobj auras don't hit flying targets
            if (getAuraObjType() == AuraObjectType.DynObj && unit.IsInFlight) {
                addUnit = false;
            }

            // Do not apply aura if it cannot stack with existing auras
            if (addUnit) {
                // Allow to remove by stack when aura is going to be applied on owner
                if (unit != getOwner()) {
                    // check if not stacking aura already on target
                    // this one prevents unwanted usefull buff loss because of stacking and prevents overriding auras periodicaly by 2 near area aura owners
                    for (var iter : unit.AppliedAuras) {
                        var aura = iter.base;

                        if (!canStackWith(aura)) {
                            addUnit = false;

                            break;
                        }
                    }
                }
            }

            if (!addUnit) {
                targets.remove(unit);
            } else {
                // owner has to be in world, or effect has to be applied to self
                if (!owner.isSelfOrInSameMap(unit)) {
                    // @todo There is a crash caused by shadowfiend load addon
                    Log.outFatal(LogFilter.spells, "Aura {0}: Owner {1} (map {2}) is not in the same map as target {3} (map {4}).", getSpellInfo().getId(), owner.getName(), owner.isInWorld() ? (int) owner.getMap().getId() : -1, unit.getName(), unit.IsInWorld ? (int) unit.Map.Id : -1);
                }

                if (aurApp != null) {
                    aurApp.updateApplyEffectMask(targets.get(unit), true); // aura is already applied, this means we need to update effects of current application
                    targets.remove(unit);
                } else {
                    unit._CreateAuraApplication(this, targets.get(unit));
                }
            }
        }

        // remove auras from units no longer needing them
        for (var unit : targetsToRemove) {
            var aurApp = getApplicationOfTarget(unit.getGUID());

            if (aurApp != null) {
                unit._UnapplyAura(aurApp, AuraRemoveMode.Default);
            }
        }

        if (!apply) {
            return;
        }

        // apply aura effects for units
        for (var pair : targets.entrySet()) {
            var aurApp = getApplicationOfTarget(pair.getKey().GUID);

            if (aurApp != null && ((!owner.isInWorld() && owner == pair.getKey()) || owner.isInMap(pair.getKey()))) {
                // owner has to be in world, or effect has to be applied to self
                pair.getKey()._ApplyAura(aurApp, pair.getValue());
            }
        }
    }

    public final boolean isUsingStacks() {
        return spellInfo.getStackAmount() > 0;
    }

    public final int calcMaxStackAmount() {
        var maxStackAmount = spellInfo.getStackAmount();
        var caster = getCaster();

        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Integer> tempRef_maxStackAmount = new tangible.RefObject<Integer>(maxStackAmount);
                modOwner.applySpellMod(spellInfo, SpellModOp.MaxAuraStacks, tempRef_maxStackAmount);
                maxStackAmount = tempRef_maxStackAmount.refArgValue;
            }
        }

        return maxStackAmount;
    }

    public final boolean modStackAmount(double num, AuraRemoveMode removeMode) {
        return modStackAmount(num, removeMode, true);
    }

    public final boolean modStackAmount(double num) {
        return modStackAmount(num, AuraRemoveMode.Default, true);
    }

    public final boolean modStackAmount(double num, AuraRemoveMode removeMode, boolean resetPeriodicTimer) {
        return modStackAmount((int) num, removeMode, resetPeriodicTimer);
    }

    public final boolean modStackAmount(int num, AuraRemoveMode removeMode) {
        return modStackAmount(num, removeMode, true);
    }

    public final boolean modStackAmount(int num) {
        return modStackAmount(num, AuraRemoveMode.Default, true);
    }

    public final boolean modStackAmount(int num, AuraRemoveMode removeMode, boolean resetPeriodicTimer) {
        var stackAmount = stackAmount + num;
        var maxStackAmount = calcMaxStackAmount();

        // limit the stack amount (only on stack increase, stack amount may be changed manually)
        if ((num > 0) && (stackAmount > maxStackAmount)) {
            // not stackable aura - set stack amount to 1
            if (spellInfo.getStackAmount() == 0) {
                stackAmount = 1;
            } else {
                stackAmount = (int) spellInfo.getStackAmount();
            }
        }
        // we're out of stacks, remove
        else if (stackAmount <= 0) {
            remove(removeMode);

            return true;
        }

        var refresh = stackAmount >= getStackAmount() && (spellInfo.getStackAmount() != 0 || (!spellInfo.hasAttribute(SpellAttr1.AuraUnique) && !spellInfo.hasAttribute(SpellAttr5.AuraUniquePerCaster)));

        // Update stack amount
        setStackAmount((byte) stackAmount);

        if (refresh) {
            refreshTimers(resetPeriodicTimer);

            // reset charges
            setCharges(calcMaxCharges());
        }

        setNeedClientUpdateForTargets();

        return false;
    }

    public final boolean hasMoreThanOneEffectForType(AuraType auraType) {
        int count = 0;

        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (hasEffect(spellEffectInfo.effectIndex) && spellEffectInfo.applyAuraName == auraType) {
                ++count;
            }
        }

        return count > 1;
    }

    public final boolean isArea() {
        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            if (hasEffect(spellEffectInfo.effectIndex) && spellEffectInfo.isAreaAuraEffect()) {
                return true;
            }
        }

        return false;
    }

    public final boolean isRemovedOnShapeLost(Unit target) {
        return Objects.equals(getCasterGuid(), target.getGUID()) && spellInfo.getStances() != 0 && !spellInfo.hasAttribute(SpellAttr2.AllowWhileNotShapeshiftedCasterForm) && !spellInfo.hasAttribute(SpellAttr0.NotShapeshifted);
    }

    public final boolean canBeSaved() {
        if (isPassive()) {
            return false;
        }

        if (getSpellInfo().isChanneled()) {
            return false;
        }

        // Check if aura is single target, not only spell info
        if (ObjectGuid.opNotEquals(getCasterGuid(), getOwner().getGUID())) {
            // owner == caster for area auras, check for possible bad data in DB
            for (var spellEffectInfo : getSpellInfo().getEffects()) {
                if (!spellEffectInfo.isEffect()) {
                    continue;
                }

                if (spellEffectInfo.isTargetingArea() || spellEffectInfo.isAreaAuraEffect()) {
                    return false;
                }
            }

            if (isSingleTarget() || getSpellInfo().isSingleTarget()) {
                return false;
            }
        }

        if (getSpellInfo().hasAttribute(SpellCustomAttributes.AuraCannotBeSaved)) {
            return false;
        }

        // don't save auras removed by proc system
        if (isUsingCharges() && getCharges() == 0) {
            return false;
        }

        // don't save permanent auras triggered by items, they'll be recasted on login if necessary
        if (!getCastItemGuid().isEmpty() && isPermanent()) {
            return false;
        }

        return true;
    }

    public final boolean isSingleTargetWith(Aura aura) {
        // Same spell?
        if (getSpellInfo().isRankOf(aura.getSpellInfo())) {
            return true;
        }

        var spec = getSpellInfo().getSpellSpecific();

        // spell with single target specific types
        switch (spec) {
            case MagePolymorph:
                if (aura.getSpellInfo().getSpellSpecific() == spec) {
                    return true;
                }

                break;
            default:
                break;
        }

        return false;
    }

    public final void unregisterSingleTarget() {
        var caster = getCaster();
        caster.getSingleCastAuras().remove(this);
        setSingleTarget(false);
    }

    public final int calcDispelChance(Unit auraTarget, boolean offensive) {
        // we assume that aura dispel chance is 100% on start
        // need formula for level difference based chance
        var resistChance = 0;

        // Apply dispel mod from aura caster
        var caster = getCaster();

        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Integer> tempRef_resistChance = new tangible.RefObject<Integer>(resistChance);
                modOwner.applySpellMod(getSpellInfo(), SpellModOp.DispelResistance, tempRef_resistChance);
                resistChance = tempRef_resistChance.refArgValue;
            }
        }

        resistChance = resistChance < 0 ? 0 : resistChance;
        resistChance = resistChance > 100 ? 100 : resistChance;

        return 100 - resistChance;
    }

    // targets have to be registered and not have effect applied yet to use this function
    public final void _ApplyEffectForTargets(int effIndex) {
        // prepare list of aura targets
        ArrayList<Unit> targetList = new ArrayList<>();

        for (var app : auraApplications.values()) {
            if (app.EffectsToApply.contains(effIndex) && !app.hasEffect(effIndex)) {
                targetList.add(app.target);
            }
        }

        // apply effect to targets
        for (var unit : targetList) {
            if (getApplicationOfTarget(unit.getGUID()) != null) {
                // owner has to be in world, or effect has to be applied to self
                unit._ApplyAuraEffect(this, effIndex);
            }
        }
    }

    public final void setLoadedState(int maxduration, int duration, int charges, byte stackamount, int recalculateMask, HashMap<Integer, Double> amount) {
        maxDuration = maxduration;
        duration = duration;
        procCharges = (byte) charges;
        isUsingCharges = procCharges != 0;
        stackAmount = stackamount;
        var caster = getCaster();

        for (var effect : getAuraEffects().entrySet()) {
            effect.getValue().setAmount(amount.get(effect.getValue().effIndex));
            effect.getValue().setCanBeRecalculated((boolean) (recalculateMask & (1 << effect.getValue().effIndex)));
            effect.getValue().calculatePeriodic(caster, false, true);
            effect.getValue().calculateSpellMod();
            effect.getValue().recalculateAmount(caster);
        }
    }

    public final boolean hasEffectType(AuraType type) {
        for (var eff : getAuraEffects().entrySet()) {
            if (eff.getValue().auraType == type) {
                return true;
            }
        }

        return false;
    }

    public final void recalculateAmountOfEffects() {
        var caster = getCaster();

        for (var effect : getAuraEffects().entrySet()) {
            if (!isRemoved()) {
                effect.getValue().recalculateAmount(caster);
            }
        }
    }

    public final void handleAllEffects(AuraApplication aurApp, AuraEffectHandleModes mode, boolean apply) {
        for (var effect : getAuraEffects().entrySet()) {
            if (!isRemoved()) {
                effect.getValue().handleEffect(aurApp, mode, apply);
            }
        }
    }

    public final ArrayList<AuraApplication> getApplicationList() {
        var applicationList = new ArrayList<>();

        for (var aurApp : auraApplications.values()) {
            if (aurApp.effectMask.count != 0) {
                applicationList.add(aurApp);
            }
        }

        return applicationList;
    }

    public final void setNeedClientUpdateForTargets() {
        for (var app : auraApplications.values()) {
            app.setNeedClientUpdate();
        }
    }

    public final void dropChargeDelayed(int delay, AuraRemoveMode removeMode) {
        // aura is already during delayed charge drop
        if (chargeDropEvent != null) {
            return;
        }

        // only units have events
        var owner = owner.toUnit();

        if (!owner) {
            return;
        }

        chargeDropEvent = new ChargeDropEvent(this, removeMode);
        owner.getEvents().addEvent(chargeDropEvent, owner.getEvents().CalculateTime(duration.ofSeconds(delay)), true);
    }

    public final boolean canStackWith(Aura existingAura) {
        // Can stack with self
        if (this == existingAura) {
            return true;
        }

        var sameCaster = Objects.equals(getCasterGuid(), existingAura.getCasterGuid());
        var existingSpellInfo = existingAura.getSpellInfo();

        // Dynobj auras do not stack when they come from the same spell cast by the same caster
        if (getAuraObjType() == AuraObjectType.DynObj || existingAura.getAuraObjType() == AuraObjectType.DynObj) {
            if (sameCaster && spellInfo.getId() == existingSpellInfo.getId()) {
                return false;
            }

            return true;
        }

        // passive auras don't stack with another rank of the spell cast by same caster
        if (isPassive() && sameCaster && (spellInfo.isDifferentRankOf(existingSpellInfo) || (spellInfo.getId() == existingSpellInfo.getId() && castItemGuid.isEmpty()))) {
            return false;
        }

        for (var spellEffectInfo : existingSpellInfo.getEffects()) {
            // prevent remove triggering aura by triggered aura
            if (spellEffectInfo.triggerSpell == getId()) {
                return true;
            }
        }

        for (var spellEffectInfo : getSpellInfo().getEffects()) {
            // prevent remove triggered aura by triggering aura refresh
            if (spellEffectInfo.triggerSpell == existingAura.getId()) {
                return true;
            }
        }

        // check spell specific stack rules
        if (spellInfo.isAuraExclusiveBySpecificWith(existingSpellInfo) || (sameCaster && spellInfo.isAuraExclusiveBySpecificPerCasterWith(existingSpellInfo))) {
            return false;
        }

        // check spell group stack rules
        switch (global.getSpellMgr().checkSpellGroupStackRules(spellInfo, existingSpellInfo)) {
            case Exclusive:
            case ExclusiveHighest: // if it reaches this point, existing aura is lower/equal
                return false;
            case ExclusiveFromSameCaster:
                if (sameCaster) {
                    return false;
                }

                break;
            case Default:
            case ExclusiveSameEffect:
            default:
                break;
        }

        if (spellInfo.getSpellFamilyName() != existingSpellInfo.getSpellFamilyName()) {
            return true;
        }

        if (!sameCaster) {
            // Channeled auras can stack if not forbidden by db or aura type
            if (existingAura.getSpellInfo().isChanneled()) {
                return true;
            }

            if (spellInfo.hasAttribute(SpellAttr3.DotStackingRule)) {
                return true;
            }

            // check same periodic auras

//			bool hasPeriodicNonAreaEffect(SpellInfo spellInfo)
//				{
//					foreach (var spellEffectInfo in spellInfo.effects)
//						switch (spellEffectInfo.applyAuraName)
//						{
//							// DOT or HOT from different casters will stack
//							case AuraType.PeriodicDamage:
//							case AuraType.PeriodicDummy:
//							case AuraType.PeriodicHeal:
//							case AuraType.PeriodicTriggerSpell:
//							case AuraType.PeriodicEnergize:
//							case AuraType.PeriodicManaLeech:
//							case AuraType.PeriodicLeech:
//							case AuraType.PowerBurn:
//							case AuraType.ObsModPower:
//							case AuraType.ObsModHealth:
//							case AuraType.PeriodicTriggerSpellWithValue:
//							{
//								// periodic auras which target areas are not allowed to stack this way (replenishment for example)
//								if (spellEffectInfo.IsTargetingArea)
//									return false;
//
//								return true;
//							}
//							default:
//								break;
//						}
//
//					return false;
//				}

            if (hasPeriodicNonAreaEffect(spellInfo) && hasPeriodicNonAreaEffect(existingSpellInfo)) {
                return true;
            }
        }

        if (hasEffectType(AuraType.ControlVehicle) && existingAura.hasEffectType(AuraType.ControlVehicle)) {
            Vehicle veh = null;

            if (getOwner().toUnit()) {
                veh = getOwner().toUnit().getVehicleKit();
            }

            if (!veh) // We should probably just let it stack. Vehicle system will prevent undefined behaviour later
            {
                return true;
            }

            if (veh.GetAvailableSeatCount() == 0) {
                return false; // No empty seat available
            }

            return true; // Empty seat available (skip rest)
        }

        if (hasEffectType(AuraType.ShowConfirmationPrompt) || hasEffectType(AuraType.ShowConfirmationPromptWithDifficulty)) {
            if (existingAura.hasEffectType(AuraType.ShowConfirmationPrompt) || existingAura.hasEffectType(AuraType.ShowConfirmationPromptWithDifficulty)) {
                return false;
            }
        }

        // spell of same spell rank chain
        if (spellInfo.isRankOf(existingSpellInfo)) {
            // don't allow passive area auras to stack
            if (spellInfo.isMultiSlotAura() && !isArea()) {
                return true;
            }

            if (!getCastItemGuid().isEmpty() && !existingAura.getCastItemGuid().isEmpty()) {
                if (ObjectGuid.opNotEquals(getCastItemGuid(), existingAura.getCastItemGuid()) && spellInfo.hasAttribute(SpellCustomAttributes.EnchantProc)) {
                    return true;
                }
            }

            // same spell with same caster should not stack
            return false;
        }

        return true;
    }

    public final boolean isProcOnCooldown(LocalDateTime now) {
        return procCooldown.compareTo(now) > 0;
    }

    public final AuraKey generateKey(tangible.OutObject<Integer> recalculateMask) {
        AuraKey key = new AuraKey(getCasterGuid(), getCastItemGuid(), getId(), 0);
        recalculateMask.outArgValue = 0;

        for (var aurEff : effects.entrySet()) {
            key.effectMask |= 1 << aurEff.getKey();

            if (aurEff.getValue().canBeRecalculated()) {
                recalculateMask.outArgValue |= 1 << aurEff.getKey();
            }
        }

        return key;
    }

    public final void resetProcCooldown() {
        procCooldown = LocalDateTime.now();
    }

    public final void prepareProcToTrigger(AuraApplication aurApp, ProcEventInfo eventInfo, LocalDateTime now) {
        var prepare = callScriptPrepareProcHandlers(aurApp, eventInfo);

        if (!prepare) {
            return;
        }

        var procEntry = global.getSpellMgr().getSpellProcEntry(getSpellInfo());

        prepareProcChargeDrop(procEntry, eventInfo);

        // cooldowns should be added to the whole aura (see 51698 area aura)
        addProcCooldown(procEntry, now);

        setLastProcSuccessTime(now);
    }

    public final void prepareProcChargeDrop(SpellProcEntry procEntry, ProcEventInfo eventInfo) {
        // take one charge, aura expiration will be handled in aura.TriggerProcOnEvent (if needed)
        if (!procEntry.getAttributesMask().hasFlag(ProcAttributes.UseStacksForCharges) && isUsingCharges() && (eventInfo.getSpellInfo() == null || !eventInfo.getSpellInfo().hasAttribute(SpellAttr6.DoNotConsumeResources))) {
            --_procCharges;
            setNeedClientUpdateForTargets();
        }
    }

    public final void consumeProcCharges(SpellProcEntry procEntry) {
        // Remove aura if we've used last charge to proc
        if (procEntry.getAttributesMask().hasFlag(ProcAttributes.UseStacksForCharges)) {
            modStackAmount(-1);
        } else if (isUsingCharges()) {
            if (getCharges() == 0) {
                remove();
            }
        }
    }

    public final HashSet<Integer> getProcEffectMask(AuraApplication aurApp, ProcEventInfo eventInfo, LocalDateTime now) {
        SpellProcEntry procEntry = null;
        this.<IAuraOverrideProcInfo>ForEachAuraScript(a -> procEntry = a.SpellProcEntry);

        if (procEntry == null) {
            global.getSpellMgr().getSpellProcEntry(getSpellInfo());
        }

        // only auras with spell proc entry can trigger proc
        if (procEntry == null) {
            return DUMMYHASHSET;
        }

        // check spell triggering us
        var spell = eventInfo.getProcSpell();

        if (spell) {
            // Do not allow auras to proc from effect triggered from itself
            if (spell.isTriggeredByAura(spellInfo)) {
                return DUMMYHASHSET;
            }

            // check if aura can proc when spell is triggered (exception for hunter auto shot & wands)
            if (!spell.getTriggeredAllowProc() && !getSpellInfo().hasAttribute(SpellAttr3.CanProcFromProcs) && !procEntry.getAttributesMask().hasFlag(ProcAttributes.TriggeredCanProc) && !eventInfo.getTypeMask().hasFlag(procFlags.AutoAttackMask)) {
                if (spell.isTriggered() && !spell.spellInfo.hasAttribute(SpellAttr3.NotAProc)) {
                    return DUMMYHASHSET;
                }
            }

            if (spell.castItem != null && procEntry.getAttributesMask().hasFlag(ProcAttributes.CantProcFromItemCast)) {
                return DUMMYHASHSET;
            }

            if (spell.spellInfo.hasAttribute(SpellAttr4.SuppressWeaponProcs) && getSpellInfo().hasAttribute(SpellAttr6.AuraIsWeaponProc)) {
                return DUMMYHASHSET;
            }

            if (getSpellInfo().hasAttribute(SpellAttr12.OnlyProcFromClassAbilities) && !spell.spellInfo.hasAttribute(SpellAttr13.AllowClassAbilityProcs)) {
                return DUMMYHASHSET;
            }
        }

        // check don't break stealth attr present
        if (spellInfo.hasAura(AuraType.ModStealth)) {
            var eventSpellInfo = eventInfo.getSpellInfo();

            if (eventSpellInfo != null) {
                if (eventSpellInfo.hasAttribute(SpellCustomAttributes.DontBreakStealth)) {
                    return DUMMYHASHSET;
                }
            }
        }

        // check if we have charges to proc with
        if (isUsingCharges()) {
            if (getCharges() == 0) {
                return DUMMYHASHSET;
            }

            if (procEntry.getAttributesMask().hasFlag(ProcAttributes.ReqSpellmod)) {
                var eventSpell = eventInfo.getProcSpell();

                if (eventSpell != null) {
                    if (!eventSpell.appliedMods.contains(this)) {
                        return DUMMYHASHSET;
                    }
                }
            }
        }

        // check proc cooldown
        if (isProcOnCooldown(now)) {
            return DUMMYHASHSET;
        }

        // do checks against db data

        if (!SpellManager.canSpellTriggerProcOnEvent(procEntry, eventInfo)) {
            return DUMMYHASHSET;
        }

        // do checks using conditions table
        if (!global.getConditionMgr().isObjectMeetingNotGroupedConditions(ConditionSourceType.SpellProc, getId(), eventInfo.getActor(), eventInfo.getActionTarget())) {
            return DUMMYHASHSET;
        }

        // AuraScript Hook
        var check = callScriptCheckProcHandlers(aurApp, eventInfo);

        if (!check) {
            return DUMMYHASHSET;
        }

        // At least one effect has to pass checks to proc aura
        var procEffectMask = aurApp.getEffectMask().ToHashSet();

        for (var aurEff : getAuraEffects().entrySet()) {
            if (procEffectMask.contains(aurEff.getKey())) {
                if ((procEntry.getDisableEffectsMask() & (1 << aurEff.getKey())) != 0 || !aurEff.getValue().checkEffectProc(aurApp, eventInfo)) {
                    procEffectMask.remove(aurEff.getKey());
                }
            }
        }

        if (procEffectMask.count == 0) {
            return DUMMYHASHSET;
        }

        // @todo
        // do allow additional requirements for procs
        // this is needed because this is the last moment in which you can prevent aura charge drop on proc
        // and possibly a way to prevent default checks (if there're going to be any)

        // Check if current equipment meets aura requirements
        // do that only for passive spells
        // @todo this needs to be unified for all kinds of auras
        var target = aurApp.getTarget();

        if (isPassive() && target.isPlayer() && getSpellInfo().getEquippedItemClass() != itemClass.NONE) {
            if (!getSpellInfo().hasAttribute(SpellAttr3.NoProcEquipRequirement)) {
                Item item = null;

                if (getSpellInfo().getEquippedItemClass() == itemClass.Weapon) {
                    if (target.toPlayer().isInFeralForm()) {
                        return DUMMYHASHSET;
                    }

                    var damageInfo = eventInfo.getDamageInfo();

                    if (damageInfo != null) {
                        if (damageInfo.getAttackType() != WeaponAttackType.OffAttack) {
                            item = target.toPlayer().getUseableItemByPos(InventorySlots.Bag0, EquipmentSlot.MainHand);
                        } else {
                            item = target.toPlayer().getUseableItemByPos(InventorySlots.Bag0, EquipmentSlot.OffHand);
                        }
                    }
                } else if (getSpellInfo().getEquippedItemClass() == itemClass.armor) {
                    // Check if player is wearing shield
                    item = target.toPlayer().getUseableItemByPos(InventorySlots.Bag0, EquipmentSlot.OffHand);
                }

                if (!item || item.isBroken() || !item.isFitToSpellRequirements(getSpellInfo())) {
                    return DUMMYHASHSET;
                }
            }
        }

        if (spellInfo.hasAttribute(SpellAttr3.OnlyProcOutdoors)) {
            if (!target.isOutdoors()) {
                return DUMMYHASHSET;
            }
        }

        if (spellInfo.hasAttribute(SpellAttr3.OnlyProcOnCaster)) {
            if (ObjectGuid.opNotEquals(target.getGUID(), getCasterGuid())) {
                return DUMMYHASHSET;
            }
        }

        if (!spellInfo.hasAttribute(SpellAttr4.AllowProcWhileSitting)) {
            if (!target.isStandState()) {
                return DUMMYHASHSET;
            }
        }

        var success = RandomUtil.randChance(calcProcChance(procEntry, eventInfo));

        setLastProcAttemptTime(now);

        if (success) {
            return procEffectMask;
        }

        return DUMMYHASHSET;
    }


//	public list<(IAuraScript, IAuraEffectHandler)> getEffectScripts(AuraScriptHookType h, int index)
//		{
//			if (effectHandlers.TryGetValue(index, out var effDict) && effDict.TryGetValue(h, out var scripts))
//				return scripts;
//
//			return DUMMYAURAEFFECTS;
//		}

    public final void triggerProcOnEvent(HashSet<Integer> procEffectMask, AuraApplication aurApp, ProcEventInfo eventInfo) {
        var prevented = callScriptProcHandlers(aurApp, eventInfo);

        if (!prevented) {
            for (var aurEff : getAuraEffects().entrySet()) {
                if (!procEffectMask.contains(aurEff.getKey())) {
                    continue;
                }

                // OnEffectProc / AfterEffectProc hooks handled in AuraEffect.handleProc()
                if (aurApp.hasEffect(aurEff.getKey())) {
                    aurEff.getValue().handleProc(aurApp, eventInfo);
                }
            }

            callScriptAfterProcHandlers(aurApp, eventInfo);
        }

        consumeProcCharges(global.getSpellMgr().getSpellProcEntry(getSpellInfo()));
    }

    public final double calcPPMProcChance(Unit actor) {
        // Formula see http://us.battle.net/wow/en/forum/topic/8197741003#1
        var ppm = spellInfo.calcProcPPM(actor, getCastItemLevel());
        var averageProcInterval = 60.0f / ppm;

        var currentTime = gameTime.Now();
        var secondsSinceLastAttempt = Math.min((float) (currentTime - lastProcAttemptTime).TotalSeconds, 10.0f);
        var secondsSinceLastProc = Math.min((float) (currentTime - lastProcSuccessTime).TotalSeconds, 1000.0f);

        var chance = Math.max(1.0f, 1.0f + ((secondsSinceLastProc / averageProcInterval - 1.5f) * 3.0f)) * ppm * secondsSinceLastAttempt / 60.0f;
        tangible.RefObject<Double> tempRef_chance = new tangible.RefObject<Double>(chance);
        MathUtil.RoundToInterval(tempRef_chance, 0.0f, 1.0f);
        chance = tempRef_chance.refArgValue;

        return chance * 100.0f;
    }

    public final void loadScripts() {
        loadedScripts = global.getScriptMgr().createAuraScripts(spellInfo.getId(), this);

        for (var script : loadedScripts) {
            Log.outDebug(LogFilter.spells, "Aura.LoadScripts: Script `{0}` for aura `{1}` is loaded now", script._GetScriptName(), spellInfo.getId());
            script.register();

            if (script instanceof IAuraScript) {
                for (var iFace : script.getClass().getInterfaces()) {
                    if (iFace.name.equals("IBaseSpellScript") || iFace.name.equals("IAuraScript")) {
                        continue;
                    }

                    TValue spellScripts;
                    if (!(auraScriptsByType.containsKey(iFace) && (spellScripts = auraScriptsByType.get(iFace)) == spellScripts)) {
                        spellScripts = new ArrayList<>();
                        auraScriptsByType.put(iFace, spellScripts);
                    }

                    spellScripts.add(script);
                    registerSpellEffectHandler(script);
                }
            }
        }
    }

    public final <T extends IAuraScript> ArrayList<IAuraScript> getAuraScripts() {
        TValue scripts;
        if (auraScriptsByType.containsKey(T.class) && (scripts = auraScriptsByType.get(T.class)) == scripts) {
            return scripts;
        }

        return DUMMY;
    }

    public final <T extends IAuraScript> void forEachAuraScript(tangible.Action1Param<T> action) {
        for (T script : this.<T>GetAuraScripts()) {
            try {
                action.invoke(script);
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final <T> boolean usesScriptType() {
        return auraScriptsByType.containsKey(T.class);
    }

    public void remove() {
        remove(AuraRemoveMode.Default);
    }

    public void remove(AuraRemoveMode removeMode) {
        this.<IAuraOnRemove>ForEachAuraScript(a -> a.AuraRemoved(removeMode));
    }

    public final void _RegisterForTargets() {
        var caster = getCaster();
        updateTargetMap(caster, false);
    }

    public final void applyForTargets() {
        var caster = getCaster();
        updateTargetMap(caster, true);
    }

    public final int calcMaxDuration() {
        return calcMaxDuration(getCaster());
    }

    public final byte calcMaxCharges() {
        return calcMaxCharges(getCaster());
    }

    public final boolean dropCharge() {
        return dropCharge(AuraRemoveMode.Default);
    }

    public final boolean dropCharge(AuraRemoveMode removeMode) {
        return modCharges(-1, removeMode);
    }

    public final boolean hasEffect(SpellEffIndex index) {
        return getEffect(index) != null;
    }

    public final AuraEffect getEffect(SpellEffIndex index) {
        TValue val;
        if (effects.containsKey(index) && (val = effects.get(index)) == val) {
            return val;
        }

        return null;
    }

    public final boolean tryGetEffect(int index, tangible.OutObject<AuraEffect> val) {
        if (effects.containsKey(index) && (val.outArgValue = effects.get(index)) == val.outArgValue) {
            return true;
        }

        return false;
    }

    // trigger effects on real aura apply/remove
    public final void handleAuraSpecificMods(AuraApplication aurApp, Unit caster, boolean apply, boolean onReapply) {
        var target = aurApp.getTarget();
        var removeMode = aurApp.getRemoveMode();
        // handle spell_area table
        var saBounds = global.getSpellMgr().getSpellAreaForAuraMapBounds(getId());

        if (saBounds != null) {
            int zone;
            tangible.OutObject<Integer> tempOut_zone = new tangible.OutObject<Integer>();
            int area;
            tangible.OutObject<Integer> tempOut_area = new tangible.OutObject<Integer>();
            target.getZoneAndAreaId(tempOut_zone, tempOut_area);
            area = tempOut_area.outArgValue;
            zone = tempOut_zone.outArgValue;

            for (var spellArea : saBounds) {
                // some auras remove at aura remove
                if (spellArea.flags.hasFlag(SpellAreaFlag.AutoRemove) && !spellArea.isFitToRequirements((player) target, zone, area)) {
                    target.removeAura(spellArea.spellId);
                }
                // some auras applied at aura apply
                else if (spellArea.flags.hasFlag(SpellAreaFlag.AutoCast)) {
                    if (!target.hasAura(spellArea.spellId)) {
                        target.castSpell(target, spellArea.spellId, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCastId(getCastId()));
                    }
                }
            }
        }

        // handle spell_linked_spell table
        if (!onReapply) {
            // apply linked auras
            if (apply) {
                var spellTriggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.aura, getId());

                if (spellTriggered != null) {
                    for (var spell : spellTriggered) {
                        if (spell < 0) {
                            target.applySpellImmune(getId(), SpellImmunity.id, (int) -spell, true);
                        } else if (caster != null) {
                            caster.addAura((int) spell, target);
                        }
                    }
                }
            } else {
                // remove linked auras
                var spellTriggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.Remove, getId());

                if (spellTriggered != null) {
                    for (var spell : spellTriggered) {
                        if (spell < 0) {
                            target.removeAura((int) -spell);
                        } else if (removeMode != AuraRemoveMode.Death) {
                            target.castSpell(target, (int) spell, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCaster(getCasterGuid()).setOriginalCastId(getCastId()));
                        }
                    }
                }

                spellTriggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.aura, getId());

                if (spellTriggered != null) {
                    for (var id : spellTriggered) {
                        if (id < 0) {
                            target.applySpellImmune(getId(), SpellImmunity.id, (int) -id, false);
                        } else {
                            target.removeAura((int) id, getCasterGuid(), removeMode);
                        }
                    }
                }
            }
        } else if (apply) {
            // modify stack amount of linked auras
            var spellTriggered = global.getSpellMgr().getSpellLinked(SpellLinkedType.aura, getId());

            if (spellTriggered != null) {
                for (var id : spellTriggered) {
                    if (id > 0) {
                        var triggeredAura = target.getAura((int) id, getCasterGuid());

                        if (triggeredAura != null) {
                            triggeredAura.modStackAmount(getStackAmount() - triggeredAura.getStackAmount());
                        }
                    }
                }
            }
        }

        // mods at aura apply
        if (apply) {
            switch (getSpellInfo().getSpellFamilyName()) {
                case Generic:
                    switch (getId()) {
                        case 33572: // Gronn Lord's Grasp, becomes stoned
                            if (getStackAmount() >= 5 && !target.hasAura(33652)) {
                                target.castSpell(target, 33652, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCastId(getCastId()));
                            }

                            break;
                        case 50836: //Petrifying Grip, becomes stoned
                            if (getStackAmount() >= 5 && !target.hasAura(50812)) {
                                target.castSpell(target, 50812, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCastId(getCastId()));
                            }

                            break;
                        case 60970: // Heroic Fury (remove Intercept cooldown)
                            if (target.isTypeId(TypeId.PLAYER)) {
                                target.getSpellHistory().resetCooldown(20252, true);
                            }

                            break;
                    }

                    break;
                case Druid:
                    if (caster == null) {
                        break;
                    }

                    // Rejuvenation
                    if (getSpellInfo().getSpellFamilyFlags().get(0).hasFlag(0x10) && getEffect(0) != null) {
                        // Druid T8 Restoration 4P Bonus
                        if (caster.hasAura(64760)) {
                            CastSpellExtraArgs args = new CastSpellExtraArgs(getEffect(0));
                            args.addSpellMod(SpellValueMod.BasePoint0, getEffect(0).getAmount());
                            caster.castSpell(target, 64801, args);
                        }
                    }

                    break;
            }
        }
        // mods at aura remove
        else {
            switch (getSpellInfo().getSpellFamilyName()) {
                case Mage:
                    switch (getId()) {
                        case 66: // Invisibility
                            if (removeMode != AuraRemoveMode.Expire) {
                                break;
                            }

                            target.castSpell(target, 32612, new CastSpellExtraArgs(getEffect(1)));

                            break;
                        default:
                            break;
                    }

                    break;
                case Priest:
                    if (caster == null) {
                        break;
                    }

                    // Power word: shield
                    if (removeMode == AuraRemoveMode.EnemySpell && getSpellInfo().getSpellFamilyFlags().get(0).hasFlag(0x00000001)) {
                        // Rapture
                        var aura = caster.getAuraOfRankedSpell(47535);

                        if (aura != null) {
                            // check cooldown
                            if (caster.isTypeId(TypeId.PLAYER)) {
                                if (caster.getSpellHistory().hasCooldown(aura.getSpellInfo())) {
                                    // This additional check is needed to add a minimal delay before cooldown in in effect
                                    // to allow all bubbles broken by a single damage source proc mana return
                                    if (caster.getSpellHistory().getRemainingCooldown(aura.getSpellInfo()) <= duration.FromSeconds(11)) {
                                        break;
                                    }
                                } else // and add if needed
                                {
                                    caster.getSpellHistory().addCooldown(aura.getId(), 0, duration.FromSeconds(12));
                                }
                            }

                            // effect on caster
                            var aurEff = aura.getEffect(0);

                            if (aurEff != null) {
                                var multiplier = aurEff.getAmount();
                                CastSpellExtraArgs args = new CastSpellExtraArgs(TriggerCastFlags.FullMask);
                                args.setOriginalCastId(getCastId());
                                args.addSpellMod(SpellValueMod.BasePoint0, MathUtil.CalculatePct(caster.getMaxPower(powerType.mana), multiplier));
                                caster.castSpell(caster, 47755, args);
                            }
                        }
                    }

                    break;
                case Rogue:
                    // Remove Vanish on stealth remove
                    if (getId() == 1784) {
                        target.removeAurasWithFamily(SpellFamilyNames.Rogue, new flagArray128(0x0000800, 0, 0), target.getGUID());
                    }

                    break;
            }
        }

        // mods at aura apply or remove
        switch (getSpellInfo().getSpellFamilyName()) {
            case Hunter:
                switch (getId()) {
                    case 19574: // Bestial Wrath
                        // The Beast Within cast on owner if talent present
                        var owner = target.getOwnerUnit();

                        if (owner != null) {
                            // Search talent
                            if (owner.hasAura(34692)) {
                                if (apply) {
                                    owner.castSpell(owner, 34471, new CastSpellExtraArgs(getEffect(0)));
                                } else {
                                    owner.removeAura(34471);
                                }
                            }
                        }

                        break;
                }

                break;
            case Paladin:
                switch (getId()) {
                    case 31821:
                        // Aura Mastery Triggered Spell Handler
                        // If apply Concentration aura . trigger . apply Aura Mastery Immunity
                        // If remove Concentration aura . trigger . remove Aura Mastery Immunity
                        // If remove Aura mastery . trigger . remove Aura Mastery Immunity
                        // Do effects only on aura owner
                        if (ObjectGuid.opNotEquals(getCasterGuid(), target.getGUID())) {
                            break;
                        }

                        if (apply) {
                            if ((getSpellInfo().getId() == 31821 && target.hasAura(19746, getCasterGuid())) || (getSpellInfo().getId() == 19746 && target.hasAura(31821))) {
                                target.castSpell(target, 64364, new CastSpellExtraArgs(true));
                            }
                        } else {
                            target.removeAurasDueToSpell(64364, getCasterGuid());
                        }

                        break;
                    case 31842: // Divine Favor
                        // Item - Paladin T10 Holy 2P Bonus
                        if (target.hasAura(70755)) {
                            if (apply) {
                                target.castSpell(target, 71166, (new CastSpellExtraArgs(TriggerCastFlags.FullMask)).setOriginalCastId(getCastId()));
                            } else {
                                target.removeAura(71166);
                            }
                        }

                        break;
                }

                break;
            case Warlock:
                // Drain Soul - If the target is at or below 25% health, Drain Soul causes four times the normal damage
                if (getSpellInfo().getSpellFamilyFlags().get(0).hasFlag(0x00004000)) {
                    if (caster == null) {
                        break;
                    }

                    if (apply) {
                        if (target != caster && !target.healthAbovePct(25)) {
                            caster.castSpell(caster, 100001, new CastSpellExtraArgs(true));
                        }
                    } else {
                        if (target != caster) {
                            caster.removeAura(getId());
                        } else {
                            caster.removeAura(100001);
                        }
                    }
                }

                break;
        }
    }

    public final void addProcCooldown(SpellProcEntry procEntry, LocalDateTime now) {
        // cooldowns should be added to the whole aura (see 51698 area aura)
        var procCooldown = procEntry.getCooldown();
        var caster = getCaster();

        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Integer> tempRef_procCooldown = new tangible.RefObject<Integer>(procCooldown);
                modOwner.applySpellMod(getSpellInfo(), SpellModOp.procCooldown, tempRef_procCooldown);
                procCooldown = tempRef_procCooldown.refArgValue;
            }
        }

        procCooldown = now + duration.ofSeconds(procCooldown);
    }

    public HashMap<unit, HashSet<Integer>> fillTargetMap(Unit caster) {
        return DUMMYAURAFILL;
    }

    public final void setLastProcAttemptTime(LocalDateTime lastProcAttemptTime) {
        lastProcAttemptTime = lastProcAttemptTime;
    }

    public final void setLastProcSuccessTime(LocalDateTime lastProcSuccessTime) {
        lastProcSuccessTime = lastProcSuccessTime;
    }

    public final UnitAura toUnitAura() {
        if (getAuraObjType() == AuraObjectType.unit) {
            return (UnitAura) this;
        }

        return null;
    }

    public final DynObjAura toDynObjAura() {
        if (getAuraObjType() == AuraObjectType.DynObj) {
            return (dynObjAura) this;
        }

        return null;
    }

    public final AuraApplication getApplicationOfTarget(ObjectGuid guid) {
        return auraApplications.get(guid);
    }

    public final boolean isAppliedOnTarget(ObjectGuid guid) {
        return auraApplications.containsKey(guid);
    }

    private WorldObject getWorldObjectCaster() {
        if (getCasterGuid().isUnit()) {
            return getCaster();
        }

        return global.getObjAccessor().GetWorldObject(getOwner(), getCasterGuid());
    }

    private void update(int diff, Unit caster) {
        this.<IAuraOnUpdate>ForEachAuraScript(u -> u.AuraOnUpdate(diff));

        if (duration > 0) {
            _duration -= (int) diff;

            if (duration < 0) {
                duration = 0;
            }

            // handle manaPerSecond/manaPerSecondPerLevel
            if (timeCla != 0) {
                if (timeCla > diff) {
                    _timeCla -= (int) diff;
                } else if (caster != null && (caster == getOwner() || !getSpellInfo().hasAttribute(SpellAttr2.NoTargetPerSecondCosts))) {
                    if (!periodicCosts.isEmpty()) {
                        timeCla += (int) (1000 - diff);

                        for (var power : periodicCosts) {
                            if (power.RequiredAuraSpellID != 0 && !caster.hasAura(power.RequiredAuraSpellID)) {
                                continue;
                            }

                            var manaPerSecond = (int) power.ManaPerSecond;

                            if (power.powerType != powerType.health) {
                                manaPerSecond += MathUtil.CalculatePct(caster.getMaxPower(power.powerType), power.PowerPctPerSecond);
                            } else {
                                manaPerSecond += (int) MathUtil.CalculatePct(caster.getMaxHealth(), power.PowerPctPerSecond);
                            }

                            if (manaPerSecond != 0) {
                                if (power.powerType == powerType.health) {
                                    if ((int) caster.getHealth() > manaPerSecond) {
                                        caster.modifyHealth(-manaPerSecond);
                                    } else {
                                        remove();
                                    }
                                } else if (caster.getPower(power.powerType) >= manaPerSecond) {
                                    caster.modifyPower(power.powerType, -manaPerSecond);
                                } else {
                                    remove();
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void refreshTimers(boolean resetPeriodicTimer) {
        maxDuration = calcMaxDuration();

        if (spellInfo.hasAttribute(SpellAttr8.DontResetPeriodicTimer)) {
            var minPeriod = maxDuration;

            for (var aurEff : getAuraEffects().entrySet()) {
                var period = aurEff.getValue().period;

                if (period != 0) {
                    minPeriod = Math.min(period, minPeriod);
                }
            }

            // If only one tick remaining, roll it over into new duration
            if (getDuration() <= minPeriod) {
                maxDuration += getDuration();
                resetPeriodicTimer = false;
            }
        }

        refreshDuration();
        var caster = getCaster();

        for (var aurEff : getAuraEffects().entrySet()) {
            aurEff.getValue().calculatePeriodic(caster, resetPeriodicTimer, false);
        }
    }

    private boolean canBeAppliedOn(Unit target) {
        for (var label : getSpellInfo().labels) {
            if (target.hasAuraTypeWithMiscvalue(AuraType.SuppressItemPassiveEffectBySpellLabel, (int) label)) {
                return false;
            }
        }

        // unit not in world or during remove from world
        if (!target.isInWorld() || target.isDuringRemoveFromWorld()) {
            // area auras mustn't be applied
            if (getOwner() != target) {
                return false;
            }

            // not selfcasted single target auras mustn't be applied
            if (ObjectGuid.opNotEquals(getCasterGuid(), getOwner().getGUID()) && getSpellInfo().isSingleTarget()) {
                return false;
            }

            return true;
        } else {
            return checkAreaTarget(target);
        }
    }

    private boolean checkAreaTarget(Unit target) {
        return callScriptCheckAreaTargetHandlers(target);
    }

    private double calcProcChance(SpellProcEntry procEntry, ProcEventInfo eventInfo) {
        double chance = procEntry.getChance();
        // calculate chances depending on unit with caster's data
        // so talents modifying chances and judgements will have properly calculated proc chance
        var caster = getCaster();

        if (caster != null) {
            // calculate ppm chance if present and we're using weapon
            if (eventInfo.getDamageInfo() != null && procEntry.getProcsPerMinute() != 0) {
                var WeaponSpeed = caster.getBaseAttackTime(eventInfo.getDamageInfo().getAttackType());
                chance = caster.getPPMProcChance(WeaponSpeed, procEntry.getProcsPerMinute(), getSpellInfo());
            }

            if (getSpellInfo().getProcBasePpm() > 0.0f) {
                chance = calcPPMProcChance(caster);
            }

            // apply chance modifer aura, applies also to ppm chance (see improved judgement of light spell)
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_chance = new tangible.RefObject<Double>(chance);
                modOwner.applySpellMod(getSpellInfo(), SpellModOp.procChance, tempRef_chance);
                chance = tempRef_chance.refArgValue;
            }
        }

        // proc chance is reduced by an additional 3.333% per level past 60
        if (procEntry.getAttributesMask().hasFlag(ProcAttributes.ReduceProc60) && eventInfo.getActor().getLevel() > 60) {
            chance = Math.max(0.0f, (1.0f - ((eventInfo.getActor().getLevel() - 60) * 1.0f / 30.0f)) * chance);
        }

        return chance;
    }

    private void _DeleteRemovedApplications() {
        removedApplications.clear();
    }

    private void registerSpellEffectHandler(AuraScript script) {
        if (script instanceof IHasAuraEffects hse) {
            for (var effect : hse.getAuraEffects()) {
                if (effect instanceof IAuraEffectHandler se) {
                    int mask = 0;

                    if (se.getEffectIndex() == SpellConst.EffectAll || se.getEffectIndex() == SpellConst.EffectFirstFound) {
                        for (var aurEff : getAuraEffects().entrySet()) {
                            if (se.getEffectIndex() == SpellConst.EffectFirstFound && mask != 0) {
                                break;
                            }

                            if (checkAuraEffectHandler(se, aurEff.getKey())) {
                                addAuraEffect(aurEff.getKey(), script, se);
                            }
                        }
                    } else {
                        if (checkAuraEffectHandler(se, se.getEffectIndex())) {
                            addAuraEffect(se.getEffectIndex(), script, se);
                        }
                    }
                }
            }
        }
    }

    private boolean checkAuraEffectHandler(IAuraEffectHandler ae, int effIndex) {
        if (spellInfo.getEffects().size() <= effIndex) {
            return false;
        }

        var spellEffectInfo = spellInfo.getEffect(effIndex);

        if (spellEffectInfo.applyAuraName == 0 && ae.getAuraType() == 0) {
            return true;
        }

        if (spellEffectInfo.applyAuraName == 0) {
            return false;
        }

        return ae.getAuraType() == AuraType.Any || spellEffectInfo.applyAuraName == ae.getAuraType();
    }

    private void addAuraEffect(int index, IAuraScript script, IAuraEffectHandler effect) {
        var effecTypes;

        if (!effectHandlers.TryGetValue(index, out effecTypes)) {
            effecTypes = new Dictionary<AuraScriptHookType, ArrayList<(IAuraScript, IAuraEffectHandler) >> ();
            effectHandlers.add(index, effecTypes);
        }

        var effects;

        if (!effecTypes.TryGetValue(effect.getHookType(), out effects)) {
            effects = new ArrayList<(IAuraScript, IAuraEffectHandler) > ();
            effecTypes.add(effect.getHookType(), effects);
        }

        effects.add((script, effect));
    }

    private byte calcMaxCharges(Unit caster) {
        var maxProcCharges = spellInfo.getProcCharges();
        var procEntry = global.getSpellMgr().getSpellProcEntry(getSpellInfo());

        if (procEntry != null) {
            maxProcCharges = procEntry.getCharges();
        }

        if (caster != null) {
            var modOwner = caster.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Integer> tempRef_maxProcCharges = new tangible.RefObject<Integer>(maxProcCharges);
                modOwner.applySpellMod(getSpellInfo(), SpellModOp.procCharges, tempRef_maxProcCharges);
                maxProcCharges = tempRef_maxProcCharges.refArgValue;
            }
        }

        return (byte) maxProcCharges;
    }


    ///#region CallScripts

    private boolean callScriptCheckAreaTargetHandlers(Unit target) {
        var result = true;

        for (IAuraCheckAreaTarget auraScript : this.<IAuraCheckAreaTarget>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.CheckAreaTarget);

                result &= auraScript.checkAreaTarget(target);

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return result;
    }

    public final void callScriptDispel(DispelInfo dispelInfo) {
        for (IAuraOnDispel auraScript : this.<IAuraOnDispel>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.dispel);

                auraScript.OnDispel(dispelInfo);

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptAfterDispel(DispelInfo dispelInfo) {
        for (IAfterAuraDispel auraScript : this.<IAfterAuraDispel>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.AfterDispel);

                auraScript.HandleDispel(dispelInfo);

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final boolean callScriptEffectApplyHandlers(AuraEffect aurEff, AuraApplication aurApp, AuraEffectHandleModes mode) {
        var preventDefault = false;

        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectApply, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectApply, aurApp);

                ((IAuraApplyHandler) auraScript.item2).apply(aurEff, mode);

                if (!preventDefault) {
                    preventDefault = auraScript.Item1._IsDefaultActionPrevented();
                }

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return preventDefault;
    }

    public final boolean callScriptEffectRemoveHandlers(AuraEffect aurEff, AuraApplication aurApp, AuraEffectHandleModes mode) {
        var preventDefault = false;

        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectRemove, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectRemove, aurApp);

                ((IAuraApplyHandler) auraScript.item2).apply(aurEff, mode);

                if (!preventDefault) {
                    preventDefault = auraScript.Item1._IsDefaultActionPrevented();
                }

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return preventDefault;
    }

    public final void callScriptAfterEffectApplyHandlers(AuraEffect aurEff, AuraApplication aurApp, AuraEffectHandleModes mode) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterApply, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterApply, aurApp);

                ((IAuraApplyHandler) auraScript.item2).apply(aurEff, mode);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptAfterEffectRemoveHandlers(AuraEffect aurEff, AuraApplication aurApp, AuraEffectHandleModes mode) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterRemove, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterRemove, aurApp);

                ((IAuraApplyHandler) auraScript.item2).apply(aurEff, mode);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final boolean callScriptEffectPeriodicHandlers(AuraEffect aurEff, AuraApplication aurApp) {
        var preventDefault = false;

        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectPeriodic, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectPeriodic, aurApp);

                ((IAuraPeriodic) auraScript.item2).handlePeriodic(aurEff);

                if (!preventDefault) {
                    preventDefault = auraScript.Item1._IsDefaultActionPrevented();
                }

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return preventDefault;
    }

    public final void callScriptEffectUpdatePeriodicHandlers(AuraEffect aurEff) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectUpdatePeriodic, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectUpdatePeriodic);

                ((IAuraUpdatePeriodic) auraScript.item2).updatePeriodic(aurEff);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectCalcAmountHandlers(AuraEffect aurEff, tangible.RefObject<Double> amount, tangible.RefObject<Boolean> canBeRecalculated) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectCalcAmount, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectCalcAmount);

                ((IAuraCalcAmount) auraScript.item2).handleCalcAmount(aurEff, amount, canBeRecalculated);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectCalcPeriodicHandlers(AuraEffect aurEff, tangible.RefObject<Boolean> isPeriodic, tangible.RefObject<Integer> amplitude) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectCalcPeriodic, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectCalcPeriodic);

                var boxed = new BoxedValue<Boolean>(isPeriodic.refArgValue);
                var amp = new BoxedValue<Integer>(amplitude.refArgValue);

                ((IAuraCalcPeriodic) auraScript.item2).calcPeriodic(aurEff, boxed, amp);

                isPeriodic.refArgValue = boxed.value;
                amplitude.refArgValue = amp.value;

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectCalcSpellModHandlers(AuraEffect aurEff, SpellModifier spellMod) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectCalcSpellmod, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectCalcSpellmod);

                ((IAuraCalcSpellMod) auraScript.item2).calcSpellMod(aurEff, spellMod);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectCalcCritChanceHandlers(AuraEffect aurEff, AuraApplication aurApp, Unit victim, tangible.RefObject<Double> critChance) {
        for (var loadedScript : getEffectScripts(AuraScriptHookType.EffectCalcCritChance, aurEff.getEffIndex())) {
            try {

                loadedScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectCalcCritChance, aurApp);

                critChance.refArgValue = ((IAuraCalcCritChance) loadedScript.item2).calcCritChance(aurEff, victim, critChance.refArgValue);

                loadedScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectAbsorbHandlers(AuraEffect aurEff, AuraApplication aurApp, DamageInfo dmgInfo, tangible.RefObject<Double> absorbAmount, tangible.RefObject<Boolean> defaultPrevented) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAbsorb, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAbsorb, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorb) auraScript.item2).handleAbsorb(aurEff, dmgInfo, absorbAmount.refArgValue);

                defaultPrevented.refArgValue = auraScript.Item1._IsDefaultActionPrevented();
                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectAfterAbsorbHandlers(AuraEffect aurEff, AuraApplication aurApp, DamageInfo dmgInfo, tangible.RefObject<Double> absorbAmount) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterAbsorb, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterAbsorb, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorb) auraScript.item2).handleAbsorb(aurEff, dmgInfo, absorbAmount.refArgValue);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectAbsorbHandlers(AuraEffect aurEff, AuraApplication aurApp, HealInfo healInfo, tangible.RefObject<Double> absorbAmount, tangible.RefObject<Boolean> defaultPrevented) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAbsorbHeal, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAbsorb, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorbHeal) auraScript.item2).handleAbsorb(aurEff, healInfo, absorbAmount.refArgValue);

                defaultPrevented.refArgValue = auraScript.Item1._IsDefaultActionPrevented();
                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectAfterAbsorbHandlers(AuraEffect aurEff, AuraApplication aurApp, HealInfo healInfo, tangible.RefObject<Double> absorbAmount) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterAbsorb, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterAbsorbHeal, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorbHeal) auraScript.item2).handleAbsorb(aurEff, healInfo, absorbAmount.refArgValue);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectManaShieldHandlers(AuraEffect aurEff, AuraApplication aurApp, DamageInfo dmgInfo, tangible.RefObject<Double> absorbAmount, tangible.RefObject<Boolean> defaultPrevented) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectManaShield, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectManaShield, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorb) auraScript.item2).handleAbsorb(aurEff, dmgInfo, absorbAmount.refArgValue);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectAfterManaShieldHandlers(AuraEffect aurEff, AuraApplication aurApp, DamageInfo dmgInfo, tangible.RefObject<Double> absorbAmount) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterManaShield, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterManaShield, aurApp);

                absorbAmount.refArgValue = ((IAuraEffectAbsorb) auraScript.item2).handleAbsorb(aurEff, dmgInfo, absorbAmount.refArgValue);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEffectSplitHandlers(AuraEffect aurEff, AuraApplication aurApp, DamageInfo dmgInfo, tangible.RefObject<Double> splitAmount) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectSplit, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectSplit, aurApp);

                splitAmount.refArgValue = ((IAuraSplitHandler) auraScript.item2).split(aurEff, dmgInfo, splitAmount.refArgValue);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final void callScriptEnterLeaveCombatHandlers(AuraApplication aurApp, boolean isNowInCombat) {
        for (IAuraEnterLeaveCombat loadedScript : this.<IAuraEnterLeaveCombat>GetAuraScripts()) {
            try {

                loadedScript._PrepareScriptCall(AuraScriptHookType.EnterLeaveCombat, aurApp);

                loadedScript.EnterLeaveCombat(isNowInCombat);

                loadedScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final boolean callScriptCheckProcHandlers(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var result = true;

        for (IAuraCheckProc auraScript : this.<IAuraCheckProc>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.CheckProc, aurApp);

                result &= auraScript.checkProc(eventInfo);

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return result;
    }

    public final boolean callScriptPrepareProcHandlers(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var prepare = true;

        for (IAuraPrepareProc auraScript : this.<IAuraPrepareProc>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.PrepareProc, aurApp);

                auraScript.DoPrepareProc(eventInfo);

                if (prepare) {
                    prepare = !auraScript._IsDefaultActionPrevented();
                }

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return prepare;
    }

    public final boolean callScriptProcHandlers(AuraApplication aurApp, ProcEventInfo eventInfo) {
        var handled = false;

        for (IAuraOnProc auraScript : this.<IAuraOnProc>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.Proc, aurApp);

                auraScript.OnProc(eventInfo);

                handled |= auraScript._IsDefaultActionPrevented();
                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return handled;
    }

    public final void callScriptAfterProcHandlers(AuraApplication aurApp, ProcEventInfo eventInfo) {
        for (IAuraAfterProc auraScript : this.<IAuraAfterProc>GetAuraScripts()) {
            try {

                auraScript._PrepareScriptCall(AuraScriptHookType.AfterProc, aurApp);

                auraScript.AfterProc(eventInfo);

                auraScript._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public final boolean callScriptCheckEffectProcHandlers(AuraEffect aurEff, AuraApplication aurApp, ProcEventInfo eventInfo) {
        var result = true;

        for (var auraScript : getEffectScripts(AuraScriptHookType.CheckEffectProc, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.CheckEffectProc, aurApp);

                result &= ((IAuraCheckEffectProc) auraScript.item2).checkProc(aurEff, eventInfo);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return result;
    }

    public final boolean callScriptEffectProcHandlers(AuraEffect aurEff, AuraApplication aurApp, ProcEventInfo eventInfo) {
        var preventDefault = false;

        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectProc, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectProc, aurApp);

                ((IAuraEffectProcHandler) auraScript.item2).handleProc(aurEff, eventInfo);

                if (!preventDefault) {
                    preventDefault = auraScript.Item1._IsDefaultActionPrevented();
                }

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }

        return preventDefault;
    }

    public final void callScriptAfterEffectProcHandlers(AuraEffect aurEff, AuraApplication aurApp, ProcEventInfo eventInfo) {
        for (var auraScript : getEffectScripts(AuraScriptHookType.EffectAfterProc, aurEff.getEffIndex())) {
            try {

                auraScript.Item1._PrepareScriptCall(AuraScriptHookType.EffectAfterProc, aurApp);

                ((IAuraEffectProcHandler) auraScript.item2).handleProc(aurEff, eventInfo);

                auraScript.Item1._FinishScriptCall();
            } catch (RuntimeException ex) {
                Log.outException(ex, "");
            }
        }
    }

    public String getDebugInfo() {
        return String.format("Id: %1$s Name: '%2$s' Caster: %3$s\nOwner: %4$s", getId(), getSpellInfo().getSpellName().get(global.getWorldMgr().getDefaultDbcLocale()), getCasterGuid(), (getOwner() != null ? getOwner().getDebugInfo() : "NULL"));
    }


    ///#endregion
}
