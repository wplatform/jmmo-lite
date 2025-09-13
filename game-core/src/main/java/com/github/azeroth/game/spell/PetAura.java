package com.github.azeroth.game.spell;

import java.util.HashMap;

public class PetAura {

    private final HashMap<Integer, Integer> auras = new HashMap<Integer, Integer>();
    private final boolean removeOnChangePet;
    private final double damage;

    public PetAura() {
        removeOnChangePet = false;
        damage = 0;
    }


    public PetAura(int petEntry, int aura, boolean removeOnChangePet, double damage) {
        removeOnChangePet = removeOnChangePet;
        damage = damage;

        auras.put(petEntry, aura);
    }


    public final int getAura(int petEntry) {
        var auraId = auras.get(petEntry);

        if (auraId != 0) {
            return auraId;
        }

        auraId = auras.get(0);

        if (auraId != 0) {
            return auraId;
        }

        return 0;
    }


    public final void addAura(int petEntry, int aura) {
        auras.put(petEntry, aura);
    }

    public final boolean isRemovedOnChangePet() {
        return removeOnChangePet;
    }

    public final double getDamage() {
        return damage;
    }
}
