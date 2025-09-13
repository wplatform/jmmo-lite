package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class CreatureImmunities {
    public int school;
    public int value;

    public void write(WorldPacket data) {
        data.writeInt32(school);
        data.writeInt32(value);
    }

    public CreatureImmunities clone() {
        CreatureImmunities varCopy = new creatureImmunities();

        varCopy.school = this.school;
        varCopy.value = this.value;

        return varCopy;
    }
}
