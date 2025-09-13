package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class BroadcastAchievement extends ServerPacket {
    public ObjectGuid playerGUID = ObjectGuid.EMPTY;
    public String name = "";
    public int achievementID;
    public boolean guildAchievement;

    public BroadcastAchievement() {
        super(ServerOpCode.BroadcastAchievement);
    }

    @Override
    public void write() {
        this.writeBits(name.getBytes().length, 7);
        this.writeBit(guildAchievement);
        this.writeGuid(playerGUID);
        this.writeInt32(achievementID);
        this.writeString(name);
    }
}
