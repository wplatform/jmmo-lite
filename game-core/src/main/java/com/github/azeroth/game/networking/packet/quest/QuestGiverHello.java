package com.github.azeroth.game.networking.packet.quest;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import io.netty.buffer.ByteBuf;

public class QuestGiverHello extends ClientPacket {
    public ObjectGuid questGiverGUID = ObjectGuid.EMPTY;

    public QuestGiverHello(ByteBuf packet) {
        super(packet);
    }

    @Override
    public void read() {
        questGiverGUID = this.readPackedGuid();
    }
}
