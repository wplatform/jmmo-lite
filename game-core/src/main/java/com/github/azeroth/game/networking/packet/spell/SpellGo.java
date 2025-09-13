package com.github.azeroth.game.networking.packet.spell;


class SpellGo extends CombatLogServerPacket {
    public SpellcastData cast = new spellCastData();

    public SpellGo() {
        super(ServerOpcode.SpellGo);
    }

    @Override
    public void write() {
        cast.write(this);

        writeLogDataBit();
        flushBits();

        writeLogData();
    }
}
