package com.github.azeroth.game.entity.player;

public final class PvpInfo {
    public boolean isHostile;
    public boolean isInHostileArea; //> Marks if player is in an area which forces PvP flag
    public boolean isInNoPvPArea; //> Marks if player is in a sanctuary or friendly capital city
    public boolean isInFfaPvPArea; //> Marks if player is in an FFAPvP area (such as Gurubashi Arena)
    public long endTimer; //> Time when player unflags himself for pvP (flag removed after 5 minutes)

}
