package com.github.azeroth.game.networking.packet.combatlog;

import com.github.azeroth.game.networking.ServerPacket;

public class SpellOrDamageImmune extends ServerPacket {
    public ObjectGuid casterGUID = ObjectGuid.EMPTY;
    public ObjectGuid victimGUID = ObjectGuid.EMPTY;
    public int spellID;
    public boolean isPeriodic;

    public SpellOrDamageImmune() {
        super(ServerOpcode.SpellOrDamageImmune);
    }

    @Override
    public void write() {
        this.writeGuid(casterGUID);
        this.writeGuid(victimGUID);
        this.writeInt32(spellID);
        this.writeBit(isPeriodic);
        this.flushBits();
    }
}
