package com.github.azeroth.defines;

import lombok.Getter;

import java.util.Objects;

@Getter
public enum Expansion {
    CLASSIC(0, 60),
    THE_BURNING_CRUSADE(1, 70),
    WRATH_OF_THE_LICH_KING(2, 80),
    CATACLYSM(3, 85),
    MISTS_OF_PANDARIA(4, 90),
    WARLORDS_OF_DRAENOR(5, 10),
    LEGION(6, 110);
    private final byte value;
    private final int level;

    public static final int MAX_EXPANSION = Expansion.values().length;
    public static final int MAX_ACCOUNT_EXPANSION = MAX_EXPANSION + 1;
    public static final Expansion CURRENT = MISTS_OF_PANDARIA;

    Expansion(int value, int level) {
        this.value = (byte) value;
        this.level = level;
    }


    public static Expansion indexOf(int value) {
        Objects.checkFromIndexSize(value, 0, values().length);
        return Expansion.values()[value];
    }

}
