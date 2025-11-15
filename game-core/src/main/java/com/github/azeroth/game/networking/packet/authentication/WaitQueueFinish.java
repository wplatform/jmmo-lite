package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class WaitQueueFinish extends ServerPacket {
    public WaitQueueFinish() {
        super(ServerOpCode.SMSG_WAIT_QUEUE_FINISH);
    }

    @Override
    public void write() {
    }
}
