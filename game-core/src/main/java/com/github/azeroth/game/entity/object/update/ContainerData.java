package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.domain.object.ObjectGuid;
import com.github.azeroth.game.entity.item.bag;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class ContainerData extends UpdateMaskObject {
    public FieldType<Integer> numSlots = new FieldType<>(0, 1);
    public Array<ObjectGuid> slots = new Array<ObjectGuid>(36, 2, 3);

    public ContainerData() {
        super(39);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Bag owner, Player receiver) {
        for (var i = 0; i < 36; ++i) {
            data.writeGuid(slots.get(i));
        }

        data.writeInt32(numSlots);
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Bag owner, Player receiver) {
        writeUpdate(data, getChangesMask(), false, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Bag owner, Player receiver) {
        data.writeBits(getChangesMask().getBlocksMask(0), 2);

        for (int i = 0; i < 2; ++i) {
            if (getChangesMask().getBlock(i) != 0) {
                data.writeBits(getChangesMask().getBlock(i), 32);
            }
        }

        data.flushBits();

        if (getChangesMask().get(0)) {
            if (getChangesMask().get(1)) {
                data.writeInt32(numSlots);
            }
        }

        if (getChangesMask().get(2)) {
            for (var i = 0; i < 36; ++i) {
                if (getChangesMask().get(3 + i)) {
                    data.writeGuid(slots.get(i));
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(numSlots);
        clearChangesMask(slots);
        getChangesMask().resetAll();
    }
}
