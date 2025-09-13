package com.github.azeroth.game.networking.packet.battleground;


public class BattlefieldStatusNone extends ServerPacket {
    public Rideticket ticket = new rideTicket();

    public BattlefieldStatusNone() {
        super(ServerOpcode.BattlefieldStatusNone);
    }

    @Override
    public void write() {
        ticket.write(this);
    }
}
