package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class BattlegroundPlayerJoined extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public BattlegroundPlayerJoined() {
        super(ServerOpcode.BattlegroundPlayerJoined);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
    }
}
