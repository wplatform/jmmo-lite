package com.github.azeroth.game.networking.packet.misc;


public class RandomRoll extends ServerPacket {
    public ObjectGuid roller = ObjectGuid.EMPTY;
    public ObjectGuid rollerWowAccount = ObjectGuid.EMPTY;
    public int min;
    public int max;
    public int result;

    public RandomRoll() {
        super(ServerOpcode.RandomRoll);
    }

    @Override
    public void write() {
        this.writeGuid(roller);
        this.writeGuid(rollerWowAccount);
        this.writeInt32(min);
        this.writeInt32(max);
        this.writeInt32(result);
    }
}
