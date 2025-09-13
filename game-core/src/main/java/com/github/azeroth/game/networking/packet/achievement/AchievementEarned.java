package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AchievementEarned extends ServerPacket {
    public ObjectGuid earner = ObjectGuid.EMPTY;
    public int earnerNativeRealm;
    public int earnerVirtualRealm;
    public int achievementID;
    public long time;
    public boolean initial;
    public ObjectGuid sender = ObjectGuid.EMPTY;

    public AchievementEarned() {
        super(ServerOpCode.SMSG_ACHIEVEMENT_EARNED);
    }

    @Override
    public void write() {
        this.writeGuid(sender);
        this.writeGuid(earner);
        this.writeInt32(achievementID);
        this.writePackedTime(time);
        this.writeInt32(earnerNativeRealm);
        this.writeInt32(earnerVirtualRealm);
        this.writeBit(initial);
        this.flushBits();
    }
}
