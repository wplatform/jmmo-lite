package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.ServerPacket;

public class GossipOptionNPCInteraction extends ServerPacket {
    public ObjectGuid gossipGUID = ObjectGuid.EMPTY;
    public int gossipNpcOptionID;
    public Integer friendshipFactionID = null;

    public GossipOptionNPCInteraction() {
        super(ServerOpcode.GossipOptionNpcInteraction);
    }

    @Override
    public void write() {
        this.writeGuid(gossipGUID);
        this.writeInt32(gossipNpcOptionID);
        this.writeBit(friendshipFactionID != null);
        this.flushBits();

        if (friendshipFactionID != null) {
            this.writeInt32(friendshipFactionID.intValue());
        }
    }
}
