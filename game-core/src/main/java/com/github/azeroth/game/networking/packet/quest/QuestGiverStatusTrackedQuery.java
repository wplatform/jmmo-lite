package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class QuestGiverStatusTrackedQuery extends ClientPacket {
    public ArrayList<ObjectGuid> questGiverGUIDs = new ArrayList<>();

    public QuestGiverStatusTrackedQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var guidCount = this.readUInt32();

        for (int i = 0; i < guidCount; ++i) {
            questGiverGUIDs.add(this.readPackedGuid());
        }
    }
}
