package com.github.azeroth.game.networking.packets;

import com.github.azeroth.game.networking.WorldPacket;

public final class CTROptions {

    public int contentTuningConditionMask;
    public int unused901;

    public int expansionLevelMask;

    public void write(WorldPacket data) {
        data.writeInt32(contentTuningConditionMask);
        data.writeInt32(unused901);
        data.writeInt32(expansionLevelMask);
    }

    public CTROptions clone() {
        CTROptions varCopy = new CTROptions();

        varCopy.contentTuningConditionMask = this.contentTuningConditionMask;
        varCopy.unused901 = this.unused901;
        varCopy.expansionLevelMask = this.expansionLevelMask;

        return varCopy;
    }
}
