package com.github.azeroth.game.entity.unit;

import com.github.azeroth.game.entity.object.WorldObject;

public class DispelInfo {
    private final WorldObject dispeller;
    private final int dispellerSpell;
    private byte chargesRemoved;

    public DispelInfo(WorldObject dispeller, int dispellerSpellId, byte chargesRemoved) {
        dispeller = dispeller;
        dispellerSpell = dispellerSpellId;
        chargesRemoved = chargesRemoved;
    }

    public final WorldObject getDispeller() {
        return dispeller;
    }

    public final byte getRemovedCharges() {
        return chargesRemoved;
    }

    public final void setRemovedCharges(byte amount) {
        chargesRemoved = amount;
    }

    private int getDispellerSpellId() {
        return dispellerSpell;
    }
}
