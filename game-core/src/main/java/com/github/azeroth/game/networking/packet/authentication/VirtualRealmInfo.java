package com.github.azeroth.game.networking.packet.authentication;

import com.github.azeroth.game.networking.WorldPacket;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public final class VirtualRealmInfo {
    public int realmAddress; // the virtual address of this realm, constructed as RealmHandle::Region << 24 | RealmHandle::Battlegroup << 16 | RealmHandle::Index
    public VirtualRealmNameInfo realmNameInfo;

    public void write(WorldPacket data) {
        data.writeInt32(realmAddress);
        realmNameInfo.write(data);
    }

}
