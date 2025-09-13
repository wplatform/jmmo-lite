package com.github.azeroth.game.networking.packet.achievement;

import com.github.azeroth.game.networking.WorldPacket;

public final class EarnedAchievement {
    public ObjectGuid owner = ObjectGuid.EMPTY;

    public int id;
    public long date;
    public int virtualRealmAddress;
    public int nativeRealmAddress;

    public void write(WorldPacket data) {
        data.writeInt32(id);
        data.writePackedTime(date);
        data.writeGuid(owner);
        data.writeInt32(virtualRealmAddress);
        data.writeInt32(nativeRealmAddress);
    }

    public EarnedAchievement clone() {
        EarnedAchievement varCopy = new EarnedAchievement();

        varCopy.id = this.id;
        varCopy.date = this.date;
        varCopy.owner = this.owner;
        varCopy.virtualRealmAddress = this.virtualRealmAddress;
        varCopy.nativeRealmAddress = this.nativeRealmAddress;

        return varCopy;
    }
}
