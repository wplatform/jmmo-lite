package com.github.azeroth.game.entity.player.enums;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public
enum ActionButtonType {
    SPELL(0x00),
    C(0x01),                         // click?
    EQSET(0x20),
    DROPDOWN(0x30),
    MACRO(0x40),
    CMACRO(C.value | MACRO.value),
    COMPANION(0x50),
    MOUNT(0x60),
    ITEM(0x80);
    public final int value;

    public static ActionButtonType valueOf(int value) {
        return Arrays.stream(ActionButtonType.values()).filter(e -> e.value == value).findFirst().get();
    }
}
