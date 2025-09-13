package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Data;

@Data
public class WeeklySpellUse {
    private int spellCategoryID;
    private byte uses;


   public void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(spellCategoryID);
        data.writeInt8(uses);
    }

   public void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(spellCategoryID);
        data.writeInt8(uses);
    }
}
