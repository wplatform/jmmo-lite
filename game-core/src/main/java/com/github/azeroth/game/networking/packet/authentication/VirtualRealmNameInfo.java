package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.WorldPacket;

final class VirtualRealmNameInfo {
    public boolean isLocal; // true if the realm is the same as the account's home realm
    public boolean isInternalRealm; // @todo research
    public String realmNameActual; // the name of the realm
    public String realmNameNormalized; // the name of the realm without spaces

    public virtualRealmNameInfo() {
    }

    public virtualRealmNameInfo(boolean isHomeRealm, boolean isInternalRealm, String realmNameActual, String realmNameNormalized) {
        isLocal = isHomeRealm;
        isInternalRealm = isInternalRealm;
        realmNameActual = realmNameActual;
        realmNameNormalized = realmNameNormalized;
    }

    public void write(WorldPacket data) {
        data.writeBit(isLocal);
        data.writeBit(isInternalRealm);
        data.writeBits(realmNameActual.getBytes().length, 8);
        data.writeBits(realmNameNormalized.getBytes().length, 8);
        data.flushBits();

        data.writeString(realmNameActual);
        data.writeString(realmNameNormalized);
    }

    public VirtualRealmNameInfo clone() {
        VirtualRealmNameInfo varCopy = new virtualRealmNameInfo();

        varCopy.isLocal = this.isLocal;
        varCopy.isInternalRealm = this.isInternalRealm;
        varCopy.realmNameActual = this.realmNameActual;
        varCopy.realmNameNormalized = this.realmNameNormalized;

        return varCopy;
    }
}
