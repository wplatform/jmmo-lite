package com.github.azeroth.game.networking.packet.combatlog;

import com.github.azeroth.game.networking.ServerPacket;

public class ProcResist extends ServerPacket {
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public ObjectGuid target = ObjectGuid.EMPTY;
    public int spellID;
    public Float rolled = null;
    public Float needed = null;

    public ProcResist() {
        super(ServerOpcode.ProcResist);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeGuid(target);
        this.writeInt32(spellID);
        this.writeBit(rolled != null);
        this.writeBit(needed != null);
        this.flushBits();

        if (rolled != null) {
            this.writeFloat(rolled.floatValue());
        }

        if (needed != null) {
            this.writeFloat(needed.floatValue());
        }
    }
}
