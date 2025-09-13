package com.github.azeroth.game.networking.packet.trait;

import com.github.azeroth.game.entity.TraitEntry;
import com.github.azeroth.game.networking.WorldPacket;

public class TraitEntryPacket {
    public int traitNodeID;
    public int traitNodeEntryID;
    public int rank;
    public int grantedRanks;

    public TraitEntryPacket() {
    }

    public TraitEntryPacket(TraitEntry ufEntry) {
        traitNodeID = ufEntry.traitNodeID;
        traitNodeEntryID = ufEntry.traitNodeEntryID;
        rank = ufEntry.rank;
        grantedRanks = ufEntry.grantedRanks;
    }

    public final void read(WorldPacket data) {
        traitNodeID = data.readInt32();
        traitNodeEntryID = data.readInt32();
        rank = data.readInt32();
        grantedRanks = data.readInt32();
    }

    public final void write(WorldPacket data) {
        data.writeInt32(traitNodeID);
        data.writeInt32(traitNodeEntryID);
        data.writeInt32(rank);
        data.writeInt32(grantedRanks);
    }
}
