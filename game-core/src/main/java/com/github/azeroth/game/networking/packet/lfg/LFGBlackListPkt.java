package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class LFGBlackListPkt {
    public ObjectGuid playerGuid = null;
    public ArrayList<LFGBlackListslot> slot = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeBit(playerGuid != null);
        data.writeInt32(slot.size());

        if (playerGuid != null) {
            data.writeGuid(playerGuid.getValue());
        }

        for (var slot : slot) {
            slot.write(data);
        }
    }
}
