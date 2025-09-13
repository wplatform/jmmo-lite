package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonRemoveFollowerResult extends ServerPacket {
    public long followerDBID;
    public int garrTypeID;
    public int result;
    public int destroyed;

    public GarrisonRemoveFollowerResult() {
        super(ServerOpcode.GarrisonRemoveFollowerResult);
    }

    @Override
    public void write() {
        this.writeInt64(followerDBID);
        this.writeInt32(garrTypeID);
        this.writeInt32(result);
        this.writeInt32(destroyed);
    }
}
