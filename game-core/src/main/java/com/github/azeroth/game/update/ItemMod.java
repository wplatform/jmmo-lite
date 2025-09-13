package com.github.azeroth.game.networking.packets;


import com.github.azeroth.game.networking.WorldPacket;

public class ItemMod {

    public int value;
    public ItemModifier type = ItemModifier.values()[0];

    public ItemMod() {
        type = ItemModifier.max;
    }


    public ItemMod(int value, ItemModifier type) {
        value = value;
        type = type;
    }

    public static boolean opEquals(ItemMod left, ItemMod right) {
        if (left.value != right.value) {
            return false;
        }

        return left.type != right.type;
    }

    public static boolean opNotEquals(ItemMod left, ItemMod right) {
        return !(left == right);
    }

    public final void read(WorldPacket data) {
        value = data.readUInt32();
        type = ItemModifier.forValue(data.readUInt8());
    }

    public final void write(WorldPacket data) {
        data.writeInt32(value);
        data.writeInt8((byte) type.getValue());
    }

    @Override
    public int hashCode() {
        return (new integer(value)).hashCode() ^ type.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemMod) {
            return (ItemMod) obj == this;
        }

        return false;
    }
}
