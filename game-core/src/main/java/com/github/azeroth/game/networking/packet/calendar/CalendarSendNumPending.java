package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarSendNumPending extends ServerPacket {
    public int numPending;

    public CalendarSendNumPending(int numPending) {
        super(ServerOpcode.CalendarSendNumPending);
        numPending = numPending;
    }

    @Override
    public void write() {
        this.writeInt32(numPending);
    }
}
