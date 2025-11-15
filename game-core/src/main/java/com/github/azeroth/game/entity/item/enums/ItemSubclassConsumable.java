package com.github.azeroth.game.entity.item.enums;

import com.github.azeroth.utils.Utils;

public enum ItemSubclassConsumable {
    CONSUMABLE,
    POTION,
    ELIXIR,
    FLASK,
    SCROLL,
    FOOD_DRINK,
    ITEM_ENHANCEMENT,
    BANDAGE,
    CONSUMABLE_OTHER,
    VANTUS_RUNE;

    public static ItemSubclassConsumable forValue(byte subclassID) {
        ItemSubclassConsumable[] values = ItemSubclassConsumable.values();
        if(Utils.checkEnumIndex(subclassID, values))
            return null;
        return values[subclassID];
    }
}
