package com.github.azeroth.game.networking.packet.pet;

import com.github.azeroth.game.networking.ServerPacket;

public class PetActionFeedbackPacket extends ServerPacket {
    public int spellID;
    public PetActionFeedback response = PetActionFeedback.values()[0];

    public PetActionFeedbackPacket() {
        super(ServerOpcode.PetStableResult);
    }

    @Override
    public void write() {
        this.writeInt32(spellID);
        this.writeInt8((byte) response.getValue());
    }
}
