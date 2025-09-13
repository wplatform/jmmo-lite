package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlemasterJoinArena extends ClientPacket {
    public byte teamSizeIndex;
    public byte roles;

    public BattlemasterJoinArena(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        teamSizeIndex = this.readUInt8();
        roles = this.readUInt8();
    }
}
