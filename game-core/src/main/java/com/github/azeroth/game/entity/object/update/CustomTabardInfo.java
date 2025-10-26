package com.github.azeroth.game.entity.object.update;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class CustomTabardInfo extends UpdateMaskObject {

    @ChangeMark(bit = 0)
    private int emblemStyle;
    @ChangeMark(bit = 1)
    private int emblemColor;
    @ChangeMark(bit = 2)
    private int borderStyle;
    @ChangeMark(bit = 3)
    private int borderColor;
    @ChangeMark(bit = 4)
    private int backgroundColor;

    public CustomTabardInfo() {
        super(5);
    }

    @Override
    public void clearChangesMask() {

    }
}
