package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RandomRollClient extends ClientPacket {
    public int min;
    public int max;
    public byte partyIndex;

    public RandomRollClient(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        min = this.readUInt32();
        max = this.readUInt32();
        partyIndex = this.readUInt8();
    }
}
