package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverRequestReward extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int questID;

    public QuestGiverRequestReward(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
        questID = this.readUInt32();
    }
}
