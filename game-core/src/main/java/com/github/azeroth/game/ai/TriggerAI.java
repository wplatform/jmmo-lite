package com.github.azeroth.game.ai;


import game.spells.*;







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