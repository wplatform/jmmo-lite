package com.github.azeroth.defines;

public enum SpellEffIndex {
    EFFECT_0, EFFECT_1, EFFECT_2, EFFECT_3, EFFECT_4, EFFECT_5, EFFECT_6, EFFECT_7, EFFECT_8, EFFECT_9, EFFECT_10,
    EFFECT_11, EFFECT_12, EFFECT_13, EFFECT_14, EFFECT_15, EFFECT_16, EFFECT_17, EFFECT_18, EFFECT_19, EFFECT_20,
    EFFECT_21, EFFECT_22, EFFECT_23, EFFECT_24, EFFECT_25, EFFECT_26, EFFECT_27, EFFECT_28, EFFECT_29, EFFECT_30,
    EFFECT_31;


    public static SpellEffIndex valueOf(int index) {
        return valueOf("EFFECT_" + index);
    }
}
