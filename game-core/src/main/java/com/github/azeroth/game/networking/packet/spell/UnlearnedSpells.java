package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class UnlearnedSpells extends ServerPacket {
    public ArrayList<Integer> spellID = new ArrayList<>();
    public boolean suppressMessaging;

    public UnlearnedSpells() {
        super(ServerOpcode.UnlearnedSpells);
    }

    @Override
    public void write() {
        this.writeInt32(spellID.size());

        for (var spellId : spellID) {
            this.writeInt32(spellId);
        }

        this.writeBit(suppressMessaging);
        this.flushBits();
    }
}
