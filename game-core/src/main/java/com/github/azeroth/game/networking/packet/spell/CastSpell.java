package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class CastSpell extends ClientPacket {
    public SpellcastRequest cast;

    public castSpell(WorldPacket packet) {
        super(packet);
        cast = new spellCastRequest();
    }

    @Override
    public void read() {
        cast.read(this);
    }
}
