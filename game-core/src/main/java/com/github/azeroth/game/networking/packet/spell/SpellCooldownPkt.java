package com.github.azeroth.game.networking.packet.spell;


import java.util.ArrayList;


public class SpellCooldownPkt extends ServerPacket {
    public ArrayList<SpellCooldownStruct> spellCooldowns = new ArrayList<>();
    public ObjectGuid caster = ObjectGuid.EMPTY;
    public SpellCooldownflags flags = SpellCooldownFlags.values()[0];

    public SpellCooldownPkt() {
        super(ServerOpcode.SpellCooldown);
    }

    @Override
    public void write() {
        this.writeGuid(caster);
        this.writeInt8((byte) flags.getValue());
        this.writeInt32(spellCooldowns.size());
        spellCooldowns.forEach(p -> p.write(this));
    }
}
