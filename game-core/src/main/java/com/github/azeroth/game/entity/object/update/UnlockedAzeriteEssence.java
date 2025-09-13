package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class UnlockedAzeriteEssence {

    public int azeriteEssenceID;

    public int rank;

    public final void writeCreate(WorldPacket data, AzeriteItem owner, Player receiver) {
        data.writeInt32(azeriteEssenceID);
        data.writeInt32(rank);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, AzeriteItem owner, Player receiver) {
        data.writeInt32(azeriteEssenceID);
        data.writeInt32(rank);
    }
}
