package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class ReplayedQuest extends UpdateMaskObject {
    public FieldType<Integer> questID = new FieldType<>(0, 1);
    public FieldType<Integer> replayTime = new FieldType<>(0, 2);

    public ReplayedQuest() {
        super(3);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(questID);
        data.writeInt32(replayTime);
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 3);

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeInt32(questID);
            }

            if (changesMask.get(2)) {
                data.writeInt32(replayTime);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(questID);
        clearChangesMask(replayTime);
        getChangesMask().resetAll();
    }
}
