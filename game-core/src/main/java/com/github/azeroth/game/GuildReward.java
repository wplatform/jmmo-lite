package com.github.azeroth.game;


import java.util.ArrayList;

public class GuildReward {
    public int itemID;
    public byte minGuildRep;
    public long raceMask;
    public long cost;
    public ArrayList<Integer> achievementsRequired = new ArrayList<>();
}
