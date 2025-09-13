package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public class SpellHistoryEntry {
    private final Integer unused622_1; // This field is not used for anything in the client in 6.2.2.20444
    private final Integer unused622_2; // This field is not used for anything in the client in 6.2.2.20444
    public int spellID;
    public int itemID;
    public int category;
    public int recoveryTime;
    public int categoryRecoveryTime;
    public float modRate = 1.0f;
    public boolean onHold;

    public final void write(WorldPacket data) {
        data.writeInt32(spellID);
        data.writeInt32(itemID);
        data.writeInt32(category);
        data.writeInt32(recoveryTime);
        data.writeInt32(categoryRecoveryTime);
        data.writeFloat(modRate);
        data.writeBit(unused622_1 != null);
        data.writeBit(unused622_2 != null);
        data.writeBit(onHold);
        data.flushBits();

        if (unused622_1 != null) {
            data.writeInt32(unused622_1.intValue());
        }

        if (unused622_2 != null) {
            data.writeInt32(unused622_2.intValue());
        }
    }
}
