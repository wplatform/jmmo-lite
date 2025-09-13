package com.github.azeroth.game.spell;


import com.github.azeroth.game.entity.item.Item;

public class ItemTargetInfo extends TargetInfoBase {
    public Item targetItem;

    @Override
    public void doTargetSpellHit(Spell spell, SpellEffectInfo spellEffectInfo) {
        spell.callScriptBeforeHitHandlers(SpellMissInfo.NONE);

        spell.handleEffects(null, targetItem, null, null, spellEffectInfo, SpellEffectHandleMode.HitTarget);

        spell.callScriptOnHitHandlers();
        spell.callScriptAfterHitHandlers();
    }
}
