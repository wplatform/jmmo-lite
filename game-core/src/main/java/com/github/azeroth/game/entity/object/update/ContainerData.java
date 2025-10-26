package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public final class ContainerData extends UpdateMaskObject {

    @ChangeMark(blockBit = 0, bit = 1)
    private int numSlots;

    @ChangeMark(size = 36, bit = 2, firstElementBit = 3)
    private List<ObjectGuid> slots;

    public ContainerData(int changeMask) {
        super(39);
    }

    @Override
    public void clearChangesMask() {

    }
}
