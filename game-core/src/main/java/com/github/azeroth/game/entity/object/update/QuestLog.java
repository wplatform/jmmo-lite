package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class QuestLog extends UpdateMaskObject {
    public FieldType<Integer> questID = new FieldType<>(0, 1);
    public FieldType<Integer> stateFlags = new FieldType<>(0, 2);
    public FieldType<Integer> endTime = new FieldType<>(0, 3);
    public FieldType<Integer> acceptTime = new FieldType<>(0, 4);
    public FieldType<Integer> objectiveFlags = new FieldType<>(0, 5);
    public Array<SHORT> objectiveProgress = new Array<SHORT>(24, 6, 7);

    public questLog() {
        super(31);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(questID);
        data.writeInt32(stateFlags);
        data.writeInt32(endTime);
        data.writeInt32(acceptTime);
        data.writeInt32(objectiveFlags);

        for (var i = 0; i < 24; ++i) {
            data.writeInt16(objectiveProgress.get(i));
        }
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlocksMask(0), 1);

        if (changesMask.getBlock(0) != 0) {
            data.writeBits(changesMask.getBlock(0), 32);
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeInt32(questID);
            }

            if (changesMask.get(2)) {
                data.writeInt32(stateFlags);
            }

            if (changesMask.get(3)) {
                data.writeInt32(endTime);
            }

            if (changesMask.get(4)) {
                data.writeInt32(acceptTime);
            }

            if (changesMask.get(5)) {
                data.writeInt32(objectiveFlags);
            }
        }

        if (changesMask.get(6)) {
            for (var i = 0; i < 24; ++i) {
                if (changesMask.get(7 + i)) {
                    data.writeInt16(objectiveProgress.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(questID);
        clearChangesMask(stateFlags);
        clearChangesMask(endTime);
        clearChangesMask(acceptTime);
        clearChangesMask(objectiveFlags);
        clearChangesMask(objectiveProgress);
        getChangesMask().resetAll();
    }
}
