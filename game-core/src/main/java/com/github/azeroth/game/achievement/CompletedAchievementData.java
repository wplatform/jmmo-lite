package com.github.azeroth.game.achievement;

import java.util.ArrayList;


public class CompletedAchievementData {
    public long date;
    public ArrayList<ObjectGuid> completingPlayers = new ArrayList<>();
    public boolean changed;
}
