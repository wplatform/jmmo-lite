package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryQuestInfo extends ClientPacket {
    public ObjectGuid questGiver = ObjectGuid.EMPTY;
    public int questID;

    public QueryQuestInfo(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questID = this.readUInt32();
        questGiver = this.readPackedGuid();
    }
}
