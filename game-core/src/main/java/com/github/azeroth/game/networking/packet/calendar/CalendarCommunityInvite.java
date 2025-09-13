package com.github.azeroth.game.networking.packet.calendar;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class CalendarCommunityInvite extends ServerPacket {
    public ArrayList<CalendarEventInitialInviteInfo> invites = new ArrayList<>();

    public CalendarCommunityInvite() {
        super(ServerOpcode.CalendarCommunityInvite);
    }

    @Override
    public void write() {
        this.writeInt32(invites.size());

        for (var invite : invites) {
            this.writeGuid(invite.inviteGuid);
            this.writeInt8(invite.level);
        }
    }
}
