package com.github.azeroth.defines;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SpellSchoolMask implements EnumFlag.FlagValue {
    NONE(0x00),
    NORMAL(1 << SpellSchool.NORMAL.ordinal()),
    HOLY(1 << SpellSchool.HOLY.ordinal()),
    FIRE(1 << SpellSchool.FIRE.ordinal()),
    NATURE(1 << SpellSchool.NATURE.ordinal()),
    FROST(1 << SpellSchool.FROST.ordinal()),
    SHADOW(1 << SpellSchool.SHADOW.ordinal()),
    ARCANE(1 << SpellSchool.ARCANE.ordinal()),
    SPELL(FIRE.value | NATURE.value | FROST.value | SHADOW.value | ARCANE.value),
    MAGIC(HOLY.value | SPELL.value), ALL(NORMAL.value | MAGIC.value);

    public final int value;


    public static SpellSchoolMask valueOf(SpellSchool school) {
        return switch (school) {
            case NORMAL -> SpellSchoolMask.NORMAL;
            case HOLY -> SpellSchoolMask.HOLY;
            case FIRE -> SpellSchoolMask.FIRE;
            case NATURE -> SpellSchoolMask.NATURE;
            case FROST -> SpellSchoolMask.FROST;
            case SHADOW -> SpellSchoolMask.SHADOW;
            case ARCANE -> SpellSchoolMask.ARCANE;
        };
    }

}
