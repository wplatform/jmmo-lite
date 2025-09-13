package com.github.azeroth.game.entity.item.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocketColor implements EnumFlag.FlagValue {
    META(0x00001),
    RED(0x00002),
    YELLOW(0x00004),
    BLUE(0x00008),
    HYDRAULIC(0x00010), // not used
    COGWHEEL(0x00020),
    PRISMATIC(0x0000E),
    RELIC_IRON(0x00040),
    RELIC_BLOOD(0x00080),
    RELIC_SHADOW(0x00100),
    RELIC_FEL(0x00200),
    RELIC_ARCANE(0x00400),
    RELIC_FROST(0x00800),
    RELIC_FIRE(0x01000),
    RELIC_WATER(0x02000),
    RELIC_LIFE(0x04000),
    RELIC_WIND(0x08000),
    RELIC_HOLY(0x10000);

    public final int value;
}
