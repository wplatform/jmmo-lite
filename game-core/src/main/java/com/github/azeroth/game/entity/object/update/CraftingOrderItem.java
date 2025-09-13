package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class CraftingOrderItem extends UpdateMaskObject {
    public FieldType<Long> field_0 = new FieldType<>(-1, 0);
    public FieldType<ObjectGuid> itemGUID = new FieldType<>(-1, 1);
    public FieldType<ObjectGuid> ownerGUID = new FieldType<>(-1, 2);
    public FieldType<Integer> itemID = new FieldType<>(-1, 3);
    public FieldType<Integer> quantity = new FieldType<>(-1, 4);
    public FieldType<Integer> reagentQuality = new FieldType<>(-1, 5);
    public OptionalUpdateField<Byte> dataSlotIndex = new OptionalUpdateField<Byte>(-1, 6);

    public CraftingOrderItem() {
        super(7);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt64(field_0);
        data.writeGuid(itemGUID);
        data.writeGuid(ownerGUID);
        data.writeInt32(itemID);
        data.writeInt32(quantity);
        data.writeInt32(reagentQuality);
        data.writeBits(dataSlotIndex.hasValue(), 1);

        if (dataSlotIndex.hasValue()) {
            data.writeInt8(dataSlotIndex);
        }
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 7);

        data.flushBits();

        if (changesMask.get(0)) {
            data.writeInt64(field_0);
        }

        if (changesMask.get(1)) {
            data.writeGuid(itemGUID);
        }

        if (changesMask.get(2)) {
            data.writeGuid(ownerGUID);
        }

        if (changesMask.get(3)) {
            data.writeInt32(itemID);
        }

        if (changesMask.get(4)) {
            data.writeInt32(quantity);
        }

        if (changesMask.get(5)) {
            data.writeInt32(reagentQuality);
        }

        data.writeBits(dataSlotIndex.hasValue(), 1);

        if (changesMask.get(6)) {
            if (dataSlotIndex.hasValue()) {
                data.writeInt8(dataSlotIndex);
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(field_0);
        clearChangesMask(itemGUID);
        clearChangesMask(ownerGUID);
        clearChangesMask(itemID);
        clearChangesMask(quantity);
        clearChangesMask(reagentQuality);
        clearChangesMask(dataSlotIndex);
        getChangesMask().resetAll();
    }
}
