package com.github.azeroth.game.networking.packet.mythicplus;

import com.github.azeroth.game.networking.WorldPacket;

public final class MythicPlusMember {
    public ObjectGuid bnetAccountGUID = ObjectGuid.EMPTY;
    public long guildClubMemberID;
    public ObjectGuid GUID = ObjectGuid.EMPTY;
    public ObjectGuid guildGUID = ObjectGuid.EMPTY;
    public int nativeRealmAddress;
    public int virtualRealmAddress;
    public int chrSpecializationID;
    public short raceID;
    public int itemLevel;
    public int covenantID;
    public int soulbindID;

    public void write(WorldPacket data) {
        data.writeGuid(bnetAccountGUID);
        data.writeInt64(guildClubMemberID);
        data.writeGuid(GUID);
        data.writeGuid(guildGUID);
        data.writeInt32(nativeRealmAddress);
        data.writeInt32(virtualRealmAddress);
        data.writeInt32(chrSpecializationID);
        data.writeInt16(raceID);
        data.writeInt32(itemLevel);
        data.writeInt32(covenantID);
        data.writeInt32(soulbindID);
    }

    public MythicPlusMember clone() {
        MythicPlusMember varCopy = new MythicPlusMember();

        varCopy.bnetAccountGUID = this.bnetAccountGUID;
        varCopy.guildClubMemberID = this.guildClubMemberID;
        varCopy.GUID = this.GUID;
        varCopy.guildGUID = this.guildGUID;
        varCopy.nativeRealmAddress = this.nativeRealmAddress;
        varCopy.virtualRealmAddress = this.virtualRealmAddress;
        varCopy.chrSpecializationID = this.chrSpecializationID;
        varCopy.raceID = this.raceID;
        varCopy.itemLevel = this.itemLevel;
        varCopy.covenantID = this.covenantID;
        varCopy.soulbindID = this.soulbindID;

        return varCopy;
    }
}
