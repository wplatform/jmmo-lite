package com.github.azeroth.game.networking.packet.query;

import com.github.azeroth.game.networking.WorldPacket;

public class PlayerGuidLookupHint {
    public integer virtualRealmAddress = new integer(); // current realm (?) (identifier made from the index, BattleGroup and Region)
    public Integer nativeRealmAddress = new integer(); // original realm (?) (identifier made from the index, BattleGroup and Region)

    public final void write(WorldPacket data) {
        data.writeBit(virtualRealmAddress != null);
        data.writeBit(nativeRealmAddress != null);
        data.flushBits();

        if (virtualRealmAddress != null) {
            data.writeInt32(virtualRealmAddress.intValue());
        }

        if (nativeRealmAddress != null) {
            data.writeInt32(nativeRealmAddress.intValue());
        }
    }
}
