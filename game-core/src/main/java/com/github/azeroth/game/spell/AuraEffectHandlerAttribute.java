package com.github.azeroth.game.spell;



public class AuraEffectHandlerAttribute extends Attribute {
    public AuraEffectHandlerAttribute(AuraType type) {
        setAuraType(type);
    }    private auraType auraType = getAuraType().values()[0];

    public final AuraType getAuraType() {
        return auraType;
    }

    public final void setAuraType(AuraType value) {
        auraType = value;
    }


}
