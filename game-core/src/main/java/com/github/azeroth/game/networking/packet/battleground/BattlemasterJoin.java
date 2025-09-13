package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlemasterJoin extends ClientPacket {

    public Array<Long> queueIDs = new Array<Long>(1);

    public byte roles;
    public int[] blacklistMap = new int[2];

    public BattlemasterJoin(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var queueCount = this.readUInt32();
        roles = this.readUInt8();
        BlacklistMap[0] = this.readInt32();
        BlacklistMap[1] = this.readInt32();

        for (var i = 0; i < queueCount; ++i) {
            queueIDs.set(i, this.readUInt64());
        }
    }
}
