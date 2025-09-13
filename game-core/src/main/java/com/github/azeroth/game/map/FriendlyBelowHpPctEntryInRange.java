package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.unit.Unit;

public class FriendlyBelowHpPctEntryInRange implements ICheck<unit> {
    private final Unit obj;
    private final int entry;
    private final float range;
    private final byte pct;
    private final boolean excludeSelf;

    public FriendlyBelowHpPctEntryInRange(Unit obj, int entry, float range, byte pct, boolean excludeSelf) {
        obj = obj;
        entry = entry;
        range = range;
        pct = pct;
        excludeSelf = excludeSelf;
    }

    public final boolean invoke(Unit u) {
        if (excludeSelf && Objects.equals(obj.getGUID(), u.getGUID())) {
            return false;
        }

        if (u.getEntry() == entry && u.isAlive() && u.isInCombat() && !obj.isHostileTo(u) && obj.isWithinDist(u, range) && u.healthBelowPct(pct)) {
            return true;
        }

        return false;
    }
}
