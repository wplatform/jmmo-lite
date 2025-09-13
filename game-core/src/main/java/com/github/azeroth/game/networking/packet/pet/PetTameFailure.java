package com.github.azeroth.game.networking.packet.pet;


import com.github.azeroth.game.networking.ServerPacket;

public class PetTameFailure extends ServerPacket {

    public byte result;

    public PetTameFailure() {
        super(ServerOpcode.PetTameFailure);
    }

    @Override
    public void write() {
        this.writeInt8(result);
    }
}
