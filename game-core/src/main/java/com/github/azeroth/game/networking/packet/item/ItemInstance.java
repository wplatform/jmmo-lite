package com.github.azeroth.game.networking.packet.item;



import com.github.azeroth.game.entity.item.Item;
import com.github.azeroth.game.entity.item.ItemMod;
import com.github.azeroth.game.entity.item.enums.ItemModifier;
import com.github.azeroth.game.entity.player.VoidStorageItem;
import com.github.azeroth.game.loot.LootItem;
import com.github.azeroth.game.networking.WorldPacket;
import com.github.azeroth.game.networking.packet.common.CompactArray;
import lombok.EqualsAndHashCode;

import static com.github.azeroth.game.entity.object.update.UpdateFields.ITEM_FIELD_MODIFIERS_MASK;

@EqualsAndHashCode
public class ItemInstance {
    public int itemID;
    public int randomPropertiesSeed;
    public int randomPropertiesID;
    public ItemBonuses itemBonus;
    public CompactArray<Integer> modifications = new CompactArray();

    public ItemInstance() {
    }

    public ItemInstance(Item item) {
        itemID = item.getEntry();
        randomPropertiesSeed = item.getItemSuffixFactor();
        randomPropertiesID   = item.getItemRandomPropertyId();
        var bonusListIds = item.getBonusListIDs();

        if (!bonusListIds.isEmpty()) {
            itemBonus = new ItemBonuses();
            itemBonus.bonusListIDs.addAll(bonusListIds);
            itemBonus.context = item.getContext();
        }

        int mask = item.getInt32Value(ITEM_FIELD_MODIFIERS_MASK);
        if(mask != 0) {
            for (int i = 0; mask != 0; mask >>= 1, ++i)
                if ((mask & 1) != 0)
                    modifications.insert(i, item.getModifier(ItemModifier(i)));
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
}
