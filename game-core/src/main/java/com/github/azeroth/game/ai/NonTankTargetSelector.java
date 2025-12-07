package game.ai;

import Framework.Constants.*;
import game.entities.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class NonTankTargetSelector implements ICheck<Unit> {
    private final Unit source;
    private final boolean playerOnly;


    public NonTankTargetSelector(Unit source) {
        this(source, true);
    }

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
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