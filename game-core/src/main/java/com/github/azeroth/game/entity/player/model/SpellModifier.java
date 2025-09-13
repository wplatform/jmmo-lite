package com.github.azeroth.game.entity.player.model;

import com.github.azeroth.game.entity.player.enums.SpellModType;
import com.github.azeroth.game.spell.Aura;
import com.github.azeroth.game.spell.enums.SpellModOp;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class SpellModifier{
    private SpellModOp op;
    private SpellModType type;

    private int spellId;
    private Aura ownerAura;

    public SpellModifier(Aura ownerAura) {
        setOp(SpellModOp.HealingAndDamage);
        setType(SpellModType.FLAT);
        setSpellId(0);
        setOwnerAura(ownerAura);
    }
}
