package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class WaitQueueUpdate extends ServerPacket {
    public AuthWaitInfo waitInfo = new AuthWaitInfo();

    public WaitQueueUpdate() {
        super(ServerOpCode.SMSG_WAIT_QUEUE_UPDATE);
    }

    @Override
    public void write() {
        waitInfo.write(this);
    }
}
