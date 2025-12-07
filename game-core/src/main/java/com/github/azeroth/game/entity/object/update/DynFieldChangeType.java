package com.github.azeroth.game.entity.object.update;

import lombok.Getter;

@Getter
public enum DynFieldChangeType {
    UNCHANGED(0),
    VALUE_CHANGED(0x7FFF),
    VALUE_AND_SIZE_CHANGED(0x8000);

    public final short value;

    DynFieldChangeType(int value) {
        this.value = (short) value;
    }

}
