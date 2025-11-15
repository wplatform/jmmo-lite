package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.WorldPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public final class VirtualRealmNameInfo {
    public boolean isLocal; // true if the realm is the same as the account's home realm
    public boolean isInternalRealm; // @todo research
    public String realmNameActual; // the name of the realm
    public String realmNameNormalized; // the name of the realm without spaces


    public void write(WorldPacket data) {
        data.writeBit(isLocal);
        data.writeBit(isInternalRealm);
        data.writeBits(realmNameActual.getBytes().length, 8);
        data.writeBits(realmNameNormalized.getBytes().length, 8);
        data.flushBits();

        data.writeString(realmNameActual);
        data.writeString(realmNameNormalized);
    }
}
