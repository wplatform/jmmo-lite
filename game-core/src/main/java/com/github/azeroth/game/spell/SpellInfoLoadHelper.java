package com.github.azeroth.game.spell;

import com.github.azeroth.dbc.defines.DbcDefine;
import com.github.azeroth.dbc.domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class SpellInfoLoadHelper {
    public SpellAuraOption auraOptions;
    public SpellAuraRestriction auraRestrictions;
    public SpellCastingRequirement castingRequirements;
    public SpellCategories categories;
    public SpellClassOption classOptions;
    public SpellCooldown cooldowns;
    public SpellEffect[] effects = new SpellEffect[DbcDefine.MAX_SPELL_EFFECTS];
    public SpellEquippedItem equippedItems;
    public SpellInterrupt interrupts;
    public ArrayList<SpellLabel> labels = new ArrayList<>();
    public SpellLevel levels;
    public SpellMiscEntry misc;
    public SpellPower[] powers = new SpellPower[DbcDefine.MAX_POWERS_PER_SPELL];
    public SpellReagent reagents;
    public ArrayList<SpellReagentsCurrency> reagentsCurrency = new ArrayList<>();
    public SpellScaling scaling;
    public SpellShapeshift shapeshift;
    public SpellTargetRestriction targetRestrictions;
    public SpellTotem totems;
    // only to group visuals when parsing sSpellXSpellVisualStore, not for loading
    public ArrayList<SpellXSpellVisual> visuals = new ArrayList<>();
}
