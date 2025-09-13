package com.github.azeroth.game.networking.packet.who;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class WhoRequestPkt extends ClientPacket {
    public Whorequest request = new whoRequest();
    public int requestID;
    public ArrayList<Integer> areas = new ArrayList<>();

    public WhoRequestPkt(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var areasCount = this.<Integer>readBit(4);

        request.read(this);
        requestID = this.readUInt32();

        for (var i = 0; i < areasCount; ++i) {
            areas.add(this.readInt32());
        }
    }
}
