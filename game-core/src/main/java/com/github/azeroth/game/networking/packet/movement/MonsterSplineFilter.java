package com.github.azeroth.game.networking.packet.movement;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class MonsterSplineFilter {
    public ArrayList<MonsterSplineFilterKey> filterKeys = new ArrayList<>();
    public byte filterFlags;
    public float baseSpeed;
    public short startOffset;
    public float distToPrevFilterKey;
    public short addedToStart;

    public final void write(WorldPacket data) {
        data.writeInt32(filterKeys.size());
        data.writeFloat(baseSpeed);
        data.writeInt16(startOffset);
        data.writeFloat(distToPrevFilterKey);
        data.writeInt16(addedToStart);

        filterKeys.forEach(p -> p.write(data));

        data.writeBits(filterFlags, 2);
        data.flushBits();
    }
}
