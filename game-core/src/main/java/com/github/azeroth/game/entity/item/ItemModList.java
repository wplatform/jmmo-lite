package com.github.azeroth.game.entity.item;

import com.github.azeroth.game.entity.BaseUpdateData;
import com.github.azeroth.game.entity.player.Player;
import com.github.azeroth.game.networking.WorldPacket;

public class ItemModList extends BaseUpdateData<item> {
    public DynamicUpdateField<ItemMod> values = new DynamicUpdateField<ItemMod>(0, 0);

    public itemModList() {
        super(1);
    }

    public final void writeCreate(WorldPacket data, Item owner, Player receiver) {
        data.writeBits(values.size(), 6);

        for (var i = 0; i < values.size(); ++i) {
            values.get(i).writeCreate(data, owner, receiver);
        }

        data.flushBits();
    }

    public final void writeUpdate(WorldPacket data, boolean ignoreChangesMask, Item owner, Player receiver) {
        var changesMask = getChangesMask();

        if (ignoreChangesMask) {
            changesMask.setAll();
        }

        data.writeBits(changesMask.getBlock(0), 1);

        if (changesMask.get(0)) {
            if (changesMask.get(0)) {
                if (!ignoreChangesMask) {
                    values.WriteUpdateMask(data, 6);
                } else {
                    writeCompleteDynamicFieldUpdateMask(values.size(), data, 6);
                }
            }
        }

        data.flushBits();

        if (changesMask.get(0)) {
            if (changesMask.get(0)) {
                for (var i = 0; i < values.size(); ++i) {
                    if (values.hasChanged(i) || ignoreChangesMask) {
                        values.get(i).writeUpdate(data, ignoreChangesMask, owner, receiver);
                    }
                }
            }
        }

        data.flushBits();
    }

    @Override
    public void clearChangesMask() {
        clearChangesMask(values);
        getChangesMask().resetAll();
    }
}
