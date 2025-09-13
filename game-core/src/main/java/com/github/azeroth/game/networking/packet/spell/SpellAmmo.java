package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellAmmo {
    public int displayID;
    public byte inventoryType;

    public void write(WorldPacket data) {
        data.writeInt32(displayID);
        data.writeInt8(inventoryType);
    }

    public SpellAmmo clone() {
        SpellAmmo varCopy = new spellAmmo();

        varCopy.displayID = this.displayID;
        varCopy.inventoryType = this.inventoryType;

        return varCopy;
    }
}
