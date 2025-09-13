package com.github.azeroth.game.networking.packet.spell;


public class SpellStart extends ServerPacket {
    public SpellcastData cast;

    public SpellStart() {
        super(ServerOpcode.SpellStart);
        cast = new spellCastData();
    }

    @Override
    public void write() {
        cast.write(this);
    }
}
