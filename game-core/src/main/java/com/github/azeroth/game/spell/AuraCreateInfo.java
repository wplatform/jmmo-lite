package com.github.azeroth.game.spell;


import com.github.azeroth.dbc.defines.Difficulty;
import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.object.WorldObject;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.packet.spell.SpellCastVisual;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;

@Data
public class AuraCreateInfo {
    public ObjectGuid casterGuid;
    public Unit caster;
    public int baseAmount;
    public ObjectGuid castItemGuid = ObjectGuid.EMPTY;
    public int castItemId = 0;
    public int castItemLevel = -1;
    public boolean isRefresh;
    public int stackAmount = 1;
    public boolean periodicReset = true;
    public int auraEffectMask;

    public ObjectGuid castId;
    public SpellInfo spellInfo;
    public Difficulty castDifficulty;
    public WorldObject ower;

    public SpellCastVisual castVisual;

    public int targetEffectMask;


    public AuraCreateInfo(ObjectGuid castId, SpellInfo spellInfo, Difficulty castDifficulty, int auraEffMask, WorldObject owner) {
        this.castId = castId;
        this.spellInfo = spellInfo;
        this.castDifficulty = castDifficulty;
        this.auraEffectMask = auraEffMask;
        this.ower = owner;
    }


    public final void setCastItem(ObjectGuid guid, int itemId, int itemLevel) {
        castItemGuid = guid;
        castItemId = itemId;
        castItemLevel = itemLevel;
    }

}
