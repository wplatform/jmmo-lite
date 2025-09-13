package com.github.azeroth.game.spell.enums;

import lombok.RequiredArgsConstructor;

// only used in code
@RequiredArgsConstructor
public enum SpellCategories {
    SPELLCATEGORY_HEALTH_MANA_POTIONS(4),
    SPELLCATEGORY_DEVOUR_MAGIC(12),
    SPELLCATEGORY_JUDGEMENT(1210),               // judgement (seal trigger)
    SPELLCATEGORY_FOOD(11),
    SPELLCATEGORY_DRINK(59);
    private final int value;
}
