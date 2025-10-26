package com.github.azeroth.game.domain.unit;

public enum UnitMod {
    STAT_STRENGTH,                                 // STAT_STRENGTH..STAT_INTELLECT must be in existed order, it's accessed by index values of Stats enum.
    STAT_AGILITY,
    STAT_STAMINA,
    STAT_INTELLECT,
    HEALTH,
    MANA,                                          // MANA..PAIN must be listed in existing order, it is accessed by index values of Powers enum.
    RAGE,
    FOCUS,
    ENERGY,
    COMBO_POINTS,
    RUNES,
    RUNIC_POWER,
    SOUL_SHARDS,
    LUNAR_POWER,
    HOLY_POWER,
    ALTERNATE,
    MAELSTROM,
    CHI,
    INSANITY,
    BURNING_EMBERS,
    DEMONIC_FURY,
    ARCANE_CHARGES,
    FURY,
    PAIN,
    ESSENCE,
    ARMOR,                                         // ARMOR..RESISTANCE_ARCANE must be in existed order, it's accessed by index values of SpellSchool enum.
    RESISTANCE_HOLY,
    RESISTANCE_FIRE,
    RESISTANCE_NATURE,
    RESISTANCE_FROST,
    RESISTANCE_SHADOW,
    RESISTANCE_ARCANE,
    ATTACK_POWER,
    ATTACK_POWER_RANGED,
    DAMAGE_MAINHAND,
    DAMAGE_OFFHAND,
    DAMAGE_RANGED
}
