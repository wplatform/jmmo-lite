package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class GossipMessagePkt extends ServerPacket {
    public ArrayList<ClientgossipOptions> gossipOptions = new ArrayList<>();
    public int friendshipFactionID;
    public ObjectGuid gossipGUID = ObjectGuid.EMPTY;
    public ArrayList<ClientgossipText> gossipText = new ArrayList<>();
    public Integer textID = null;
    public Integer textID2 = null;

    public int gossipID;

    public GossipMessagePkt() {
        super(ServerOpcode.GossipMessage);
    }

    @Override
    public void write() {
        this.writeGuid(gossipGUID);
        this.writeInt32(gossipID);
        this.writeInt32(friendshipFactionID);
        this.writeInt32(gossipOptions.size());
        this.writeInt32(gossipText.size());
        this.writeBit(textID != null);
        this.writeBit(textID2 != null);
        this.flushBits();

        for (var options : gossipOptions) {
            options.write(this);
        }

        if (textID != null) {
            this.writeInt32(textID.intValue());
        }

        if (textID2 != null) {
            this.writeInt32(textID2.intValue());
        }

        for (var text : gossipText) {
            text.write(this);
        }
    }
}
