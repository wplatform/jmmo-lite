package com.github.azeroth.game.spell;



public class SpellEffectHandlerAttribute extends Attribute {
    private SpelleffectName effectName = SpellEffectName.values()[0];

    public SpellEffectHandlerAttribute(SpellEffectName effectName) {
        setEffectName(effectName);
    }

    public final SpellEffectName getEffectName() {
        return effectName;
    }

    public final void setEffectName(SpellEffectName value) {
        effectName = value;
    }
}
