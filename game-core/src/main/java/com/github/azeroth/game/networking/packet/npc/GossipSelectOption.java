package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GossipSelectOption extends ClientPacket {
    public ObjectGuid gossipUnit = ObjectGuid.EMPTY;
    public int gossipOptionID;

    public int gossipID;
    public String promotionCode;

    public GossipSelectOption(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        gossipUnit = this.readPackedGuid();
        gossipID = this.readUInt32();
        gossipOptionID = this.readInt32();

        var length = this.<Integer>readBit(8);
        promotionCode = this.readString(length);
    }
}
