package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class RemoveNewItem extends ClientPacket {
    private ObjectGuid itemGuid = ObjectGuid.EMPTY;

    public RemoveNewItem(WorldPacket packet) {
        super(packet);
    }

    public final ObjectGuid getItemGuid() {
        return itemGuid;
    }

    public final void setItemGuid(ObjectGuid value) {
        itemGuid = value;
    }

    @Override
    public void read() {
        setItemGuid(this.readPackedGuid());
    }
}
