package com.github.azeroth.game.map;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;

class PlayerAtMinimumRangeAway implements ICheck<Player> {
    private final Unit unit;
    private final float fRange;

    public PlayerAtMinimumRangeAway(Unit unit, float fMinRange) {
        unit = unit;
        fRange = fMinRange;
    }

    public final boolean invoke(Player player) {
        //No threat list check, must be done explicit if expected to be in combat with creature
        if (!player.isGameMaster() && player.isAlive() && !unit.isWithinDist(player, fRange, false)) {
            return true;
        }

        return false;
    }
}
