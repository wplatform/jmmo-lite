package com.github.azeroth.game.networking.packet.battleground;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class BattlefieldListRequest extends ClientPacket {
    public int listID;

    public BattlefieldListRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        listID = this.readInt32();
    }
}
