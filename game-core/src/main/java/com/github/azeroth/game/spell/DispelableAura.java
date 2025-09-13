package com.github.azeroth.game.spell;

public class DispelableAura {
    private final Aura aura;
    private final int chance;
    private byte charges;

    public DispelableAura(Aura aura, int dispelChance, byte dispelCharges) {
        aura = aura;
        chance = dispelChance;
        charges = dispelCharges;
    }

    public final boolean rollDispel() {
        return RandomUtil.randChance(chance);
    }

    public final Aura getAura() {
        return aura;
    }

    public final byte getDispelCharges() {
        return charges;
    }

    public final void incrementCharges() {
        ++charges;
    }

    public final boolean decrementCharge(byte charges) {
        if (charges == 0) {
            return false;
        }

        _charges -= charges;

        return charges > 0;
    }
}
