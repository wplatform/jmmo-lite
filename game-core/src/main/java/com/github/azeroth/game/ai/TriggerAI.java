package game.ai;

import game.entities.*;
import game.spells.*;
import game.*;

// Copyright (c) Forged WoW LLC <https://github.com/ForgedWoW/ForgedCore>
// Licensed under GPL-3.0 license. See <https://github.com/ForgedWoW/ForgedCore/blob/master/LICENSE> for full information.




public class TriggerAI extends NullCreatureAI {
    public TriggerAI(Creature c) {
        super(c);
    }

    @Override
    public void isSummonedBy(WorldObject summoner) {
        if (me.spells[0] != 0) {
            CastSpellExtraArgs extra = new CastSpellExtraArgs();
            extra.originalCaster = summoner.getGUID().clone();
            me.CastSpell(me, me.spells[0], extra);
        }
    }
}