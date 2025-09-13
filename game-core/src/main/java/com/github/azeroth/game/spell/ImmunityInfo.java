package com.github.azeroth.game.spell;


import java.util.ArrayList;


public class ImmunityInfo {
    public int schoolImmuneMask;
    public int applyHarmfulAuraImmuneMask;
    public long mechanicImmuneMask;
    public int dispelImmune;
    public int damageSchoolMask;

    public ArrayList<auraType> auraTypeImmune = new ArrayList<>();
    public ArrayList<SpellEffectName> spellEffectImmune = new ArrayList<>();
}
