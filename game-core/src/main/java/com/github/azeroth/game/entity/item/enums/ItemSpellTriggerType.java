package com.github.azeroth.game.entity.item.enums;

public enum ItemSpellTriggerType {
    ON_USE,                  // use after equip cooldown
    ON_EQUIP,
    ON_PROC,
    SUMMONED_BY_SPELL,
    ON_DEATH,
    ON_PICKUP,
    ON_LEARN,                  // used in ItemEffect in second slot with spell_id with SPELL_GENERIC_LEARN in spell_1
    ON_LOOTED,
}
