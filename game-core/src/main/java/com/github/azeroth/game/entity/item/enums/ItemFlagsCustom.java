package com.github.azeroth.game.entity.item.enums;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ItemFlagsCustom implements EnumFlag.FlagValue {
    CU_UNUSED(0x0001),
    CU_IGNORE_QUEST_STATUS(0x0002),   // No quest status will be checked when this item drops
    CU_FOLLOW_LOOT_RULES(0x0004);    // Item will always follow group/master/need before greed looting rules

    public final int value;
}
