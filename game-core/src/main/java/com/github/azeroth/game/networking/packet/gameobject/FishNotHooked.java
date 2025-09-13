package com.github.azeroth.game.networking.packet.gameobject;

import com.github.azeroth.game.networking.ServerPacket;

public class FishNotHooked extends ServerPacket {
    public FishNotHooked() {
        super(ServerOpcode.FishNotHooked);
    }

    @Override
    public void write() {
    }
}
