package com.github.azeroth.game.networking.packet.achievement;


import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

import java.util.ArrayList;


public class AllGuildAchievements extends ServerPacket {
    public ArrayList<EarnedAchievement> earned = new ArrayList<>();

    public AllGuildAchievements() {
        super(ServerOpCode.SMSG_ALL_GUILD_ACHIEVEMENTS);
    }

    @Override
    public void write() {
        this.writeInt32(earned.size());

        for (var earned : earned) {
            earned.write(this);
        }
    }
}
