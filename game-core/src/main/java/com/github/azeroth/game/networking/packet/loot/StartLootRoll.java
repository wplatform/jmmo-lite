package com.github.azeroth.game.networking.packet.loot;

import com.github.azeroth.game.networking.ServerPacket;

public class StartLootRoll extends ServerPacket {
    public ObjectGuid lootObj = ObjectGuid.EMPTY;
    public int mapID;
    public int rollTime;
    public Lootmethod method = lootMethod.values()[0];
    public RollMask validRolls = RollMask.values()[0];
    public Array<LootRollIneligibilityReason> lootRollIneligibleReason = new Array<LootRollIneligibilityReason>(4);
    public LootitemData item = new lootItemData();

    public StartLootRoll() {
        super(ServerOpcode.StartLootRoll);
    }

    @Override
    public void write() {
        this.writeGuid(lootObj);
        this.writeInt32(mapID);
        this.writeInt32(rollTime);
        this.writeInt8((byte) validRolls.getValue());

        for (var reason : lootRollIneligibleReason) {
            this.writeInt32((int) reason.getValue());
        }

        this.writeInt8((byte) method.getValue());
        item.write(this);
    }
}
