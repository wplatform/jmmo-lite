package com.github.azeroth.game.ai;

import com.github.azeroth.game.entity.unit.Unit;

final class ValidTargetSelectPredicate implements ICheck<unit> {
    private final IUnitAI ai;

    public ValidTargetSelectPredicate() {
    }

    public ValidTargetSelectPredicate(IUnitAI ai) {
        ai = ai;
    }

    public boolean invoke(Unit target) {
        return ai.canAIAttack(target);
    }

    public ValidTargetSelectPredicate clone() {
        ValidTargetSelectPredicate varCopy = new ValidTargetSelectPredicate();

        varCopy.ai = this.ai;

        return varCopy;
    }
}
