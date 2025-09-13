package com.github.azeroth.game.networking.packet.lfg;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class LfgPartyInfo extends ServerPacket {
    public ArrayList<LFGBlackList> player = new ArrayList<>();

    public LfgPartyInfo() {
        super(ServerOpcode.LfgPartyInfo);
    }

    @Override
    public void write() {
        this.writeInt32(player.size());

        for (var blackList : player) {
            blackList.write(this);
        }
    }
}
