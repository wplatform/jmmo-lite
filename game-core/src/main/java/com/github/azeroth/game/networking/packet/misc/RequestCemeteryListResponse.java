package com.github.azeroth.game.networking.packet.misc;


import java.util.ArrayList;


public class RequestCemeteryListResponse extends ServerPacket {
    public boolean isGossipTriggered;
    public ArrayList<Integer> cemeteryID = new ArrayList<>();

    public RequestCemeteryListResponse() {
        super(ServerOpcode.RequestCemeteryListResponse);
    }

    @Override
    public void write() {
        this.writeBit(isGossipTriggered);
        this.flushBits();

        this.writeInt32(cemeteryID.size());

        for (var cemetery : cemeteryID) {
            this.writeInt32(cemetery);
        }
    }
}
