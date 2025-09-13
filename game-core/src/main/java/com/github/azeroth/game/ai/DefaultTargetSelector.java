package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.unit.Unit;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
// default predicate function to select target based on distance, player and/or aura criteria
public class DefaultTargetSelector implements ICheck<unit> {
    // unit: the reference unit
    // dist: if 0: ignored, if > 0: maximum distance to the reference unit, if < 0: minimum distance to the reference unit
    // playerOnly: self explaining
    // withMainTank: allow current tank to be selected
    // aura: if 0: ignored, if > 0: the target shall have the aura, if < 0, the target shall NOT have the aura
    private final Unit me;
    private final float dist;
    private final boolean playerOnly;
    private final Unit exception;
    private final int aura;


    public final boolean invoke(Unit target) {
        if (me == null) {
            return false;
        }

        if (target == null) {
            return false;
        }

        if (exception != null && target == exception) {
            return false;
        }

        if (playerOnly && !target.isTypeId(TypeId.PLAYER)) {
            return false;
        }

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist)) {
            return false;
        }

        if (dist < 0.0f && me.isWithinCombatRange(target, -_dist)) {
            return false;
        }

        if (aura != 0) {
            if (aura > 0) {
                if (!target.hasAura((int) aura)) {
                    return false;
                }
            } else {
                if (target.hasAura((int) -_aura)) {
                    return false;
                }
            }
        }

        return false;
    }
}

