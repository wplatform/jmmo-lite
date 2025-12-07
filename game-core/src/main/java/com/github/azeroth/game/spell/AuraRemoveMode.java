package com.github.azeroth.game.spell;

public enum AuraRemoveMode {
    NONE,
    BY_DEFAULT,       // scripted remove, remove by stack with aura with different ids and sc aura remove
    BY_INTERRUPT,         // removed by aura interrupt flag
    BY_CANCEL,
    BY_ENEMY_SPELL,       // dispel and absorb aura destroy
    BY_EXPIRE,            // aura duration has ended
    BY_DEATH

}
