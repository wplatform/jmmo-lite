package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ServerPacket;

public class SetPetSpecialization extends ServerPacket {
    public short specID;

    public SetPetSpecialization() {
        super(ServerOpcode.SetPetSpecialization);
    }

    @Override
    public void write() {
        this.writeInt16(specID);
    }
}
