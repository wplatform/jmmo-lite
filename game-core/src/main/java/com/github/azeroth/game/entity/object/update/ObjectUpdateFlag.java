package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ObjectUpdateFlag implements EnumFlag.FlagValue {
    NONE                  (0x0000),
    SELF                  (0x0001),
    TRANSPORT             (0x0002),
    HAS_TARGET            (0x0004),
    LIVING                (0x0008),
    STATIONARY_POSITION   (0x0010),
    VEHICLE               (0x0020),
    TRANSPORT_POSITION    (0x0040),
    ROTATION              (0x0080),
    ANIM_KITS             (0x0100),
    AREA_TRIGGER          (0x0200),
    GAME_OBJECT           (0x0400),
    //REPLACE_ACTIVE        (0x0800),
    //NO_BIRTH_ANIM         (0x1000),
    //ENABLE_PORTALS        (0x2000),
    //PLAY_HOVER_ANIM       (0x4000),
    //IS_SUPPRESSING_GREETINGS (0x8000),
    SCENE_OBJECT           (0x10000);
    //SCENE_PENDING_INSTANCE (0x20000)

    private final int value;
}
