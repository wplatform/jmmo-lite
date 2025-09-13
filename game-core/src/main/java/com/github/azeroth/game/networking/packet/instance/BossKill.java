package com.github.azeroth.game.networking.packet.instance;


import com.github.azeroth.game.networking.ServerPacket;

public class BossKill extends ServerPacket {

    public int dungeonEncounterID;

    public BossKill() {
        super(ServerOpcode.BossKill);
    }

    @Override
    public void write() {
        this.writeInt32(dungeonEncounterID);
    }
}
