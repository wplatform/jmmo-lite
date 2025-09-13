package com.github.azeroth.game.spell;

import java.util.ArrayList;


public class SpellLogEffect {
    public int effect;

    public ArrayList<SpellLogEffectPowerDrainParams> powerDrainTargets = new ArrayList<>();
    public ArrayList<SpellLogEffectExtraAttacksParams> extraAttacksTargets = new ArrayList<>();
    public ArrayList<SpellLogEffectDurabilityDamageParams> durabilityDamageTargets = new ArrayList<>();
    public ArrayList<SpellLogEffectGenericVictimParams> genericVictimTargets = new ArrayList<>();
    public ArrayList<SpellLogEffectTradeSkillItemParams> tradeSkillTargets = new ArrayList<>();
    public ArrayList<SpellLogEffectFeedPetParams> feedPetTargets = new ArrayList<>();
}
