package com.github.azeroth.game.entity.player.model;

import com.github.azeroth.common.Flag128;
import com.github.azeroth.game.spell.Aura;

public class SpellModifierByClassMask extends SpellModifier {
    public int value;
    public Flag128 mask;

    public SpellModifierByClassMask(Aura ownerAura) {
        super(ownerAura);
        value = 0;
        mask = new Flag128();
    }
}
