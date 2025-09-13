package com.github.azeroth.game.achievement;


import java.util.ArrayList;

public class CriteriaTree {

    public int id;
    public CriteriaTreeRecord entry;
    public achievementRecord achievement;
    public scenarioStepRecord scenarioStep;
    public questObjective questObjective;
    public criteria criteria;
    public ArrayList<CriteriaTree> children = new ArrayList<>();
}
