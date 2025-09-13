package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class CraftingOrderData extends UpdateMaskObject {
    public Dynamic<CraftingOrderItem> reagents = new Dynamic<CraftingOrderItem>(0, 1);
    public FieldType<Integer> field_0 = new FieldType<>(0, 2);
    public FieldType<Long> orderID = new FieldType<>(0, 3);
    public FieldType<Integer> skillLineAbilityID = new FieldType<>(0, 4);
    public FieldType<Byte> orderState = new FieldType<>(5, 6);
    public FieldType<Byte> orderType = new FieldType<>(5, 7);
    public FieldType<Byte> minQuality = new FieldType<>(5, 8);
    public FieldType<Long> expirationTime = new FieldType<>(5, 9);
    public FieldType<Long> claimEndTime = new FieldType<>(10, 11);
    public FieldType<Long> tipAmount = new FieldType<>(10, 12);
    public FieldType<Long> consortiumCut = new FieldType<>(10, 13);
    public FieldType<Integer> flags = new FieldType<>(10, 14);
    public FieldType<ObjectGuid> customerGUID = new FieldType<>(15, 16);
    public FieldType<ObjectGuid> customerAccountGUID = new FieldType<>(15, 17);
    public FieldType<ObjectGuid> crafterGUID = new FieldType<>(15, 18);
    public FieldType<ObjectGuid> personalCrafterGUID = new FieldType<>(15, 19);
    public updateFieldString customerNotes = new updateFieldString(20, 21);
    public OptionalUpdateField<CraftingOrderItem> outputItem = new OptionalUpdateField<CraftingOrderItem>(20, 22);
    public OptionalUpdateField<itemInstance> outputItemData = new OptionalUpdateField<itemInstance>(20, 23);

    public CraftingOrderData() {
        super(24);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.writeInt32(field_0);
        data.writeInt64(orderID);
        data.writeInt32(skillLineAbilityID);
        data.writeInt8(orderState);
        data.writeInt8(orderType);
        data.writeInt8(minQuality);
        data.writeInt64(expirationTime);
        data.writeInt64(claimEndTime);
        data.writeInt64(tipAmount);
        data.writeInt64(consortiumCut);
        data.writeInt32(flags);
        data.writeGuid(customerGUID);
        data.writeGuid(customerAccountGUID);
        data.writeGuid(crafterGUID);
        data.writeGuid(personalCrafterGUID);
        data.writeInt32(reagents.size());
        data.writeBits(customerNotes.getValue().getBytes().length, 10);
        data.writeBits(outputItem.hasValue(), 1);
        data.writeBits(outputItemData.hasValue(), 1);

        for (var i = 0; i < reagents.size(); ++i) {
            reagents.get(i).writeCreate(data, owner, receiver);
        }

        data.writeString(customerNotes);

        if (outputItem.hasValue()) {
            outputItem.getValue().writeCreate(data, owner, receiver);
        }

        if (outputItemData.hasValue()) {
            outputItemData.getValue().write(data);
        }

        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 24);

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                if (!ignoreChangesMask) {
                    reagents.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(reagents.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                for (var i = 0; i < reagents.size(); ++i) {
                    if (reagents.hasChanged(i) || ignoreChangesMask) {
                        reagents.get(i).writeUpdate(data, ignoreChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(2)) {
                data.writeInt32(field_0);
            }

            if (changesMask.get(3)) {
                data.writeInt64(orderID);
            }

            if (changesMask.get(4)) {
                data.writeInt32(skillLineAbilityID);
            }
        }

        if (changesMask.get(5)) {
            if (changesMask.get(6)) {
                data.writeInt8(orderState);
            }

            if (changesMask.get(7)) {
                data.writeInt8(orderType);
            }

            if (changesMask.get(8)) {
                data.writeInt8(minQuality);
            }

            if (changesMask.get(9)) {
                data.writeInt64(expirationTime);
            }
        }

        if (changesMask.get(10)) {
            if (changesMask.get(11)) {
                data.writeInt64(claimEndTime);
            }

            if (changesMask.get(12)) {
                data.writeInt64(tipAmount);
            }

            if (changesMask.get(13)) {
                data.writeInt64(consortiumCut);
            }

            if (changesMask.get(14)) {
                data.writeInt32(flags);
            }
        }

        if (changesMask.get(15)) {
            if (changesMask.get(16)) {
                data.writeGuid(customerGUID);
            }

            if (changesMask.get(17)) {
                data.writeGuid(customerAccountGUID);
            }

            if (changesMask.get(18)) {
                data.writeGuid(crafterGUID);
            }

            if (changesMask.get(19)) {
                data.writeGuid(personalCrafterGUID);
            }
        }

        if (changesMask.get(20)) {
            if (changesMask.get(21)) {
                data.writeBits(customerNotes.getValue().getBytes().length, 10);
                data.writeString(customerNotes);
            }

            data.writeBits(outputItem.hasValue(), 1);
            data.writeBits(outputItemData.hasValue(), 1);

            if (changesMask.get(22)) {
                if (outputItem.hasValue()) {
                    outputItem.getValue().writeUpdate(data, ignoreChangesMask, owner, receiver);
                }
            }

            if (changesMask.get(23)) {
                if (outputItemData.hasValue()) {
                    outputItemData.getValue().write(data);
                }
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(reagents);
        clearChangesMask(field_0);
        clearChangesMask(orderID);
        clearChangesMask(skillLineAbilityID);
        clearChangesMask(orderState);
        clearChangesMask(orderType);
        clearChangesMask(minQuality);
        clearChangesMask(expirationTime);
        clearChangesMask(claimEndTime);
        clearChangesMask(tipAmount);
        clearChangesMask(consortiumCut);
        clearChangesMask(flags);
        clearChangesMask(customerGUID);
        clearChangesMask(customerAccountGUID);
        clearChangesMask(crafterGUID);
        clearChangesMask(personalCrafterGUID);
        clearChangesMask(customerNotes);
        clearChangesMask(outputItem);
        clearChangesMask(outputItemData);
        getChangesMask().resetAll();
    }
}
