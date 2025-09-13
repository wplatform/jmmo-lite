package com.github.azeroth.game.achievement;


import java.util.ArrayList;

public class ModifierTreeNode {
    public ModifierTreeRecord entry;
    public ArrayList<ModifierTreeNode> children = new ArrayList<>();
}
