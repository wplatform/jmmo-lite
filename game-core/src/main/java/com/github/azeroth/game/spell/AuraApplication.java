package com.github.azeroth.game.spell;


import com.github.azeroth.common.Assert;
import com.github.azeroth.common.EnumFlag;
import com.github.azeroth.common.Logs;
import com.github.azeroth.defines.SpellAttr11;
import com.github.azeroth.defines.SpellAttr5;
import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packet.spell.AuraDataInfo;
import com.github.azeroth.game.networking.packet.spell.AuraInfo;
import com.github.azeroth.game.networking.packet.spell.AuraUpdate;
import lombok.Getter;

import java.util.Objects;

import static com.github.azeroth.game.spell.auras.SpellAuraDefine.MAX_AURAS;

@Getter
public class AuraApplication implements Comparable<AuraApplication> {
    private final Unit target;
    private final Aura base;
    private short slot; // Aura slot on unit
    private final int effectMask;
    private EnumFlag<AuraFlags> flags; // Aura info flag
    private int effectsToApply = 0; // Used only at spell hit to determine which effect should be applied
    private boolean needClientUpdate;
    private AuraRemoveMode removeMode;

    public AuraApplication(Unit target, Unit caster, Aura aura, int effMask) {
        Objects.requireNonNull(target);
        Objects.requireNonNull(aura);
        this.target = target;
        this.base = aura;
        this.slot = MAX_AURAS;
        this.effectMask = 0;
        flags = EnumFlag.of(AuraFlags.NONE);
        removeMode = AuraRemoveMode.NONE;
        effectsToApply = effMask;
        needClientUpdate = true;

        // Try find slot for aura
        byte slot = 0;
        // Lookup for auras already applied from spell
        for (var visibleAura : target.getVisibleAuras()) {
            if (slot < visibleAura.slot) {
                break;
            }
            ++slot;
        }

        // Register Visible Aura
        if (slot < MAX_AURAS) {
            this.slot = slot;
            target.setVisibleAura(this);
            needClientUpdate = true;
            Logs.SPELLS.debug("Aura: {} Effect: {} put to unit visible auras slot: {}", getBase().getId(), getEffectMask(), slot);

        } else {
            Logs.SPELLS.error("Aura: {} Effect: {} could not find empty unit visible slot", getBase().getId(), getEffectMask());
        }


        _InitFlags(caster, effMask);
    }


    public final void _Remove() {
        // update for out of range group members
        if (getSlot() < MAX_AURAS) {
            getTarget().removeVisibleAura(this);
            clientUpdate(true);
        }
    }

    public final void _HandleEffect(SpellEffIndex effIndex, boolean apply) {
        var aurEff = getBase().getEffect(effIndex);

        if (aurEff == null) {
            Logs.SPELLS.error("Aura {} has no effect at effectIndex {} but _HandleEffect was called", getBase().getSpellInfo().getId(), effIndex);

            return;
        }

        Assert.isTrue(hasEffect(effIndex) == !apply, "Aura {} has effect at effectIndex {} but _HandleEffect with {} was called", getBase().getSpellInfo().getId(), effIndex, apply);

        Assert.isTrue(((1 << effIndex.ordinal()) & effectsToApply) != 0);

        Logs.SPELLS.debug("AuraApplication._HandleEffect: {}, apply: {}, amount: {}", aurEff.getAuraType(), apply, aurEff.getAmount());

        if (apply) {
            effectMask.add(effIndex);
            aurEff.handleEffect(this, AuraEffectHandleModes.Real, true);
        } else {
            effectMask.remove((Integer) effIndex);
            aurEff.handleEffect(this, AuraEffectHandleModes.Real, false);
        }

        setNeedClientUpdate();
    }

    public final void updateApplyEffectMask(int newEffMask, boolean canHandleNewEffects) {
        if (effectsToApply == newEffMask)
            return;

        var toAdd = newEffMask.ToHashSet();
        var toRemove = effectsToApply.ToHashSet();

        toAdd.SymmetricExceptWith(effectsToApply);
        toRemove.SymmetricExceptWith(newEffMask);

        toAdd.ExceptWith(effectsToApply);
        toRemove.ExceptWith(newEffMask);

        // quick check, removes application completely
        if (toAdd.SetEquals(toRemove) && toAdd.count == 0) {
            target._UnapplyAura(this, AuraRemoveMode.Default);

            return;
        }

        // update real effects only if they were applied already
        effectsToApply = newEffMask;

        for (var eff : getBase().getAuraEffects().entrySet()) {
            if (hasEffect(eff.getKey()) && toRemove.contains(eff.getKey())) {
                _HandleEffect(eff.getKey(), false);
            }

            if (canHandleNewEffects) {
                if (toAdd.contains(eff.getKey())) {
                    _HandleEffect(eff.getKey(), true);
                }
            }
        }
    }

    public final void setNeedClientUpdate() {
        if (needClientUpdate || getRemoveMode() != AuraRemoveMode.NONE) {
            return;
        }

        needClientUpdate = true;
        target.setVisibleAuraUpdate(this);
    }

    public final void buildUpdatePacket(AuraInfo auraInfo, boolean remove) {
        auraInfo.slot = getSlot();

        if (remove) {
            return;
        }

        auraInfo.auraData = new AuraDataInfo();

        var aura = getBase();

        var auraData = auraInfo.auraData;
        auraData.castID = aura.getCastId();
        auraData.spellID = aura.getId();
        auraData.visual = aura.getSpellVisual();
        auraData.flags = (short) flags.getFlag();

        if (aura.getAuraObjType() != AuraObjectType.DynObj && aura.getMaxDuration() > 0 && !aura.getSpellInfo().hasAttribute(SpellAttr5.DO_NOT_DISPLAY_DURATION)) {
            auraData.flags = (short) (auraData.flags | AuraFlags.DURATION.getValue());
        }

        auraData.activeFlags = getEffectMask();

        if (!aura.getSpellInfo().hasAttribute(SpellAttr11.SCALES_WITH_ITEM_LEVEL)) {
            auraData.castLevel = aura.getCasterLevel();
        } else {
            auraData.castLevel = (short) aura.getCastItemLevel();
        }

        // send stack amount for aura which could be stacked (never 0 - causes incorrect display) or charges
        // stack amount has priority over charges (checked on retail with spell 50262)
        auraData.applications = aura.isUsingStacks() ? aura.getStackAmount() : aura.getCharges();

        if (!aura.getCasterGuid().isUnit()) {
            auraData.castUnit = ObjectGuid.EMPTY; // optional data is filled in, but cast unit contains empty guid in packet
        } else if ((auraData.flags & AuraFlags.NO_CASTER.getValue()) == 0) {
            auraData.castUnit = aura.getCasterGuid();
        }

        if ((auraData.flags & AuraFlags.DURATION.getValue()) != 0) {
            auraData.duration = aura.getMaxDuration();
            auraData.remaining = aura.getDuration();
        }

        if ((auraData.flags & AuraFlags.SCALABLE.getValue()) != 0) {
            auraData.points.reserve(aura.getAuraEffectCount());
            var hasEstimatedAmounts = false;
            for (var effect : aura.getAuraEffects()) {
                if (hasEffect(effect.getEffIndex()))       // Not all of aura's effects have to be applied on every target
                {
                    Trinity::Containers::EnsureWritableVectorIndex(auraData.Points, effect->GetEffIndex()) = float(effect->GetAmount());
                    if (effect->GetEstimatedAmount())
                        hasEstimatedAmounts = true;
                }
            }
            if (hasEstimatedAmounts)
            {
                // When sending EstimatedPoints all effects (at least up to the last one that uses GetEstimatedAmount) must have proper value in packet
                auraData.EstimatedPoints.resize(auraData.Points.size());
                for (AuraEffect const* effect : GetBase()->GetAuraEffects())
                if (HasEffect(effect->GetEffIndex()))       // Not all of aura's effects have to be applied on every target
                    auraData.EstimatedPoints[effect->GetEffIndex()] = effect->GetEstimatedAmount().value_or(effect->GetAmount());
            }
        }
    }


    public final void clientUpdate() {
        clientUpdate(false);
    }

    public final void clientUpdate(boolean remove) {
        needClientUpdate = false;

        AuraUpdate update = new AuraUpdate();
        update.updateAll = false;
        update.unitGUID = target.getGUID();

        AuraInfo auraInfo = new AuraInfo();
        tangible.RefObject<AuraInfo> tempRef_auraInfo = new tangible.RefObject<AuraInfo>(auraInfo);
        buildUpdatePacket(tempRef_auraInfo, remove);
        auraInfo = tempRef_auraInfo.refArgValue;
        update.auras.add(auraInfo);

        target.sendMessageToSet(update, true);
    }

    public final String getDebugInfo() {
        return String.format("Base: %1$s\nTarget: %2$s", (getBase() != null ? getBase().getDebugInfo() : "NULL"), (getTarget() != null ? getTarget().getDebugInfo() : "NULL"));
    }

    public final boolean hasEffect(SpellEffIndex effect) {
        return (effectMask & (1 << effect.ordinal())) != 0;
    }

    private void _InitFlags(Unit caster, int effMask) {
        // mark as selfcasted if needed
        flags = flags.getValue() | (Objects.equals(getBase().getCasterGuid(), getTarget().getGUID())).getValue() ? AuraFlags.NoCaster : AuraFlags.NONE;

        // aura is casted by self or an enemy
        // one negative effect and we know aura is negative
        if (isSelfcasted() || caster == null || !caster.isFriendlyTo(getTarget())) {
            var negativeFound = false;

            for (var spellEffectInfo : getBase().getSpellInfo().getEffects()) {
                if (effMask.contains(spellEffectInfo.effectIndex) && !getBase().getSpellInfo().isPositiveEffect(spellEffectInfo.effectIndex)) {
                    negativeFound = true;

                    break;
                }
            }

            flags = flags.getValue() | negativeFound.getValue() ? AuraFlags.Negative : AuraFlags.positive;
        }
        // aura is casted by friend
        // one positive effect and we know aura is positive
        else {
            var positiveFound = false;

            for (var spellEffectInfo : getBase().getSpellInfo().getEffects()) {
                if (effMask.contains(spellEffectInfo.effectIndex) && getBase().getSpellInfo().isPositiveEffect(spellEffectInfo.effectIndex)) {
                    positiveFound = true;

                    break;
                }
            }

            flags = flags.getValue() | positiveFound.getValue() ? AuraFlags.Positive : AuraFlags.NEGATIVE;
        }


//		bool effectNeedsAmount(KeyValuePair<int, AuraEffect> effect)
//			{
//				return EffectsToApply.contains(effect.value.effIndex) && aura.effectTypeNeedsSendingAmount(effect.value.auraType);
//			}

        if (getBase().getSpellInfo().hasAttribute(SpellAttr8.AuraSendAmount) || getBase().getAuraEffects().Any(effectNeedsAmount)) {
            flags = AuraFlags.forValue(flags.getValue() | AuraFlags.SCALABLE.getValue());
        }
    }

    @Override
    public int compareTo(AuraApplication o) {
        return Integer.compare(slot, o.slot);
    }
}
