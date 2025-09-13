package com.github.azeroth.game.networking.packet.party;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class PartyMemberPetStats {
    public ObjectGuid GUID = ObjectGuid.EMPTY;
    public String name;
    public short modelId;

    public int currentHealth;
    public int maxHealth;

    public ArrayList<PartyMemberAuraStates> auras = new ArrayList<>();

    public final void write(WorldPacket data) {
        data.writeGuid(GUID);
        data.writeInt32(modelId);
        data.writeInt32(currentHealth);
        data.writeInt32(maxHealth);
        data.writeInt32(auras.size());
        auras.forEach(p -> p.write(data));

        data.writeBits(name.getBytes().length, 8);
        data.flushBits();
        data.writeString(name);
    }
}
