package com.github.azeroth.game.spell;

import com.github.azeroth.game.entity.player.model.SpellModifier;

public class SpellFlatModifierByLabel extends SpellModifier {
    public SpellFlatModByLabel value = new SpellFlatModByLabel();

    public SpellFlatModifierByLabel(Aura ownerAura) {
        super(ownerAura);
    }
}
