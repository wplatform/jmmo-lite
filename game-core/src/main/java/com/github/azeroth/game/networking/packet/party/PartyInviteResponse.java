package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class PartyInviteResponse extends ClientPacket {
    public byte partyIndex;
    public boolean accept;
    public Integer rolesDesired = null;

    public PartyInviteResponse(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        partyIndex = this.readUInt8();

        accept = this.readBit();

        var hasRolesDesired = this.readBit();

        if (hasRolesDesired) {
            rolesDesired = this.readUInt32();
        }
    }
}
