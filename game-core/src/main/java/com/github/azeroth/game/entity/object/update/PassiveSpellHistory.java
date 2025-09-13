package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.entity.unit.Unit;
import com.github.azeroth.game.networking.WorldPacket;

public class PassiveSpellHistory {
    public int spellID;
    public int auraSpellID;

    public final void writeCreate(WorldPacket data, Unit owner, Player receiver) {
        data.writeInt32(spellID);
        data.writeInt32(auraSpellID);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Unit owner, Player receiver) {
        data.writeInt32(spellID);
        data.writeInt32(auraSpellID);
    }
}
