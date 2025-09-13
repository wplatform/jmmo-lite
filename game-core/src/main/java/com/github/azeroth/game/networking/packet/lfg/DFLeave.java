package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DFLeave extends ClientPacket {
    public Rideticket ticket = new rideTicket();

    public DFLeave(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ticket.read(this);
    }
}
