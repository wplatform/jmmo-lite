package com.github.azeroth.game.entity.item.enums;

// EnumUtils: DESCRIBE THIS
public enum SellResult {
    CANT_FIND_ITEM,       // DESCRIPTION The item was not found.
    CANT_SELL_ITEM,       // DESCRIPTION The merchant doesn't want that item.
    CANT_FIND_VENDOR,       // DESCRIPTION The merchant doesn't like you.
    YOU_DONT_OWN_THAT_ITEM,       // DESCRIPTION You don't own that item.
    UNK,       // DESCRIPTION nothing appears...
    ONLY_EMPTY_BAG,       // DESCRIPTION You can only do that with empty bags.
    CANT_SELL_TO_THIS_MERCHANT                  // DESCRIPTION You cannot sell items to this merchant.
}
