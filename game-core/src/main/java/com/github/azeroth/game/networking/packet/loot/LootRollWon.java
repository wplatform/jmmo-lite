package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootRollWon extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public ObjectGuid winner = ObjectGuid.EMPTY;
    public int roll;
    public RollVote rollType = RollVote.values()[0];
    public LootitemData item = new lootItemData();
    public boolean mainSpec;

    public LootRollWon() {
        super(ServerOpcode.LootRollWon);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        this.writeGuid(winner);
        this.writeInt32(roll);
        this.writeInt8((byte) rollType.getValue());
        item.write(this);
        this.writeBit(mainSpec);
        this.flushBits();
    }
}
