package com.github.azeroth.game.networking.packet.loot;

public final class LootRequest {
    public objectGuid object = ObjectGuid.EMPTY;
    public byte lootListID;

    public LootRequest clone() {
        LootRequest varCopy = new LootRequest();

        varCopy.object = this.object;
        varCopy.lootListID = this.lootListID;

        return varCopy;
    }
}
