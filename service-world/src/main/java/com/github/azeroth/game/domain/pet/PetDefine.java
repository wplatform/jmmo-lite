package com.github.azeroth.game.domain.pet;

public interface PetDefine {

    short MAX_ACTIVE_PETS = 5;
    short MAX_PET_STABLES = 200;


    int CALL_PET_SPELL_ID = 883;
    int PET_SUMMONING_DISORIENTATION = 32752;


    float PET_FOLLOW_DIST = 1.0f;
    float PET_FOLLOW_ANGLE = (float) Math.PI;


    short PET_SAVE_AS_DELETED = -2;                        // not saved in fact
    short PET_SAVE_AS_CURRENT = -3;                        // in current slot (with player)
    short PET_SAVE_FIRST_ACTIVE_SLOT = 0;
    short PET_SAVE_LAST_ACTIVE_SLOT = PET_SAVE_FIRST_ACTIVE_SLOT + MAX_ACTIVE_PETS;
    short PET_SAVE_FIRST_STABLE_SLOT = 5;
    short PET_SAVE_LAST_STABLE_SLOT = PET_SAVE_FIRST_STABLE_SLOT + MAX_PET_STABLES; // last in DB stable slot index
    short PET_SAVE_NOT_IN_SLOT = -1;                        // for avoid conflict with stable size grow will use negative value

    static boolean isActivePetSlot(short slot) {
        return slot >= PET_SAVE_FIRST_ACTIVE_SLOT && slot < PET_SAVE_LAST_ACTIVE_SLOT;
    }

    static boolean isStabledPetSlot(short slot) {
        return slot >= PET_SAVE_FIRST_STABLE_SLOT && slot < PET_SAVE_LAST_STABLE_SLOT;
    }

}
