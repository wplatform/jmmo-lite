package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class PowerUsersSelector implements ICheck<Unit> {
    private final Unit me;
    private final PowerType power = PowerType.values()[0];
    private final float dist;
    private final boolean playerOnly;

    public PowerUsersSelector(Unit unit, PowerType power, float dist, boolean playerOnly) {
        me = unit;
        this.power = power;
        this.dist = dist;
        this.playerOnly = playerOnly;
    }

    public final boolean invoke(Unit target) {
        if (me == null || target == null) {
            return false;
        }

        if (target.getDisplayPowerType() != power) {
            return false;
        }

        if (playerOnly && target.getTypeId() != TypeId.Player) {
            return false;
        }

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist)) {
            return false;
        }

        if (dist < 0.0f && me.isWithinCombatRange(target, -dist)) {
            return false;
        }

        return true;
    }
}