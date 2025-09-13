package com.github.azeroth.game.networking.packet.garrison;


import com.github.azeroth.dbc.domain.GarrAbility;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class GarrisonFollower {
    public long dbID;
    public int garrFollowerID;
    public int quality;
    public int followerLevel;
    public int itemLevelWeapon;
    public int itemLevelArmor;
    public int xp;
    public int durability;
    public int currentBuildingID;
    public int currentMissionID;
    public ArrayList<GarrAbility> abilityID = new ArrayList<>();
    public int zoneSupportSpellID;
    public int followerStatus;
    public int health;
    public long healingTimestamp;
    public byte boardIndex;
    public String customName = "";

    public final void write(WorldPacket data) {
        data.writeInt64(dbID);
        data.writeInt32(garrFollowerID);
        data.writeInt32(quality);
        data.writeInt32(followerLevel);
        data.writeInt32(itemLevelWeapon);
        data.writeInt32(itemLevelArmor);
        data.writeInt32(xp);
        data.writeInt32(durability);
        data.writeInt32(currentBuildingID);
        data.writeInt32(currentMissionID);
        data.writeInt32(abilityID.size());
        data.writeInt32(zoneSupportSpellID);
        data.writeInt32(followerStatus);
        data.writeInt32(health);
        data.writeInt8(boardIndex);
        data.writeInt64(healingTimestamp);

        abilityID.forEach(ability -> data.writeInt32(ability.getId()));

        data.writeBits(customName.getBytes().length, 7);
        data.flushBits();
        data.writeString(customName);
    }
}
