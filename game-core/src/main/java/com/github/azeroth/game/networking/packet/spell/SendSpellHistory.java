package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class SendSpellHistory extends ServerPacket {
    public ArrayList<SpellHistoryEntry> entries = new ArrayList<>();

    public SendSpellHistory() {
        super(ServerOpcode.SendSpellHistory);
    }

    @Override
    public void write() {
        this.writeInt32(entries.size());
        entries.forEach(p -> p.write(this));
    }
}
