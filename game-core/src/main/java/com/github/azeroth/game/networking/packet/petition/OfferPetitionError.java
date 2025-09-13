package com.github.azeroth.game.networking.packet.petition;


import com.github.azeroth.game.networking.ServerPacket;

public class OfferPetitionError extends ServerPacket {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;

    public OfferPetitionError() {
        super(ServerOpcode.OfferPetitionError);
    }

    @Override
    public void write() {
        this.writeGuid(playerGUID);
    }
}
