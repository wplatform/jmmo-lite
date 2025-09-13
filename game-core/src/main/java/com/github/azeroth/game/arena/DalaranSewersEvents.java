package com.github.azeroth.game.arena;


final class DalaranSewersEvents {
    public static final int WATERFALLWARNING = 1; // Water starting to fall, but no LoS Blocking nor movement blocking
    public static final int waterfallOn = 2; // LoS and Movement blocking active
    public static final int waterfallOff = 3;
    public static final int waterfallKnockback = 4;

    public static final int pipeKnockback = 5;
}
