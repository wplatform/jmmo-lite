package com.github.azeroth.game.networking.packet.misc;

import com.github.azeroth.game.networking.WorldPacket;

public class SetRaidDifficulty extends ClientPacket {
    public int difficultyID;
    public byte legacy;

    public SetRaidDifficulty(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        difficultyID = this.readInt32();
        legacy = this.readUInt8();
    }
}
