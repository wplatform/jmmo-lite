package com.github.azeroth.game.networking.packet.guild;

import com.github.azeroth.game.networking.WorldPacket;


public class GuildRosterMemberData {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public long weeklyXP;
    public long totalXP;
    public int rankID;
    public int areaID;
    public int personalAchievementPoints;
    public int guildReputation;
    public int guildRepToCap;
    public float lastSave;
    public String name;
    public int virtualRealmAddress;
    public String note;
    public String officerNote;
    public byte status;
    public byte level;
    public byte classID;
    public byte gender;
    public long guildClubMemberID;
    public byte raceID;
    public boolean authenticated;
    public boolean sorEligible;
    public GuildRosterprofessionData[] profession = new GuildRosterProfessionData[2];
    public dungeonScoreSummary dungeonScore = new dungeonScoreSummary();

    public final void write(WorldPacket data) {
        data.writeGuid(UUID);
        data.writeInt32(rankID);
        data.writeInt32(areaID);
        data.writeInt32(personalAchievementPoints);
        data.writeInt32(guildReputation);
        data.writeFloat(lastSave);

        for (byte i = 0; i < 2; i++) {
            Profession[i].write(data);
        }

        data.writeInt32(virtualRealmAddress);
        data.writeInt8(status);
        data.writeInt8(level);
        data.writeInt8(classID);
        data.writeInt8(gender);
        data.writeInt64(guildClubMemberID);
        data.writeInt8(raceID);

        data.writeBits(name.getBytes().length, 6);
        data.writeBits(note.getBytes().length, 8);
        data.writeBits(officerNote.getBytes().length, 8);
        data.writeBit(authenticated);
        data.writeBit(sorEligible);
        data.flushBits();

        dungeonScore.write(data);

        data.writeString(name);
        data.writeString(note);
        data.writeString(officerNote);
    }
}
