package com.github.azeroth.game.spell;

public enum AuraRemoveMode {
    NONE,
    DEFAULT,       // scripted remove, remove by stack with aura with different ids and sc aura remove
    INTERRUPT,         // removed by aura interrupt flag
    CANCEL,
    ENEMY_SPELL,       // dispel and absorb aura destroy
    EXPIRE,            // aura duration has ended
    DEATH

}
