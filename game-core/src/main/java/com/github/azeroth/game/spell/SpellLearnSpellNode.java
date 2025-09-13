package com.github.azeroth.game.spell;

public class SpellLearnSpellNode {
    public int spell;
    public int overridesSpell;
    public boolean active; // show in spellbook or not
    public boolean autoLearned; // This marks the spell as automatically learned from another source that - will only be used for unlearning
}
