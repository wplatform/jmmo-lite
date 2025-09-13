package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.ServerPacket;

public class GarrisonCreateResult extends ServerPacket {
    public int garrSiteLevelID;
    public int result;

    public GarrisonCreateResult() {
        super(ServerOpcode.GarrisonCreateResult);
    }

    @Override
    public void write() {
        this.writeInt32(result);
        this.writeInt32(garrSiteLevelID);
    }
}

//Structs

