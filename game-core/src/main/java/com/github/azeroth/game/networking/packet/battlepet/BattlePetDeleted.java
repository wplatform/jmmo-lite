package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.networking.ServerPacket;

public class BattlePetDeleted extends ServerPacket {
    public ObjectGuid petGuid = ObjectGuid.EMPTY;

    public BattlePetDeleted() {
        super(ServerOpcode.BattlePetDeleted);
    }

    @Override
    public void write() {
        this.writeGuid(petGuid);
    }
}
