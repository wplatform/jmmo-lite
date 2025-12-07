package com.github.azeroth.game.entity.player.model;

import com.github.azeroth.game.spell.Aura;

public class SpellPctModifierByLabel extends SpellModifier {
    public SpellPctModByLabel value = new SpellPctModByLabel();

    public SpellPctModifierByLabel(Aura ownerAura) {
        super(ownerAura);
    }
}
