package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverStatusQuery extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;

    public QuestGiverStatusQuery(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
    }
}

//Structs

