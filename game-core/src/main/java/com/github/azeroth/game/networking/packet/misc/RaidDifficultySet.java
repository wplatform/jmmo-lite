package com.github.azeroth.game.networking.packet.misc;


public class RaidDifficultySet extends ServerPacket {
    public int difficultyID;
    public boolean legacy;

    public RaidDifficultySet() {
        super(ServerOpcode.RaidDifficultySet);
    }

    @Override
    public void write() {
        this.writeInt32(difficultyID);
        this.writeInt8((byte) (Legacy ? 1 : 0));
    }
}
