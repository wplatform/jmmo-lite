package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public final class InvUpdate {
    public ArrayList<InvItem> items;

    public invUpdate() {
    }

    public invUpdate(WorldPacket data) {
        items = new ArrayList<>();
        var size = data.readBit(2);
        data.resetBitPos();

        for (var i = 0; i < size; ++i) {
            var item = new InvItem();
            item.containerSlot = data.readUInt8();
            item.slot = data.readUInt8();

            items.add(item);
        }
    }

    public InvUpdate clone() {
        InvUpdate varCopy = new invUpdate();

        varCopy.items = this.items;

        return varCopy;
    }


    public final static class InvItem {

        public byte containerSlot;

        public byte slot;

        public InvItem clone() {
            InvItem varCopy = new InvItem();

            varCopy.containerSlot = this.containerSlot;
            varCopy.slot = this.slot;

            return varCopy;
        }
    }
}
