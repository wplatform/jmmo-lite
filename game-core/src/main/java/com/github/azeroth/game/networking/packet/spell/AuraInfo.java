package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class AuraInfo {
    public short slot;
    public AuraDataInfo auraData;

    public void write(WorldPacket data) {
        data.writeInt8(slot);
        data.writeBit(auraData != null);
        data.flushBits();

        if (auraData != null) {
            auraData.write(data);
        }
    }
}
