package com.github.azeroth.game.networking.packet.pet;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PetStableList extends ServerPacket {
    public ObjectGuid stableMaster = ObjectGuid.EMPTY;
    public ArrayList<PetStableInfo> pets = new ArrayList<>();

    public PetStableList() {
        super(ServerOpcode.PetStableList);
    }

    @Override
    public void write() {
        this.writeGuid(stableMaster);

        this.writeInt32(pets.size());

        for (var pet : pets) {
            this.writeInt32(pet.petSlot);
            this.writeInt32(pet.petNumber);
            this.writeInt32(pet.creatureID);
            this.writeInt32(pet.displayID);
            this.writeInt32(pet.experienceLevel);
            this.writeInt8((byte) pet.petFlags.getValue());
            this.writeBits(pet.petName.getBytes().length, 8);
            this.writeString(pet.petName);
        }
    }
}
