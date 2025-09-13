package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class ClearCooldowns extends ServerPacket {
    public ArrayList<Integer> spellID = new ArrayList<>();
    public boolean isPet;

    public ClearCooldowns() {
        super(ServerOpcode.ClearCooldowns);
    }

    @Override
    public void write() {
        this.writeInt32(spellID.size());

        if (!spellID.isEmpty()) {
            spellID.forEach(p -> this.writeInt32(p));
        }

        this.writeBit(isPet);
        this.flushBits();
    }
}
