package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ServerPacket;

public class PetActionSound extends ServerPacket {
    public ObjectGuid unitGUID = ObjectGuid.EMPTY;
    public PetTalk action = PetTalk.values()[0];

    public PetActionSound() {
        super(ServerOpcode.PetStableResult);
    }

    @Override
    public void write() {
        this.writeGuid(unitGUID);
        this.writeInt32((int) action.getValue());
    }
}
