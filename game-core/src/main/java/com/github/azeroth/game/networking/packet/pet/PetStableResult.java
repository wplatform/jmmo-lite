package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ServerPacket;

public class PetStableResult extends ServerPacket {
    public Stableresult result = StableResult.values()[0];

    public PetStableResult() {
        super(ServerOpcode.PetStableResult);
    }

    @Override
    public void write() {
        this.writeInt8((byte) result.getValue());
    }
}
