package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AchievementDeleted extends ServerPacket {
    public int achievementID;
    public int immunities; // this is just garbage, not used by client

    public AchievementDeleted() {
        super(ServerOpCode.SMSG_ACHIEVEMENT_DELETED);
    }

    @Override
    public void write() {
        this.writeInt32(achievementID);
        this.writeInt32(immunities);
    }
}
