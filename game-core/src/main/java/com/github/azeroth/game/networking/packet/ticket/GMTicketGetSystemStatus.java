package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GMTicketGetSystemStatus extends ClientPacket {
    public GMTicketGetSystemStatus(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}

//Structs

