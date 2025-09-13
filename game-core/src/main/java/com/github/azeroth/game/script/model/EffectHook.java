package com.github.azeroth.game.script.model;

import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.defines.SpellEffectName;
import com.github.azeroth.game.spell.SpellEffectInfo;
import com.github.azeroth.game.spell.SpellInfo;
import com.github.azeroth.game.spell.auras.enums.AuraType;
import com.github.azeroth.utils.StringUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class EffectHook<F, T> {

    protected final SpellEffIndex effIndex;
    protected final T checkType;
    protected final F function;

    public final boolean checkEffect(SpellInfo spellInfo, SpellEffIndex effIndex) {
        if (spellInfo.getEffects().size() <= effIndex.ordinal())
            return false;
        SpellEffectInfo spellEffectInfo = spellInfo.getEffect(effIndex);
        if (spellEffectInfo.getApplyAuraName() == null && checkType == null)
            return true;
        if (spellEffectInfo.getApplyAuraName() == null)
            return false;

        if (checkType instanceof AuraType type) {
            return (type == AuraType.SPELL_AURA_ANY) || (spellEffectInfo.getApplyAuraName() == type);
        } else if (checkType instanceof SpellEffectName name) {
            return (name == SpellEffectName.SPELL_EFFECT_ANY) || (spellEffectInfo.getEffect() == name);
        } else {
            throw new IllegalArgumentException("auraType must be AuraType or SpellEffectName");
        }
    }


    public final int getAffectedEffectsMask(SpellInfo spellInfo) {
        int mask = 0;
        if (effIndex == SpellEffIndex.EFFECT_ALL || effIndex == SpellEffIndex.EFFECT_FIRST_FOUND) {
            for (SpellEffectInfo effect : spellInfo.getEffects()) {
                if (effIndex == SpellEffIndex.EFFECT_FIRST_FOUND && mask != 0) {
                    return mask;
                }
                if (checkEffect(spellInfo, effect.effectIndex))
                    mask |= 1 << effect.effectIndex.ordinal();
            }
        } else {
            if (checkEffect(spellInfo, effIndex))
                mask |= 1 << effIndex.ordinal();
        }
        return mask;
    }

    @Override
    public String toString() {
        if(checkType instanceof AuraType auraType) {
            if (auraType == AuraType.SPELL_AURA_ANY) {
                return StringUtil.format("Index: {}, AuraName: SPELL_AURA_ANY", effIndex);
            }
            return StringUtil.format("Index: {}, AuraName: SPELL_AURA_{}", effIndex, auraType);
        } else if (checkType instanceof SpellEffectName effName) {
            if (effName == SpellEffectName.SPELL_EFFECT_ANY) {
                return StringUtil.format("Index: {}, Effect: SPELL_EFFECT_ANY", effIndex);
            }
            return StringUtil.format("Index: {}, Effect: {}", effIndex, effName);
        } else {
            throw new IllegalArgumentException("auraType must be AuraType or SpellEffectName");
        }
    }
}
