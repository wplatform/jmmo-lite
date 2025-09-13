package com.github.azeroth.game.networking.packet.reputation;

import com.github.azeroth.game.networking.WorldPacket;

public final class ForcedReaction {
    public int faction;
    public int reaction;

    public void write(WorldPacket data) {
        data.writeInt32(faction);
        data.writeInt32(reaction);
    }

    public ForcedReaction clone() {
        ForcedReaction varCopy = new ForcedReaction();

        varCopy.faction = this.faction;
        varCopy.reaction = this.reaction;

        return varCopy;
    }
}
