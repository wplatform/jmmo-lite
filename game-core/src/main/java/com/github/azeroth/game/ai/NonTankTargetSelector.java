package com.github.azeroth.game.ai;










public class NonTankTargetSelector implements ICheck<Unit> {
    private final Unit source;
    private final boolean playerOnly;


    public NonTankTargetSelector(Unit source) {
        this(source, true);
    }


//ORIGINAL LINE: public NonTankTargetSelector(Unit source, bool playerOnly = true)
    public NonTankTargetSelector(Unit source, boolean playerOnly) {
        this.source = source;
        this.playerOnly = playerOnly;
    }

    public final boolean invoke(Unit target) {
        if (target == null) {
            return false;
        }

        if (playerOnly && !target.isTypeId(TypeId.Player)) {
            return false;
        }

        var currentVictim = source.getThreatManager().getCurrentVictim();

        if (currentVictim != null) {
            return target != currentVictim;
        }

        return target != source.getVictim();
    }
}