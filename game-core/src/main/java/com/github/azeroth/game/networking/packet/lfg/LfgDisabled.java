package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ServerPacket;

public class LfgDisabled extends ServerPacket {
    public LfgDisabled() {
        super(ServerOpcode.LfgDisabled);
    }

    @Override
    public void write() {
    }
}
