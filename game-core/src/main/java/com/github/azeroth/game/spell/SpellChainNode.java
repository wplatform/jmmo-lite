package com.github.azeroth.game.spell;

public class SpellChainNode {
    public SpellInfo prev;
    public SpellInfo next;
    public SpellInfo first;
    public SpellInfo last;
    public byte rank;
}
