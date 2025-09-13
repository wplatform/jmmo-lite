package com.github.azeroth.game.networking.packet.voidstorage;

import com.github.azeroth.game.networking.WorldPacket;

final class VoidItem {
    public ObjectGuid guid = ObjectGuid.EMPTY;
    public ObjectGuid creator = ObjectGuid.EMPTY;
    public int slot;
    public itemInstance item;

    public void write(WorldPacket data) {
        data.writeGuid(guid);
        data.writeGuid(creator);
        data.writeInt32(slot);
        item.write(data);
    }

    public VoidItem clone() {
        VoidItem varCopy = new VoidItem();

        varCopy.guid = this.guid;
        varCopy.creator = this.creator;
        varCopy.slot = this.slot;
        varCopy.item = this.item;

        return varCopy;
    }
}
