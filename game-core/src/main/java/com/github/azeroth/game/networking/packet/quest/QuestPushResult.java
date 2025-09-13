package com.github.azeroth.game.networking.packet.quest;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.domain.quest.QuestPushReason;

class QuestPushResult extends ClientPacket {
    public ObjectGuid senderGUID = ObjectGuid.EMPTY;
    public int questID;
    public QuestPushReason result = QuestPushReason.Success;

    public QuestPushResult(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        senderGUID = this.readPackedGuid();
        questID = this.readUInt32();
        result = QuestPushReason.forValue(this.readUInt8());
    }
}
