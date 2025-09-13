package com.github.azeroth.game.networking.packet.guild;


import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class QueryGuildInfoResponse extends ServerPacket {
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public Guildinfo info = new guildInfo();
    public boolean hasGuildInfo;

    public QueryGuildInfoResponse() {
        super(ServerOpcode.QueryGuildInfoResponse);
    }

    @Override
    public void write() {
        this.writeGuid(guildGUID);
        this.writeBit(hasGuildInfo);
        this.flushBits();

        if (hasGuildInfo) {
            this.writeGuid(info.guildGuid);
            this.writeInt32(info.virtualRealmAddress);
            this.writeInt32(info.ranks.size());
            this.writeInt32(info.emblemStyle);
            this.writeInt32(info.emblemColor);
            this.writeInt32(info.borderStyle);
            this.writeInt32(info.borderColor);
            this.writeInt32(info.backgroundColor);
            this.writeBits(info.guildName.getBytes().length, 7);
            this.flushBits();

            for (var rank : info.ranks) {
                this.writeInt32(rank.rankID);
                this.writeInt32(rank.rankOrder);

                this.writeBits(rank.rankName.getBytes().length, 7);
                this.writeString(rank.rankName);
            }

            this.writeString(info.guildName);
        }
    }

    public static class GuildInfo {
        public ObjectGuid guildGuid = ObjectGuid.EMPTY;


        public int virtualRealmAddress; // a special identifier made from the index, BattleGroup and Region.


        public int emblemStyle;

        public int emblemColor;

        public int borderStyle;

        public int borderColor;

        public int backgroundColor;
        public ArrayList<RankInfo> ranks = new ArrayList<>();
        public String guildName = "";


        public final static class RankInfo {

            public int rankID;

            public int rankOrder;
            public String rankName;

            public RankInfo() {
            }

            public RankInfo(int id, int order, String name) {
                rankID = id;
                rankOrder = order;
                rankName = name;
            }

            public RankInfo clone() {
                RankInfo varCopy = new RankInfo();

                varCopy.rankID = this.rankID;
                varCopy.rankOrder = this.rankOrder;
                varCopy.rankName = this.rankName;

                return varCopy;
            }
        }
    }
}
