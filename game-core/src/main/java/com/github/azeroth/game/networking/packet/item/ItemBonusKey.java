package com.github.azeroth.game.networking.packet.item;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class ItemBonusKey implements IEquatable<itemBonusKey> {
    public int itemID;
    public ArrayList<Integer> bonusListIDs = new ArrayList<>();
    public ArrayList<ItemMod> modifications = new ArrayList<>();

    public final boolean equals(ItemBonusKey right) {
        if (itemID != right.itemID) {
            return false;
        }

        if (bonusListIDs != right.bonusListIDs) {
            return false;
        }

        if (modifications != right.modifications) {
            return false;
        }

        return true;
    }

    public final void write(WorldPacket data) {
        data.writeInt32(itemID);
        data.writeInt32(bonusListIDs.size());
        data.writeInt32(modifications.size());

        if (!bonusListIDs.isEmpty()) {
            for (var id : bonusListIDs) {
                data.writeInt32(id);
            }
        }

        for (var modification : modifications) {
            modification.write(data);
        }
    }
}
