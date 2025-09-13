package com.github.azeroth.game.entity.item.enums;

// -1 from client enchantment slot number
public enum EnchantmentSlot {
    PERM_ENCHANTMENT_SLOT,
    TEMP_ENCHANTMENT_SLOT,
    SOCK_ENCHANTMENT_SLOT,
    SOCK_ENCHANTMENT_SLOT_2,
    SOCK_ENCHANTMENT_SLOT_3,
    BONUS_ENCHANTMENT_SLOT,
    PRISMATIC_ENCHANTMENT_SLOT,                    // added at apply special permanent enchantment
    USE_ENCHANTMENT_SLOT,

    MAX_INSPECTED_ENCHANTMENT_SLOT,

    PROP_ENCHANTMENT_SLOT_0,                   // used with RandomSuffix
    PROP_ENCHANTMENT_SLOT_1,                   // used with RandomSuffix
    PROP_ENCHANTMENT_SLOT_2,                   // used with RandomSuffix and RandomProperty
    PROP_ENCHANTMENT_SLOT_3,                   // used with RandomProperty
    PROP_ENCHANTMENT_SLOT_4,                   // used with RandomProperty
    MAX_ENCHANTMENT_SLOT
}
