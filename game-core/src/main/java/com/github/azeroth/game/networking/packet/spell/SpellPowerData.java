package com.github.azeroth.game.networking.packet.spell;


import com.github.azeroth.game.networking.WorldPacket;

public final class SpellPowerData {
    public int cost;
    public Powertype type = powerType.values()[0];

    public void write(WorldPacket data) {
        data.writeInt32(cost);
        data.writeInt8((byte) type.getValue());
    }

    public SpellPowerData clone() {
        SpellPowerData varCopy = new SpellPowerData();

        varCopy.cost = this.cost;
        varCopy.type = this.type;

        return varCopy;
    }
}
