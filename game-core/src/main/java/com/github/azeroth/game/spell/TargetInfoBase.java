package com.github.azeroth.game.spell;

import java.util.HashSet;


public class TargetInfoBase {
    public HashSet<Integer> effects;

    public void preprocessTarget(Spell spell) {
    }

    public void doTargetSpellHit(Spell spell, SpellEffectInfo spellEffectInfo) {
    }

    public void doDamageAndTriggers(Spell spell) {
    }
}
