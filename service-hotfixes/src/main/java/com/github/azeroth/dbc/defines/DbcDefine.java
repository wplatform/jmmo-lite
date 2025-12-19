package com.github.azeroth.dbc.defines;

public interface DbcDefine {

    byte BG_BRACKET_ID_FIRST = 0;
    byte BG_BRACKET_ID_LAST = 11;

    // must be max value in PvPDificulty slot + 1
    byte MAX_BATTLEGROUND_BRACKETS = 12;

    int ACHIEVEMENT_CATEGORY_PET_BATTLES = 15117;

    int MAX_ARTIFACT_TIER = 1;

    int BATTLE_PET_SPECIES_MAX_ID = 2164;


    int CURVE_ID_ARTIFACT_RELIC_ITEM_LEVEL_BONUS = 1718;

    byte MAX_ITEM_PROTO_FLAGS = 4;

    byte MAX_ITEM_PROTO_SOCKETS = 3;

    byte MAX_ITEM_PROTO_STATS = 10;

    int ITEM_SET_FLAG_LEGACY_INACTIVE = 0x01;

    int PRESTIGE_FLAG_DISABLED = 0x01;                      // Prestige levels with this flag won't be included to calculate max prestigelevel.

    int CanFallbackToLearnedOnSkillLearn = 0x80;

    int MAX_SPELL_EFFECTS = 32;
    int MAX_EFFECT_MASK = 0xFFFFFFFF;

    byte MAX_SPELL_AURA_INTERRUPT_FLAGS = 2;

    byte MAX_POWERS_PER_SPELL = 4;

    byte MAX_TALENT_TIERS = 7;

    byte MAX_TALENT_COLUMNS = 3;

    byte MAX_PVP_TALENT_TIERS = 6;

    byte MAX_PVP_TALENT_COLUMNS = 3;

    byte MAX_SPELL_TOTEMS = 2;

    byte MAX_SPELL_REAGENTS = 8;

    int WORLD_MAP_TRANSFORMS_FLAG_DUNGEON = 0x04;


}
