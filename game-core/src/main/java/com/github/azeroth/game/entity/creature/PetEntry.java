package com.github.azeroth.game.entity.creature;

enum PetEntry {
    // Warlock pets
    Imp(416),
    FelHunter(691),
    VoidWalker(1860),
    Succubus(1863),
    Doomguard(18540),
    Felguard(30146),

    // Death Knight pets
    Ghoul(26125),
    Abomination(106848),

    // Shaman pet
    SpiritWolf(29264);

    public static final int SIZE = Integer.SIZE;
    private static java.util.HashMap<Integer, PetEntry> mappings;
    private int intValue;

    private PetEntry(int value) {
        intValue = value;
        getMappings().put(value, this);
    }

    private static java.util.HashMap<Integer, PetEntry> getMappings() {
        if (mappings == null) {
            synchronized (PetEntry.class) {
                if (mappings == null) {
                    mappings = new java.util.HashMap<Integer, PetEntry>();
                }
            }
        }
        return mappings;
    }

    public static PetEntry forValue(int value) {
        return getMappings().get(value);
    }

    public int getValue() {
        return intValue;
    }
}
