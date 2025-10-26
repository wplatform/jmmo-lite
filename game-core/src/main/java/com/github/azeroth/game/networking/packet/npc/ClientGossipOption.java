package com.github.azeroth.game.networking.packet.npc;


import com.github.azeroth.game.domain.gossip.GossipOptionFlag;
import com.github.azeroth.game.domain.gossip.GossipOptionNpc;
import com.github.azeroth.game.domain.gossip.GossipOptionStatus;
import com.github.azeroth.game.networking.WorldPacket;

public class ClientGossipOption {
    public int gossipOptionID;
    public GossipOptionNpc optionNPC;

    public byte optionFlags;
    public int optionCost;

    public int optionLanguage;
    public GossipOptionFlag flags;
    public int orderIndex;
    public GossipOptionStatus status;
    public String text = "";
    public String confirm = "";
    public TreasureLootList treasure;
    public Integer spellID = null;
    public Integer overrideIconID = null;

    public final void write(WorldPacket data) {
        data.writeInt32(gossipOptionID);
        data.writeInt8((byte) optionNPC.ordinal());
        data.writeInt8(optionFlags);
        data.writeInt32(optionCost);
        data.writeInt32(optionLanguage);
        data.writeInt32(flags.ordinal());
        data.writeInt32(orderIndex);
        data.writeBits(text.getBytes().length, 12);
        data.writeBits(confirm.getBytes().length, 12);
        data.writeBits((byte) status.ordinal(), 2);
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
