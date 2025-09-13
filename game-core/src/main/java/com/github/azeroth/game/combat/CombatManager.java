package com.github.azeroth.game.combat;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.domain.unit.UnitState;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;

import java.util.HashMap;


public class CombatManager {
    private final Unit owner;
    private final HashMap<ObjectGuid, CombatReference> pveRefs = new HashMap<>();
    private final HashMap<ObjectGuid, PvPCombatReference> pvpRefs = new HashMap<>();

    public CombatManager(Unit owner) {
        this.owner = owner;
    }

    public static boolean canBeginCombat(Unit a, Unit b) {
        // Checks combat validity before initial reference creation.
        // For the combat to be valid...
        // ...the two units need to be different
        if (a == b) {
            return false;
        }

        // ...the two units need to be in the world
        if (!a.isInWorld() || !b.isInWorld()) {
            return false;
        }

        // ...the two units need to both be alive
        if (!a.isAlive() || !b.isAlive()) {
            return false;
        }

        // ...the two units need to be on the same map
        if (a.getMap() != b.getMap()) {
            return false;
        }

        // ...the two units need to be in the same phase
        if (!WorldObject.inSamePhase(a, b)) {
            return false;
        }

        if (a.hasUnitState(UnitState.EVADE) || b.hasUnitState(UnitState.EVADE)) {
            return false;
        }

        if (a.hasUnitState(UnitState.IN_FLIGHT) || b.hasUnitState(UnitState.IN_FLIGHT)) {
            return false;
        }

        // ... both units must be allowed to enter combat
        if (a.isCombatDisallowed() || b.isCombatDisallowed()) {
            return false;
        }

        if (a.isFriendlyTo(b) || b.isFriendlyTo(a)) {
            return false;
        }

        var playerA = a.getCharmerOrOwnerPlayerOrPlayerItself();
        var playerB = b.getCharmerOrOwnerPlayerOrPlayerItself();

        // ...neither of the two units must be (owned by) a player with .gm on
        return (playerA == null || !playerA.isGameMaster()) && (playerB == null || !playerB.isGameMaster());
    }

    public static void notifyAICombat(Unit me, Unit other) {
        var ai = me.getAI();

        if (ai != null) {
            ai.justEnteredCombat(other);
        }
    }

    public final Unit getOwner() {
        return owner;
    }

    public final boolean getHasCombat() {
        return hasPvECombat() || hasPvPCombat();
    }

    public final HashMap<ObjectGuid, CombatReference> getPvECombatRefs() {
        return pveRefs;
    }

    public final HashMap<ObjectGuid, PvPCombatReference> getPvPCombatRefs() {
        return pvpRefs;
    }

    public final void update(int tdiff) {
        for (var pair : pvpRefs.ToList()) {
            var refe = pair.value;

            if (refe.first == owner && !refe.update(tdiff)) // only update if we're the first unit involved (otherwise double decrement)
            {
                pvpRefs.remove(pair.key);
                refe.endCombat(); // this will remove it from the other side
            }
        }
    }

    public final boolean hasPvECombat() {
        synchronized (pveRefs) {

            for (var(_, refe) : pveRefs) {
                if (!refe.isSuppressedFor(owner)) {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean hasPvECombatWithPlayers() {
        synchronized (pveRefs) {
            for (var reference : pveRefs.entrySet()) {
                if (!reference.getValue().isSuppressedFor(owner) && reference.getValue().getOther(owner).IsPlayer) {
                    return true;
                }
            }
        }

        return false;
    }

    public final boolean hasPvPCombat() {
        synchronized (pveRefs) {
            for (var pair : pvpRefs.entrySet()) {
                if (!pair.getValue().isSuppressedFor(owner)) {
                    return true;
                }
            }
        }

        return false;
    }

    public final Unit getAnyTarget() {
        synchronized (pveRefs) {
            for (var pair : pveRefs.entrySet()) {
                if (!pair.getValue().isSuppressedFor(owner)) {
                    return pair.getValue().getOther(owner);
                }
            }

            for (var pair : pvpRefs.entrySet()) {
                if (!pair.getValue().isSuppressedFor(owner)) {
                    return pair.getValue().getOther(owner);
                }
            }
        }

        return null;
    }

    public final boolean setInCombatWith(Unit who) {
        return setInCombatWith(who, false);
    }

    public final boolean setInCombatWith(Unit who, boolean addSecondUnitSuppressed) {
        // Are we already in combat? If yes, refresh pvp combat
        synchronized (pveRefs) {
            var existingPvpRef = pvpRefs.get(who.getGUID());

            if (existingPvpRef != null) {
                existingPvpRef.refreshTimer();
                existingPvpRef.refresh();

                return true;
            }


            var existingPveRef = pveRefs.get(who.getGUID());

            if (existingPveRef != null) {
                existingPveRef.refresh();

                return true;
            }
        }

        // Otherwise, check validity...
        if (!canBeginCombat(owner, who)) {
            return false;
        }

        // ...then create new reference
        CombatReference refe;

        if (owner.isControlledByPlayer() && who.isControlledByPlayer()) {
            refe = new PvPCombatReference(owner, who);
        } else {
            refe = new CombatReference(owner, who);
        }

        if (addSecondUnitSuppressed) {
            refe.suppress(who);
        }

        // ...and insert it into both managers
        putReference(who.getGUID(), refe);
        who.getCombatManager().putReference(owner.getGUID(), refe);

        // now, sequencing is important - first we update the combat state, which will set both units in combat and do non-AI combat start stuff
        var needSelfAI = updateOwnerCombatState();
        var needOtherAI = who.getCombatManager().updateOwnerCombatState();

        // then, we finally notify the AI (if necessary) and let it safely do whatever it feels like
        if (needSelfAI) {
            notifyAICombat(owner, who);
        }

        if (needOtherAI) {
            notifyAICombat(who, owner);
        }

        return isInCombatWith(who);
    }

    public final boolean isInCombatWith(ObjectGuid guid) {
        synchronized (pveRefs) {
            return pveRefs.containsKey(guid) || pvpRefs.containsKey(guid);
        }
    }

    public final boolean isInCombatWith(Unit who) {
        return isInCombatWith(who.getGUID());
    }

    public final void inheritCombatStatesFrom(Unit who) {
        var mgr = who.getCombatManager();

        synchronized (pveRefs) {
            for (var refe : mgr.pveRefs.entrySet()) {
                if (!isInCombatWith(refe.getKey())) {
                    var target = refe.getValue().getOther(who);

                    if ((owner.isImmuneToPC() && target.hasUnitFlag(UnitFlag.PlayerControlled)) || (owner.isImmuneToNPC() && !target.hasUnitFlag(UnitFlag.PlayerControlled))) {
                        continue;
                    }

                    setInCombatWith(target);
                }
            }

            for (var refe : mgr.pvpRefs.entrySet()) {
                var target = refe.getValue().getOther(who);

                if ((owner.isImmuneToPC() && target.hasUnitFlag(UnitFlag.PlayerControlled)) || (owner.isImmuneToNPC() && !target.hasUnitFlag(UnitFlag.PlayerControlled))) {
                    continue;
                }

                setInCombatWith(target);
            }
        }
    }

    public final void endCombatBeyondRange(float range, boolean includingPvP) {
        synchronized (pveRefs) {
            for (var pair : pveRefs.ToList()) {
                var refe = pair.value;

                if (!refe.first.isWithinDistInMap(refe.second, range)) {
                    pveRefs.remove(pair.key);
                    refe.endCombat();
                }
            }

            if (!includingPvP) {
                return;
            }

            for (var pair : pvpRefs.ToList()) {
                CombatReference refe = pair.value;

                if (!refe.first.isWithinDistInMap(refe.second, range)) {
                    pvpRefs.remove(pair.key);
                    refe.endCombat();
                }
            }
        }
    }

    public final void suppressPvPCombat() {
        synchronized (pveRefs) {
            for (var pair : pvpRefs.entrySet()) {
                pair.getValue().suppress(owner);
            }
        }

        if (updateOwnerCombatState()) {
            var ownerAI = owner.getAI();

            if (ownerAI != null) {
                ownerAI.justExitedCombat();
            }
        }
    }

    public final void endAllPvECombat() {
        // cannot have threat without combat
        owner.getThreatManager().removeMeFromThreatLists();
        owner.getThreatManager().clearAllThreat();

        synchronized (pveRefs) {
            while (!pveRefs.isEmpty()) {
                pveRefs.firstEntry().value.endCombat();
            }
        }
    }

    public final void revalidateCombat() {
        synchronized (pveRefs) {

            for (var(guid, refe) : pveRefs.ToList()) {
                if (!canBeginCombat(owner, refe.getOther(owner))) {
                    pveRefs.remove(guid); // erase manually here to avoid iterator invalidation
                    refe.endCombat();
                }
            }


            for (var(guid, refe) : pvpRefs.ToList()) {
                if (!canBeginCombat(owner, refe.getOther(owner))) {
                    pvpRefs.remove(guid); // erase manually here to avoid iterator invalidation
                    refe.endCombat();
                }
            }
        }
    }

    public final void purgeReference(ObjectGuid guid, boolean pvp) {
        synchronized (pveRefs) {
            if (pvp) {
                pvpRefs.remove(guid);
            } else {
                pveRefs.remove(guid);
            }
        }
    }

    public final boolean updateOwnerCombatState() {
        var combatState = getHasCombat();

        if (combatState == owner.isInCombat()) {
            return false;
        }

        if (combatState) {
            owner.setUnitFlag(UnitFlag.IN_COMBAT);
            owner.atEnterCombat();

            if (!owner.isCreature()) {
                owner.atEngage(getAnyTarget());
            }
        } else {
            owner.removeUnitFlag(UnitFlag.IN_COMBAT);
            owner.atExitCombat();

            if (!owner.isCreature()) {
                owner.atDisengage();
            }
        }

        var master = owner.getCharmerOrOwner();

        if (master != null) {
            master.updatePetCombatState();
        }

        return true;
    }

    public final void endAllCombat() {
        endAllPvECombat();
        endAllPvPCombat();
    }

    private void endAllPvPCombat() {
        synchronized (pveRefs) {
            while (!pvpRefs.isEmpty()) {
                pvpRefs.firstEntry().value.endCombat();
            }
        }
    }

    private void putReference(ObjectGuid guid, CombatReference refe) {
        synchronized (pveRefs) {
            if (refe.isPvP) {
                pvpRefs.put(guid, (PvPCombatReference) refe);
            } else {
                pveRefs.put(guid, refe);
            }
        }
    }
}
