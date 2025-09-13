package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.WorldPacket;

final class CalendarAddEventInviteInfo {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public byte status;
    public byte moderator;
    public ObjectGuid unused801_1 = null;
    public Long unused801_2 = null;
    public Long unused801_3 = null;

    public void read(WorldPacket data) {
        guid = data.readPackedGuid();
        status = data.readUInt8();
        moderator = data.readUInt8();

        var hasUnused801_1 = data.readBit();
        var hasUnused801_2 = data.readBit();
        var hasUnused801_3 = data.readBit();

        if (hasUnused801_1) {
            unused801_1 = data.readPackedGuid();
        }

        if (hasUnused801_2) {
            unused801_2 = data.readUInt64();
        }

        if (hasUnused801_3) {
            unused801_3 = data.readUInt64();
        }
    }

    public CalendarAddEventInviteInfo clone() {
        CalendarAddEventInviteInfo varCopy = new CalendarAddEventInviteInfo();

        varCopy.guid = this.guid;
        varCopy.status = this.status;
        varCopy.moderator = this.moderator;
        varCopy.unused801_1 = this.unused801_1;
        varCopy.unused801_2 = this.unused801_2;
        varCopy.unused801_3 = this.unused801_3;

        return varCopy;
    }
}
