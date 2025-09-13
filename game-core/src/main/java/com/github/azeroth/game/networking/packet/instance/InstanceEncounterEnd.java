package com.github.azeroth.game.networking.packet.instance;

import com.github.azeroth.game.networking.ServerPacket;

public class InstanceEncounterEnd extends ServerPacket {
    public InstanceEncounterEnd() {
        super(ServerOpcode.InstanceEncounterEnd);
    }

    @Override
    public void write() {
    }
}
