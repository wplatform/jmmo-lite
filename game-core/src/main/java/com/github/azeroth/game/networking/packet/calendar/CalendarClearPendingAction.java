package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ServerPacket;

public class CalendarClearPendingAction extends ServerPacket {
    public CalendarClearPendingAction() {
        super(ServerOpcode.CalendarClearPendingAction);
    }

    @Override
    public void write() {
    }
}
