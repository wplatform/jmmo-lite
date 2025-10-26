package com.github.azeroth.game.domain.pet;

import java.util.ArrayList;
import java.util.List;

public class PetStable {

    static final int UnslottedPetIndexMask = 0x80000000;


    Integer currentPetIndex;                               // index into ActivePets or UnslottedPets if highest bit is set
    PetInfo[] activePets = new PetInfo[PetDefine.MAX_ACTIVE_PETS];      // PET_SAVE_FIRST_ACTIVE_SLOT - PET_SAVE_LAST_ACTIVE_SLOT
    PetInfo[] stabledPets = new PetInfo[PetDefine.MAX_PET_STABLES];     // PET_SAVE_FIRST_STABLE_SLOT - PET_SAVE_LAST_STABLE_SLOT
    List<PetInfo> unslottedPets = new ArrayList<>();// PET_SAVE_NOT_IN_SLOT


    public PetInfo GetCurrentPet() {
        if (currentPetIndex == null)
            return null;

        Integer activePetIndex = GetCurrentActivePetIndex();
        if (activePetIndex != null)
            return activePets[activePetIndex];

        Integer unslottedPetIndex = GetCurrentUnslottedPetIndex();
        if (unslottedPetIndex != null)
            return unslottedPetIndex < unslottedPets.size() ? unslottedPets.get(unslottedPetIndex) : null;

        return null;
    }

    public Integer GetCurrentActivePetIndex() {
        return currentPetIndex != null && ((currentPetIndex & UnslottedPetIndexMask) == 0) ? currentPetIndex : null;
    }

    public void SetCurrentActivePetIndex(Integer index) {
        currentPetIndex = index;
    }

    public Integer GetCurrentUnslottedPetIndex() {
        return currentPetIndex != null && ((currentPetIndex & UnslottedPetIndexMask) != 0) ? currentPetIndex & ~UnslottedPetIndexMask : null;
    }

    public void setCurrentUnslottedPetIndex(int index) {
        currentPetIndex = index | UnslottedPetIndexMask;
    }

}
