package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class Ping extends ClientPacket {
    public int serial;
    public int latency;

    public Ping(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        serial = this.readUInt32();
        latency = this.readUInt32();
    }
}

//Structs

