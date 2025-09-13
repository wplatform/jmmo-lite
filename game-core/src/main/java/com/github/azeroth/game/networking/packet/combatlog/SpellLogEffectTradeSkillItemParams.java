package com.github.azeroth.game.networking.packet.combatlog;

public final class SpellLogEffectTradeSkillItemParams {
    public int itemID;

    public SpellLogEffectTradeSkillItemParams clone() {
        SpellLogEffectTradeSkillItemParams varCopy = new SpellLogEffectTradeSkillItemParams();

        varCopy.itemID = this.itemID;

        return varCopy;
    }
}
