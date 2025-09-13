package com.github.azeroth.game.networking.packet.equipment;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class DeleteEquipmentSet extends ClientPacket {
    public long ID;

    public deleteEquipmentSet(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        ID = this.readUInt64();
    }
}
