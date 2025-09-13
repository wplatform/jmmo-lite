package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.domain.object.ObjectGuid;

import java.util.List;

public final class StableInfo extends UpdateMaskObject {

    private List<StablePetInfo> pets;
    private ObjectGuid stableMaster;

    public StableInfo() {
        super(3);
    }

    @Override
    public void clearChangesMask() {

    }
}
