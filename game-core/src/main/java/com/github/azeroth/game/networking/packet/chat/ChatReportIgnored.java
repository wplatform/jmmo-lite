package com.github.azeroth.game.networking.packet.chat;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class ChatReportIgnored extends ClientPacket {
    public ObjectGuid ignoredGUID = ObjectGuid.EMPTY;
    public byte reason;

    public ChatReportIgnored(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ignoredGUID = this.readPackedGuid();
        reason = this.readUInt8();
    }
}
