package com.github.azeroth.game.entity.object.update;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public final class StablePetInfo extends UpdateMaskObject {

    private int petSlot;
    private int petNumber;
    private int creatureID;
    private int displayID;
    private int experienceLevel;
    private String name;
    private int petFlags;
    private int field_96;

    public StablePetInfo() {
        super(9);
    }

    @Override
    public void clearChangesMask() {

    }
}
