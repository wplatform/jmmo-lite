package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class PartyMemberAuraStates {
    public int spellID;
    public short flags;
    public int activeFlags;
    public ArrayList<Float> points = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt32(spellID);
        data.writeInt16(flags);
        data.writeInt32(activeFlags);
        data.writeInt32(points.size());

        for (var points : points) {
            data.writeFloat(points);
        }
    }
}
