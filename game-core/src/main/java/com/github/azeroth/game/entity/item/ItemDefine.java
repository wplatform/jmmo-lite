package com.github.azeroth.game.entity.item;

import com.github.azeroth.game.entity.item.enums.*;

public interface ItemDefine {

    // 2 fields per visible item (entry+enchantment)
    byte MAX_VISIBLE_ITEM_OFFSET = 2;


    int STANDARD = (SocketColor.RED.value | SocketColor.YELLOW.value | SocketColor.BLUE.value);


    int MASK_WEAPON_RANGED = (
            (1 << ItemSubclassWeapon.WEAPON_BOW.ordinal()) | (1 << ItemSubclassWeapon.WEAPON_GUN.ordinal()) |
                    (1 << ItemSubclassWeapon.WEAPON_CROSSBOW.ordinal()));


    int MAX_QUEST = 9;


    int[] MAX_VALUES = {
            ItemSubclassConsumable.values().length,
            ItemSubclassContainer.values().length,
            ItemSubclassWeapon.values().length,
            ItemSubclassGem.values().length,
            ItemSubClassArmor.values().length,
            ItemSubclassReagent.values().length,
            ItemSubclassProjectile.values().length,
            ItemSubclassTradeGoods.values().length,
            ItemSubclassItemEnhancement.values().length,
            ItemSubclassRecipe.values().length,
            ItemSubclassMoney.values().length,
            ItemSubclassQuiver.values().length,
            ItemSubclassQuest.values().length,
            ItemSubclassKey.values().length,
            ItemSubclassPermanent.values().length,
            ItemSubclassJunk.values().length,
            ItemSubclassGlyph.values().length,
            ItemSubclassBattlePet.values().length,
            ItemSubclassWowToken.values().length,
    };


}
