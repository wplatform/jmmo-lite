package com.github.azeroth.game.spell;

import com.badlogic.gdx.utils.IntFloatMap;
import com.github.azeroth.game.entity.object.WorldObject;

public class SpellValue {
    public IntFloatMap effectBasePoints = new IntFloatMap(16);


    public int customBasePointsMask;

    public int maxAffectedTargets;
    public float radiusMod;
    public int auraStackAmount;
    public float durationMul;
    public float criticalChance;
    public Integer duration = null;
    public Float summonDuration = null;

    public SpellValue(SpellInfo proto, WorldObject caster) {
        for (var spellEffectInfo : proto.getEffects()) {
            effectBasePoints.put(spellEffectInfo.effectIndex, spellEffectInfo.calcBaseValue(caster, null, 0, -1));
        }

        customBasePointsMask = 0;
        maxAffectedTargets = proto.getMaxAffectedTargets();
        radiusMod = 1.0f;
        auraStackAmount = 1;
        criticalChance = 0.0f;
        durationMul = 1;
    }
}
