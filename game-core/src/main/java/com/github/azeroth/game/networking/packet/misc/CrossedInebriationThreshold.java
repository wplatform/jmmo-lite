package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ServerPacket;

public class CrossedInebriationThreshold extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public int itemID;
    public int threshold;

    public CrossedInebriationThreshold() {
        super(ServerOpcode.CrossedInebriationThreshold);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
        this.writeInt32(threshold);
        this.writeInt32(itemID);
    }
}
