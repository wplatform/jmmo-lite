package com.github.azeroth.game.networking.packet.toy;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class UseToy extends ClientPacket {
    public SpellcastRequest cast = new spellCastRequest();

    public UseToy(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        cast.read(this);
    }
}
