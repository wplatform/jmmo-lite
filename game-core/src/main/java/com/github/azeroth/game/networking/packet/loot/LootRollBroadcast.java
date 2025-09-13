package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class LootRollBroadcast extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public ObjectGuid player = ObjectGuid.EMPTY;
    public int roll; // Roll value can be negative, it means that it is an "offspec" roll but only during roll selection broadcast (not when sending the result)
    public RollVote rollType = RollVote.values()[0];
    public LootitemData item = new lootItemData();
    public boolean autopassed; // Triggers message |HlootHistory:%d|h[Loot]|h: You automatically passed on: %s because you cannot loot that item.

    public LootRollBroadcast() {
        super(ServerOpcode.LootRoll);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        this.writeGuid(player);
        this.writeInt32(roll);
        this.writeInt8((byte) rollType.getValue());
        item.write(this);
        this.writeBit(autopassed);
        this.flushBits();
    }
}
