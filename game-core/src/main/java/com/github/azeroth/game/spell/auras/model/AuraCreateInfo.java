package com.github.azeroth.game.spell.auras.model;

import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.world.entities.object.ObjectGuid;
import com.github.azeroth.world.entities.object.WorldObject;

@Data
public
class AuraCreateInfo {


    ObjectGuid casterGUID;
    Unit caster;
    int baseAmount;
    ObjectGuid CastItemGUID;
    int castItemId = 0;
    int castItemLevel = -1;
    boolean isRefresh;
    boolean resetPeriodicTimer = true;

    ObjectGuid castId;
    SpellInfo spellInfo;
    Difficulty castDifficulty = Difficulty.DIFFICULTY_NONE;
    int _auraEffectMask = 0;
    WorldObject owner;

    int targetEffectMask = 0;
}
