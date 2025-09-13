package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.WorldPacket;

public final class GuildRosterProfessionData {
    public int dbID;
    public int rank;
    public int step;

    public void write(WorldPacket data) {
        data.writeInt32(dbID);
        data.writeInt32(rank);
        data.writeInt32(step);
    }

    public GuildRosterProfessionData clone() {
        GuildRosterProfessionData varCopy = new GuildRosterProfessionData();

        varCopy.dbID = this.dbID;
        varCopy.rank = this.rank;
        varCopy.step = this.step;

        return varCopy;
    }
}
