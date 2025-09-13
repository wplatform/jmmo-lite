package com.github.azeroth.game.networking.packet.ticket;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GMTicketGetCaseStatus extends ClientPacket {
    public GMTicketGetCaseStatus(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
