package com.github.azeroth.game.networking.packet.combatlog;

import com.github.azeroth.game.networking.ServerPacket;

public class SpellInterruptLog extends ServerPacket {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public int interruptedSpellID;
    public int spellID;

    public SpellInterruptLog() {
        super(ServerOpcode.SpellInterruptLog);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeGuid(victim);
        this.writeInt32(interruptedSpellID);
        this.writeInt32(spellID);
    }
}
