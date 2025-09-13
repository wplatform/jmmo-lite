package com.github.azeroth.game.networking.packet.who;

import com.github.azeroth.game.networking.WorldPacket;

public final class WhoRequestServerInfo {
    public int factionGroup;
    public int locale;
    public int requesterVirtualRealmAddress;

    public void read(WorldPacket data) {
        factionGroup = data.readInt32();
        locale = data.readInt32();
        requesterVirtualRealmAddress = data.readUInt32();
    }

    public WhoRequestServerInfo clone() {
        WhoRequestServerInfo varCopy = new WhoRequestServerInfo();

        varCopy.factionGroup = this.factionGroup;
        varCopy.locale = this.locale;
        varCopy.requesterVirtualRealmAddress = this.requesterVirtualRealmAddress;

        return varCopy;
    }
}
