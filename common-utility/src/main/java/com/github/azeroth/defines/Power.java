package com.github.azeroth.defines;

public enum Power {

    HEALTH(-2),
    MANA(0),
    RAGE(1),
    FOCUS(2),
    ENERGY(3),
    COMBO_POINTS(4),
    RUNES(5),
    RUNIC_POWER(6),
    SOUL_SHARDS(7),
    LUNAR_POWER(8),
    HOLY_POWER(9),
    ALTERNATE_POWER(10),
    MAELSTROM(11),
    CHI(12),
    INSANITY(13),
    BURNING_EMBERS(14),
    DEMONIC_FURY(15),
    ARCANE_CHARGES(16),
    FURY(17),
    PAIN(18),
    ESSENCE(19),
    RUNE_BLOOD(20), // TITLE Blood Runes
    RUNE_FROST(21), // TITLE Frost Runes
    RUNE_UNHOLY(22), // TITLE Unholy Runes
    ALTERNATE_QUEST(23), // TITLE Alternate (Quest)
    ALTERNATE_ENCOUNTER(24), // TITLE Alternate (Encounter)
    ALTERNATE_MOUNT(25), // TITLE Alternate (Mount)
    BALANCE(26), // TITLE Balance
    HAPPINESS(27), // TITLE Happiness
    MAX_POWERS(28),
    POWER_ALL(127);

    public final byte index;

    Power(int index) {
        this.index = (byte) index;
    }

    public static Power valueOf(byte index) {
        return switch (index) {
            case -2 -> HEALTH;
            case 0 -> MANA;
            case 1 -> RAGE;
            case 2 -> FOCUS;
            case 3 -> ENERGY;
            case 4 -> COMBO_POINTS;
            case 5 -> RUNES;
            case 6 -> RUNIC_POWER;
            case 7 -> SOUL_SHARDS;
            case 8 -> LUNAR_POWER;
            case 9 -> HOLY_POWER;
            case 10 -> ALTERNATE_POWER;
            case 11 -> MAELSTROM;
            case 12 -> CHI;
            case 13 -> INSANITY;
            case 14 -> BURNING_EMBERS;
            case 15 -> DEMONIC_FURY;
            case 16 -> ARCANE_CHARGES;
            case 17 -> FURY;
            case 18 -> PAIN;
            case 19 -> ESSENCE;
            case 20 -> RUNE_BLOOD;
            case 21 -> RUNE_FROST;
            case 22 -> RUNE_UNHOLY;
            case 23 -> ALTERNATE_QUEST;
            case 24 -> ALTERNATE_ENCOUNTER;
            case 25 -> ALTERNATE_MOUNT;
            case 26 -> BALANCE;
            case 27 -> HAPPINESS;
            case 28 -> MAX_POWERS;
            case 127 -> POWER_ALL;
            default -> throw new IllegalArgumentException("Unknown power index: " + index);
        };
    }
}
