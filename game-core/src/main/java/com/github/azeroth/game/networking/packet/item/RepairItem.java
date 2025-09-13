package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

public class RepairItem extends ClientPacket {
    public ObjectGuid npcGUID = ObjectGuid.EMPTY;
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public boolean useGuildBank;

    public RepairItem(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        npcGUID = this.readPackedGuid();
        itemGUID = this.readPackedGuid();
        useGuildBank = this.readBit();
    }
}
