package com.github.azeroth.game.combat;


import com.github.azeroth.game.domain.unit.UnitFlag;
import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import lombok.Data;

@Data
public class ThreatReference implements Comparable<ThreatReference> {
    private final Creature owner;
    private final Unit victim;
    private ThreatManager mgr;
    private OnlineState onlineState;
    private int tempModifier; // Temporary effects (auras with SPELL_AURA_MOD_TOTAL_THREAT) - set from victim's threatmanager in ThreatManager::UpdateMyTempModifiers
    private double baseAmount;
    private TauntState taunted;

    public ThreatReference(ThreatManager mgr, Unit victim) {
        this.owner = mgr.owner.toCreature();
        this.victim = victim;
        this.mgr = mgr;
        this.onlineState = OnlineState.OFFLINE;
    }

    public static boolean flagsAllowFighting(Unit a, Unit b) {
        if (a.isCreature() && a.toCreature().isTrigger()) {
            return false;
        }

        if (a.hasUnitFlag(UnitFlag.PLAYER_CONTROLLED)) {
            return !b.hasUnitFlag(UnitFlag.IMMUNE_TO_PC);
        } else {
            return !b.hasUnitFlag(UnitFlag.IMMUNE_TO_NPC);
        }
    }

    public final boolean shouldBeOffline() {
        if (!owner.canSeeOrDetect(victim)) {
            return true;
        }

        if (!owner.isTargetAcceptable(victim) || !owner.canCreatureAttack(victim)) {
            return true;
        }

        if (!flagsAllowFighting(owner, victim) || !flagsAllowFighting(victim, owner)) {
            return true;
        }

        return false;
    }

    public final boolean getShouldBeSuppressed() {
        if (isTaunting()) // a taunting victim can never be suppressed
        {
            return false;
        }

        if (victim.isImmunedToDamage(owner.getMeleeDamageSchoolMask())) {
            return true;
        }

        if (victim.hasAuraType(AuraType.MOD_CONFUSE)) {
            return true;
        }

        if (victim.hasBreakableByDamageAuraType(AuraType.MOD_STUN)) {
            return true;
        }

        return false;
    }



    public final double getThreat() {
        return Math.max(baseAmount + tempModifier, 0.0f);
    }

    public final void setThreat(float amount) {
        baseAmount = amount;
        listNotifyChanged();
    }

    public final OnlineState getOnlineState() {
        return onlineState;
    }

    public final boolean isOnline() {
        return onlineState.getValue() >= OnlineState.ONLINE.getValue();
    }

    public final boolean isAvailable() {
        return onlineState.getValue() > OnlineState.OFFLINE.getValue();
    }

    public final boolean isSuppressed() {
        return onlineState == OnlineState.SUPPRESSED;
    }

    public final boolean isOffline() {
        return onlineState.getValue() <= OnlineState.OFFLINE.getValue();
    }

    public final TauntState getTauntState() {
        return isTaunting() ? TauntState.TAUNT : taunted;
    }

    public final boolean isTaunting() {
        return taunted.getValue() >= TauntState.TAUNT.getValue();
    }

    public final boolean isDetaunted() {
        return taunted == TauntState.DE_TAUNT;
    }

    public final int compareTo(ThreatReference other) {
        return ThreatManager.compareReferencesLT(this, other, 1.0f) ? 1 : -1;
    }

    public final void addThreat(double amount) {
        if (amount == 0.0f) {
            return;
        }

        baseAmount = Math.max(baseAmount + amount, 0.0f);
        listNotifyChanged();
        mgr.needClientUpdate = true;
    }

    public final void scaleThreat(double factor) {
        if (factor == 1.0f) {
            return;
        }
        baseAmount *= factor;
        if (factor > 1.0f)
            heapNotifyIncreased();
        else
            heapNotifyDecreased();
        mgr.needClientUpdate = true;
    }



    void heapNotifyIncreased()
    {
        mgr.sortedThreatList.increase(static_cast<ThreatReferenceImpl*>(this)->_handle);
    }

    void heapNotifyDecreased()
    {
        _mgr._sortedThreatList->decrease(static_cast<ThreatReferenceImpl*>(this)->_handle);
    }

    public final void updateOffline() {
        var shouldBeOffline = shouldBeOffline();

        if (shouldBeOffline == isOffline()) {
            return;
        }

        if (shouldBeOffline) {
            onlineState = OnlineState.OFFLINE;
            listNotifyChanged();
            mgr.sendRemoveToClients(victim);
        } else {
            onlineState = getShouldBeSuppressed() ? OnlineState.SUPPRESSED : OnlineState.ONLINE;
            listNotifyChanged();
            mgr.registerForAIUpdate(this);
        }
    }

    public final void updateTauntState() {
        updateTauntState(TauntState.NONE);
    }

    public final void updateTauntState(TauntState state) {
        // Check for SPELL_AURA_MOD_DETAUNT (applied from owner to victim)
        if (state.getValue() < TauntState.TAUNT.getValue() && victim.hasAuraTypeWithCaster(AuraType.ModDetaunt, owner.getGUID())) {
            state = TauntState.DE_TAUNT;
        }

        if (state == taunted) {
            return;
        }

        tangible.RefObject<T> tempRef_state = new tangible.RefObject<T>(state);
        tangible.RefObject<T> tempRef__taunted = new tangible.RefObject<T>(taunted);
        Extensions.Swap(tempRef_state, tempRef__taunted);
        taunted = tempRef__taunted.refArgValue;
        state = tempRef_state.refArgValue;

        listNotifyChanged();
        mgr.needClientUpdate = true;
    }

    public final void clearThreat() {
        mgr.clearThreat(this);
    }

    public final void unregisterAndFree() {
        owner.getThreatManager().purgeThreatListRef(victim.getGUID());
        victim.getThreatManager().purgeThreatenedByMeRef(owner.getGUID());
    }

    public final void modifyThreatByPercent(int percent) {
        if (percent != 0) {
            scaleThreat(0.01f * (100f + percent));
        }
    }

    public final void listNotifyChanged() {
        mgr.listNotifyChanged();
    }
}
