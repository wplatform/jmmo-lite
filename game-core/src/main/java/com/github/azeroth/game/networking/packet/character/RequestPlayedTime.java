package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.WorldPacket;

public class RequestPlayedTime extends ClientPacket {
    public boolean triggerScriptEvent;

    public RequestPlayedTime(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        triggerScriptEvent = this.readBit();
    }
}
