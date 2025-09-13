package com.github.azeroth.game.networking.packet.loot;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class MasterLootCandidateList extends ServerPacket {
    public ArrayList<ObjectGuid> players = new ArrayList<>();
    public ObjectGuid lootObj = ObjectGuid.EMPTY;

    public MasterLootCandidateList() {
        super(ServerOpcode.MasterLootCandidateList);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        this.writeInt32(players.size());
        players.forEach(guid -> this.writeGuid(guid));
    }
}
