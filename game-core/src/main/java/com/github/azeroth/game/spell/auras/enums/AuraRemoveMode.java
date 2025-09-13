package com.github.azeroth.game.spell.auras.enums;

public enum AuraRemoveMode {
    AURA_REMOVE_NONE,
    AURA_REMOVE_BY_DEFAULT,       // scripted remove, remove by stack with aura with different ids and sc aura remove
    AURA_REMOVE_BY_INTERRUPT,         // removed by aura interrupt flag
    AURA_REMOVE_BY_CANCEL,
    AURA_REMOVE_BY_ENEMY_SPELL,       // dispel and absorb aura destroy
    AURA_REMOVE_BY_EXPIRE,            // aura duration has ended
    AURA_REMOVE_BY_DEATH
}
