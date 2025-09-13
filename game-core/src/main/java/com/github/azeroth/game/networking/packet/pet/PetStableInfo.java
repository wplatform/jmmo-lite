package com.github.azeroth.game.networking.packet.pet;


final class PetStableInfo {

    public int petSlot;

    public int petNumber;

    public int creatureID;

    public int displayID;

    public int experienceLevel;
    public PetStableinfo petFlags = PetStableinfo.values()[0];
    public String petName;

    public PetStableInfo clone() {
        PetStableInfo varCopy = new PetStableInfo();

        varCopy.petSlot = this.petSlot;
        varCopy.petNumber = this.petNumber;
        varCopy.creatureID = this.creatureID;
        varCopy.displayID = this.displayID;
        varCopy.experienceLevel = this.experienceLevel;
        varCopy.petFlags = this.petFlags;
        varCopy.petName = this.petName;

        return varCopy;
    }
}
