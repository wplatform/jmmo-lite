package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QuestGiverChooseReward extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int questID;
    public QuestChoiceItem choice = new QuestChoiceItem();

    public QuestGiverChooseReward(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
        questID = this.readUInt32();
        choice.read(this);
    }
}
