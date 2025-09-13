package com.github.azeroth.game.networking.packet.battlepet;


import com.github.azeroth.game.networking.WorldPacket;

public class BattlePetSlot {
    public BattlepetStruct pet = new battlePetStruct();
    public int collarID;
    public byte index;
    public boolean locked = true;

    public final void write(WorldPacket data) {
        data.writeGuid(pet.guid.isEmpty() ? ObjectGuid.create(HighGuid.BattlePet, 0) : pet.guid);
        data.writeInt32(collarID);
        data.writeInt8(index);
        data.writeBit(locked);
        data.flushBits();
    }
}
