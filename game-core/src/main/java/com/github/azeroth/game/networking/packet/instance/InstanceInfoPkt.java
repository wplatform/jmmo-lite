package com.github.azeroth.game.networking.packet.instance;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class InstanceInfoPkt extends ServerPacket {
    public ArrayList<InstanceLockPkt> lockList = new ArrayList<>();

    public InstanceInfoPkt() {
        super(ServerOpcode.InstanceInfo);
    }

    @Override
    public void write() {
        this.writeInt32(lockList.size());

        for (var lockInfos : lockList) {
            lockInfos.write(this);
        }
    }
}
