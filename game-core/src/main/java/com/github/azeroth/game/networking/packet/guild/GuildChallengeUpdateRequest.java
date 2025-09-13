package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GuildChallengeUpdateRequest extends ClientPacket {
    public GuildChallengeUpdateRequest(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
