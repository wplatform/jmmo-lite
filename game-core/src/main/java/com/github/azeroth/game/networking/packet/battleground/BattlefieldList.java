package com.github.azeroth.game.networking.packet.battleground;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class BattlefieldList extends ServerPacket {
    public ObjectGuid battlemasterGuid = ObjectGuid.EMPTY;
    public int battlemasterListID;
    public byte minLevel;
    public byte maxLevel;
    public ArrayList<Integer> battlefields = new ArrayList<>(); // Players cannot join a specific Battleground instance anymore - this is always empty
    public boolean pvpAnywhere;
    public boolean hasRandomWinToday;

    public BattlefieldList() {
        super(ServerOpcode.BattlefieldList);
    }

    @Override
    public void write() {
        this.writeGuid(battlemasterGuid);
        this.writeInt32(battlemasterListID);
        this.writeInt8(minLevel);
        this.writeInt8(maxLevel);
        this.writeInt32(battlefields.size());

        for (var field : battlefields) {
            this.writeInt32(field);
        }

        this.writeBit(pvpAnywhere);
        this.writeBit(hasRandomWinToday);
        this.flushBits();
    }
}
