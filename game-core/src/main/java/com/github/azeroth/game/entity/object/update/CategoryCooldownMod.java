package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import lombok.Data;

@Data
public class CategoryCooldownMod {

    private int spellCategoryID;
    private int modCooldown;

    public void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(spellCategoryID);
        data.writeInt32(modCooldown);
    }

    public void writeUpdate(WorldPacket data, Player owner, Player receiver) {
        this.writeCreate(data, owner, receiver);
    }

}
