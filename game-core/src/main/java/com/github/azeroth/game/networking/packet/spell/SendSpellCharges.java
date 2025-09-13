package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.ServerPacket;

import java.util.ArrayList;

public class SendSpellCharges extends ServerPacket {
    public ArrayList<SpellChargeEntry> entries = new ArrayList<>();

    public SendSpellCharges() {
        super(ServerOpcode.SendSpellCharges);
    }

    @Override
    public void write() {
        this.writeInt32(entries.size());
        entries.forEach(p -> p.write(this));
    }
}
