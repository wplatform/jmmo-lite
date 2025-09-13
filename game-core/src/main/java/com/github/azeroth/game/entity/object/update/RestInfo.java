package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class RestInfo extends UpdateMaskObject {

    public FieldType<Integer> threshold = new FieldType<>(0, 1);

    public FieldType<Byte> stateID = new FieldType<>(0, 2);

    RestInfo() {
        super(3);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(threshold);
        data.writeInt8(stateID);
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
                data.writeInt32(threshold);
            }

            if (changesMask.get(2)) {
                data.writeInt8(stateID);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(threshold);
        clearChangesMask(stateID);
        getChangesMask().resetAll();
    }
}
