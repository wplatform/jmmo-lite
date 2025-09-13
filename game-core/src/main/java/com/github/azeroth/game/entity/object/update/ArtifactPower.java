package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class ArtifactPower {
    public short artifactPowerId;
    public byte purchasedRank;
    public byte currentRankWithBonus;

    public final void writeCreate(WorldPacket data, Item owner, Player receiver) {
        data.writeInt16(artifactPowerId);
        data.writeInt8(purchasedRank);
        data.writeInt8(currentRankWithBonus);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Item owner, Player receiver) {
        data.writeInt16(artifactPowerId);
        data.writeInt8(purchasedRank);
        data.writeInt8(currentRankWithBonus);
    }
}
