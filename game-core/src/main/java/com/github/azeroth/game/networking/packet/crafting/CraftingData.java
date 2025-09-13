package com.github.azeroth.game.networking.packet.crafting;

import com.github.azeroth.game.networking.WorldPacket;

import java.util.ArrayList;


class CraftingData {
    public int craftingQualityID;
    public float qualityProgress;
    public int skillLineAbilityID;
    public int craftingDataID;
    public int multicraft;
    public int skillFromReagents;
    public int skill;
    public int critBonusSkill;
    public float field_1C;
    public long field_20;
    public boolean isCrit;
    public boolean field_29;
    public boolean field_2A;
    public boolean bonusCraft;
    public ArrayList<SpellReducedReagent> resourcesReturned = new ArrayList<>();
    public int operationID;
    public ObjectGuid itemGUID = ObjectGuid.EMPTY;
    public int quantity;
    public itemInstance oldItem = new itemInstance();
    public ItemInstance newItem = new itemInstance();
    public int enchantID;

    public final void write(WorldPacket data) {
        data.writeInt32(craftingQualityID);
        data.writeFloat(qualityProgress);
        data.writeInt32(skillLineAbilityID);
        data.writeInt32(craftingDataID);
        data.writeInt32(multicraft);
        data.writeInt32(skillFromReagents);
        data.writeInt32(skill);
        data.writeInt32(critBonusSkill);
        data.writeFloat(field_1C);
        data.writeInt64(field_20);
        data.writeInt32(resourcesReturned.size());
        data.writeInt32(operationID);
        data.writeGuid(itemGUID);
        data.writeInt32(quantity);
        data.writeInt32(enchantID);

        for (var spellReducedReagent : resourcesReturned) {
            spellReducedReagent.write(data);
        }

        data.writeBit(isCrit);
        data.writeBit(field_29);
        data.writeBit(field_2A);
        data.writeBit(bonusCraft);
        data.flushBits();

        oldItem.write(data);
        newItem.write(data);
    }
}
