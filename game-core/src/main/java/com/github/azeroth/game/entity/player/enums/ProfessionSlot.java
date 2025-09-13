package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public
enum ProfessionSlot {
    PROFESSION1_TOOL(19),
    PROFESSION1_GEAR1(20),
    PROFESSION1_GEAR2(21),
    PROFESSION2_TOOL(22),
    PROFESSION2_GEAR1(23),
    PROFESSION2_GEAR2(24),
    COOKING_TOOL(25),
    COOKING_GEAR1(26),
    FISHING_TOOL(27),
    FISHING_GEAR1(28),
    FISHING_GEAR2(29);

    public final int index;

}
