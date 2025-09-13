package com.github.azeroth.game.networking.packet.lfg;

import com.github.azeroth.game.networking.ServerPacket;

public class LfgBootPlayer extends ServerPacket {
    public LfgBootinfo info = new lfgBootInfo();

    public LfgBootPlayer() {
        super(ServerOpcode.LfgBootPlayer);
    }

    @Override
    public void write() {
        info.write(this);
    }
}
