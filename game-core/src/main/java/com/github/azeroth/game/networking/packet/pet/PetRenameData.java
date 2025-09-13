package com.github.azeroth.game.networking.packet.pet;

final class PetRenameData {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;
    public int petNumber;
    public String newName;
    public boolean hasDeclinedNames;
    public DeclinedName declinedNames;

    public PetRenameData clone() {
        PetRenameData varCopy = new petRenameData();

        varCopy.petGUID = this.petGUID;
        varCopy.petNumber = this.petNumber;
        varCopy.newName = this.newName;
        varCopy.hasDeclinedNames = this.hasDeclinedNames;
        varCopy.declinedNames = this.declinedNames;

        return varCopy;
    }
}
