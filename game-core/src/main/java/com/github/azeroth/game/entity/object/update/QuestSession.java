package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class QuestSession extends UpdateMaskObject {
    public FieldType<ObjectGuid> owner = new FieldType<>(0, 1);
    public Array<Long> questCompleted = new Array<Long>(875, 2, 3);

    QuestSession() {
        super(878);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeGuid(owner);

        for (var i = 0; i < 875; ++i) {
            data.writeInt64(questCompleted.get(i));
        }
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlocksMask(0), 28);

        for (int i = 0; i < 28; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                data.writeGuid(owner);
            }
        }

        if (changesMask.get(2)) {
            for (var i = 0; i < 875; ++i) {
                if (changesMask.get(3 + i)) {
                    data.writeInt64(questCompleted.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(owner);
        clearChangesMask(questCompleted);
        getChangesMask().resetAll();
    }
}
