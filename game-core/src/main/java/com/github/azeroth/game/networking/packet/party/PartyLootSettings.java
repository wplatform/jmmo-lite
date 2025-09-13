package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

final class PartyLootSettings {
    public byte method;
    public ObjectGuid lootMaster = ObjectGuid.EMPTY;
    public byte threshold;

    public void write(WorldPacket data) {
        data.writeInt8(method);
        data.writeGuid(lootMaster);
        data.writeInt8(threshold);
    }

    public PartyLootSettings clone() {
        PartyLootSettings varCopy = new PartyLootSettings();

        varCopy.method = this.method;
        varCopy.lootMaster = this.lootMaster;
        varCopy.threshold = this.threshold;

        return varCopy;
    }
}
