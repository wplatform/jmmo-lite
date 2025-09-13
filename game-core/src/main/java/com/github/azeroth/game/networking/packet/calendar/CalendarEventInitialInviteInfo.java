package com.github.azeroth.game.networking.packet.calendar;

class CalendarEventInitialInviteInfo {
    public ObjectGuid inviteGuid = ObjectGuid.EMPTY;
    public byte level = 100;

    public CalendarEventInitialInviteInfo(ObjectGuid inviteGuid, byte level) {
        inviteGuid = inviteGuid;
        level = level;
    }
}
