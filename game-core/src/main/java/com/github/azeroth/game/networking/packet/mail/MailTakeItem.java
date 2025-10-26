package com.github.azeroth.game.networking.packet.mail;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;
import io.netty.buffer.ByteBuf;

public class MailTakeItem extends ClientPacket {
    public ObjectGuid mailbox;
    public long mailID;
    public long attachID;

    public MailTakeItem(ByteBuf data) {
        super(data);
    }


    @Override
    public void read() {
        mailbox = this.readPackedGuid();
        mailID = this.readUInt64();
        attachID = this.readUInt64();
    }
}
