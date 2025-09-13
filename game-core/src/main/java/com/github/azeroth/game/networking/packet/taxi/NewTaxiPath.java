package com.github.azeroth.game.networking.packet.taxi;

import com.github.azeroth.game.networking.ServerPacket;

public class NewTaxiPath extends ServerPacket {
    public NewTaxiPath() {
        super(ServerOpcode.NewTaxiPath);
    }

    @Override
    public void write() {
    }
}
