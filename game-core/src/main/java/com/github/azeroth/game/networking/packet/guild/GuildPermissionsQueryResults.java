package com.github.azeroth.game.networking.packet.guild;


import java.util.ArrayList;


public class GuildPermissionsQueryResults extends ServerPacket {
    public int numTabs;
    public int withdrawGoldLimit;
    public int flags;
    public int rankID;
    public ArrayList<GuildRanktabPermissions> tab;

    public GuildPermissionsQueryResults() {
        super(ServerOpcode.GuildPermissionsQueryResults);
        tab = new ArrayList<>();
    }

    @Override
    public void write() {
        this.writeInt32(rankID);
        this.writeInt32(withdrawGoldLimit);
        this.writeInt32(flags);
        this.writeInt32(numTabs);
        this.writeInt32(tab.size());

        for (var tab : tab) {
            this.writeInt32(tab.flags);
            this.writeInt32(tab.withdrawItemLimit);
        }
    }

    public final static class GuildRankTabPermissions {
        public int flags;
        public int withdrawItemLimit;

        public GuildRankTabPermissions clone() {
            GuildRankTabPermissions varCopy = new GuildRankTabPermissions();

            varCopy.flags = this.flags;
            varCopy.withdrawItemLimit = this.withdrawItemLimit;

            return varCopy;
        }
    }
}
