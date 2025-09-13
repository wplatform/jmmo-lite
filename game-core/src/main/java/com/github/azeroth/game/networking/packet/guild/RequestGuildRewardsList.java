package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RequestGuildRewardsList extends ClientPacket {
    public long currentVersion;

    public RequestGuildRewardsList(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        currentVersion = this.readInt64();
    }
}
