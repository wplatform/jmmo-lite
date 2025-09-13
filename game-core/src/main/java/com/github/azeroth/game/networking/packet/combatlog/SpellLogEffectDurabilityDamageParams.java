package com.github.azeroth.game.networking.packet.combatlog;

public final class SpellLogEffectDurabilityDamageParams {
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public int itemID;
    public int amount;

    public SpellLogEffectDurabilityDamageParams clone() {
        SpellLogEffectDurabilityDamageParams varCopy = new SpellLogEffectDurabilityDamageParams();

        varCopy.victim = this.victim;
        varCopy.itemID = this.itemID;
        varCopy.amount = this.amount;

        return varCopy;
    }
}
