package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ServerPacket;

public class CalendarCommandResult extends ServerPacket {

    public byte command;
    public CalendarError result = CalendarError.values()[0];
    public String name;

    public CalendarCommandResult() {
        super(ServerOpcode.CalendarCommandResult);
    }


    public CalendarCommandResult(byte command, CalendarError result, String name) {
        super(ServerOpcode.CalendarCommandResult);
        command = command;
        result = result;
        name = name;
    }

    @Override
    public void write() {
        this.writeInt8(command);
        this.writeInt8((byte) result.getValue());

        this.writeBits(name.getBytes().length, 9);
        this.flushBits();
        this.writeString(name);
    }
}
