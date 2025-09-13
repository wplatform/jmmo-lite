package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ClassTalentsRenameConfig extends ClientPacket {
    public int configID;
    public String name;

    public ClassTalentsRenameConfig(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        configID = this.readInt32();
        var nameLength = this.<Integer>readBit(9);
        name = this.readString(nameLength);
    }
}
