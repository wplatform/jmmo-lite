package com.github.azeroth.game.spell;

public final class SpellMisc {
    // Alternate names for this second

    public int talentId;

    
    public int spellId;

    
    public int specializationId;

    // SPELL_EFFECT_SET_FOLLOWER_QUALITY
    // SPELL_EFFECT_INCREASE_FOLLOWER_ITEM_LEVEL
    // SPELL_EFFECT_INCREASE_FOLLOWER_EXPERIENCE
    // SPELL_EFFECT_RANDOMIZE_FOLLOWER_ABILITIES
    // SPELL_EFFECT_LEARN_FOLLOWER_ABILITY

    public int followerId;

    
    public int followerAbilityId; // only SPELL_EFFECT_LEARN_FOLLOWER_ABILITY

    // SPELL_EFFECT_FINISH_GARRISON_MISSION

    public int garrMissionId;

    // SPELL_EFFECT_UPGRADE_HEIRLOOM

    public int itemId;

    
    public int data0;

    
    public int data1;


    public int[] getRawData() {
        return new int[]{data0, data1};
    }

}
