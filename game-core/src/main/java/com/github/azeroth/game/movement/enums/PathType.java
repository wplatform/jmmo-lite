package com.github.azeroth.game.movement.enums;


import com.github.azeroth.common.EnumFlag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathType implements EnumFlag.FlagValue {
    BLANK(0x00),   // path not built yet
    NORMAL(0x01),   // normal path
    SHORTCUT(0x02),   // travel through obstacles, terrain, air, etc (old behavior)
    INCOMPLETE(0x04),   // we have partial path to follow - getting closer to target
    NOPATH(0x08),   // no valid path at all or error in generating one
    NOT_USING_PATH(0x10),   // used when we are either flying/swiming or on map w/o mmaps
    SHORT(0x20),   // path is longer or equal to its limited path length
    FARFROMPOLY_START(0x40),   // start position is far from the mmap poligon
    FARFROMPOLY_END(0x80),   // end positions is far from the mmap poligon
    FARFROMPOLY(FARFROMPOLY_START.value | FARFROMPOLY_END.value); // start or end positions are far from the mmap poligon

    public final int value;

}
