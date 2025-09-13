package com.github.azeroth.game.networking.packet.calendar;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class SetSavedInstanceExtend extends ClientPacket {
    public int mapID;
    public boolean extend;
    public int difficultyID;

    public SetSavedInstanceExtend(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        mapID = this.readInt32();
        difficultyID = this.readUInt32();
        extend = this.readBit();
    }
}
