package com.github.azeroth.game.networking.packet.petition;


public class TurnInPetitionResult extends ServerPacket {
    public PetitionTurns result = PetitionTurns.forValue(0); // PetitionError

    public TurnInPetitionResult() {
        super(ServerOpcode.TurnInPetitionResult);
    }

    @Override
    public void write() {
        this.writeBits(result, 4);
        this.flushBits();
    }
}
