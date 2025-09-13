package com.github.azeroth.game.dungeonfinding;


public class LfgReward {
    public int maxLevel;
    public int firstQuest;
    public int otherQuest;


    public LfgReward(int maxLevel, int firstQuest) {
        this(maxLevel, firstQuest, 0);
    }

    public LfgReward(int maxLevel) {
        this(maxLevel, 0, 0);
    }

    public LfgReward() {
        this(0, 0, 0);
    }

    public LfgReward(int maxLevel, int firstQuest, int otherQuest) {
        maxLevel = maxLevel;
        firstQuest = firstQuest;
        otherQuest = otherQuest;
    }
}
