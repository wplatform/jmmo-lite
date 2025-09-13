package com.github.azeroth.game.entity.item.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum ItemFlag {
    NO_PICKUP(0x00000001),
    CONJURED(0x00000002), // Conjured item
    HAS_LOOT(0x00000004), // Item can be right clicked to open for loot
    HEROIC_TOOLTIP(0x00000008), // Makes green "Heroic" text appear on item
    DEPRECATED(0x00000010), // Cannot equip or use
    NO_USER_DESTROY(0x00000020), // Item can not be destroyed, except by using spell (item can be reagent for spell)
    PLAYERCAST(0x00000040), // Item's spells are castable by players
    NO_EQUIP_COOLDOWN(0x00000080), // No default 30 seconds cooldown when equipped
    LEGACY(0x00000100), // Effects are disabled
    IS_WRAPPER(0x00000200), // Item can wrap other items
    USES_RESOURCES(0x00000400),
    MULTI_DROP(0x00000800), // Looting this item does not remove it from available loot
    ITEM_PURCHASE_RECORD(0x00001000), // Item can be returned to vendor for its original cost (extended cost)
    PETITION(0x00002000), // Item is guild or arena charter
    HAS_TEXT(0x00004000), // Only readable items have this (but not all)
    NO_DISENCHANT(0x00008000),
    REAL_DURATION(0x00010000),
    NO_CREATOR(0x00020000),
    IS_PROSPECTABLE(0x00040000), // Item can be prospected
    UNIQUE_EQUIPPABLE(0x00080000), // You can only equip one of these
    DISABLE_AUTO_QUOTES(0x00100000), // Disables quotes around item description in tooltip
    IGNORE_DEFAULT_ARENA_RESTRICTIONS(0x00200000), // Item can be used during arena match
    NO_DURABILITY_LOSS(0x00400000), // Some Thrown weapons have it (and only Thrown) but not all
    USE_WHEN_SHAPESHIFTED(0x00800000), // Item can be used in shapeshift forms
    HAS_QUEST_GLOW(0x01000000),
    HIDE_UNUSABLE_RECIPE(0x02000000), // Profession recipes: can only be looted if you meet requirements and don't already know it
    NOT_USEABLE_IN_ARENA(0x04000000), // Item cannot be used in arena
    IS_BOUND_TO_ACCOUNT(0x08000000), // Item binds to account and can be sent only to your own character
    NO_REAGENT_COST(0x10000000), // Spell is cast ignoring reagents
    IS_MILLABLE(0x20000000), // Item can be milled
    REPORT_TO_GUILD_CHAT(0x40000000),
    NO_PROGRESSIVE_LOOT(0x80000000);

    public final int value;
}
