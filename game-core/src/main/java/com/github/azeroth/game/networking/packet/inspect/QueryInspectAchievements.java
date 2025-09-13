package com.github.azeroth.game.networking.packet.inspect;

import com.github.azeroth.game.entity.ObjectGuid;
import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class QueryInspectAchievements extends ClientPacket {
    public ObjectGuid guid = ObjectGuid.EMPTY;

    public QueryInspectAchievements(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        guid = this.readPackedGuid();
    }
}
