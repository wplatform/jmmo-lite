package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.unit.Unit;

class AllFriendlyUnitsInGrid implements ICheck<unit> {
    private final Unit unit;

    public AllFriendlyUnitsInGrid(Unit obj) {
        unit = obj;
    }

    public final boolean invoke(Unit u) {
        if (u.isAlive() && u.isVisible() && u.isFriendlyTo(unit)) {
            return true;
        }

        return false;
    }
}
