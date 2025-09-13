package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarModeratorStatusQuery extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public long eventID;

    public long inviteID;

    public long moderatorID;

    public byte status;

    public CalendarModeratorStatusQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
        eventID = this.readUInt64();
        inviteID = this.readUInt64();
        moderatorID = this.readUInt64();
        status = this.readUInt8();
    }
}
