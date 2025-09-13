package com.github.azeroth.game.networking.packet.party;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

class PartyMemberStats {

    public short level;
    public GroupMemberOnlinestatus status = GroupMemberOnlineStatus.values()[0];

    public int currentHealth;
    public int maxHealth;


    public byte powerType;

    public short currentPower;

    public short maxPower;


    public short zoneID;
    public short positionX;
    public short positionY;
    public short positionZ;

    public int vehicleSeat;

    public partyMemberPhaseStates phases = new partyMemberPhaseStates();
    public ArrayList<PartyMemberAuraStates> auras = new ArrayList<>();
    public PartyMemberpetStats petStats;


    public short powerDisplayID;

    public short specID;

    public short wmoGroupID;

    public int wmoDoodadPlacementID;
    public byte[] partyType = new byte[2];
    public CTROptions chromieTime;
    public dungeonScoreSummary dungeonScore = new dungeonScoreSummary();

    public final void write(WorldPacket data) {
        for (byte i = 0; i < 2; i++) {
            data.writeInt8(PartyType[i]);
        }

        data.writeInt16((short) status.getValue());
        data.writeInt8(powerType);
        data.writeInt16((short) powerDisplayID);
        data.writeInt32(currentHealth);
        data.writeInt32(maxHealth);
        data.writeInt16(currentPower);
        data.writeInt16(maxPower);
        data.writeInt16(level);
        data.writeInt16(specID);
        data.writeInt16(zoneID);
        data.writeInt16(wmoGroupID);
        data.writeInt32(wmoDoodadPlacementID);
        data.writeInt16(positionX);
        data.writeInt16(positionY);
        data.writeInt16(positionZ);
        data.writeInt32(vehicleSeat);
        data.writeInt32(auras.size());

        phases.write(data);
        chromieTime.write(data);

        for (var aura : auras) {
            aura.write(data);
        }

        data.writeBit(petStats != null);
        data.flushBits();

        dungeonScore.write(data);

        if (petStats != null) {
            petStats.write(data);
        }
    }
}
