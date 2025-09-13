package com.github.azeroth.game.networking.packet.pet;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class PetLearnedSpells extends ServerPacket {

    public ArrayList<Integer> spells = new ArrayList<>();

    public PetLearnedSpells() {
        super(ServerOpcode.PetLearnedSpells);
    }

    @Override
    public void write() {
        this.writeInt32(spells.size());

        for (var spell : spells) {
            this.writeInt32(spell);
        }
    }
}
