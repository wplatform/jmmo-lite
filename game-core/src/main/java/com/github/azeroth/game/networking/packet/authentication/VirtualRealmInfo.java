package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.WorldPacket;

final class VirtualRealmInfo {
    public int realmAddress; // the virtual address of this realm, constructed as RealmHandle::Region << 24 | RealmHandle::Battlegroup << 16 | RealmHandle::Index
    public VirtualrealmNameInfo realmNameInfo = new virtualRealmNameInfo();

    public virtualRealmInfo() {
    }

    public virtualRealmInfo(int realmAddress, boolean isHomeRealm, boolean isInternalRealm, String realmNameActual, String realmNameNormalized) {
        realmAddress = realmAddress;
        realmNameInfo = new virtualRealmNameInfo(isHomeRealm, isInternalRealm, realmNameActual, realmNameNormalized);
    }

    public void write(WorldPacket data) {
        data.writeInt32(realmAddress);
        realmNameInfo.write(data);
    }

    public VirtualRealmInfo clone() {
        VirtualRealmInfo varCopy = new virtualRealmInfo();

        varCopy.realmAddress = this.realmAddress;
        varCopy.realmNameInfo = this.realmNameInfo;

        return varCopy;
    }
}
