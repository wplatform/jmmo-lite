package com.github.azeroth.game.networking.packet.collection;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class CollectionItemSetFavorite extends ClientPacket {
    public Collectiontype type = CollectionType.values()[0];
    public int id;
    public boolean isFavorite;

    public CollectionItemSetFavorite(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        type = CollectionType.forValue(this.readUInt32());
        id = this.readUInt32();
        isFavorite = this.readBit();
    }
}
