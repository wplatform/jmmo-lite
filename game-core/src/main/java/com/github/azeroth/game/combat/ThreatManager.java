package com.github.azeroth.game.combat;


import com.github.azeroth.game.entity.creature.Creature;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packet.HighestThreatUpdate;
import com.github.azeroth.game.networking.packet.ThreatClear;
import com.github.azeroth.game.networking.packet.ThreatRemove;
import com.github.azeroth.game.networking.packet.ThreatUpdate;
import com.github.azeroth.game.spell.SpellInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.collections;


public class ThreatManager {
    public static int THREAT_UPDATE_INTERVAL = 1000;
    private final ArrayList<ThreatReference> sortedThreatList = new ArrayList<>();
    private final HashMap<ObjectGuid, ThreatReference> myThreatListEntries = new HashMap<ObjectGuid, ThreatReference>();
    private final ArrayList<ThreatReference> needsAIUpdate = new ArrayList<>();
    private final HashMap<ObjectGuid, ThreatReference> threatenedByMe = new HashMap<ObjectGuid, ThreatReference>(); // these refs are entries for myself on other units' threat lists
    public Unit owner;
    public boolean needClientUpdate;
    public double[] singleSchoolModifiers = new double[SpellSchool.max.getValue()]; // most spells are single school - we pre-calculate these and store them
    public volatile HashMap<spellSchoolMask, Double> multiSchoolModifiers = new HashMap<spellSchoolMask, Double>(); // these are calculated on demand
    public ArrayList<Tuple<ObjectGuid, Integer>> redirectInfo = new ArrayList<Tuple<ObjectGuid, Integer>>(); // current redirection targets and percentages (updated from registry in ThreatManager::UpdateRedirectInfo)
    public HashMap<Integer, HashMap<ObjectGuid, Integer>> redirectRegistry = new HashMap<Integer, HashMap<ObjectGuid, Integer>>(); // spellid . (victim . pct); all redirection effects on us (removal individually managed by spell scripts because blizzard is dumb)
    private boolean ownerCanHaveThreatList;
    private int updateTimer;
    private ThreatReference currentVictimRef;
    private ThreatReference fixateRef;

    public ThreatManager(Unit owner) {
        owner = owner;
        updateTimer = THREAT_UPDATE_INTERVAL;

        for (var i = 0; i < SpellSchool.max.getValue(); ++i) {
            _singleSchoolModifiers[i] = 1.0f;
        }
    }

    public static boolean canHaveThreatListForUnit(Unit who) {
        var cWho = who.toCreature();

        // only creatures can have threat list
        if (!cWho) {
            return false;
        }

        // pets, totems and triggers cannot have threat list
        if (cWho.isPet() || cWho.isTotem() || cWho.isTrigger()) {
            return false;
        }

        // summons cannot have a threat list if they were summoned by a player
        if (cWho.hasUnitTypeMask(UnitTypeMask.minion.getValue() | UnitTypeMask.Guardian.getValue())) {
            var tWho = cWho.toTempSummon();

            if (tWho != null) {
                if (tWho.getSummonerGUID().isPlayer()) {
                    return false;
                }
            }
        }

        return true;
    }

    // can our owner have a threat list?
    // identical to ThreatManager::CanHaveThreatList(GetOwner())

    // returns true if a is LOWER on the threat list than b
    public static boolean compareReferencesLT(ThreatReference a, ThreatReference b, float aWeight) {
        if (a.getOnlineState() != b.getOnlineState()) // online state precedence (ONLINE > SUPPRESSED > OFFLINE)
        {
            return a.getOnlineState().getValue() < b.getOnlineState().getValue();
        }

        if (a.getTauntState() != b.getTauntState()) // taunt state precedence (TAUNT > NONE > DETAUNT)
        {
            return a.getTauntState().getValue() < b.getTauntState().getValue();
        }

        return (a.getThreat() * aWeight < b.getThreat());
    }

    public static double calculateModifiedThreat(double threat, Unit victim, SpellInfo spell) {
        // modifiers by spell
        if (spell != null) {
            var threatEntry = global.getSpellMgr().getSpellThreatEntry(spell.getId());

            if (threatEntry != null) {
                if (threatEntry.pctMod != 1.0f) // flat/AP modifiers handled in Spell::HandleThreatSpells
                {
                    threat *= threatEntry.pctMod;
                }
            }

            var modOwner = victim.getSpellModOwner();

            if (modOwner != null) {
                tangible.RefObject<Double> tempRef_threat = new tangible.RefObject<Double>(threat);
                modOwner.applySpellMod(spell, SpellModOp.Hate, tempRef_threat);
                threat = tempRef_threat.refArgValue;
            }
        }

        // modifiers by effect school
        var victimMgr = victim.getThreatManager();
        var mask = spell != null ? spell.getSchoolMask() : spellSchoolMask.NORMAL;

        switch (mask) {
            case Normal:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.NORMAL.getValue()];

                break;
            case Holy:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Holy.getValue()];

                break;
            case Fire:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Fire.getValue()];

                break;
            case Nature:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Nature.getValue()];

                break;
            case Frost:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Frost.getValue()];

                break;
            case Shadow:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Shadow.getValue()];

                break;
            case Arcane:
                threat *= victimMgr._singleSchoolModifiers[SpellSchool.Arcane.getValue()];

                break;
            default: {
                TValue value;
                if (victimMgr.multiSchoolModifiers.containsKey(mask) && (value = victimMgr.multiSchoolModifiers.get(mask)) == value) {
                    threat *= value;

                    break;
                }

                var mod = victim.getTotalAuraMultiplierByMiscMask(AuraType.ModThreat, (int) mask.getValue());
                victimMgr.multiSchoolModifiers.put(mask, mod);
                threat *= mod;

                break;
            }
        }

        return threat;
    }

    // fastest of the three threat list getters - gets the threat list in "arbitrary" order

    public final Unit getCurrentVictim() {
        if (currentVictimRef == null || currentVictimRef.getShouldBeOffline()) {
            updateVictim();
        }

        return (currentVictimRef == null ? null : currentVictimRef.getVictim());
    }

    public final Unit getLastVictim() {
        if (currentVictimRef != null && !currentVictimRef.getShouldBeOffline()) {
            return currentVictimRef.getVictim();
        }

        return null;
    }

    // never nullptr

    public final boolean getCanHaveThreatList() {
        return ownerCanHaveThreatList;
    }

    public final int getThreatListSize() {
        return sortedThreatList.size();
    }

    public final ArrayList<ThreatReference> getSortedThreatList() {
        return sortedThreatList;
    }

    public final HashMap<ObjectGuid, ThreatReference> getThreatenedByMeList() {
        return threatenedByMe;
    }

    public final Unit getOwner() {
        return owner;
    }

    public final void initialize() {
        ownerCanHaveThreatList = canHaveThreatListForUnit(owner);
    }

    public final void update(int tdiff) {
        if (!getCanHaveThreatList() || isThreatListEmpty(true)) {
            return;
        }

        if (updateTimer <= tdiff) {
            updateVictim();
            updateTimer = THREAT_UPDATE_INTERVAL;
        } else {
            _updateTimer -= tdiff;
        }
    }

    public final Unit getAnyTarget() {
        for (var refe : sortedThreatList) {
            if (!refe.isOffline()) {
                return refe.getVictim();
            }
        }

        return null;
    }

    public final boolean isThreatListEmpty() {
        return isThreatListEmpty(false);
    }

    public final boolean isThreatListEmpty(boolean includeOffline) {
        if (includeOffline) {
            return sortedThreatList.isEmpty();
        }

        for (var refe : sortedThreatList) {
            if (refe.isAvailable()) {
                return false;
            }
        }

        return true;
    }

    public final boolean isThreatenedBy(ObjectGuid who) {
        return isThreatenedBy(who, false);
    }

    public final boolean isThreatenedBy(ObjectGuid who, boolean includeOffline) {
        var refe = myThreatListEntries.get(who);

        if (refe == null) {
            return false;
        }

        return (includeOffline || refe.IsAvailable);
    }

    public final boolean isThreatenedBy(Unit who) {
        return isThreatenedBy(who, false);
    }

    public final boolean isThreatenedBy(Unit who, boolean includeOffline) {
        return isThreatenedBy(who.getGUID(), includeOffline);
    }

    public final double getThreat(Unit who) {
        return getThreat(who, false);
    }

    public final double getThreat(Unit who, boolean includeOffline) {
        var refe = myThreatListEntries.get(who.getGUID());

        if (refe == null) {
            return 0.0f;
        }

        return (includeOffline || refe.IsAvailable) ? refe.Threat : 0.0f;
    }

    public final ArrayList<ThreatReference> getModifiableThreatList() {
        return new ArrayList<ThreatReference>(sortedThreatList);
    }

    public final boolean isThreateningAnyone() {
        return isThreateningAnyone(false);
    }

    public final boolean isThreateningAnyone(boolean includeOffline) {
        if (includeOffline) {
            return !threatenedByMe.isEmpty();
        }

        for (var pair : threatenedByMe.entrySet()) {
            if (pair.getValue().IsAvailable) {
                return true;
            }
        }

        return false;
    }

    public final boolean isThreateningTo(ObjectGuid who) {
        return isThreateningTo(who, false);
    }

    public final boolean isThreateningTo(ObjectGuid who, boolean includeOffline) {
        var refe = threatenedByMe.get(who);

        if (refe == null) {
            return false;
        }

        return (includeOffline || refe.IsAvailable);
    }

    public final boolean isThreateningTo(Unit who) {
        return isThreateningTo(who, false);
    }

    public final boolean isThreateningTo(Unit who, boolean includeOffline) {
        return isThreateningTo(who.getGUID(), includeOffline);
    }

    public final void evaluateSuppressed() {
        evaluateSuppressed(false);
    }

    public final void evaluateSuppressed(boolean canExpire) {
        for (var pair : threatenedByMe.entrySet()) {
            var shouldBeSuppressed = pair.getValue().ShouldBeSuppressed;

            if (pair.getValue().IsOnline && shouldBeSuppressed) {
                pair.getValue().online = OnlineState.Suppressed;
                pair.getValue().listNotifyChanged();
            } else if (canExpire && pair.getValue().IsSuppressed && !shouldBeSuppressed) {
                pair.getValue().online = OnlineState.online;
                pair.getValue().listNotifyChanged();
            }
        }
    }

    public final void addThreat(Unit target, double amount, SpellInfo spell, boolean ignoreModifiers) {
        addThreat(target, amount, spell, ignoreModifiers, false);
    }

    public final void addThreat(Unit target, double amount, SpellInfo spell) {
        addThreat(target, amount, spell, false, false);
    }

    public final void addThreat(Unit target, double amount) {
        addThreat(target, amount, null, false, false);
    }

    public final void addThreat(Unit target, double amount, SpellInfo spell, boolean ignoreModifiers, boolean ignoreRedirects) {
        // step 1: we can shortcut if the spell has one of the NO_THREAT attrs set - nothing will happen
        if (spell != null) {
            if (spell.hasAttribute(SpellAttr1.NoThreat)) {
                return;
            }

            if (!owner.isEngaged() && spell.hasAttribute(SpellAttr2.NoInitialThreat)) {
                return;
            }
        }

        // while riding a vehicle, all threat goes to the vehicle, not the pilot
        var vehicle = target.getVehicleBase();

        if (vehicle != null) {
            addThreat(vehicle, amount, spell, ignoreModifiers, ignoreRedirects);

            if (target.hasUnitTypeMask(UnitTypeMask.Accessory)) // accessories are fully treated as components of the parent and cannot have threat
            {
                return;
            }

            amount = 0.0f;
        }

        // If victim is personal spawn, redirect all aggro to summoner
        if (target.isPrivateObject() && (!getOwner().isPrivateObject() || !getOwner().checkPrivateObjectOwnerVisibility(target))) {
            var privateObjectOwner = global.getObjAccessor().GetUnit(getOwner(), target.getPrivateObjectOwner());

            if (privateObjectOwner != null) {
                addThreat(privateObjectOwner, amount, spell, ignoreModifiers, ignoreRedirects);
                amount = 0.0f;
            }
        }

        // if we cannot actually have a threat list, we instead just set combat state and avoid creating threat refs altogether
        if (!getCanHaveThreatList()) {
            var combatMgr = owner.getCombatManager();

            if (!combatMgr.setInCombatWith(target)) {
                return;
            }

            // traverse redirects and put them in combat, too
            for (var pair : target.getThreatManager().redirectInfo) {
                if (!combatMgr.isInCombatWith(pair.Item1)) {
                    var redirTarget = global.getObjAccessor().GetUnit(owner, pair.Item1);

                    if (redirTarget != null) {
                        combatMgr.setInCombatWith(redirTarget);
                    }
                }
            }

            return;
        }

        // apply threat modifiers to the amount
        if (!ignoreModifiers) {
            amount = calculateModifiedThreat(amount, target, spell);
        }

        // if we're increasing threat, send some/all of it to redirection targets instead if applicable
        if (!ignoreRedirects && amount > 0.0f) {
            var redirInfo = target.getThreatManager().redirectInfo;

            if (!redirInfo.isEmpty()) {
                var origAmount = amount;

                // intentional iteration by index - there's a nested AddThreat call further down that might cause AI calls which might modify redirect info through spells
                for (var i = 0; i < redirInfo.size(); ++i) {
                    var pair = redirInfo.get(i); // (victim,pct)
                    Unit redirTarget;

                    var refe = myThreatListEntries.get(pair.Item1); // try to look it up in our threat list first (faster)

                    if (refe != null) {
                        redirTarget = refe.victim;
                    } else {
                        redirTarget = global.getObjAccessor().GetUnit(owner, pair.Item1);
                    }

                    if (redirTarget) {
                        var amountRedirected = MathUtil.CalculatePct(origAmount, pair.item2);
                        addThreat(redirTarget, amountRedirected, spell, true, true);
                        amount -= amountRedirected;
                    }
                }
            }
        }

        // ensure we're in combat (threat implies combat!)
        if (!owner.getCombatManager().setInCombatWith(target)) // if this returns false, we're not actually in combat, and thus cannot have threat!
        {
            return; // typical causes: bad scripts trying to add threat to GMs, dead targets etc
        }

        // ok, now we actually apply threat
        // check if we already have an entry - if we do, just increase threat for that entry and we're done
        var targetRefe = myThreatListEntries.get(target.getGUID());

        if (targetRefe != null) {
            // SUPPRESSED threat states don't go back to ONLINE until threat is caused by them (retail behavior)
            if (targetRefe.OnlineState == OnlineState.Suppressed) {
                if (!targetRefe.ShouldBeSuppressed) {
                    targetRefe.online = OnlineState.online;
                    targetRefe.listNotifyChanged();
                }
            }

            if (targetRefe.IsOnline) {
                targetRefe.addThreat(amount);
            }

            return;
        }

        // ok, we're now in combat - create the threat list reference and push it to the respective managers
        ThreatReference newRefe = new ThreatReference(this, target);
        putThreatListRef(target.getGUID(), newRefe);
        target.getThreatManager().putThreatenedByMeRef(owner.getGUID(), newRefe);

        // afterwards, we evaluate whether this is an online reference (it might not be an acceptable target, but we need to add it to our threat list before we check!)
        newRefe.updateOffline();

        if (newRefe.isOnline()) // we only add the threat if the ref is currently available
        {
            newRefe.addThreat(amount);
        }

        if (currentVictimRef == null) {
            updateVictim();
        } else {
            processAIUpdates();
        }
    }

    public final void matchUnitThreatToHighestThreat(Unit target) {
        if (sortedThreatList.isEmpty()) {
            return;
        }

        var index = 0;

        var highest = sortedThreatList.get(index);

        if (!highest.isAvailable()) {
            return;
        }

        if (highest.isTaunting() && (++index) != sortedThreatList.size() - 1) // might need to skip this - max threat could be the preceding element (there is only one taunt element)
        {
            var a = sortedThreatList.get(index);

            if (a.isAvailable() && a.getThreat() > highest.getThreat()) {
                highest = a;
            }
        }

        addThreat(target, highest.getThreat() - getThreat(target, true), null, true, true);
    }

    public final void tauntUpdate() {
        var tauntEffects = owner.getAuraEffectsByType(AuraType.ModTaunt);
        var state = TauntState.Taunt;
        HashMap<ObjectGuid, TauntState> tauntStates = new HashMap<ObjectGuid, TauntState>();

        // Only the last taunt effect applied by something still on our threat list is considered
        for (var auraEffect : tauntEffects) {
            tauntStates.put(auraEffect.getCasterGuid(), state++);
        }

        for (var pair : myThreatListEntries.entrySet()) {
            TValue tauntState;
            if (tauntStates.containsKey(pair.getKey()) && (tauntState = tauntStates.get(pair.getKey())) == tauntState) {
                pair.getValue().updateTauntState(tauntState);
            } else {
                pair.getValue().updateTauntState();
            }
        }

        // taunt aura update also re-evaluates all suppressed states (retail behavior)
        evaluateSuppressed(true);
    }

    public final void resetAllThreat() {
        for (var pair : myThreatListEntries.entrySet()) {
            pair.getValue().scaleThreat(0.0f);
        }
    }

    public final void clearThreat(Unit target) {
        var refe = myThreatListEntries.get(target.getGUID());

        if (refe != null) {
            clearThreat(refe);
        }
    }

    public final void clearThreat(ThreatReference refe) {
        sendRemoveToClients(refe.getVictim());
        refe.unregisterAndFree();

        if (currentVictimRef == null) {
            updateVictim();
        }
    }

    public final void clearAllThreat() {
        if (!myThreatListEntries.isEmpty()) {
            sendClearAllThreatToClients();

            do {
                myThreatListEntries.FirstOrDefault().value.unregisterAndFree();
            } while (!myThreatListEntries.isEmpty());
        }
    }

    public final void fixateTarget(Unit target) {
        if (target) {
            var it = myThreatListEntries.get(target.getGUID());

            if (it != null) {
                fixateRef = it;

                return;
            }
        }

        fixateRef = null;
    }

    public final Unit getFixateTarget() {
        if (fixateRef != null) {
            return fixateRef.getVictim();
        } else {
            return null;
        }
    }

    public final void clearFixate() {
        fixateTarget(null);
    }

    public final void forwardThreatForAssistingMe(Unit assistant, double baseAmount, SpellInfo spell) {
        forwardThreatForAssistingMe(assistant, baseAmount, spell, false);
    }

    public final void forwardThreatForAssistingMe(Unit assistant, double baseAmount) {
        forwardThreatForAssistingMe(assistant, baseAmount, null, false);
    }

    public final void forwardThreatForAssistingMe(Unit assistant, double baseAmount, SpellInfo spell, boolean ignoreModifiers) {
        if (spell != null && (spell.hasAttribute(SpellAttr1.NoThreat) || spell.hasAttribute(SpellAttr4.NoHelpfulThreat))) // shortcut, none of the calls would do anything
        {
            return;
        }

        if (threatenedByMe.isEmpty()) {
            return;
        }

        ArrayList<Creature> canBeThreatened = new ArrayList<>();
        ArrayList<Creature> cannotBeThreatened = new ArrayList<>();

        for (var pair : threatenedByMe.entrySet()) {
            var owner = pair.getValue().owner;

            if (!owner.hasUnitState(UnitState.controlled)) {
                canBeThreatened.add(owner);
            } else {
                cannotBeThreatened.add(owner);
            }
        }

        if (!canBeThreatened.isEmpty()) // targets under CC cannot gain assist threat - split evenly among the rest
        {
            var perTarget = baseAmount / canBeThreatened.size();

            for (var threatened : canBeThreatened) {
                threatened.getThreatManager().addThreat(assistant, perTarget, spell, ignoreModifiers);
            }
        }

        for (var threatened : cannotBeThreatened) {
            threatened.getThreatManager().addThreat(assistant, 0.0f, spell, true);
        }
    }

    public final void removeMeFromThreatLists() {
        while (!threatenedByMe.isEmpty()) {
            var refe = threatenedByMe.FirstOrDefault().value;
            refe.mgr.clearThreat(owner);
        }
    }

    public final void updateMyTempModifiers() {
        double mod = 0;

        for (var eff : owner.getAuraEffectsByType(AuraType.ModTotalThreat)) {
            mod += eff.getAmount();
        }

        if (threatenedByMe.isEmpty()) {
            return;
        }

        for (var pair : threatenedByMe.entrySet()) {
            pair.getValue().tempModifier = (int) mod;
            pair.getValue().listNotifyChanged();
        }
    }

    public final void updateMySpellSchoolModifiers() {
        for (byte i = 0; i < SpellSchool.max.getValue(); ++i) {
            _singleSchoolModifiers[i] = owner.getTotalAuraMultiplierByMiscMask(AuraType.ModThreat, 1 << i);
        }

        multiSchoolModifiers.clear();
    }

    public final void registerRedirectThreat(int spellId, ObjectGuid victim, int pct) {
        if (!redirectRegistry.containsKey(spellId)) {
            redirectRegistry.put(spellId, new HashMap<ObjectGuid, Integer>());
        }

        redirectRegistry.get(spellId).put(victim, pct);
        updateRedirectInfo();
    }

    public final void unregisterRedirectThreat(int spellId) {
        if (redirectRegistry.remove(spellId)) {
            updateRedirectInfo();
        }
    }

    public final void sendRemoveToClients(Unit victim) {
        ThreatRemove threatRemove = new ThreatRemove();
        threatRemove.unitGUID = owner.getGUID();
        threatRemove.aboutGUID = victim.getGUID();
        owner.sendMessageToSet(threatRemove, false);
    }

    public final void purgeThreatListRef(ObjectGuid guid) {
        var refe = myThreatListEntries.get(guid);

        if (refe == null) {
            return;
        }

        myThreatListEntries.remove(guid);
        sortedThreatList.remove(refe);

        if (fixateRef == refe) {
            fixateRef = null;
        }

        if (currentVictimRef == refe) {
            currentVictimRef = null;
        }
    }

    public final void purgeThreatenedByMeRef(ObjectGuid guid) {
        threatenedByMe.remove(guid);
    }

    public final void listNotifyChanged() {
        collections.sort(sortedThreatList);
    }

    // Modify target's threat by +percent%
    public final void modifyThreatByPercent(Unit target, double percent) {
        if (percent != 0) {
            scaleThreat(target, 0.01f * (100f + percent));
        }
    }

    // Resets the specified unit's threat to zero
    public final void resetThreat(Unit target) {
        scaleThreat(target, 0.0f);
    }

    public final void registerForAIUpdate(ThreatReference refe) {
        needsAIUpdate.add(refe);
    }

    private void scaleThreat(Unit target, double factor) {
        var refe = myThreatListEntries.get(target.getGUID());

        if (refe != null) {
            refe.scaleThreat(Math.max(factor, 0.0f));
        }
    }

    private void updateVictim() {
        var newVictim = reselectVictim();
        var newHighest = newVictim != null && (newVictim != currentVictimRef);

        currentVictimRef = newVictim;

        if (newHighest || needClientUpdate) {
            sendThreatListToClients(newHighest);
            needClientUpdate = false;
        }

        processAIUpdates();
    }

    private ThreatReference reselectVictim() {
        if (sortedThreatList.isEmpty()) {
            return null;
        }

        for (var pair : myThreatListEntries.entrySet()) {
            pair.getValue().updateOffline(); // AI notifies are processed in ::UpdateVictim caller
        }

        // fixated target is always preferred
        if (fixateRef != null && fixateRef.isAvailable()) {
            return fixateRef;
        }

        var oldVictimRef = currentVictimRef;

        if (oldVictimRef != null && oldVictimRef.isOffline()) {
            oldVictimRef = null;
        }

        // in 99% of cases - we won't need to actually look at anything beyond the first element
        var highest = sortedThreatList.get(0);

        // if the highest reference is offline, the entire list is offline, and we indicate this
        if (!highest.IsAvailable) {
            return null;
        }

        // if we have no old victim, or old victim is still highest, then highest is our target and we're done
        if (oldVictimRef == null || highest == oldVictimRef) {
            return highest;
        }

        // if highest threat doesn't break 110% of old victim, nothing below it is going to do so either; new victim = old victim and done
        if (!compareReferencesLT(oldVictimRef, highest, 1.1f)) {
            return oldVictimRef;
        }

        // if highest threat breaks 130%, it's our new target regardless of range (and we're done)
        if (compareReferencesLT(oldVictimRef, highest, 1.3f)) {
            return highest;
        }

        // if it doesn't break 130%, we need to check if it's melee - if yes, it breaks 110% (we checked earlier) and is our new target
        if (owner.isWithinMeleeRange(highest.victim)) {
            return highest;
        }

        // If we get here, highest threat is ranged, but below 130% of current - there might be a melee that breaks 110% below us somewhere, so now we need to actually look at the next highest element
        // luckily, this is a heap, so getting the next highest element is O(log n), and we're just gonna do that repeatedly until we've seen enough targets (or find a target)
        for (var next : sortedThreatList) {
            // if we've found current victim, we're done (nothing above is higher, and nothing below can be higher)
            if (next == oldVictimRef) {
                return next;
            }

            // if next isn't above 110% threat, then nothing below it can be either - we're done, old victim stays
            if (!compareReferencesLT(oldVictimRef, next, 1.1f)) {
                return oldVictimRef;
            }

            // if next is melee, he's above 110% and our new victim
            if (owner.isWithinMeleeRange(next.getVictim())) {
                return next;
            }

            // otherwise the next highest target may still be a melee above 110% and we need to look further
        }

        return null;
    }

    private void processAIUpdates() {
        var ai = owner.toCreature().getAI();
        ArrayList<ThreatReference> v = new ArrayList<ThreatReference>(needsAIUpdate); // _needClientUpdate is now empty in case this triggers a recursive call

        if (ai == null) {
            return;
        }

        for (var refe : v) {
            ai.justStartedThreateningMe(refe.getVictim());
        }
    }

    private void unregisterRedirectThreat(int spellId, ObjectGuid victim) {
        var victimMap = redirectRegistry.get(spellId);

        if (victimMap == null) {
            return;
        }

        if (victimMap.remove(victim)) {
            updateRedirectInfo();
        }
    }

    private void sendClearAllThreatToClients() {
        ThreatClear threatClear = new ThreatClear();
        threatClear.unitGUID = owner.getGUID();
        owner.sendMessageToSet(threatClear, false);
    }

    private void sendThreatListToClients(boolean newHighest) {

//		void fillSharedPacketDataAndSend(dynamic packet)
//			{
//				packet.unitGUID = owner.GUID;
//
//				foreach (var refe in sortedThreatList)
//				{
//					if (!refe.IsAvailable)
//						continue;
//
//					ThreatInfo threatInfo = new();
//					threatInfo.unitGUID = refe.victim.GUID;
//					threatInfo.threat = (long)(refe.Threat * 100);
//					packet.threatList.add(threatInfo);
//				}
//
//				owner.sendMessageToSet(packet, false);
//			}

        if (newHighest) {
            HighestThreatUpdate highestThreatUpdate = new HighestThreatUpdate();
            highestThreatUpdate.highestThreatGUID = currentVictimRef.getVictim().getGUID();
            fillSharedPacketDataAndSend(highestThreatUpdate);
        } else {
            ThreatUpdate threatUpdate = new ThreatUpdate();
            fillSharedPacketDataAndSend(threatUpdate);
        }
    }

    private void putThreatListRef(ObjectGuid guid, ThreatReference refe) {
        needClientUpdate = true;
        myThreatListEntries.put(guid, refe);
        sortedThreatList.add(refe);
        collections.sort(sortedThreatList);
    }

    private void putThreatenedByMeRef(ObjectGuid guid, ThreatReference refe) {
        threatenedByMe.put(guid, refe);
    }

    private void updateRedirectInfo() {
        redirectInfo.clear();
        int totalPct = 0;

        for (var pair : redirectRegistry.entrySet()) // (spellid, victim . pct)
        {
            for (var victimPair : pair.getValue()) // (victim,pct)
            {
                var thisPct = Math.min(100 - totalPct, victimPair.value);

                if (thisPct > 0) {
                    redirectInfo.add(Tuple.create(victimPair.key, thisPct));
                    totalPct += thisPct;

                    if (totalPct == 100) {
                        return;
                    }
                }
            }
        }
    }
}
