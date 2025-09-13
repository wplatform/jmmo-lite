package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class QuestGiverAcceptQuest extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;
    public int questID;
    public boolean startCheat;

    public QuestGiverAcceptQuest(ByteBuf packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
        questID = this.readUInt32();
        startCheat = this.readBit();
    }
}
