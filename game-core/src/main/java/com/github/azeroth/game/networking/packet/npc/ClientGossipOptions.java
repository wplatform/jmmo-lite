package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.networking.WorldPacket;

public class ClientGossipOptions {
    public int gossipOptionID;
    public GossipOptionNpc optionNPC = GossipOptionNpc.values()[0];

    public byte optionFlags;
    public int optionCost;

    public int optionLanguage;
    public GossipOptionflags flags = GossipOptionFlags.values()[0];
    public int orderIndex;
    public GossipOptionstatus status = GossipOptionStatus.values()[0];
    public String text = "";
    public String confirm = "";
    public treasureLootList treasure = new treasureLootList();
    public Integer spellID = null;
    public Integer overrideIconID = null;

    public final void write(WorldPacket data) {
        data.writeInt32(gossipOptionID);
        data.writeInt8((byte) optionNPC.getValue());
        data.writeInt8((byte) optionFlags);
        data.writeInt32(optionCost);
        data.writeInt32(optionLanguage);
        data.writeInt32(flags.getValue());
        data.writeInt32(orderIndex);
        data.writeBits(text.getBytes().length, 12);
        data.writeBits(confirm.getBytes().length, 12);
        data.writeBits((byte) status.getValue(), 2);
        data.writeBit(spellID != null);
        data.writeBit(overrideIconID != null);
        data.flushBits();

        treasure.write(data);

        data.writeString(text);
        data.writeString(confirm);

        if (spellID != null) {
            data.writeInt32(spellID.intValue());
        }

        if (overrideIconID != null) {
            data.writeInt32(overrideIconID.intValue());
        }
    }
}
