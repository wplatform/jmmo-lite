package com.github.azeroth.game.networking.packet.spell;

public final class SpellWeight {
    public int type;
    public int ID;
    public int quantity;

    public SpellWeight clone() {
        SpellWeight varCopy = new SpellWeight();

        varCopy.type = this.type;
        varCopy.ID = this.ID;
        varCopy.quantity = this.quantity;

        return varCopy;
    }
}
