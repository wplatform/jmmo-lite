package com.github.azeroth.game.networking.packet.battlepet;

import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.networking.WorldPacket;


public final class BattlePetStruct {
    public ObjectGuid UUID = ObjectGuid.EMPTY;
    public int species;
    public int creatureID;
    public int displayID;
    public short breed;
    public short level;
    public short exp;
    public short flags;
    public int power;
    public int health;
    public int maxHealth;
    public int speed;
    public byte quality;
    public BattlePetOwnerInfo ownerInfo = null;
    public String name;

    public void write(WorldPacket data) {
        data.writeGuid(UUID);
        data.writeInt32(species);
        data.writeInt32(creatureID);
        data.writeInt32(displayID);
        data.writeInt16(breed);
        data.writeInt16(level);
        data.writeInt16(exp);
        data.writeInt16(flags);
        data.writeInt32(power);
        data.writeInt32(health);
        data.writeInt32(maxHealth);
        data.writeInt32(speed);
        data.writeInt8(quality);
        data.writeBits(name.getBytes().length, 7);
        data.writeBit(ownerInfo != null); // HasOwnerInfo
        data.writeBit(false); // NoRename
        data.flushBits();

        data.writeString(name);

        if (ownerInfo != null) {
            data.writeGuid(ownerInfo.getValue().guid);
            data.writeInt32(ownerInfo.getValue().playerVirtualRealm); // Virtual
            data.writeInt32(ownerInfo.getValue().playerNativeRealm); // Native
        }
    }


    public final static class BattlePetOwnerInfo {
        public ObjectGuid UUID = ObjectGuid.EMPTY;
        public int playerVirtualRealm;
        public int playerNativeRealm;

    }
}
