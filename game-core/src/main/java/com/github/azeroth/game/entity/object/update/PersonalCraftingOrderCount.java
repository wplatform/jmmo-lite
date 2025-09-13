package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class PersonalCraftingOrderCount implements IEquatable<PersonalCraftingOrderCount> {
    public int professionID;
    public int count;

    public final boolean equals(PersonalCraftingOrderCount right) {
        return professionID == right.professionID && count == right.count;
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(professionID);
        data.writeInt32(count);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(professionID);
        data.writeInt32(count);
    }
}
