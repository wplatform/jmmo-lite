package com.github.azeroth.game.networking.packet.garrison;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class GarrisonEncounter {
    public int garrEncounterID;
    public ArrayList<Integer> mechanics = new ArrayList<>();
    public int garrAutoCombatantID;
    public int health;
    public int maxHealth;
    public int attack;
    public byte boardIndex;

    public final void write(WorldPacket data) {
        data.writeInt32(garrEncounterID);
        data.writeInt32(mechanics.size());
        data.writeInt32(garrAutoCombatantID);
        data.writeInt32(health);
        data.writeInt32(maxHealth);
        data.writeInt32(attack);
        data.writeInt8(boardIndex);

        if (!mechanics.isEmpty()) {
            mechanics.forEach(id -> data.writeInt32(id));
        }
    }
}
