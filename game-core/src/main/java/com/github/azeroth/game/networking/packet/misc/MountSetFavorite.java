package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class MountSetFavorite extends ClientPacket {
    public int mountSpellID;
    public boolean isFavorite;

    public mountSetFavorite(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        mountSpellID = this.readUInt32();
        isFavorite = this.readBit();
    }
}
