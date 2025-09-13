package com.github.azeroth.game.spell;


public class CorpseTargetInfo extends TargetInfoBase {
    public ObjectGuid targetGuid = ObjectGuid.EMPTY;

    public long timeDelay;

    @Override
    public void doTargetSpellHit(Spell spell, SpellEffectInfo spellEffectInfo) {
        var corpse = ObjectAccessor.getCorpse(spell.getCaster(), targetGuid);

        if (corpse == null) {
            return;
        }

        spell.callScriptBeforeHitHandlers(SpellMissInfo.NONE);

        spell.handleEffects(null, null, null, corpse, spellEffectInfo, SpellEffectHandleMode.HitTarget);

        spell.callScriptOnHitHandlers();
        spell.callScriptAfterHitHandlers();
    }
}
