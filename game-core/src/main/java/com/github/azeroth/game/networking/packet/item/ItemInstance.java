package com.github.azeroth.game.networking.packet.item;


import com.github.azeroth.game.entity.SocketedGem;
import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemMod;
import com.github.azeroth.game.entity.item.ItemModList;
import com.github.azeroth.game.entity.player.VoidStorageItem;
import com.github.azeroth.game.loot.LootItem;
import com.github.azeroth.game.networking.WorldPacket;

public class ItemInstance {
    public int itemID;
    public ItemBonuses itemBonus;
    public ItemModList modifications = new ItemModList();

    public ItemInstance() {
    }

    public ItemInstance(Item item) {
        itemID = item.getEntry();
        var bonusListIds = item.getBonusListIDs();

        if (!bonusListIds.isEmpty()) {
            itemBonus = new ItemBonuses();
            itemBonus.bonusListIDs.addAll(bonusListIds);
            itemBonus.context = item.getContext();
        }

        for (var mod : item.getItemData().modifiers.getValue().VALUES) {
            modifications.VALUES.add(new ItemMod(mod.value, ItemModifier.forValue(mod.type)));
        }
    }

    public ItemInstance(LootItem lootItem) {
        itemID = lootItem.itemid;

        if (!lootItem.bonusListIDs.isEmpty() || lootItem.randomBonusListId != 0) {
            itemBonus = new ItemBonuses();
            itemBonus.bonusListIDs = lootItem.bonusListIDs;
            itemBonus.context = lootItem.context;

            if (lootItem.randomBonusListId != 0) {
                itemBonus.bonusListIDs.add(lootItem.randomBonusListId);
            }
        }
    }

    public ItemInstance(VoidStorageItem voidItem) {
        itemID = voidItem.getItemEntry();

        if (voidItem.getFixedScalingLevel() != 0) {
            modifications.VALUES.add(new ItemMod(voidItem.getFixedScalingLevel(), ItemModifier.TimewalkerLevel));
        }

        if (voidItem.getArtifactKnowledgeLevel() != 0) {
            modifications.VALUES.add(new ItemMod(voidItem.getArtifactKnowledgeLevel(), ItemModifier.artifactKnowledgeLevel));
        }

        if (!voidItem.getBonusListIDs().isEmpty()) {
            itemBonus = new ItemBonuses();
            itemBonus.context = voidItem.getContext();
            itemBonus.bonusListIDs = voidItem.getBonusListIDs();
        }
    }

    public ItemInstance(SocketedGem gem) {
        itemID = gem.itemId;

        ItemBonuses bonus = new ItemBonuses();
        bonus.context = itemContext.forValue((byte) gem.context);

        for (var bonusListId : gem.bonusListIDs) {
            if (bonusListId != 0) {
                bonus.bonusListIDs.add(bonusListId);
            }
        }

        if (bonus.context != 0 || !bonus.bonusListIDs.isEmpty()) {
            itemBonus = bonus;
        }
    }

    public static boolean opEquals(ItemInstance left, ItemInstance right) {
        if (left == right) {
            return true;
        }

        if (left == null) {
            return false;
        }

        if (right == null) {
            return false;
        }

        if (left.itemID != right.itemID) {
            return false;
        }

        if (left.itemBonus != null && right.itemBonus != null && ItemBonuses.opNotEquals(left.itemBonus, right.itemBonus)) {
            return false;
        }

        if (left.modifications != right.modifications) {
            return false;
        }

        return true;
    }

    public static boolean opNotEquals(ItemInstance left, ItemInstance right) {
        return !(ItemInstance.opEquals(left, right));
    }

    public final void write(WorldPacket data) {
        data.writeInt32(itemID);

        data.writeBit(itemBonus != null);
        data.flushBits();

        modifications.write(data);

        if (itemBonus != null) {
            itemBonus.write(data);
        }
    }

    public final void read(WorldPacket data) {
        itemID = data.readUInt32();

        if (data.readBit()) {
            itemBonus = new ItemBonuses();
        }

        data.resetBitPos();

        modifications.read(data);

        if (itemBonus != null) {
            itemBonus.read(data);
        }
    }

    @Override
    public int hashCode() {
        return (new integer(itemID)).hashCode() ^ itemBonus.hashCode() ^ modifications.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemInstance) {
            return ItemInstance.opEquals((ItemInstance) obj, this);
        }

        return false;
    }
}
