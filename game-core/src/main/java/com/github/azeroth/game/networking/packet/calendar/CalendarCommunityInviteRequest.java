package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CalendarCommunityInviteRequest extends ClientPacket {
    public long clubId;
    public byte minLevel = 1;
    public byte maxLevel = 100;
    public byte maxRankOrder;

    public CalendarCommunityInviteRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        clubId = this.readUInt64();
        minLevel = this.readUInt8();
        maxLevel = this.readUInt8();
        maxRankOrder = this.readUInt8();
    }
}
