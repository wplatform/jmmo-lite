package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildEventLogQueryResults extends ServerPacket {
    public ArrayList<GuildEvententry> entry;

    public GuildEventLogQueryResults() {
        super(ServerOpcode.GuildEventLogQueryResults);
        entry = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32(entry.size());

        for (var entry : entry) {
            this.writeGuid(entry.playerGUID);
            this.writeGuid(entry.otherGUID);
            this.writeInt8(entry.transactionType);
            this.writeInt8(entry.rankID);
            this.writeInt32(entry.transactionDate);
        }
    }
}
