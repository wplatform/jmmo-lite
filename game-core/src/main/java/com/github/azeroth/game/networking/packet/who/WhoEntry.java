package com.github.azeroth.game.networking.packet.who;

import com.github.azeroth.game.networking.WorldPacket;

public class WhoEntry {
    public playerGuidLookupData playerData = new playerGuidLookupData();
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int guildVirtualRealmAddress;
    public String guildName = "";
    public int areaID;
    public boolean isGM;

    public final void write(WorldPacket data) {
        playerData.write(data);

        data.writeGuid(guildGUID);
        data.writeInt32(guildVirtualRealmAddress);
        data.writeInt32(areaID);

        data.writeBits(guildName.getBytes().length, 7);
        data.writeBit(isGM);
        data.writeString(guildName);

        data.flushBits();
    }
}
