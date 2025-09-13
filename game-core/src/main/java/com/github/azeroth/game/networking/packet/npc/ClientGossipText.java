package com.github.azeroth.game.networking.packet.npc;

import com.github.azeroth.game.networking.WorldPacket;

public class ClientGossipText {
    public int questID;
    public int contentTuningID;
    public int questType;
    public boolean repeatable;
    public String questTitle;
    public int questFlags;
    public int questFlagsEx;

    public final void write(WorldPacket data) {
        data.writeInt32(questID);
        data.writeInt32(contentTuningID);
        data.writeInt32(questType);
        data.writeInt32(questFlags);
        data.writeInt32(questFlagsEx);

        data.writeBit(repeatable);
        data.writeBits(questTitle.getBytes().length, 9);
        data.flushBits();

        data.writeString(questTitle);
    }
}
