package com.github.azeroth.game.entity.object.update;


import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemEnchantment;
import com.github.azeroth.game.entity.item.ItemModList;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.itemBonusKey;

public final class ItemData extends UpdateMaskObject {
    public DynamicUpdateField<ArtifactPower> artifactPowers = new DynamicUpdateField<ArtifactPower>(0, 1);
    public DynamicUpdateField<SocketedGem> gems = new DynamicUpdateField<SocketedGem>(0, 2);
    public UpdateField<ObjectGuid> owner = new UpdateField<>(0, 3);
    public UpdateField<ObjectGuid> containedIn = new UpdateField<>(0, 4);
    public UpdateField<ObjectGuid> creator = new UpdateField<>(0, 5);
    public UpdateField<ObjectGuid> giftCreator = new UpdateField<>(0, 6);
    public UpdateField<Integer> stackCount = new UpdateField<>(0, 7);
    public UpdateField<Integer> expiration = new UpdateField<>(0, 8);
    public UpdateField<Integer> dynamicFlags = new UpdateField<>(0, 9);
    public UpdateField<Integer> durability = new UpdateField<>(0, 10);
    public UpdateField<Integer> maxDurability = new UpdateField<>(0, 11);
    public UpdateField<Integer> createPlayedTime = new UpdateField<>(0, 12);
    public UpdateField<Integer> context = new UpdateField<>(0, 13);
    public UpdateField<Long> createTime = new UpdateField<>(0, 14);
    public UpdateField<Long> artifactXP = new UpdateField<>(0, 15);
    public UpdateField<Byte> itemAppearanceModID = new UpdateField<>(0, 16);
    public UpdateField<ItemModList> modifiers = new UpdateField<>(0, 17);
    public UpdateField<Integer> dynamicFlags2 = new UpdateField<>(0, 18);
    public UpdateField<itemBonusKey> itemBonusKey = new UpdateField<>(0, 19);
    public UpdateField<SHORT> DEBUGItemLevel = new UpdateField<>(0, 20);
    public UpdateFieldArray<Integer> spellCharges = new UpdateFieldArray<Integer>(5, 21, 22);
    public UpdateFieldArray<Itemenchantment> enchantment = new UpdateFieldArray<ItemEnchantment>(13, 27, 28);

    public ItemData() {
        super(41);
    }

    public final void writeCreate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Item owner, Player receiver) {
        data.writeGuid(owner);
        data.writeGuid(containedIn);
        data.writeGuid(creator);
        data.writeGuid(giftCreator);

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            data.writeInt32(stackCount);
            data.writeInt32(expiration);

            for (var i = 0; i < 5; ++i) {
                data.writeInt32(spellCharges.get(i));
            }
        }

        data.writeInt32(dynamicFlags);

        for (var i = 0; i < 13; ++i) {
            enchantment.get(i).writeCreate(data, owner, receiver);
        }

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            data.writeInt32(durability);
            data.writeInt32(maxDurability);
        }

        data.writeInt32(createPlayedTime);
        data.writeInt32(context);
        data.writeInt64(createTime);

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            data.writeInt64(artifactXP);
            data.writeInt8(itemAppearanceModID);
        }

        data.writeInt32(artifactPowers.size());
        data.writeInt32(gems.size());

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            data.writeInt32(dynamicFlags2);
        }

        itemBonusKey.getValue().write(data);

        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            data.writeInt16(DEBUGItemLevel);
        }

        for (var i = 0; i < artifactPowers.size(); ++i) {
            artifactPowers.get(i).writeCreate(data, owner, receiver);
        }

        for (var i = 0; i < gems.size(); ++i) {
            gems.get(i).writeCreate(data, owner, receiver);
        }

        modifiers.getValue().writeCreate(data, owner, receiver);
    }

    public final void writeUpdate(WorldPacket data, UpdateFieldFlag fieldVisibilityFlags, Item owner, Player receiver) {
        UpdateMask allowedMaskForTarget = new UpdateMask(41, new int[]{0xF80A727F, 0x000001FF});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        writeUpdate(data, UpdateMask.opBitwiseAnd(getChangesMask(), allowedMaskForTarget), false, owner, receiver);
    }

    public final void appendAllowedFieldsMaskForFlag(UpdateMask allowedMaskForTarget, UpdateFieldFlag fieldVisibilityFlags) {
        if (fieldVisibilityFlags.hasFlag(UpdateFieldFlag.owner)) {
            allowedMaskForTarget.OR(new UpdateMask(41, new int[]{0x07F58D80, 0x00000000}));
        }
    }

    public final void filterDisallowedFieldsMaskForFlag(UpdateMask changesMask, UpdateFieldFlag fieldVisibilityFlags) {
        UpdateMask allowedMaskForTarget = new UpdateMask(41, new Object[]{0xF80A727F, 0x000001FF});

        appendAllowedFieldsMaskForFlag(allowedMaskForTarget, fieldVisibilityFlags);
        changesMask.AND(allowedMaskForTarget);
    }

    public final void writeUpdate(WorldPacket data, UpdateMask changesMask, boolean ignoreNestedChangesMask, Item owner, Player receiver) {
        data.writeBits(changesMask.getBlocksMask(0), 2);

        for (int i = 0; i < 2; ++i) {
            if (changesMask.getBlock(i) != 0) {
                data.writeBits(changesMask.getBlock(i), 32);
            }
        }

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                if (!ignoreNestedChangesMask) {
                    artifactPowers.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(artifactPowers.size(), data);
                }
            }

            if (changesMask.get(2)) {
                if (!ignoreNestedChangesMask) {
                    gems.WriteUpdateMask(data);
                } else {
                    writeCompleteDynamicFieldUpdateMask(gems.size(), data);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(1)) {
                for (var i = 0; i < artifactPowers.size(); ++i) {
                    if (artifactPowers.hasChanged(i) || ignoreNestedChangesMask) {
                        artifactPowers.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(2)) {
                for (var i = 0; i < gems.size(); ++i) {
                    if (gems.hasChanged(i) || ignoreNestedChangesMask) {
                        gems.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                    }
                }
            }

            if (changesMask.get(3)) {
                data.writeGuid(owner);
            }

            if (changesMask.get(4)) {
                data.writeGuid(containedIn);
            }

            if (changesMask.get(5)) {
                data.writeGuid(creator);
            }

            if (changesMask.get(6)) {
                data.writeGuid(giftCreator);
            }

            if (changesMask.get(7)) {
                data.writeInt32(stackCount);
            }

            if (changesMask.get(8)) {
                data.writeInt32(expiration);
            }

            if (changesMask.get(9)) {
                data.writeInt32(dynamicFlags);
            }

            if (changesMask.get(10)) {
                data.writeInt32(durability);
            }

            if (changesMask.get(11)) {
                data.writeInt32(maxDurability);
            }

            if (changesMask.get(12)) {
                data.writeInt32(createPlayedTime);
            }

            if (changesMask.get(13)) {
                data.writeInt32(context);
            }

            if (changesMask.get(14)) {
                data.writeInt64(createTime);
            }

            if (changesMask.get(15)) {
                data.writeInt64(artifactXP);
            }

            if (changesMask.get(16)) {
                data.writeInt8(itemAppearanceModID);
            }

            if (changesMask.get(18)) {
                data.writeInt32(dynamicFlags2);
            }

            if (changesMask.get(19)) {
                itemBonusKey.getValue().write(data);
            }

            if (changesMask.get(20)) {
                data.writeInt16(DEBUGItemLevel);
            }

            if (changesMask.get(17)) {
                modifiers.getValue().writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
            }
        }

        if (changesMask.get(21)) {
            for (var i = 0; i < 5; ++i) {
                if (changesMask.get(22 + i)) {
                    data.writeInt32(spellCharges.get(i));
                }
            }
        }

        if (changesMask.get(27)) {
            for (var i = 0; i < 13; ++i) {
                if (changesMask.get(28 + i)) {
                    enchantment.get(i).writeUpdate(data, ignoreNestedChangesMask, owner, receiver);
                }
            }
        }
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(artifactPowers);
        clearChangesMask(gems);
        clearChangesMask(owner);
        clearChangesMask(containedIn);
        clearChangesMask(creator);
        clearChangesMask(giftCreator);
        clearChangesMask(stackCount);
        clearChangesMask(expiration);
        clearChangesMask(dynamicFlags);
        clearChangesMask(durability);
        clearChangesMask(maxDurability);
        clearChangesMask(createPlayedTime);
        clearChangesMask(context);
        clearChangesMask(createTime);
        clearChangesMask(artifactXP);
        clearChangesMask(itemAppearanceModID);
        clearChangesMask(modifiers);
        clearChangesMask(dynamicFlags2);
        clearChangesMask(itemBonusKey);
        clearChangesMask(DEBUGItemLevel);
        clearChangesMask(spellCharges);
        clearChangesMask(enchantment);
        getChangesMask().resetAll();
    }
}
