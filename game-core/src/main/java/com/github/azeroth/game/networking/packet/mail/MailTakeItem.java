package com.github.azeroth.game.networking.packet.mail;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class MailTakeItem extends ClientPacket {
    public ObjectGuid mailbox = ObjectGuid.EMPTY;
    public long mailID;
    public long attachID;

    public MailTakeItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        mailbox = this.readPackedGuid();
        mailID = this.readUInt64();
        attachID = this.readUInt64();
    }
}
