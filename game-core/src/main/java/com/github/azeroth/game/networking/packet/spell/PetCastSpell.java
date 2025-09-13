package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class PetCastSpell extends ClientPacket {
    public ObjectGuid petGUID = ObjectGuid.EMPTY;
    public SpellcastRequest cast;

    public PetCastSpell(WorldPacket packet) {
        super(packet);
        cast = new spellCastRequest();
    }

    @Override
    public void read() {
        petGUID = this.readPackedGuid();
        cast.read(this);
    }
}
