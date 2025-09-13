package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class FishEscaped extends ServerPacket {
    public FishEscaped() {
        super(ServerOpcode.FishEscaped);
    }

    @Override
    public void write() {
    }
}
