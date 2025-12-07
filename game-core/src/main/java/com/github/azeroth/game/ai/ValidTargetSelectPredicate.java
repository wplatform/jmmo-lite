package game.ai;

import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




//C# TO JAVA CONVERTER WARNING: Java does not allow user-defined value types. The behavior of this class may differ from the original:
//ORIGINAL LINE: struct ValidTargetSelectPredicate : ICheck<Unit>
public final class ValidTargetSelectPredicate implements ICheck<Unit> {
    private final IUnitAI ai;

    public ValidTargetSelectPredicate() {
    }

    public ValidTargetSelectPredicate(IUnitAI ai) {
        this.ai = ai;
    }

    public boolean invoke(Unit target) {
        return ai.canAIAttack(target);
    }

    public ValidTargetSelectPredicate clone() {
        ValidTargetSelectPredicate varCopy = new ValidTargetSelectPredicate();

        varCopy._ai = this._ai;

        return varCopy;
    }
}