package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;

public class SpellDelayed extends ServerPacket {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public int actualDelay;

    public SpellDelayed() {
        super(ServerOpcode.SpellDelayed);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeInt32(actualDelay);
    }
}
