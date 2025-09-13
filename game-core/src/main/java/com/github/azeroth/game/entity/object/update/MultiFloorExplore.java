package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;

public class MultiFloorExplore {
    public ArrayList<Integer> worldMapOverlayIDs = new ArrayList<>();

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(worldMapOverlayIDs.size());

        for (var i = 0; i < worldMapOverlayIDs.size(); ++i) {
            data.writeInt32(worldMapOverlayIDs.get(i));
        }
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        data.writeInt32(worldMapOverlayIDs.size());

        for (var i = 0; i < worldMapOverlayIDs.size(); ++i) {
            data.writeInt32(worldMapOverlayIDs.get(i));
        }

        data.flushBits();
    }
}
