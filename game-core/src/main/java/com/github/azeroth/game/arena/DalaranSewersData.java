package com.github.azeroth.game.arena;


final class DalaranSewersData {

    public static final int pipeKnockbackTotalCount = 2;

    public static final int npcWaterSpout = 28567;
    // These values are NOT blizzlike... need the correct data!
    public static Duration WATERFALLTIMERMIN = duration.FromSeconds(30);
    public static Duration WATERFALLTIMERMAX = duration.FromSeconds(60);
    public static Duration WATERWARNINGDURATION = duration.FromSeconds(5);
    public static Duration WATERFALLDURATION = duration.FromSeconds(30);
    public static Duration WATERFALLKNOCKBACKTIMER = duration.FromSeconds(1.5);
    public static Duration PIPEKNOCKBACKFIRSTDELAY = duration.FromSeconds(5);
    public static Duration PIPEKNOCKBACKDELAY = duration.FromSeconds(3);
}
