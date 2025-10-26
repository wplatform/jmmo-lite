package com.github.azeroth.game.entity.object.update;

import java.util.List;

public final class DeclinedNames extends UpdateMaskObject {
    @ChangeMark(size = 5, blockBit = 0, bit = 1, type = FieldType.ARRAY)
    private List<String> declinedNames;

    DeclinedNames() {
        super(6);
    }

    @Override
    public void clearChangesMask() {

    }
}
