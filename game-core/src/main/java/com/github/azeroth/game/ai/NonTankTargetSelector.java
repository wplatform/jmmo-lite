package com.github.azeroth.game.ai;


import com.github.azeroth.game.entity.unit.Unit;

public class NonTankTargetSelector implements ICheck<unit> {
    private final Unit source;
    private final boolean playerOnly;


    public NonTankTargetSelector(Unit source) {
        this(source, true);
    }

    public NonTankTargetSelector(Unit source, boolean playerOnly) {
        source = source;
        playerOnly = playerOnly;
    }

    public final boolean invoke(Unit target) {
        if (target == null) {
            return false;
        }

        if (playerOnly && !target.isTypeId(TypeId.PLAYER)) {
            return false;
        }

        var currentVictim = source.getThreatManager().getCurrentVictim();

        if (currentVictim != null) {
            return target != currentVictim;
        }

        return target != source.getVictim();
    }
}
