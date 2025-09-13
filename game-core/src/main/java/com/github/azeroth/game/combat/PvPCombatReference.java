package com.github.azeroth.game.combat;

import com.github.azeroth.game.entity.unit.Unit;

public class PvPCombatReference extends CombatReference {

    public static int PVP_COMBAT_TIMEOUT = 5 * time.InMilliseconds;


    private int combatTimer = PVP_COMBAT_TIMEOUT;


    public PvPCombatReference(Unit first, Unit second) {
        super(first, second, true);
    }


    public final boolean update(int tdiff) {
        if (combatTimer <= tdiff) {
            return false;
        }

        _combatTimer -= tdiff;

        return true;
    }

    public final void refreshTimer() {
        combatTimer = PVP_COMBAT_TIMEOUT;
    }
}
