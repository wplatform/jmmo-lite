package com.github.azeroth.game.networking.packet.inspect;


import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


public class InspectItemData {
    public ObjectGuid creatorGUID = ObjectGuid.EMPTY;
    public itemInstance item;
    public byte index;
    public boolean usable;
    public ArrayList<InspectEnchantData> enchants = new ArrayList<>();
    public ArrayList<ItemGemData> gems = new ArrayList<>();
    public ArrayList<Integer> azeritePowers = new ArrayList<>();
    public ArrayList<AzeriteEssenceData> azeriteEssences = new ArrayList<>();

    public InspectItemData(com.github.azeroth.game.entity.item.Item item, byte index) {
        creatorGUID = item.getCreator();

        item = new itemInstance(item);
        index = index;
        usable = true; // @todo

        for (EnchantmentSlot enchant = 0; enchant.getValue() < EnchantmentSlot.max.getValue(); ++enchant) {
            var enchId = item.getEnchantmentId(enchant);

            if (enchId != 0) {
                enchants.add(new InspectEnchantData(enchId, (byte) enchant.getValue()));
            }
        }

        byte i = 0;

        for (var gemData : item.getItemData().gems) {
            if (gemData.itemId != 0) {
                ItemGemData gem = new ItemGemData();
                gem.slot = i;
                gem.item = new itemInstance(gemData);
                gems.add(gem);
            }

            ++i;
        }

        var azeriteItem = item.getAsAzeriteItem();

        if (azeriteItem != null) {
            var essences = azeriteItem.GetSelectedAzeriteEssences();

            if (essences != null) {
                for (byte slot = 0; slot < essences.azeriteEssenceID.getSize(); ++slot) {
                    AzeriteEssenceData essence = new AzeriteEssenceData();
                    essence.index = slot;
                    essence.azeriteEssenceID = essences.azeriteEssenceID.get(slot);

                    if (essence.azeriteEssenceID != 0) {
                        essence.rank = azeriteItem.GetEssenceRank(essence.azeriteEssenceID);
                        essence.SlotUnlocked = true;
                    } else {
                        essence.SlotUnlocked = azeriteItem.HasUnlockedEssenceSlot(slot);
                    }

                    azeriteEssences.add(essence);
                }
            }
        }
    }

    public final void write(WorldPacket data) {
        data.writeGuid(creatorGUID);
        data.writeInt8(index);
        data.writeInt32(azeritePowers.size());
        data.writeInt32(azeriteEssences.size());

        for (var id : azeritePowers) {
            data.writeInt32(id);
        }

        item.write(data);
        data.writeBit(usable);
        data.writeBits(enchants.size(), 4);
        data.writeBits(gems.size(), 2);
        data.flushBits();

        for (var azeriteEssenceData : azeriteEssences) {
            azeriteEssenceData.write(data);
        }

        for (var enchantData : enchants) {
            enchantData.write(data);
        }

        for (var gem : gems) {
            gem.write(data);
        }
    }
}
