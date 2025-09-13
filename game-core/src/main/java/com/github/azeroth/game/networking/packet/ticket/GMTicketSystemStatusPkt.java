package com.github.azeroth.game.networking.packet.ticket;


public class GMTicketSystemStatusPkt extends ServerPacket {
    public int status;

    public GMTicketSystemStatusPkt() {
        super(ServerOpcode.GmTicketSystemStatus);
    }

    @Override
    public void write() {
        this.writeInt32(status);
    }
}
