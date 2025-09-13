package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ConversationLineStarted extends ClientPacket {
    public ObjectGuid conversationGUID = ObjectGuid.EMPTY;
    public int lineID;

    public ConversationLineStarted(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        conversationGUID = this.readPackedGuid();
        lineID = this.readUInt32();
    }
}
