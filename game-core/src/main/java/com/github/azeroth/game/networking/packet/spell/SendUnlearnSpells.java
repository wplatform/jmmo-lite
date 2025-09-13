package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class SendUnlearnSpells extends ServerPacket {
    private final ArrayList<Integer> spells = new ArrayList<>();

    public sendUnlearnSpells() {
        super(ServerOpcode.SendUnlearnSpells);
    }

    @Override
    public void write() {
        this.writeInt32(spells.size());

        for (var spell : spells) {
            this.writeInt32(spell);
        }
    }
}
