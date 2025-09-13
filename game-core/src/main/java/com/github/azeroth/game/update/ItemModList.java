package com.github.azeroth.game.networking.packets;


import com.github.azeroth.game.networking.WorldPacket;

public class ItemModList {
    public Array<ItemMod> values = new Array<ItemMod>(ItemModifier.max.getValue());

    public static boolean opEquals(ItemModList left, ItemModList right) {
        if (left.values.count != right.values.count) {
            return false;
        }

        return !left.values.Except(right.values).Any();
    }

    public static boolean opNotEquals(ItemModList left, ItemModList right) {
        return !(left == right);
    }

    public final void read(WorldPacket data) {
        var itemModListCount = data.<Integer>readBit(6);
        data.resetBitPos();

        for (var i = 0; i < itemModListCount; ++i) {
            var itemMod = new ItemMod();
            itemMod.read(data);
            values.set(i, itemMod);
        }
    }

    public final void write(WorldPacket data) {
        data.writeBits(values.size(), 6);
        data.flushBits();

        for (var itemMod : values) {
            itemMod.write(data);
        }
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemModList) {
            return (ItemModList) obj == this;
        }

        return false;
    }
}
