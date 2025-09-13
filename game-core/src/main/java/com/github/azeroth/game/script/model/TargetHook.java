package com.github.azeroth.game.script.model;

import com.github.azeroth.defines.SpellEffIndex;
import com.github.azeroth.defines.Target;
import com.github.azeroth.utils.StringUtil;
import lombok.Getter;

@Getter
public class TargetHook<F> extends EffectHook<F, Void> {

    private final Target targetType;
    private final boolean area;
    private final boolean dest;

    public TargetHook(SpellEffIndex effIndex, F function, Target targetType, boolean area, boolean dest) {
        super(effIndex, null, function);
        this.targetType = targetType;
        this.area = area;
        this.dest = dest;
    }


    @Override
    public String toString() {
        return StringUtil.format("Index: {} Target: {}", effIndex, targetType);
    }
}
