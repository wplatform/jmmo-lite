package com.github.azeroth.game.networking.packet.duel;


public class CanDuelResult extends ServerPacket {
    public ObjectGuid targetGUID = ObjectGuid.EMPTY;
    public boolean result;

    public CanDuelResult() {
        super(ServerOpcode.CanDuelResult);
    }

    @Override
    public void write() {
        this.writeGuid(targetGUID);
        this.writeBit(result);
        this.flushBits();
    }
}
