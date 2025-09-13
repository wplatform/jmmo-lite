package com.github.azeroth.game.dungeonfinding;


import game.Quest;

public class LfgPlayerRewardData {

    public int rdungeonEntry;

    public int sdungeonEntry;
    public boolean done;
    public Quest quest;


    public LfgPlayerRewardData(int random, int current, boolean _done, Quest quest) {
        rdungeonEntry = random;
        sdungeonEntry = current;
        done = _done;
        quest = quest;
    }
}
