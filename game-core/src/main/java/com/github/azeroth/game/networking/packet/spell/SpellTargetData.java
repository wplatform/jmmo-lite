package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;


public class SpellTargetData {
    public int flags;
    public ObjectGuid unit = ObjectGuid.EMPTY;
    public ObjectGuid item = ObjectGuid.EMPTY;
    public TargetLocation srcLocation;
    public TargetLocation dstLocation;
    public Float orientation = null;
    public Integer mapID = null;
    public String name = "";

    public final void read(WorldPacket data) {
        data.resetBitPos();
        flags = data.readBit(28);

        if (data.readBit()) {
            srcLocation = new TargetLocation();
        }

        if (data.readBit()) {
            dstLocation = new TargetLocation();
        }

        var hasOrientation = data.readBit();
        var hasMapId = data.readBit();

        var nameLength = data.readBit(7);

        unit = data.readPackedGuid();
        item = data.readPackedGuid();

        if (srcLocation != null) {
            srcLocation.read(data);
        }

        if (dstLocation != null) {
            dstLocation.read(data);
        }

        if (hasOrientation) {
            orientation = data.readFloat();
        }

        if (hasMapId) {
            mapID = data.readInt32();
        }

        name = data.readString(nameLength);
    }

    public final void write(WorldPacket data) {
        data.writeBits(flags, 28);
        data.writeBit(srcLocation != null);
        data.writeBit(dstLocation != null);
        data.writeBit(orientation != null);
        data.writeBit(mapID != null);
        data.writeBits(name.length(), 7);
        data.flushBits();

        data.writeGuid(unit);
        data.writeGuid(item);

        if (srcLocation != null) {
            srcLocation.write(data);
        }

        if (dstLocation != null) {
            dstLocation.write(data);
        }

        if (orientation != null) {
            data.writeFloat(orientation);
        }

        if (mapID != null) {
            data.writeInt32(mapID);
        }

        data.writeString(name);
    }
}
