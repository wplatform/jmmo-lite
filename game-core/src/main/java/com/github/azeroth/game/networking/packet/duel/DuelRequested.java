package com.github.azeroth.game.networking.packet.duel;


public class DuelRequested extends ServerPacket {
    public ObjectGuid arbiterGUID = ObjectGuid.EMPTY;
    public ObjectGuid requestedByGUID = ObjectGuid.EMPTY;
    public ObjectGuid requestedByWowAccount = ObjectGuid.EMPTY;

    public DuelRequested() {
        super(ServerOpcode.DuelRequested);
    }

    @Override
    public void write() {
        this.writeGuid(arbiterGUID);
        this.writeGuid(requestedByGUID);
        this.writeGuid(requestedByWowAccount);
    }
}
