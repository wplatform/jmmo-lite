package com.github.azeroth.game.networking.packet.petition;


public class PetitionSignResults extends ServerPacket {
    public ObjectGuid item = ObjectGuid.EMPTY;
    public ObjectGuid player = ObjectGuid.EMPTY;
    public PetitionSigns error = PetitionSigns.forValue(0);

    public PetitionSignResults() {
        super(ServerOpcode.PetitionSignResults);
    }

    @Override
    public void write() {
        this.writeGuid(item);
        this.writeGuid(player);

        this.writeBits(error, 4);
        this.flushBits();
    }
}
