package com.github.azeroth.game.networking.packet.combatlog;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;

public class SpellLogMissEntry {
    public ObjectGuid victim = ObjectGuid.EMPTY;
    public byte missReason;
    private SpellLogMissDebug debug = null;

    public SpellLogMissEntry(ObjectGuid victim, byte missReason) {
        this.victim = victim;
        this.missReason = missReason;
    }

    public final void write(WorldPacket data) {
        data.writeGuid(victim);
        data.writeInt8(missReason);

        if (data.writeBit(debug != null)) {
            debug.write(data);
        }

        data.flushBits();
    }
}
