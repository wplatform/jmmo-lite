package com.github.azeroth.game.networking.packet.character;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class GetUndeleteCharacterCooldownStatus extends ClientPacket {
    public GetUndeleteCharacterCooldownStatus(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
    }
}
