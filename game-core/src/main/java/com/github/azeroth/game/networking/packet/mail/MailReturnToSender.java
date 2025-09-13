package com.github.azeroth.game.networking.packet.mail;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MailReturnToSender extends ClientPacket {
    public long mailID;
    public ObjectGuid senderGUID = ObjectGuid.EMPTY;

    public MailReturnToSender(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        mailID = this.readUInt64();
        senderGUID = this.readPackedGuid();
    }
}
