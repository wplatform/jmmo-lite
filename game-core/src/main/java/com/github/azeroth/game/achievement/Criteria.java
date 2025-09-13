package com.github.azeroth.game.achievement;


import com.github.azeroth.dbc.domain.CriteriaEntity;

public class Criteria {
    public int id;
    public CriteriaEntity entry;
    public ModifierTreeNode modifier;
    public CriteriaFlagCu flagsCu = CriteriaFlagsCu.values()[0];
}
