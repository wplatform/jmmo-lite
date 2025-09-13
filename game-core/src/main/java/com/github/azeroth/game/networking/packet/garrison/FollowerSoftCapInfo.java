package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

final class FollowerSoftCapInfo {
    public int garrFollowerTypeID;
    public int count;

    public void write(WorldPacket data) {
        data.writeInt32(garrFollowerTypeID);
        data.writeInt32(count);
    }

    public FollowerSoftCapInfo clone() {
        FollowerSoftCapInfo varCopy = new FollowerSoftCapInfo();

        varCopy.garrFollowerTypeID = this.garrFollowerTypeID;
        varCopy.count = this.count;

        return varCopy;
    }
}
