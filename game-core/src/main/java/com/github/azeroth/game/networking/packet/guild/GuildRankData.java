package com.github.azeroth.game.networking.packet.guild;


import com.github.azeroth.game.networking.WorldPacket;

public class GuildRankData {
    public byte rankID;
    public int rankOrder;
    public int flags;
    public int withdrawGoldLimit;
    public String rankName;
    public int[] tabFlags = new int[GuildConst.MaxBankTabs];
    public int[] tabWithdrawItemLimit = new int[GuildConst.MaxBankTabs];

    public final void write(WorldPacket data) {
        data.writeInt8(rankID);
        data.writeInt32(rankOrder);
        data.writeInt32(flags);
        data.writeInt32(withdrawGoldLimit);

        for (byte i = 0; i < GuildConst.MaxBankTabs; i++) {
            data.writeInt32(TabFlags[i]);
            data.writeInt32(TabWithdrawItemLimit[i]);
        }

        data.writeBits(rankName.getBytes().length, 7);
        data.writeString(rankName);
    }
}
