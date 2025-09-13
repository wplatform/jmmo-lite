package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.ServerPacket;

public class WaitQueueUpdate extends ServerPacket {
    public AuthwaitInfo waitInfo = new authWaitInfo();

    public WaitQueueUpdate() {
        super(ServerOpcode.WaitQueueUpdate);
    }

    @Override
    public void write() {
        waitInfo.write(this);
    }
}
