package com.github.azeroth.game.ai.enums;

import com.github.azeroth.game.domain.object.enums.TypeId;
import com.github.azeroth.game.entity.unit.Unit;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.function.Predicate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DefaultTargetSelector implements Predicate<Unit> {
    private Unit me;
    private float dist;
    private boolean playerOnly;
    private Unit exception;
    private int aura;

    @Override
    public boolean test(Unit target) {
        if (me == null)
            return false;

        if (target == null)
            return false;

        if (exception != null && target == exception)
            return false;

        if (playerOnly && (target.getObjectTypeId() != TypeId.PLAYER))
            return false;

        if (dist > 0.0f && !me.isWithinCombatRange(target, dist))
            return false;

        if (dist < 0.0f && !me.isWithinCombatRange(target, -dist))
            return false;

        if (aura != 0) {
            if (aura > 0) {
                return target.hasAura(aura);
            } else {
                return !target.hasAura(-aura);
            }
        }

        return true;
    }
}
