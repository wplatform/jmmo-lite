package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class RecipeProgressionInfo {
    public short recipeProgressionGroupID;
    public short experience;

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt16(recipeProgressionGroupID);
        data.writeInt16(experience);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt16(recipeProgressionGroupID);
        data.writeInt16(experience);
    }
}
