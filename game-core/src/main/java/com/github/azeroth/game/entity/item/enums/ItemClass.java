package com.github.azeroth.game.entity.item.enums;

import com.github.azeroth.utils.Utils;

public enum ItemClass {
    CONSUMABLE,
    CONTAINER,
    WEAPON,
    GEM,
    ARMOR,
    REAGENT,
    PROJECTILE,
    TRADE_GOODS,
    ITEM_ENHANCEMENT,
    RECIPE,
    MONEY, // OBSOLETE
    QUIVER,
    QUEST,
    KEY,
    PERMANENT, // OBSOLETE
    MISCELLANEOUS,
    GLYPH,
    BATTLE_PETS,
    WOW_TOKEN;

    public static ItemClass forValue(byte classID) {
        ItemClass[] values = ItemClass.values();
        if(Utils.checkEnumIndex(classID, values))
            return null;
        return values[classID];
    }
}
