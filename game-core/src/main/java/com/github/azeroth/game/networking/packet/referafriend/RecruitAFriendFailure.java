package com.github.azeroth.game.networking.packet.referafriend;


public class RecruitAFriendFailure extends ServerPacket {
    public string str;
    public ReferAFriendError reason = ReferAFriendError.values()[0];

    public RecruitAFriendFailure() {
        super(ServerOpcode.RecruitAFriendFailure);
    }

    @Override
    public void write() {
        this.writeInt32(reason.getValue());
        // Client uses this string only if reason == ERR_REFER_A_FRIEND_NOT_IN_GROUP || reason == ERR_REFER_A_FRIEND_SUMMON_OFFLINE_S
        // but always reads it from packet
        this.writeBits(str.getBytes().length, 6);
        this.writeString(str);
    }
}
