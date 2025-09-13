package com.github.azeroth.game.networking.packet.spell;

import com.github.azeroth.game.networking.ClientPacket;
import com.github.azeroth.game.networking.WorldPacket;

class TradeSkillSetFavorite extends ClientPacket {

    public int recipeID;
    public boolean isFavorite;

    public TradeSkillSetFavorite(WorldPacket packet) {
        super(packet);
    }

    @Override
    public void read() {
        recipeID = this.readUInt32();
        isFavorite = this.readBit();
    }
}
