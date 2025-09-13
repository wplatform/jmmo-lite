package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceReset extends ServerPacket {
    public int mapID;

    public InstanceReset() {
        super(ServerOpcode.InstanceReset);
    }

    @Override
    public void write() {
        this.writeInt32(mapID);
    }
}
