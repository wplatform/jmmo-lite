package com.github.azeroth.game.networking.packet.petition;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class SignPetition extends ClientPacket {
    public ObjectGuid petitionGUID = ObjectGuid.EMPTY;
    public byte choice;

    public SignPetition(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        petitionGUID = this.readPackedGuid();
        choice = this.readUInt8();
    }
}
