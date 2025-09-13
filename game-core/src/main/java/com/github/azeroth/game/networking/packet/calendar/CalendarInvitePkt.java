package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarInvitePkt extends ClientPacket {
    public long moderatorID;
    public boolean isSignUp;
    public boolean creating = true;
    public long eventID;
    public long clubID;
    public String name;

    public CalendarInvitePkt(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        eventID = this.readUInt64();
        moderatorID = this.readUInt64();
        clubID = this.readUInt64();

        var nameLen = this.<SHORT>readBit(9);
        creating = this.readBit();
        isSignUp = this.readBit();

        name = this.readString(nameLen);
    }
}
