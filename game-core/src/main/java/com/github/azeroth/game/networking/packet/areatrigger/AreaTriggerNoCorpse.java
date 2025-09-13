package com.github.azeroth.game.networking.packet.areatrigger;

import com.github.azeroth.game.networking.ServerPacket;

public class AreaTriggerNoCorpse extends ServerPacket {
    public AreaTriggerNoCorpse() {
        super(ServerOpcode.AreaTriggerNoCorpse);
    }

    @Override
    public void write() {
    }
}
