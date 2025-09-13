package com.github.azeroth.game.networking.packet.guild;


import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildSetRankPermissions extends ClientPacket {
    public byte rankID;
    public int rankOrder;
    public int withdrawGoldLimit;
    public int flags;
    public int oldFlags;
    public int[] tabFlags = new int[GuildConst.MaxBankTabs];
    public int[] tabWithdrawItemLimit = new int[GuildConst.MaxBankTabs];
    public String rankName;

    public GuildSetRankPermissions(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        rankID = this.readUInt8();
        rankOrder = this.readInt32();
        flags = this.readUInt32();
        withdrawGoldLimit = this.readUInt32();

        for (byte i = 0; i < GuildConst.MaxBankTabs; i++) {
            TabFlags[i] = this.readUInt32();
            TabWithdrawItemLimit[i] = this.readUInt32();
        }

        this.resetBitPos();
        var rankNameLen = this.<Integer>readBit(7);

        rankName = this.readString(rankNameLen);

        oldFlags = this.readUInt32();
    }
}
