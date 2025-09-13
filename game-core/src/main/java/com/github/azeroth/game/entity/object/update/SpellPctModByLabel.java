package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class SpellPctModByLabel {
    public int modIndex;
    public float modifierValue;
    public int labelID;

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(modIndex);
        data.writeFloat(modifierValue);
        data.writeInt32(labelID);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(modIndex);
        data.writeFloat(modifierValue);
        data.writeInt32(labelID);
    }
}
