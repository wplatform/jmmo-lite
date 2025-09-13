package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.WorldPacket;

public final class SpellExtraCurrencyCost {
    public int currencyID;
    public int count;

    public void read(WorldPacket data) {
        currencyID = data.readInt32();
        count = data.readInt32();
    }

    public SpellExtraCurrencyCost clone() {
        SpellExtraCurrencyCost varCopy = new SpellExtraCurrencyCost();

        varCopy.currencyID = this.currencyID;
        varCopy.count = this.count;

        return varCopy;
    }
}
