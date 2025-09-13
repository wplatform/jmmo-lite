package com.github.azeroth.game.combat;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;

public class ThreatReference implements Comparable<ThreatReference> {
    private final Creature owner;
    private final Unit victim;
    public ThreatManager mgr;
    public onlineState online = OnlineState.values()[0];
    public int tempModifier; // Temporary effects (auras with SPELL_AURA_MOD_TOTAL_THREAT) - set from victim's threatmanager in ThreatManager::UpdateMyTempModifiers
    private double baseAmount;
    private TauntState taunted = TauntState.values()[0];

    public ThreatReference(ThreatManager mgr, Unit victim) {
        owner = mgr._owner instanceof Creature ? (CREATURE) mgr._owner : null;
        mgr = mgr;
        victim = victim;
        online = OnlineState.Offline;
    }

    public static boolean flagsAllowFighting(Unit a, Unit b) {
        if (a.isCreature() && a.toCreature().isTrigger()) {
            return false;
        }

        if (a.hasUnitFlag(UnitFlag.PlayerControlled)) {
            return !b.hasUnitFlag(UnitFlag.ImmuneToPc);
        } else {
            return !b.hasUnitFlag(UnitFlag.ImmuneToNpc);
        }
    }

    public final boolean getShouldBeOffline() {
        if (!owner.canSeeOrDetect(victim)) {
            return true;
        }

        if (!owner._IsTargetAcceptable(victim) || !owner.canCreatureAttack(victim)) {
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

        if (victim.hasAuraType(AuraType.ModConfuse)) {
            return true;
        }

        if (victim.hasBreakableByDamageAuraType(AuraType.ModStun)) {
            return true;
        }

        return false;
    }

    public final Creature getOwner() {
        return owner;
    }

    public final Unit getVictim() {
        return victim;
    }

    public final double getThreat() {
        return Math.max(baseAmount + tempModifier, 0.0f);
    }

    public final void setThreat(float amount) {
        baseAmount = amount;
        listNotifyChanged();
    }

    public final OnlineState getOnlineState() {
        return online;
    }

    public final boolean isOnline() {
        return online.getValue() >= OnlineState.online.getValue();
    }

    public final boolean isAvailable() {
        return online.getValue() > OnlineState.Offline.getValue();
    }

    public final boolean isSuppressed() {
        return online == OnlineState.Suppressed;
    }

    public final boolean isOffline() {
        return online.getValue() <= OnlineState.Offline.getValue();
    }

    public final TauntState getTauntState() {
        return isTaunting() ? TauntState.Taunt : taunted;
    }

    public final boolean isTaunting() {
        return taunted.getValue() >= TauntState.Taunt.getValue();
    }

    public final boolean isDetaunted() {
        return taunted == TauntState.Detaunt;
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

        _baseAmount *= factor;
        listNotifyChanged();
        mgr.needClientUpdate = true;
    }

    public final void updateOffline() {
        var shouldBeOffline = getShouldBeOffline();

        if (shouldBeOffline == isOffline()) {
            return;
        }

        if (shouldBeOffline) {
            online = OnlineState.Offline;
            listNotifyChanged();
            mgr.sendRemoveToClients(victim);
        } else {
            online = getShouldBeSuppressed() ? OnlineState.Suppressed : OnlineState.online;
            listNotifyChanged();
            mgr.registerForAIUpdate(this);
        }
    }

    public final void updateTauntState() {
        updateTauntState(TauntState.NONE);
    }

    public final void updateTauntState(TauntState state) {
        // Check for SPELL_AURA_MOD_DETAUNT (applied from owner to victim)
        if (state.getValue() < TauntState.Taunt.getValue() && victim.hasAuraTypeWithCaster(AuraType.ModDetaunt, owner.getGUID())) {
            state = TauntState.Detaunt;
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
