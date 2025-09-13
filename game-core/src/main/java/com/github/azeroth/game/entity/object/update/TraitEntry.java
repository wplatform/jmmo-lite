package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class TraitEntry implements IEquatable<TraitEntry> {
    public int traitNodeID;
    public int traitNodeEntryID;
    public int rank;
    public int grantedRanks;

    public final boolean equals(TraitEntry right) {
        return traitNodeID == right.traitNodeID && traitNodeEntryID == right.traitNodeEntryID && rank == right.rank && grantedRanks == right.grantedRanks;
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(traitNodeID);
        data.writeInt32(traitNodeEntryID);
        data.writeInt32(rank);
        data.writeInt32(grantedRanks);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(traitNodeID);
        data.writeInt32(traitNodeEntryID);
        data.writeInt32(rank);
        data.writeInt32(grantedRanks);
    }
}
