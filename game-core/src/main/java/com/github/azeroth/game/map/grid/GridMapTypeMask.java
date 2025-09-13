package com.github.azeroth.game.map.grid;

import com.github.azeroth.common.EnumFlag;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GridMapTypeMask implements EnumFlag.FlagValue {
    CORPSE(0x01),
    CREATURE(0x02),
    DYNAMIC_OBJECT(0x04),
    GAME_OBJECT(0x08),
    PLAYER(0x10),
    AREA_TRIGGER(0x20),
    SCENE_OBJECT(0x40),
    CONVERSATION(0x80),
    ALL(0xFF);
    public final int value;
}
