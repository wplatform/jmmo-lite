package com.github.azeroth.game.spell;

import com.github.azeroth.dbc.domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SpellInfoLoadHelper {
    public SpellAuraOption auraOptions;
    public SpellAuraRestriction auraRestrictions;
    public SpellCastingRequirement castingRequirements;
    public Spellcategories categories;
    public SpellClassOption classOptions;
    public SpellCooldown cooldowns;
    public HashMap<Integer, SpellEffect> effects = new HashMap<>();
    public SpellEquippedItem equippedItems;
    public SpellInterrupt interrupts;
    public ArrayList<SpellLabel> labels = new ArrayList<>();
    public SpellLevel levels;
    public Spellmisc misc;
    public SpellPower[] powers = new SpellPower[SpellConst.MaxPowersPerSpell];
    public SpellReagent reagents;
    public ArrayList<SpellreagentsCurrency> reagentsCurrency = new ArrayList<>();
    public Spellscaling scaling;
    public Spellshapeshift shapeshift;
    public SpellTargetRestriction targetRestrictions;
    public SpellTotem totems;
    // only to group visuals when parsing sSpellXSpellVisualStore, not for loading
    public ArrayList<SpellXSpellVisual> visuals = new ArrayList<>();
}
