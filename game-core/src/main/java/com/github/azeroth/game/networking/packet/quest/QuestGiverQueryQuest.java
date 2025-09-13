package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverQueryQuest extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int questID;
    public boolean respondToGiver;

    public QuestGiverQueryQuest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
        questID = this.readUInt32();
        respondToGiver = this.readBit();
    }
}
