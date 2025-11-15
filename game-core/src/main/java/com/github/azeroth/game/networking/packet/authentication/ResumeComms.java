package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;
import com.github.azeroth.game.networking.opcode.ServerOpCode;

public class ResumeComms extends ServerPacket {
    public ResumeComms() {
        super(ServerOpCode.SMSG_RESUME_COMMS);
    }

    @Override
    public void write() {
    }
}
