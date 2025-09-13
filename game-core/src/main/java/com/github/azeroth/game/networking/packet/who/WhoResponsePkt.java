package com.github.azeroth.game.networking.packet.who;


import java.util.ArrayList;


public class WhoResponsePkt extends ServerPacket {
    public int requestID;
    public ArrayList<WhoEntry> response = new ArrayList<>();

    public WhoResponsePkt() {
        super(ServerOpcode.Who);
    }

    @Override
    public void write() {
        this.writeInt32(requestID);
        this.writeBits(response.size(), 6);
        this.flushBits();

        response.forEach(p -> p.write(this));
    }
}
