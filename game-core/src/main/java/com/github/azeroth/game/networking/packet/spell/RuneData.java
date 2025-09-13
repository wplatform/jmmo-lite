package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class RuneData {
    public byte start;
    public byte count;
    public ArrayList<Byte> cooldowns = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeInt8(start);
        data.writeInt8(count);
        data.writeInt32(cooldowns.size());

        for (var cd : cooldowns) {
            data.writeInt8(cd);
        }
    }
}
