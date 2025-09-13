package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class AllAchievementData extends ServerPacket {
    public AllAchievements data = new AllAchievements();

    public AllAchievementData() {
        super(ServerOpCode.SMSG_ALL_ACHIEVEMENT_DATA);
    }

    @Override
    public void write() {
        data.write(this);
    }
}

//Structs

