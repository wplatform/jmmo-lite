package com.github.azeroth.game.networking.packet.petition;


public class QueryPetitionResponse extends ServerPacket {
    public int petitionID = 0;
    public boolean allow = false;
    public Petitioninfo info;

    public QueryPetitionResponse() {
        super(ServerOpcode.QueryPetitionResponse);
    }

    @Override
    public void write() {
        this.writeInt32(petitionID);
        this.writeBit(allow);
        this.flushBits();

        if (allow) {
            info.write(this);
        }
    }
}
