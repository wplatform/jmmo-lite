package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildShiftRank extends ClientPacket {
    public boolean shiftUp;
    public int rankOrder;

    public GuildShiftRank(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        rankOrder = this.readInt32();
        shiftUp = this.readBit();
    }
}
