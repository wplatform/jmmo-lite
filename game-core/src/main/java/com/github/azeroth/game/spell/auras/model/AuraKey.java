package com.github.azeroth.game.spell.auras.model;

import com.github.azeroth.world.entities.object.ObjectGuid;

// Structure representing database aura primary key fields
@Data
public
class AuraKey {
    ObjectGuid caster;
    ObjectGuid item;
    int spellId;
    int effectMask;
}
