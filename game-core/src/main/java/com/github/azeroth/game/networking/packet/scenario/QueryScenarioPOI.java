package com.github.azeroth.game.networking.packet.scenario;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class QueryScenarioPOI extends ClientPacket {
    public Array<Integer> missingScenarioPOIs = new Array<Integer>(50);

    public QueryScenarioPOI(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        var count = this.readUInt32();

        for (var i = 0; i < count; ++i) {
            missingScenarioPOIs.set(i, this.readInt32());
        }
    }
}
