package com.github.azeroth.game.entity.object.update;

import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public final class CraftingOrder extends UpdateMaskObject {
    public Dynamic<ItemEnchantData> enchantments = new Dynamic<ItemEnchantData>(-1, 0);
    public Dynamic<ItemGemData> gems = new Dynamic<ItemGemData>(-1, 1);
    public FieldType<CraftingOrderdata> data = new FieldType<>(-1, 2);
    public OptionalUpdateField<itemInstance> recraftItemInfo = new OptionalUpdateField<itemInstance>(-1, 3);

    public CraftingOrder() {
        super(4);
    }

    public final void writeCreate(WorldPacket data, Player owner, Player receiver) {
        data.getValue().writeCreate(data, owner, receiver);
        data.writeBits(recraftItemInfo.hasValue(), 1);
        data.writeBits(enchantments.size(), 4);
        data.writeBits(gems.size(), 2);

        if (recraftItemInfo.hasValue()) {
            recraftItemInfo.getValue().write(data);
        }

        for (var i = 0; i < enchantments.size(); ++i) {
            enchantments.get(i).write(data);
        }

        for (var i = 0; i < gems.size(); ++i) {
            gems.get(i).write(data);
        }

        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Player owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 4);

        if (changesMask.get(0)) {
            if (!ignoreChangesMask) {
                enchantments.WriteUpdateMask(data, 4);
            } else {
                writeCompleteDynamicFieldUpdateMask(enchantments.size(), data, 4);
            }
        }

        if (changesMask.get(1)) {
            if (!ignoreChangesMask) {
                gems.WriteUpdateMask(data, 2);
            } else {
                writeCompleteDynamicFieldUpdateMask(gems.size(), data, 2);
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            for (var i = 0; i < enchantments.size(); ++i) {
                if (enchantments.hasChanged(i) || ignoreChangesMask) {
                    enchantments.get(i).write(data);
                }
            }
        }

        if (changesMask.get(1)) {
            for (var i = 0; i < gems.size(); ++i) {
                if (gems.hasChanged(i) || ignoreChangesMask) {
                    gems.get(i).write(data);
                }
            }
        }

        if (changesMask.get(2)) {
            data.getValue().writeUpdate(data, ignoreChangesMask, owner, receiver);
        }

        data.writeBits(recraftItemInfo.hasValue(), 1);

        if (changesMask.get(3)) {
            if (recraftItemInfo.hasValue()) {
                recraftItemInfo.getValue().write(data);
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(enchantments);
        clearChangesMask(gems);
        clearChangesMask(data);
        clearChangesMask(recraftItemInfo);
        getChangesMask().resetAll();
    }
}
