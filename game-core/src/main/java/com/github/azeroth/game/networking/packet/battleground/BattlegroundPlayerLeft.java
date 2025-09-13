package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ServerPacket;

public class BattlegroundPlayerLeft extends ServerPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public BattlegroundPlayerLeft() {
        super(ServerOpcode.BattlegroundPlayerLeft);
    }

    @Override
    public void write() {
        this.writeGuid(guid);
    }
}
