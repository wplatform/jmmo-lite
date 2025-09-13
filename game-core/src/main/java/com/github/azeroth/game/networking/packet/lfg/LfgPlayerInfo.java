package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

class LfgPlayerInfo extends ServerPacket {
    public LFGblackList blackList = new LFGBlackList();
    public ArrayList<LfgPlayerDungeonInfo> dungeons = new ArrayList<>();

    public LfgPlayerInfo() {
        super(ServerOpcode.LfgPlayerInfo);
    }

    @Override
    public void write() {
        this.writeInt32(dungeons.size());
        blackList.write(this);

        for (var dungeonInfo : dungeons) {
            dungeonInfo.write(this);
        }
    }
}
