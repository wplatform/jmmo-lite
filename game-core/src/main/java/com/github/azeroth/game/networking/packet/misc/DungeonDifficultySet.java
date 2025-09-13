package com.github.azeroth.game.networking.packet.misc;


public class DungeonDifficultySet extends ServerPacket {
    public int difficultyID;

    public DungeonDifficultySet() {
        super(ServerOpcode.SetDungeonDifficulty);
    }

    @Override
    public void write() {
        this.writeInt32(difficultyID);
    }
}
